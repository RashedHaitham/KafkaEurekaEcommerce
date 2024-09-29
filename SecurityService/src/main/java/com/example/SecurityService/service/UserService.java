package com.example.SecurityService.service;

import com.example.SecurityService.model.User;
import com.example.SecurityService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.Name;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private LdapTemplate ldapTemplate;


    @Autowired
    private UserRepository userRepository;

    public Mono<User> findByUsername(String username) {
        try {
            LdapQuery query = LdapQueryBuilder.query()
                    .where("objectClass").is("inetOrgPerson")
                    .and("uid").is(username);

            User user = ldapTemplate.findOne(query, User.class);
            return user != null ? Mono.just(user) : Mono.empty();
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error finding person with username: " + username, e));
        }
    }


    public Mono<Void> deletePerson(User user) {
        return Mono.fromRunnable(() -> {
            try {
                userRepository.delete(user);
            } catch (Exception e) {
                throw new RuntimeException("Error deleting person: " + user.getFullName(), e);
            }
        });
    }



    public Flux<User> findAll() {
        List<User> users = userRepository.findAll();  // Assuming the repository returns a List

        users.forEach(user -> {
            if (user.getId() != null && user.getId().toString().contains("ou=admins")) {
                user.setRole("admin");
            } else {
                user.setRole("user");
            }
        });

        return Flux.fromIterable(users);
    }



    public Mono<User> savePerson(User user) {
        user.setRole(user.getRole().toUpperCase());
        String userOu = user.getRole().equals("ADMIN") ? "admins" : "users";  // Check the role

        return Mono.fromCallable(() -> {
            Name dn = LdapNameBuilder.newInstance()
                    .add("ou", userOu)
                    .add("uid", user.getUsername())
                    .build();

            try {
                ldapTemplate.lookup(dn);  // If found, do nothing, entry already exists
                throw new IllegalStateException("Entry already exists: " + dn.toString());
            } catch (NameNotFoundException e) {
                DirContextAdapter context = new DirContextAdapter(dn);
                context.setAttributeValues("objectClass", new String[]{"inetOrgPerson", "top"});
                context.setAttributeValue("uid", user.getUsername());
                context.setAttributeValue("cn", user.getFullName());
                context.setAttributeValue("sn", user.getLastName());
                context.setAttributeValue("mail", user.getEmail());

                context.setAttributeValue("userPassword", user.getPassword());

                ldapTemplate.bind(context);

                return user;
            }
        });
    }


}

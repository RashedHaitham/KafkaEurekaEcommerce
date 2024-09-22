package com.example.apiGateway.service;

import com.example.apiGateway.model.User;
import com.example.apiGateway.repository.UserRepository;
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
        return (Flux<User>) userRepository.findAll();
    }


    public Mono<User> savePerson(User user) {
        return Mono.fromCallable(() -> {
            Name dn = LdapNameBuilder.newInstance()
                    .add("ou", "users")
                    .add("uid", user.getUsername())  // Use uid for login
                    .build();

            try {
                ldapTemplate.lookup(dn);  // If found, do nothing, entry already exists
                throw new IllegalStateException("Entry already exists: " + dn.toString());
            } catch (NameNotFoundException e) {
                // Entry does not exist, proceed with saving it
            }

            DirContextAdapter context = new DirContextAdapter(dn);
            context.setAttributeValues("objectClass", new String[] {"inetOrgPerson", "top"});
            context.setAttributeValue("uid", user.getUsername());
            context.setAttributeValue("cn", user.getFullName());
            context.setAttributeValue("sn", user.getLastName());
            context.setAttributeValue("mail", user.getEmail());

            context.setAttributeValue("userPassword", user.getPassword());

            // Bind the new entry to the LDAP directory
            ldapTemplate.bind(context);

            // Return the saved user for further processing in a reactive chain
            return user;
        });
    }


}

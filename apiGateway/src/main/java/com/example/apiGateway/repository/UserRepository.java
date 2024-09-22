package com.example.apiGateway.repository;

import com.example.apiGateway.model.User;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends LdapRepository<User> {
    User findByFullName(String fullName);
}

package com.example.SecurityService.repository;

import com.example.SecurityService.model.User;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends LdapRepository<User> {
    User findByFullName(String fullName);
}

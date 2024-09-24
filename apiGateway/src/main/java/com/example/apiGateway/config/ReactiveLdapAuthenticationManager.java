package com.example.apiGateway.config;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;

public class ReactiveLdapAuthenticationManager implements ReactiveAuthenticationManager {

    private final LdapAuthenticator ldapAuthenticator;

    public ReactiveLdapAuthenticationManager(LdapAuthenticator ldapAuthenticator) {
        this.ldapAuthenticator = ldapAuthenticator;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Perform LDAP authentication asynchronously using Mono
        return Mono.fromCallable(() -> {
            DirContextOperations context = ldapAuthenticator.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            String dn = context.getNameInNamespace();  // Get the distinguished name
            return new UsernamePasswordAuthenticationToken(dn, null, new ArrayList<>());
        });
    }
}

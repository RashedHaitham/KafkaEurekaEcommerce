package com.example.SecurityService.config;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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
                    // Authenticate the user with LDAP
                    DirContextOperations context = ldapAuthenticator.authenticate(
                            new UsernamePasswordAuthenticationToken(username, password)
                    );

                    String dn = context.getNameInNamespace();

                    // Determine role based on whether DN contains "ou=users" or "ou=admins"
                    String role;
                    if (dn.contains("ou=admins")) {
                        role = "ADMIN";  // If the user is in ou=admins
                    } else if (dn.contains("ou=users")) {
                        role = "USER";   // If the user is in ou=users
                    } else {
                        throw new BadCredentialsException("Unknown OU: Unable to determine role");
                    }

                    // Map the role to GrantedAuthority
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                    return new UsernamePasswordAuthenticationToken(username, null, authorities);
                });
    }

}

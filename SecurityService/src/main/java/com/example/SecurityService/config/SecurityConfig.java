package com.example.SecurityService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

//    private final JWTAuthenticationManager authenticationManager;
//
//    public SecurityConfig(@Lazy JWTAuthenticationManager authenticationManager) {
//        this.authenticationManager = authenticationManager;
//    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }


    @Bean
    @Primary
    public ReactiveAuthenticationManager reactiveAuthenticationManager(BindAuthenticator bindAuthenticator) {
        return new ReactiveLdapAuthenticationManager(bindAuthenticator);
    }

    @Bean
    public BindAuthenticator bindAuthenticator(DefaultSpringSecurityContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserDnPatterns(new String[]{"uid={0},ou=users","uid={0},ou=admins"});
        return authenticator;
    }

    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        return new DefaultSpringSecurityContextSource("ldap://localhost:10389/dc=example,dc=com");
    }

    //    @Bean
//    public LdapContextSource contextSource() {
//        LdapContextSource contextSource = new LdapContextSource();
//        contextSource.setUrl("ldap://localhost:10389");  // LDAP server URL
//        contextSource.setBase("dc=example,dc=com");  // Base DN
//        contextSource.setUserDn("uid=admin,ou=system");  // LDAP admin user
//        contextSource.setPassword("secret");  // LDAP admin password
//        return contextSource;
//    }

//    @Bean
//    public AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) {
//        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
//        factory.setUserDnPatterns(new String[]{"uid={0},ou=users","uid={0},ou=admins"});
//        return factory.createAuthenticationManager();
//    }

//    @Bean
//    public AuthenticationWebFilter jwtAuthenticationWebFilter() {
//        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
//        authenticationWebFilter.setServerAuthenticationConverter(authenticationManager.authenticationConverter());
//        return authenticationWebFilter;
//    }
}

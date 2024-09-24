package com.example.apiGateway.config;

import com.example.apiGateway.service.JWTAuthenticationManager;
import com.example.apiGateway.service.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

//    private final JWTAuthenticationManager authenticationManager;
//
//    public SecurityConfig(@Lazy JWTAuthenticationManager authenticationManager) {
//        this.authenticationManager = authenticationManager;
//    }

    private final JWTFilter jwtFilter;

    public SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/signup", "/api/auth/login").permitAll() // Expose login and signup endpoints
                        .pathMatchers(HttpMethod.PUT,"/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE,"/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST,"/api/inventory/**").hasRole("ADMIN")
                        .anyExchange().authenticated()  // Protect all other endpoints
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                //.authenticationManager(authenticationManager)
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
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

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }


    @Bean
    public AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("uid={0},ou=users");
        factory.setUserDnPatterns("uid={0},ou=admins");
        return factory.createAuthenticationManager();
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
//    public AuthenticationWebFilter jwtAuthenticationWebFilter() {
//        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
//        authenticationWebFilter.setServerAuthenticationConverter(authenticationManager.authenticationConverter());
//        return authenticationWebFilter;
//    }
}

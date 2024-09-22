package com.example.apiGateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
public class LdapService {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<String> searchByCommonName(String cn) {
        return ldapTemplate.search(
                "ou=users", // Search base
                "(cn=" + cn + ")", // LDAP search filter
                (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        );
    }

    public List<String> searchByLastName(String sn) {
        return ldapTemplate.search(
                "ou=users", // Search base
                "(sn=" + sn + ")", // LDAP search filter
                (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        );
    }

    public List<String> searchByFirstName(String firstName) {
        LdapQuery query = LdapQueryBuilder.query()
                .where("cn").like(firstName + "*");  // Wildcard search

        return ldapTemplate.search(query, (Attributes attributes) -> {
            NamingEnumeration<?> commonNames = attributes.get("cn").getAll();
            StringBuilder result = new StringBuilder();
            while (commonNames.hasMore()) {
                result.append(commonNames.next().toString()).append(", ");
            }
            return result.toString();
        });
    }

//    public List<String> searchByFirstNameWithPagination(String firstName, int pageSize, int pageNumber) {
//        LdapQuery query = LdapQueryBuilder.query()
//                .where("cn").like(firstName + "*");  // Wildcard search
//
//        PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(pageSize);
//
//        List<String> results = new ArrayList<>();
//
//        for (int i = 0; i < pageNumber; i++) {
//            results = ldapTemplate.search(query, (Attributes attributes) -> {
//                return attributes.get("cn").get().toString();
//            }, processor);
//
//            // If no more pages, exit early
//            if (processor.getCookie() == null) {
//                break;
//            }
//        }
//
//        return results;
//    }



}


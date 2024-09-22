package com.example.apiGateway.service;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
public class OrganizationUnitService {

    private final LdapTemplate ldapTemplate;

    public OrganizationUnitService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void createOrganizationalUnitIfNotExists(String ouName) {
        Name dn = LdapNameBuilder.newInstance()
                .add("ou", ouName)
                .build();

        try {
            // Check if the organizational unit already exists
            ldapTemplate.lookup(dn);  // If found, it exists
            return;  // OU already exists, do nothing
        } catch (NameNotFoundException e) {
            // Organizational unit doesn't exist, proceed to create it
        }

        // Attributes for the organizational unit
        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues("objectClass", new String[] {"top", "organizationalUnit"});
        context.setAttributeValue("ou", ouName);

        // Bind the new entry
        ldapTemplate.bind(context);
    }
}

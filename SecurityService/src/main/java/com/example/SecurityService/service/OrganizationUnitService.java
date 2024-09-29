package com.example.SecurityService.service;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.logging.Logger;

@Service
public class OrganizationUnitService {

    private final LdapTemplate ldapTemplate;
    Logger logger = Logger.getLogger(OrganizationUnitService.class.getName());

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
        } catch (NameNotFoundException e) {
            logger.info("Organizational unit with name " + ouName + " does not exists, creating it....");
            DirContextAdapter context = new DirContextAdapter(dn);
            context.setAttributeValues("objectClass", new String[] {"top", "organizationalUnit"});
            context.setAttributeValue("ou", ouName);

            ldapTemplate.bind(context);
        }


    }
}

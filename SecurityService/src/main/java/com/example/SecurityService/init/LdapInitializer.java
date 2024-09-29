package com.example.SecurityService.init;

import com.example.SecurityService.service.OrganizationUnitService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class LdapInitializer {

    private final OrganizationUnitService organizationUnitService;

    public LdapInitializer(OrganizationUnitService organizationUnitService) {
        this.organizationUnitService = organizationUnitService;
    }

    @PostConstruct
    public void initializeLdapEntries() {
        organizationUnitService.createOrganizationalUnitIfNotExists("users");
        organizationUnitService.createOrganizationalUnitIfNotExists("admins");
    }
}

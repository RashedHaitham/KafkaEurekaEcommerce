package com.example.apiGateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Attribute;

import javax.naming.Name;
@Entry(base = "ou=users", objectClasses = { "inetOrgPerson", "top" })
@Data
public class User {

    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name = "uid")
    private String username;

    @Attribute(name = "cn")
    private String fullName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "mail")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Attribute(name = "userPassword")
    private String password;
}

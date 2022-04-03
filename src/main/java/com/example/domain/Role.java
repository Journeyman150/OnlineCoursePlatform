package com.example.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, AUTHOR, ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.toString();
    }
}

package com.example.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;

public class HeaderUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    public HeaderUsernamePasswordAuthenticationFilter(AuthenticationManager auth) {
        super(auth);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter#obtainPassword(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return request.getHeader(this.getPasswordParameter());
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter#obtainUsername(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return request.getHeader(this.getPasswordParameter());
    }
}

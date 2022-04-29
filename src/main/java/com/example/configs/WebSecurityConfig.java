package com.example.configs;

import com.example.domain.Role;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userService;
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    public WebSecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .mvcMatchers("/", "/home", "/registration").permitAll()
                    .mvcMatchers("/account", "/courses").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.AUTHOR.name())
                    .mvcMatchers("/author/**").hasRole(Role.AUTHOR.name())
                    .mvcMatchers("/admin/**").hasRole(Role.ADMIN.name())
                    .anyRequest().authenticated()
                .and()
                    .formLogin().loginPage("/login").permitAll()
                .and()
                    .logout().permitAll();
    }

    @Bean
    public static BCryptPasswordEncoder getBCryptPasswordEncoder() {
        return bCryptPasswordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(getBCryptPasswordEncoder());
    }
}
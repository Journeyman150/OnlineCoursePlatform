package com.example.domain;

import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

public class User implements UserDetails {
    private long id;

    @NotEmpty(message = "Name should not be empty.")
    @Size(min = 2, max = 30, message = "Name should be between 2 and 30 characters.")
    private String name;

    @NotEmpty(message = "Surname should not be empty.")
    @Size(min = 2, max = 30, message = "Surname should be between 2 and 30 characters.")
    private String surname;

    @NotEmpty(message = "Email should not be empty.")
    @Email(message = "Email should be valid.")
    private String email;

    @NotEmpty(message = "Password should not be empty.")
    @Size(min = 7, max = 15, message = "Password length should be between 7 and 15.")
    private String password;

    private Set<Role> authorities;

    private int balance;

    public User() {
    }

    public User(String name, String surname, String email, String password, Set<Role> authorities) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Set<Role> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(Set<Role> authorities) {
        this.authorities = authorities;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setAll(User updatedUser) {
        this.setName(updatedUser.getName());
        this.setSurname(updatedUser.getSurname());
        this.setEmail(updatedUser.getEmail());
        this.setPassword(updatedUser.getPassword());
        this.setAuthorities(updatedUser.getAuthorities());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", authorities=" + authorities +
                ", balance=" + balance +
                '}';
    }
}

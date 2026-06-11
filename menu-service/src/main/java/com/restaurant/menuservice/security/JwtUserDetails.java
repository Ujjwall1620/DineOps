package com.restaurant.menuservice.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
public class JwtUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String springRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(springRole));
    }

    @Override public String getPassword()                  { return null; }
    @Override public String getUsername()                  { return username; }
    @Override public boolean isAccountNonExpired()         { return true; }
    @Override public boolean isAccountNonLocked()          { return true; }
    @Override public boolean isCredentialsNonExpired()     { return true; }
    @Override public boolean isEnabled()                   { return true; }
}

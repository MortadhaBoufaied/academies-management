package com.footballacademy.security;

import com.footballacademy.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public
class UserPrincipal implements UserDetails {
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;
    public UserPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> roleNames = new LinkedHashSet<>();
        if (user.getMainRole() != null) {
            roleNames.add(user.getMainRole() .name());
        }
        if (user.getRoles() != null) {
            user.getRoles() .stream() .filter(role -> role != null && role.getName() != null) .map(role -> role.getName() .trim()) .filter(name -> !name.isBlank()) .forEach(roleNames::add);
        } return roleNames.stream() .map(roleName -> roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName) .map(SimpleGrantedAuthority::new) .collect(Collectors.toList());
    }
    @Override
    public String getPassword() {
        return user.getMdp();
    }
    @Override
    public String getUsername() {
        return user.getEmail();
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
        return user.getActive() == null || Boolean.TRUE.equals(user.getActive());
    }
    // Direct access to user properties
    public Long getId() {
        return user.getId();
    }
    public String getNom() {
        return user.getNom();
    }
    public User.UserRole getMainRole() {
        return user.getMainRole();
    }
    public Long getAcademyId() {
        return user.getAcademyId();
    }
    public User getUser() {
        return user;
    }
}

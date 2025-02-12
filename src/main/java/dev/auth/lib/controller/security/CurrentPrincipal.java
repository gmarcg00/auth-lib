package dev.auth.lib.controller.security;

import dev.auth.lib.data.model.User;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class CurrentPrincipal {

    private static final String ROLE_PREFIX = "ROLE_";

    @Getter
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CurrentPrincipal () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        user = (User) authentication.getPrincipal();
        authorities = authentication.getAuthorities();
    }

    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_PREFIX + "ADMIN"));
    }

    public long getId() {
        return user.getId();
    }
}

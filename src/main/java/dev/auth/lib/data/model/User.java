package dev.auth.lib.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column(name = "creation_date")
    private Instant creationDate;

    @Column(name = "last_password_change")
    private Instant lastPasswordChange;

    @Column(name = "external_user")
    private Boolean externalUser;

    @Column(name = "verification_code")
    private String verificationCode;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private UserStatus status;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Collection<String> getFlattenRoles() {
        return roles.stream()
                .map(Role::getName)
                .toList();
    }

    public Collection<String> getFlattenPermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorities = new HashSet<>();

        for (Role role: roles) {
            authorities.add(getAuthorityForRole(role));
            authorities.addAll(this.getPermissionsAuthoritiesFromRole(role));
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new).toList();
    }

    private static String getAuthorityForRole(Role role) {
        return ROLE_PREFIX + role.getName();
    }

    private List<String> getPermissionsAuthoritiesFromRole(Role role) {
        if (Objects.isNull(role.getPermissions())) {
            return Collections.emptyList();
        }
        return role.getPermissions().stream()
                .map(Permission::getName)
                .toList();
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
}

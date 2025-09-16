package org.example.expert.domain.common.dto;

import java.util.Collection;
import java.util.List;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthUser(
        Long id,
        String email,
        UserRole userRole,
        String nickname,
        Collection<? extends GrantedAuthority> authorities) {

    public AuthUser(Long userId, String email, UserRole userRole, String nickname) {
        this(userId, email, userRole, nickname, List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name())));
    }
}

package org.example.expert.domain.common.dto;

import org.example.expert.domain.user.enums.UserRole;

public record AuthUser(Long id, String email, UserRole userRole, String nickname) {

}

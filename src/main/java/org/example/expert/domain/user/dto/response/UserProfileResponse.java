package org.example.expert.domain.user.dto.response;

public record UserProfileResponse(
        Long userId,
        String email,
        String nickname,
        String imageDownloadUrl) {
}


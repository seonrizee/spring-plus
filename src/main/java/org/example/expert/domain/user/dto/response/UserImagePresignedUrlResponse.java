package org.example.expert.domain.user.dto.response;

public record UserImagePresignedUrlResponse(
        String presignedUrl,
        String objectKey) {
}

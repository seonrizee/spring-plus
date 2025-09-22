package org.example.expert.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.response.UserImagePresignedUrlResponse;
import org.example.expert.domain.user.dto.response.UserProfileResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.infra.s3.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    private static final String PROFILE_IMAGE_FOLDER_NAME = "profiles";

    /**
     * 내 프로필 정보와 프로필 이미지의 Presigned URL을 함께 반환합니다.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        String objectKey = user.getProfileImageUrl();
        String downloadUrl = null;
        if (objectKey != null && !objectKey.isBlank()) {
            downloadUrl = s3Service.generatePresignedUrlForGet(objectKey);
        }
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), downloadUrl);
    }

    /**
     * 프로필 이미지 업로드를 위한 Presigned URL과 S3 Object Key를 생성합니다. 항상 새로운 objectKey를 발급합니다.
     */
    public UserImagePresignedUrlResponse getProfileImageUploadUrl(Long userId, String filename) {

        String safeFilename = sanitizeFilename(filename);
        String objectKey = String.format("%s/%d/%s_%s",
                PROFILE_IMAGE_FOLDER_NAME, userId, UUID.randomUUID(), safeFilename);
        String contentType = determineContentType(safeFilename);
        String presignedUrl = s3Service.generatePresignedUrlForUpload(objectKey, contentType);
        return new UserImagePresignedUrlResponse(presignedUrl, objectKey);
    }

    /**
     * S3 업로드 완료 후, 파일 경로를 DB에 저장합니다. 이전 이미지가 있으면 S3에서 삭제합니다.
     */
    @Transactional
    public void updateProfileImagePath(Long userId, String objectKey) {

        String expectedPrefix = "profiles/" + userId + "/";
        if (objectKey == null || !objectKey.startsWith(expectedPrefix)) {
            throw new InvalidRequestException("Invalid objectKey");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String oldObjectKey = user.getProfileImageUrl();
        if (oldObjectKey != null && !oldObjectKey.equals(objectKey)) {
            s3Service.deleteObject(oldObjectKey);
        }

        user.updateProfileImageUrl(objectKey);
    }

    /**
     * 서버측에서 즉시 프로필 이미지를 삭제하고 DB 경로를 비웁니다.
     */
    @Transactional
    public void deleteProfileImage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        String objectKey = user.getProfileImageUrl();
        if (objectKey == null || objectKey.isBlank()) {
            return; // 삭제할 이미지 없음
        }

        s3Service.deleteObject(objectKey);
        user.updateProfileImageUrl(null);
    }


    /**
     * 파일 확장자에 따른 Content-Type을 결정합니다. 알 수 없는 확장자는 application/octet-stream으로 처리합니다.
     */
    private String determineContentType(String filename) {

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    /**
     * 파일 이름에서 경로 구분자 제거 및 안전한 문자만 허용하도록 정제합니다.
     */
    private String sanitizeFilename(String filename) {

        if (filename == null || filename.isBlank()) {
            throw new InvalidRequestException("filename is required");
        }

        String lastSegment = filename.replace('\\', '/');
        int idx = lastSegment.lastIndexOf('/');
        if (idx >= 0) {
            lastSegment = lastSegment.substring(idx + 1);
        }

        lastSegment = lastSegment.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (lastSegment.isBlank()) {
            throw new InvalidRequestException("invalid filename");
        }

        return lastSegment;
    }
}


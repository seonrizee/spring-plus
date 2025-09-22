package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.ImagePathRequest;
import org.example.expert.domain.user.dto.request.UserImageUploadRequest;
import org.example.expert.domain.user.dto.response.UserImagePresignedUrlResponse;
import org.example.expert.domain.user.dto.response.UserProfileResponse;
import org.example.expert.domain.user.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 내 프로필 조회 (프로필 이미지를 위한 Presigned URL 포함)
    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(profileService.getMyProfile(authUser.id()));
    }

    // 업로드용 Presigned URL 발급 (추가 + 수정)
    @PostMapping("/image/url")
    public ResponseEntity<UserImagePresignedUrlResponse> getUploadUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserImageUploadRequest request) {
        return ResponseEntity.ok(profileService.getProfileImageUploadUrl(authUser.id(), request.filename()));
    }

    // 클라이언트에서 업로드 완료 후 DB에 경로(objectKey) 저장 요청 (추가 + 수정)
    @PutMapping("/image")
    public ResponseEntity<Void> updateImagePath(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody ImagePathRequest request) {
        profileService.updateProfileImagePath(authUser.id(), request.objectKey());
        return ResponseEntity.ok().build();
    }

    // 서버에서 S3 삭제 + DB 경로 삭제
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteProfileImage(@AuthenticationPrincipal AuthUser authUser) {
        profileService.deleteProfileImage(authUser.id());
        return ResponseEntity.noContent().build();
    }
}

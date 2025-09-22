package org.example.expert.infra.s3;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 파일을 업로드할 수 있는 Presigned URL을 생성합니다.
     *
     * @param objectKey   S3에 저장될 파일의 고유한 경로와 이름
     * @param contentType 업로드될 파일의 MimeType (예: "image/jpeg")
     * @return 클라이언트가 파일을 직접 업로드할 수 있는 서명된 URL
     */
    public String generatePresignedUrlForUpload(String objectKey, String contentType) {

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // URL 유효 시간 10분
                .putObjectRequest(objectRequest)
                .build();

        String url = s3Presigner.presignPutObject(presignRequest).url().toString();
        log.info("Generated Presigned URL for PUT: [{}]", url);
        return url;
    }

    /**
     * 파일 다운로드를 위한 Presigned GET URL을 생성합니다.
     *
     * @param objectKey S3에서 다운로드 받을 파일의 고유한 경로와 이름
     * @return 클라이언트가 파일을 직접 다운로드 받을 수 있는 서명된 URL
     */
    public String generatePresignedUrlForGet(String objectKey) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // URL 유효 시간 10분
                .getObjectRequest(getObjectRequest)
                .build();

        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        log.info("Generated Presigned URL for GET: [{}]", url);
        return url;
    }

    /**
     * S3에서 특정 파일을 바로 삭제합니다.
     *
     * @param objectKey 삭제할 파일의 고유한 경로와 이름
     */
    public void deleteObject(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("Deleted object from S3: [{}]", objectKey);
    }
}

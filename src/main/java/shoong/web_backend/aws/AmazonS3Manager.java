package shoong.web_backend.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.config.AmazonConfig;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;



@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

    private final S3Client s3Client;
    private final AmazonConfig amazonConfig;

    public String upLoadFile(String keyName, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fullKeyName = keyName + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(amazonConfig.getBucket())
                    .key(fullKeyName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // URL 생성
            return getFileUrl(amazonConfig.getBucket(), fullKeyName);
        } catch (IOException e) {
            log.error("error at AmazonS3Manager uploadFile", e);
            throw e;
        }
    }

    private String getFileUrl(String bucket, String key) {
        // 일반 URL 생성
        String endpoint = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                amazonConfig.getRegion(),
                key);
        return endpoint;

        // 또는 서명된 URL이 필요한 경우 아래 코드 사용
        /*
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7)) // URL의 만료 기간 설정
                .getObjectRequest(req -> req.bucket(bucket).key(key))
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
        */
    }

    public void deleteFile(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();

            // 경로에서 첫 번째 '/'를 제거
            String key = path;
            if (path.startsWith("/")) {
                key = path.substring(1);
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(amazonConfig.getBucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (URISyntaxException e) {
            log.error("error at AmazonS3Manager deleteFile: Invalid URL", e);
        } catch (Exception e) {
            log.error("error at AmazonS3Manager deleteFile", e);
        }
    }

    public String generateItemKeyName() {
        String uuid = UUID.randomUUID().toString();
        return amazonConfig.getItemPath() + '/' + uuid;
    }

    public String generateLiveKeyName() {
        String uuid = UUID.randomUUID().toString();
        return amazonConfig.getLivePath() + '/' + uuid;
    }

    public String generateBrandKeyName() {
        String uuid = UUID.randomUUID().toString();
        return amazonConfig.getBrandPath() + '/' + uuid;
    }
}
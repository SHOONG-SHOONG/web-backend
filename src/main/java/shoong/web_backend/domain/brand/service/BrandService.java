package shoong.web_backend.domain.brand.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.aws.AmazonS3Manager;
import shoong.web_backend.domain.brand.dto.BrandRequestDto;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.brand.repository.BrandRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final AmazonS3Manager amazonS3Manager;
    private final UserRepository userRepository;

    @Transactional
    public void createBrand(BrandRequestDto brandRequestDto, User user) {
        if (user.getRole() == null || !user.getRole().equals(UserRole.STREAMER)) {
            throw new IllegalStateException("해당 유저는 스트리머 권한이 없습니다.");
        }

        // 기본 이미지 URL (이미지가 없을 경우를 대비)
        String imageUrl = "https://기본이미지URL.jpg"; // 필요시 기본 이미지 URL 설정

        MultipartFile imageFile = brandRequestDto.getImageFile();

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // S3에 라이브 이미지 업로드
                String keyName = amazonS3Manager.generateBrandKeyName();
                imageUrl = amazonS3Manager.upLoadFile(keyName, imageFile);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        Brand brand = Brand.builder()
                .brandName(brandRequestDto.getBrandName())
                .brandDescription(brandRequestDto.getBrandDescription())
                .logoUrl(imageUrl)
                .build();

        brandRepository.save(brand);

        user.setBrand(brand);
        userRepository.save(user);
    }
}
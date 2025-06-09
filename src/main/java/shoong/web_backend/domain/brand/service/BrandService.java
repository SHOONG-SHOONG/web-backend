package shoong.web_backend.domain.brand.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.aws.AmazonS3Manager;
import shoong.web_backend.domain.brand.dto.BrandItemDto;
import shoong.web_backend.domain.brand.dto.BrandRequestDto;
import shoong.web_backend.domain.brand.dto.BrandResponseDto;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.brand.repository.BrandRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;

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

        // 테스트 용 이미지
        String imageUrl = "https://기본이미지URL.jpg";

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

    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(Long brandId) {
        Brand brand = brandRepository.findById(brandId).orElseThrow(() ->
                new IllegalArgumentException("해당 브랜드가 존재하지 않습니다."));

        List<BrandItemDto> brandItems = brand.getItems().stream()
                .map(item -> BrandItemDto.builder()
                        .itemId(item.getItemId())
                        .name(item.getItemName())
                        .price(item.getPrice())
                        .imageUrl(item.getItemImages().get(0).getUrl())
                        .build())
                .toList();

        return BrandResponseDto.builder()
                .brandName(brand.getBrandName())
                .brandDescription(brand.getBrandDescription())
                .logoUrl(brand.getLogoUrl())
                .items(brandItems)
                .build();
    }
}
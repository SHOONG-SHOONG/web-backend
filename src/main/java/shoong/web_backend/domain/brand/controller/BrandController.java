package shoong.web_backend.domain.brand.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.domain.brand.dto.BrandRequestDto;
import shoong.web_backend.domain.brand.dto.BrandResponseDto;
import shoong.web_backend.domain.brand.service.BrandService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/brand")
public class BrandController {

    private final BrandService brandService;
    private final UserRepository userRepository;

    @Operation(summary = "브랜드 등록", description = "브랜드 등록 API")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createBrand(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {

        BrandRequestDto brandRequestDto = new BrandRequestDto(name, description, imageFile);

        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));

        brandService.createBrand(brandRequestDto, user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "브랜드 상세 조회", description = "브랜드 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{brandId}")
    public ResponseEntity<BrandResponseDto> getBrand(@PathVariable Long brandId) {
        BrandResponseDto brandResponseDto = brandService.getBrandById(brandId);

        return ResponseEntity.ok(brandResponseDto);
    }
}
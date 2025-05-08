package shoong.web_backend.domain.item_image.service;// example

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.aws.AmazonS3Manager;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item_image.entity.ItemImage;
import shoong.web_backend.domain.item_image.repository.ItemImageRepository;

@Service
@RequiredArgsConstructor
public class ItemImageService {

    private final AmazonS3Manager amazonS3Manager;
    private final ItemImageRepository itemImageRepository;

//    @Transactional
//    public void saveMultiImages(MultipartFile[] imageFiles, Item item) {
//        if (imageFiles == null || imageFiles.length == 0) return;
//
//        for (MultipartFile imageFile : imageFiles) {
//            if (imageFile != null && !imageFile.isEmpty()) {
//                try {
//                    // S3에 이미지 업로드
//                    String keyName = amazonS3Manager.generateLiveKeyName();
//                    String imageUrl = amazonS3Manager.upLoadFile(keyName, imageFile);
//
//                    // ItemImage 엔티티 생성 및 저장
//                    ItemImage itemImage = new ItemImage();
//                    itemImage.setUrl(imageUrl);
//                    itemImage.setCreatedAt(LocalDateTime.now());
//                    itemImage.setItem(item);
//
//                    // 양방향 관계 설정 - ItemImage를 Item의 컬렉션에도 추가
//                    item.getItemImages().add(itemImage);
//
//                    itemImageRepository.save(itemImage);
//                } catch (IOException e) {
//                    throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
//                }
//            }
//        }
//    }

    /**
     * 이미지를 저장하고 저장된 이미지 목록을 반환하는 메서드
     */
    @Transactional
    public List<ItemImage> saveMultiImagesAndReturnList(MultipartFile[] imageFiles, Item item) {
        List<ItemImage> savedImages = new ArrayList<>();

        if (imageFiles == null || imageFiles.length == 0) return savedImages;

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    // S3에 이미지 업로드
                    String keyName = amazonS3Manager.generateItemKeyName();
                    String imageUrl = amazonS3Manager.upLoadFile(keyName, imageFile);

                    // ItemImage 엔티티 생성 및 저장
                    ItemImage itemImage = new ItemImage();
                    itemImage.setUrl(imageUrl);
                    itemImage.setCreatedAt(LocalDateTime.now());
                    itemImage.setItem(item);

                    // 양방향 관계 설정 - ItemImage를 Item의 컬렉션에도 추가
                    item.getItemImages().add(itemImage);

                    ItemImage savedImage = itemImageRepository.save(itemImage);
                    savedImages.add(savedImage);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
                }
            }
        }

        return savedImages;
    }

    @Transactional(readOnly = true)
    public List<ItemImage> getImagesByItemId(Long itemId) {
        return itemImageRepository.findByItem_ItemId(itemId);
    }

    /**
     * 이미지 삭제 메서드
     */
    @Transactional
    public void deleteImage(ItemImage itemImage) {
        try {
            // 이미지 URL에서 키 추출 (S3 URL 형식에 따라 변경 필요)
            String imageUrl = itemImage.getUrl();
            String keyName = extractKeyFromUrl(imageUrl);

            // S3에서 파일 삭제
            amazonS3Manager.deleteFile(keyName);

            // DB에서 이미지 정보 삭제
            itemImageRepository.delete(itemImage);
        } catch (Exception e) {
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * URL에서 S3 키 추출하는 메서드
     * S3 URL 구조에 맞게 커스터마이징 필요
     */
    private String extractKeyFromUrl(String imageUrl) {
        // 예시: https://bucket-name.s3.region.amazonaws.com/images/2023/11/filename.jpg
        // 에서 "images/2023/11/filename.jpg" 부분을 추출

        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                return path.substring(1); // 첫 번째 '/' 제거
            }
            return path;
        } catch (MalformedURLException e) {
            throw new RuntimeException("S3 URL 파싱 중 오류가 발생했습니다.", e);
        }
    }
}
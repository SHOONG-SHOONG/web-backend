package shoong.web_backend.domain.user.dto;// UserDTO.java

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long brandId;
    private String userEmail;
    private String userPassword;      // 필요에 따라 제외 가능
    private String userName;
    private String userPhone;
    private String bdate;             // String or LocalDate (프론트와 협의)
    private String role;
    private String registrationNumber;
    private String userStatus;
    private String accessToken;
    private Long kakaoId;
}

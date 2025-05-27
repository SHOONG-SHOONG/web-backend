package shoong.web_backend.domain.user.dto;// UserDTO.java

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String brandName;
    private String userEmail;
    private String userPassword;      // 필요에 따라 제외 가능
    private String userName;
    private String userPhone;
    private LocalDate bdate;
    private String role;
    private String registrationNumber;
    private String userStatus;

}

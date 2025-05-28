package shoong.web_backend.domain.admin.dto;

import lombok.Builder;
import lombok.Data;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;

import java.time.LocalDate;

@Data
@Builder
public class AdminUserDto {
    private Long id;
    private String userAlias;
    private String userEmail;
    private String userName;
    private String name;
    private String userPhone;
    private LocalDate birthDay;
    private UserRole role;
    private String registrationNumber;
    private UserStatus userStatus;
    private String userAddress;
}

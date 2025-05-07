package shoong.web_backend.domain.user.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 회원가입DTO
@NoArgsConstructor
@Getter
@Setter
public class JoinDto {
    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String userEmail;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String userPassword;

    @NotBlank(message = "ID는 빈 칸이 될 수 없습니다.")
    private String userName;

    @NotBlank(message =  "이름은 필수입니다.")
    private String name;

    @Pattern(regexp = "\\d{3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 올바르지 않습니다.")
    private String userPhone;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDay;

    private String registrationNumber;
    // 주소는 필수 X
    private String userAddress;

    public JoinDto(String userEmail, String userPassword, String userName, String userPhone,
                   LocalDate birthDay, String registrationNumber, String userAddress) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userPhone = userPhone;
        this.birthDay = birthDay;
        this.registrationNumber = registrationNumber;
        this.userAddress = userAddress;
    }
}

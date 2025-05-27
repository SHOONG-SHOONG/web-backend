package shoong.web_backend.domain.user.repository;// example

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUserName(String username);
    User findByUserName(String username);
    User findByUserEmail(String email);
    Optional<User> findByBrandBrandId(Long brandId);
    boolean existsByUserEmailAndUserName(@Email(message = "이메일 형식이 아닙니다.") @NotBlank(message = "이메일은 필수입니다.") String userEmail, @NotBlank(message = "사용자 이름은 필수입니다.") String userName);
    boolean existsByUserEmail(String email);
    List<User> findByRoleAndUserStatus(UserRole role, UserStatus status);
}
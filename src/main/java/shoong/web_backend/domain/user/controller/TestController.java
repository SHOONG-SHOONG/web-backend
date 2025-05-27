package shoong.web_backend.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.user.dto.UserDTO;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.dto.form.JoinDto;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final UserRepository userRepository;

    @GetMapping("/myPage")
    public ResponseEntity<UserDTO> whoAreU(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Optional<User> userOptional = userRepository.findById(customUserDetails.getUserId());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();

        UserDTO userDTO = UserDTO.builder()
                .brandName(user.getBrand().getBrandName())
                .userEmail(user.getUserEmail())
                .userName(user.getName())
                .userPhone(user.getUserPhone())
                .bdate(user.getBirthDay())
                .registrationNumber(user.getRegistrationNumber())
                .userStatus(user.getUserStatus().name())
                .build();

        return ResponseEntity.ok(userDTO);
    }
}

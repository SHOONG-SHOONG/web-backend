package shoong.web_backend.domain.user.service.form;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.user.dto.form.JoinDto;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;
import shoong.web_backend.domain.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void join(JoinDto joinDto) {
        // 이메일 또는 유저명 중복 확인
        boolean isExist = userRepository.existsByUserEmail(joinDto.getUserEmail());

        if (isExist) {
            System.out.println("이미 존재하는 사용자입니다.(중복 이메일)");
            return;
        }
        // 회원가입 분기점
        // 사업자 번호가 존재할 경우 -> 스트리머
        // 사업자 번호가 존재하지 않을 경우 -> 클라이언트
        if (joinDto.getRegistrationNumber() == null || joinDto.getRegistrationNumber().isBlank()) {
            User userEntity = User.builder()
                    .userEmail(joinDto.getUserEmail())
                    .userPassword(bCryptPasswordEncoder.encode(joinDto.getUserPassword()))
                    .userName(joinDto.getUserName())
                    .userPhone(joinDto.getUserPhone())
                    .birthDay(joinDto.getBirthDay())
                    .registrationNumber(joinDto.getRegistrationNumber())
                    .userAddress(joinDto.getUserAddress())
                    .role(UserRole.CLIENT)  // 기본값 USER, 필요시 ADMIN도 가능
                    .userStatus(UserStatus.ACTIVE)  // 기본값 ACTIVE로 가정
                    .build();
            userRepository.save(userEntity);

        }
        if (!joinDto.getRegistrationNumber().isBlank()) {
            User userEntity = User.builder()
                    .userEmail(joinDto.getUserEmail())
                    .userPassword(bCryptPasswordEncoder.encode(joinDto.getUserPassword()))
                    .userName(joinDto.getUserName())
                    .userPhone(joinDto.getUserPhone())
                    .birthDay(joinDto.getBirthDay())
                    .registrationNumber(joinDto.getRegistrationNumber())
                    .userAddress(joinDto.getUserAddress())
                    .role(UserRole.STREAMER)  // 기본값 USER, 필요시 ADMIN도 가능
                    .userStatus(UserStatus.ACTIVE)  // 기본값 ACTIVE로 가정
                    .build();

            userRepository.save(userEntity);
        }
    }
}

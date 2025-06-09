package shoong.web_backend.domain.user.service.form;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.user.dto.form.JoinDto;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.generator.NickNameGenerator;

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
        // 유저 별명
        String aliasToSet = Optional.ofNullable(joinDto.getUserAlias())
                .filter(alias -> !alias.isBlank())
                .orElseGet(NickNameGenerator::getRandomNickname);
        // 회원가입 분기점
        // 사업자 번호가 존재할 경우 -> 스트리머
        // 사업자 번호가 존재하지 않을 경우 -> 클라이언트
        System.out.println("유저 회원가입 분기점 돌입");
        if (joinDto.getRegistrationNumber() == null || joinDto.getRegistrationNumber().isBlank()) {
            User userEntity = User.builder()
                    .userEmail(joinDto.getUserEmail())
                    .userPassword(bCryptPasswordEncoder.encode(joinDto.getUserPassword()))
                    .userName(joinDto.getUserName())
                    .name(joinDto.getName())
                    .userPhone(joinDto.getUserPhone())
                    .birthDay(joinDto.getBirthDay())
                    .registrationNumber(joinDto.getRegistrationNumber())
                    .userAddress(joinDto.getUserAddress())
                    .role(UserRole.CLIENT)
                    .userStatus(UserStatus.ACTIVE)
                    .userAlias(aliasToSet)
                    .build();
            userRepository.save(userEntity);
        }

        if (!joinDto.getRegistrationNumber().isBlank()) {
            User userEntity = User.builder()
                    .userEmail(joinDto.getUserEmail())
                    .userPassword(bCryptPasswordEncoder.encode(joinDto.getUserPassword()))
                    .userName(joinDto.getUserName())
                    .name(joinDto.getName())
                    .userPhone(joinDto.getUserPhone())
                    .birthDay(joinDto.getBirthDay())
                    .registrationNumber(joinDto.getRegistrationNumber())
                    .userAddress(joinDto.getUserAddress())
                    .role(UserRole.STREAMER)
                    .userStatus(UserStatus.PENDING)
                    .userAlias(aliasToSet)
                    .build();

            userRepository.save(userEntity);
        }
    }
}

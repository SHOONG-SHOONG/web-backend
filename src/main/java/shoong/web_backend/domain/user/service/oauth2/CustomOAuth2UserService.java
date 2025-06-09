package shoong.web_backend.domain.user.service.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shoong.web_backend.domain.user.dto.oauth2.CustomOAuth2User;
import shoong.web_backend.domain.user.dto.oauth2.KakaoResponse;
import shoong.web_backend.domain.user.dto.oauth2.OAuth2Response;
import shoong.web_backend.domain.user.dto.oauth2.OAuth2UserDto;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.generator.NickNameGenerator;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response response = null;

        if (registrationId.equals("kakao")) {
            response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }
        // provider name + provider Id 로 username(식별자) 생성
        String username = response.getProvider() + " " + response.getProviderId();
        CustomOAuth2User customOAuth2User = null;
        String role = String.valueOf(UserRole.CLIENT);
        System.out.println("식별자 생성 loadUser 실행" + username);

        // DB에 저장
        saveUser(response, username, role);

        OAuth2UserDto oAuth2UserDto = OAuth2UserDto.builder()
                .username(username)
                .name(response.getName())
                .email(response.getEmail())
                .role(role)
                .build();

        customOAuth2User = new CustomOAuth2User(oAuth2UserDto);

        return customOAuth2User;
    }

    // 업데이트
    private void saveUser(OAuth2Response oAuth2Response, String username, String role) {
        // DB 조회
        User isExist = userRepository.findByUserEmail(oAuth2Response.getEmail());

        if (isExist != null) {
            isExist.setUserName(username);
            isExist.setUserEmail(oAuth2Response.getEmail());
            isExist.setRole(UserRole.CLIENT);
        } else {
            String nickname = NickNameGenerator.getRandomNickname();
            User saveUserEntity = User.builder()
                    .userName(username)
                    .name(oAuth2Response.getName())
                    .userPassword("abcdefghi!")
                    .userEmail(oAuth2Response.getEmail())
                    .role(UserRole.CLIENT)
                    .userStatus(UserStatus.ACTIVE)
                    .userAlias(nickname)
                    .build();
            userRepository.save(saveUserEntity);
        }
    }
}

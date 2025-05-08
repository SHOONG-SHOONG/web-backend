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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // userRequest -> registration 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response response = null;

        // 존재하는 provider 인지 확인
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

        // DB save
        saveUser(response, username, role);

        // Entity 목적 순수하게 유지하기 위해서 dto 로 전달..
        OAuth2UserDto oAuth2UserDto = OAuth2UserDto.builder()
                .username(username)
                .name(response.getName())
                .email(response.getEmail())
                .role(role)
                .build();

        customOAuth2User = new CustomOAuth2User(oAuth2UserDto);

        // 서버 내부에서 사용하기 위한 인증 정보
        return customOAuth2User;
    }

    /**
     * 이미 존재하는 경우 update
     * 존재하지 않는 경우 save
     */
    private void saveUser(OAuth2Response oAuth2Response, String username, String role) {
        // DB 조회
        User isExist = userRepository.findByUserEmail(oAuth2Response.getEmail());

        if (isExist != null) {
            isExist.setUserName(oAuth2Response.getName());
            isExist.setUserEmail(oAuth2Response.getEmail());
            isExist.setRole(UserRole.CLIENT);
        } else {
            User saveUserEntity = User.builder()
                    .userName(username)
                    .name(oAuth2Response.getName())
                    .userEmail(oAuth2Response.getEmail())
                    .role(UserRole.CLIENT)
                    .userStatus(UserStatus.ACTIVE)
                    .build();
            userRepository.save(saveUserEntity);
        }
    }
}
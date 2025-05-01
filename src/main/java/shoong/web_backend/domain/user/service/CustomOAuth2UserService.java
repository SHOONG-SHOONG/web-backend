//package shoong.web_backend.domain.user.service;
//
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import shoong.web_backend.domain.user.dto.UserLoginResponseDTO;
//
//import java.util.Map;
//
//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private HttpSession session;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
//
//        System.out.println("🎀🎀 loadUser() 호출됨");
//
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        String accessToken = userRequest.getAccessToken().getTokenValue();
//        Long kakaoId = ((Number) attributes.get("id")).longValue();
//        System.out.println("AccessToken: " + accessToken);
//        System.out.println("KakaoId: " + kakaoId);
//
//
//        if ("kakao".equals(registrationId)) {
//            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
//
//            UserLoginResponseDTO user = UserLoginResponseDTO.builder()
//                    .userEmail(kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : null)
//                    .userPassword(null)
//                    .userName(properties != null && properties.get("nickname") != null ? properties.get("nickname").toString() : null)
//                    .userPhone(kakaoAccount.get("phone_number") != null ? kakaoAccount.get("phone_number").toString() : null)
//                    .bdate(null) // birthday는 형식 변환이 필요할 수도 있음!
//                    .role("USER")
//                    .registrationNumber(null)
//                    .userStatus("ACTIVE")
//                    .accessToken(accessToken)
//                    .kakaoId(kakaoId)
//                    .build();
//
//            // access token, user 서버세션에 저장
//            session.setAttribute("access_token", accessToken);
//            session.setAttribute("user", user);
//
//            /**
//             * 1. DB에 이 user 저장 (userRepository.save())
//             * 2. 이미 가입된 유저면 로그인 처리만
//             */
//
//            // 예시
//            // User existingUser = userRepository.findByKakaoId(kakaoId);
//            // if (existingUser == null) {
//            //     userRepository.save(user);
//            // }
//
//            // ** 추가로 세션 저장할 수 있음
//        }
//
//        return oAuth2User;
//    }
//
//}

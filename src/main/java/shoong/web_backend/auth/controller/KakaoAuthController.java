package shoong.web_backend.auth.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import shoong.web_backend.domain.user.dto.UserDTO;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class KakaoAuthController {

    private final String restApiKey = System.getenv("REST_API_KEY");
    private final String redirectUri = System.getenv("REDIRECT_URI");


    // POST /api/auth/kakao  (프론트에서 code를 POST로 보냄)
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> params) throws Exception {
        System.out.println("kakao login");
        String code = params.get("code");

        // 1. 인가코드로 카카오 access token 요청
        String accessToken = getKakaoAccessToken(code);

        // 2. access token으로 사용자 정보 요청
        UserDTO user = getKakaoUserInfo(accessToken);

        // 3. DB에 등록 할거임 (추후)

        // 4. 결과 반환
        Map<String, Object> result = new HashMap<>();
        System.out.println("결과" + user);
        result.put("user", user);
        // result.getSession().setAttribute("user", user);
        return ResponseEntity.ok(result);
    }

    // 카카오에서 access token 받아오기
    private String getKakaoAccessToken(String code) throws Exception {
        String url = "https://kauth.kakao.com/oauth/token";
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", restApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        System.out.println((String) (response.getBody().get("access_token")));
        return (String) response.getBody().get("access_token");
    }

    // access token으로 카카오 사용자 정보 받아서 UserDTO 생성
    private UserDTO getKakaoUserInfo(String accessToken) throws Exception {
        String url = "https://kapi.kakao.com/v2/user/me";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);


        Map<String, Object> responseMap = response.getBody();
        System.out.println("카카오 응답: " + responseMap);

        if (responseMap == null || responseMap.get("kakao_account") == null) {
            throw new RuntimeException("카카오 계정 정보가 없습니다. 전체 응답: " + responseMap);
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) responseMap.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) responseMap.get("properties");
        Long kakaoId = ((Number) responseMap.get("id")).longValue();

        UserDTO user = UserDTO.builder()
                .userEmail(kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : null)
                .userPassword(null)
                .userName(properties != null && properties.get("nickname") != null ? properties.get("nickname").toString() : null)
                .userPhone(kakaoAccount.get("phone_number") != null ? kakaoAccount.get("phone_number").toString() : null)
                .bdate(kakaoAccount.get("birthday") != null ? kakaoAccount.get("birthday").toString() : null)
                .role("USER")
                .registrationNumber(null)
                .userStatus("ACTIVE")
                .accessToken(accessToken)
                .kakaoId(kakaoId)
                .build();

        return user;
    }
}

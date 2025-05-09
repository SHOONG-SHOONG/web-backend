package shoong.web_backend.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.dto.form.JoinDto;

@RestController
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/test")
    public String whoAreU(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return customUserDetails.getUserId().toString();
    }
}

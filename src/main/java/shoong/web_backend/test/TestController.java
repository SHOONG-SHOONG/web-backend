package shoong.web_backend.test;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;

@RestController("/test")
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/whoami")
    public String whoAreU(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return customUserDetails.getUserId().toString();
    }

    @GetMapping("/user-alias")
    public String userAlias(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return customUserDetails.getUserAlias().toString();
    }
}

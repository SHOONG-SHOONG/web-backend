package shoong.web_backend.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.user.dto.form.JoinDto;
import shoong.web_backend.domain.user.service.form.JoinService;

@RestController
@RequiredArgsConstructor
public class JoinController {
    private final JoinService joinService;
    @PostMapping("/join")
    public String joinProc(@ModelAttribute JoinDto joinDto) {
        joinService.join(joinDto);
        return "ok";
    }
}

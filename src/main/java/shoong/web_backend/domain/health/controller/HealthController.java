package shoong.web_backend.domain.health.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
@RequestMapping("/actuator")

public class HealthController {
    @GetMapping("/health") // 또는 /actuator/health 등 원하는 경로
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK"); // 200 OK 응답과 "OK" 문자열 반환
    }
}

package shoong.web_backend.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.item.service.ItemService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

// 판매자(admin) 페이지
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final ItemService itemService;
    private final UserRepository userRepository;
    @PostMapping("/")
    public String adminP(){
        return "Admin Page";
    }

    @GetMapping("/item-list")
    public String adminItems(@AuthenticationPrincipal CustomUserDetails userDetails){
        User user = userRepository.findByUserName(userDetails.getUsername());
        itemService.getAdminItemList(user.getId());
        return "Admin Page";
    }
}
package shoong.web_backend.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.admin.dto.AdminItemDto;
import shoong.web_backend.domain.admin.dto.AdminUserDto;
import shoong.web_backend.domain.admin.service.SuperAdminService;
import shoong.web_backend.domain.user.entity.User;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SuperAdminController {
    private final SuperAdminService adminService;

    @Operation(summary = "대기 사용자 조회")
    @GetMapping("/pending/users")
    public List<AdminUserDto> getPendingUsers() {
        return adminService.getPendingUsers();
    }

    @Operation(summary = "사용자 승인")
    @PostMapping("/{userId}/user/approve")
    public ResponseEntity<Void> approveStreamer(@PathVariable Long userId) {
        adminService.approveStreamer(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 거절")
    @PostMapping("/{userId}/user/reject")
    public ResponseEntity<Void> rejectStreamer(@PathVariable Long userId) {
        adminService.rejectStreamer(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "대기 아이템 조회")
    @GetMapping("/pending/items")
    public List<AdminItemDto> getPendingItems() {
        return adminService.getPendingItem();
    }

    @Operation(summary = "아이템 승인")
    @PostMapping("/{itemId}/item/approve")
    public ResponseEntity<Void> approveItem(@PathVariable Long itemId) {
        adminService.approveItems(itemId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "아이템 거절")
    @PostMapping("/{itemId}/item/reject")
    public ResponseEntity<Void> rejectItem(@PathVariable Long itemId) {
        adminService.rejectItems(itemId);
        return ResponseEntity.ok().build();
    }
}

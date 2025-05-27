package shoong.web_backend.domain.user.enums;

public enum UserStatus {
    ACTIVE, // 관리자가 승인
    PENDING, // 대기 상태
    INACTIVE // 관리자가 거절
}

package shoong.web_backend.domain.item.enums;

public enum ItemStatus {
    ON_SALE, // 관리자 승인
    SOLD_OUT, // 판매 종료
    DELETED, // 삭제
    PENDING, // 승인 대기
    INACTIVE // 관리자 거절
}

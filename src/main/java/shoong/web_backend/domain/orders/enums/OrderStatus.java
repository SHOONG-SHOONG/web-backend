package shoong.web_backend.domain.orders.enums;

public enum OrderStatus {
    CREATED,       // 장바구니에서 주문하기 버튼 눌렀을 때 생성
    PAID,          // 결제 완료
    FAILED,        // 결제 실패
    CANCELED       // 사용자가 결제 취소
}

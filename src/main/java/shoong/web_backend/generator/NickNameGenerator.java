package shoong.web_backend.generator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NickNameGenerator {
    private static final List<String> ADJECTIVES = List.of(
            "떠드는", "신나는", "설레는", "귀여운", "활기찬", "유쾌한",
            "조용한", "따뜻한", "즐거운", "명랑한", "상큼한", "발랄한",
            "씩씩한", "다정한", "포근한", "반짝이는", "반가운", "쾌활한",
            "톡톡 튀는", "사랑스러운", "달콤한", "상냥한", "밝은", "멋진",
            "느긋한", "부드러운", "행복한", "순수한", "당당한", "멋쟁이"
    );
    private static final List<String> FRIENDS = List.of(
            "쿠", "달보", "위비", "봄봄", "바몽", "두지"
    );

    // 멀티 스레드 환경을 위한 ThreadLocalRandom
    public static String getRandomNickname() {
        String adjective = ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size()));
        String friend = FRIENDS.get(ThreadLocalRandom.current().nextInt(FRIENDS.size()));
        return adjective + " " + friend;
    }
}

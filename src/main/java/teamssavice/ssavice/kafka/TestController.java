package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/test")
@RequiredArgsConstructor
public class TestController {
    private final DiscordNotifier discordNotifier;

    @GetMapping
    public void test() {
        discordNotifier.sendDltAlert("원복토픽", "예외 메시지", "테스트 트레이스");
    }
}

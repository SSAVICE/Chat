package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.account.AccountInfoDto;
import teamssavice.ssavice.account.service.AccountReadService;

import java.util.List;

@RestController
@RequestMapping("api/test")
@RequiredArgsConstructor
public class TestController {
    private final AccountReadService accountReadService;


    @GetMapping
    public Mono<List<AccountInfoDto>> test() {
        return accountReadService.findAccountBySubjectIn(List.of(1L, 2L));
    }

}

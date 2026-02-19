package teamssavice.ssavice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.DataNotFoundException;
import teamssavice.ssavice.user.entity.UserEntity;
import teamssavice.ssavice.user.infrastructure.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserReadService {
    private final UserRepository userRepository;

    public Mono<UserEntity> findById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new DataNotFoundException(ErrorCode.USER_NOT_FOUND)));
    }
}

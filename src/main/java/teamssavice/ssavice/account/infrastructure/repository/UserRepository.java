package teamssavice.ssavice.account.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import teamssavice.ssavice.account.UserEntity;

public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
}

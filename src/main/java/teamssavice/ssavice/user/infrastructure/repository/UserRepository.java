package teamssavice.ssavice.user.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import teamssavice.ssavice.user.entity.UserEntity;

public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
}

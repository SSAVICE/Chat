package teamssavice.ssavice.account.infrastructure.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import teamssavice.ssavice.account.AccountEntity;
import teamssavice.ssavice.account.AccountInfoDto;

import java.util.List;

public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {
    @Query("""
                SELECT a.id as account_id, a.provider_id as provider_id, a.role as role,
                       c.company_name as company_name, u.name as user_name
                FROM account a
                LEFT JOIN company c ON a.id = c.id
                LEFT JOIN users u ON a.id = u.id
                WHERE a.id IN (:subjects)
            """)
    Flux<AccountInfoDto> findAccountInfoDtoBySubjectIn(List<Long> subjects);
}

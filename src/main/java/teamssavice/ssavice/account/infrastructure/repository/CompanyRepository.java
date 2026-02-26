package teamssavice.ssavice.account.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import teamssavice.ssavice.account.CompanyEntity;

public interface CompanyRepository extends R2dbcRepository<CompanyEntity, Long> {
}

package teamssavice.ssavice.company.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import teamssavice.ssavice.company.entity.CompanyEntity;

public interface CompanyRepository extends R2dbcRepository<CompanyEntity, Long> {
}

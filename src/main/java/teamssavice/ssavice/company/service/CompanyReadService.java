package teamssavice.ssavice.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.company.entity.CompanyEntity;
import teamssavice.ssavice.company.infrastructure.repository.CompanyRepository;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.DataNotFoundException;

@Service
@RequiredArgsConstructor
public class CompanyReadService {
    private final CompanyRepository companyRepository;

    public Mono<CompanyEntity> findById(Long id) {
        return companyRepository.findById(id)
                .switchIfEmpty(Mono.error(new DataNotFoundException(ErrorCode.COMPANY_NOT_FOUND)));
    }
}

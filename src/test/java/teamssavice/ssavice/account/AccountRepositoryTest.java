package teamssavice.ssavice.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import teamssavice.ssavice.account.infrastructure.repository.AccountRepository;
import teamssavice.ssavice.account.infrastructure.repository.CompanyRepository;
import teamssavice.ssavice.account.infrastructure.repository.UserRepository;
import teamssavice.ssavice.global.constants.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private AccountEntity account1;
    private AccountEntity account2;
    private UserEntity user;
    private CompanyEntity company;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll().block();
        userRepository.deleteAll().block();
        companyRepository.deleteAll().block();

        AccountEntity account1 = AccountEntity.builder().providerId("provider1").role(Role.USER).build();
        AccountEntity account2 = AccountEntity.builder().providerId("provider1").role(Role.COMPANY).build();

        UserEntity user = UserEntity.builder().id(account1.getId()).name("user").build();
        CompanyEntity company = CompanyEntity.builder().id(account2.getId()).companyName("name").build();

        this.account1 = accountRepository.save(account1).block();
        this.account2 = accountRepository.save(account2).block();
        this.user = userRepository.save(user).block();
        this.company = companyRepository.save(company).block();
    }

    @Test
    @DisplayName("subject로 account 조회 테스트")
    void findAccountInfoDtoBySubjectTest() {

        StepVerifier.create(accountRepository.findAccountInfoDtoBySubject(account1.getId()))
                .assertNext(account -> {
                    assertThat(account.getRole()).isEqualTo(Role.USER);
                    assertThat(account.getUserName()).isEqualTo(user.getName());
                })
                .verifyComplete();
    }
}
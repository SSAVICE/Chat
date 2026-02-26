package teamssavice.ssavice.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import teamssavice.ssavice.global.constants.Role;

@Table("account")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {
    @Id
    private Long id;
    @NotNull
    private String providerId;
    @NotNull
    private Role role;
}

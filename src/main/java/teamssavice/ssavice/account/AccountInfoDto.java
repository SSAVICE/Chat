package teamssavice.ssavice.account;

import lombok.Builder;
import lombok.Getter;
import teamssavice.ssavice.global.constants.Role;

@Builder
@Getter
public class AccountInfoDto {
    private Long accountId;
    private String providerId;
    private Role role;
    private String userName;
    private String companyName;

    public String getName() {
        return Role.USER.equals(role) ? userName : companyName;
    }
}

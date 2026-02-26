package teamssavice.ssavice.account;

import lombok.Getter;
import teamssavice.ssavice.global.constants.Role;

@Getter
public class AccountInfoDto {
    private Long accountId;
    private String providerId;
    private Role role;
    private String userName;
    private String companyName;
}

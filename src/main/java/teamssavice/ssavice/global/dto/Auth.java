package teamssavice.ssavice.global.dto;

import teamssavice.ssavice.global.constants.Role;

public record Auth(
        String subject,
        Role role,
        Long id
) {
    public static Auth of(String subject) {
        String[] auth = subject.split(":");
        return new Auth(subject, Role.valueOf(auth[0]), Long.parseLong(auth[1]));
    }

    public boolean canAccess(Role required) {
        return required.canAccess(role);
    }
}

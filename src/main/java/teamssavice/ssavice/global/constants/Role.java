package teamssavice.ssavice.global.constants;

public enum Role {
    USER,
    ADMIN,
    COMPANY;

    public boolean canAccess(Role required) {
        return this == required;
    }
}

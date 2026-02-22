package teamssavice.ssavice.global.dto;

public record Auth(
        Long subject
) {
    public static Auth of(Long subject) {
        return new Auth(subject);
    }
}

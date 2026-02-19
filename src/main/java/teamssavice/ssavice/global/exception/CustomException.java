package teamssavice.ssavice.global.exception;

import lombok.Getter;
import teamssavice.ssavice.global.constants.ErrorCode;

@Getter
public abstract class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String title;

    public CustomException(ErrorCode errorCode, String title) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.title = title;
    }
}
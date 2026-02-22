package teamssavice.ssavice.global.exception;

import teamssavice.ssavice.global.constants.ErrorCode;

public class ForbiddenException extends CustomException {

    private static final String DEFAULT_TITLE = "Forbidden";

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode, DEFAULT_TITLE);
    }

}
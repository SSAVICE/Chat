package teamssavice.ssavice.global.exception;

import teamssavice.ssavice.global.constants.ErrorCode;

public class AuthenticationException extends CustomException {

    private static final String DEFAULT_TITLE = "Authentication Error";

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode, DEFAULT_TITLE);
    }

}
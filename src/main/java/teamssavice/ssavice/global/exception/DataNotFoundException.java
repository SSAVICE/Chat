package teamssavice.ssavice.global.exception;

import teamssavice.ssavice.global.constants.ErrorCode;

public class DataNotFoundException extends CustomException {
    private static final String DEFAULT_TITLE = "Data Not Found";

    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode, DEFAULT_TITLE);
    }
}

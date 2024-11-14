package Back.whats_your_ETF.apiPayload.handler;


import Back.whats_your_ETF.apiPayload.GeneralException;
import Back.whats_your_ETF.apiPayload.code.BaseErrorCode;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

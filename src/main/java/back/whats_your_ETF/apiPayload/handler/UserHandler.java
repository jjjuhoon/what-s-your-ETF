package back.whats_your_ETF.apiPayload.handler;


import back.whats_your_ETF.apiPayload.GeneralException;
import back.whats_your_ETF.apiPayload.code.BaseErrorCode;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

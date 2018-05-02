package nl.wittig.net2grid_ble.onboarding.api;

import android.content.Context;

import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.onboarding.api.model.BaseInternalResponse;
import nl.wittig.net2grid_ble.onboarding.api.model.Error;

public class CodeMsgHandler {

    public static final int[] REGISTRATION_FATAL_EXCEPTION_CODES = new int[] {102, 111, 120, 121, 122, 123};
    public static final int[] NON_TOAST_EXCEPTION_CODES = new int[] {101, 110, 170, 190, 200, 201, 202, 203, 204, 224, 401};

    public static final String UNKNOWN_CODE = "unknown code";

    public interface STANDARD_RESPONSE_HTTP_CODE {
        int OK = 200;
        int AUTHORIZATION_FAILED = 401;
        int INTERNAL_SERVER_ERROR = 500;    //add others if need
    }

    private Context context;

    public CodeMsgHandler(Context context) {
        this.context = context;
    }

    public String getMessageFromStandardHTTPCode(int code){
        switch (code) {
            case STANDARD_RESPONSE_HTTP_CODE.AUTHORIZATION_FAILED:
                return getString(R.string.authorization_fail);
            case STANDARD_RESPONSE_HTTP_CODE.INTERNAL_SERVER_ERROR:
                return getString(R.string.internal_server_error);
            default:
                return getString(R.string.unknown_server_error);
        }
    }

    public BaseInternalResponse prepareMessageObjectFromApiError(Error error) {
        if (error == null || error.getCode() == Error.NO_CODE || error.getMessage() == null || error.getMessage().isEmpty()) {
            return new BaseInternalResponse(getMessageForUnhandledStatus(), error.getCode());
        } else if (error.getCode() == 102) {
            return new BaseInternalResponse(getString(R.string.registration_error_102_msg) + " " + error.getCode() + ")", error.getCode());
        } else if ((error.getCode() >= 120 && error.getCode() <= 123) || error.getCode() == 111) {
            return new BaseInternalResponse(getString(R.string.registration_error_12X_msg) + " " + error.getCode() + ")", error.getCode());
        } else {
            return new BaseInternalResponse(error.getMessage(), error.getCode());
        }
    }

    public String getNoInternetMeassage(){
        return getString(R.string.no_internet);
    }

    /**
     * Returns an universal message for response with status field other than STATUS_OK, but without error object
     * @return
     */
    public String getMessageForUnhandledStatus() {
        return getString(R.string.unknown_error);
    }

    public String getStandardApiErrorMessage() {
        return getString(R.string.standard_api_error_message);
    }

    private String getString(int resStringId) {
        return context.getResources().getString(resStringId);
    }
}

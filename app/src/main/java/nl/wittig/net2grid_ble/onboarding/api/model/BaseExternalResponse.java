package nl.wittig.net2grid_ble.onboarding.api.model;

import com.google.gson.annotations.SerializedName;

import nl.wittig.net2grid_ble.onboarding.api.CodeMsgHandler;
import nl.wittig.net2grid_ble.utils.Tools;

/**
 *
 * Every response should extend that class, to could handle status and error code/msg
 */
public class BaseExternalResponse {

    public static final String STATUS_OK = "ok";

    @SerializedName("status")
    private String status;
    private Error error;

    public BaseExternalResponse() {
    }

    public String getStatus() {
        return status;
    }

    public Error getError() {
        return error;
    }

    public boolean isFatalRegistrationError() {
        return getError() != null && Tools.isCodeInArray(getError().getCode(), CodeMsgHandler.REGISTRATION_FATAL_EXCEPTION_CODES);
    }

}

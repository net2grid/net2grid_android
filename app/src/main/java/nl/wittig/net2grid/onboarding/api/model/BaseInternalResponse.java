package nl.wittig.net2grid.onboarding.api.model;

public class BaseInternalResponse {

    public static final int NO_CODE = 0;

    private boolean isSuccess;
    private String message;
    private int code = NO_CODE;

    public BaseInternalResponse() {
    }

    public BaseInternalResponse(String message) {
        this.message = message;
    }

    public BaseInternalResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

package nl.wittig.net2grid_ble.onboarding.api.model;

public class Error {

    public static final int NO_CODE = -1;

    private int code = NO_CODE;
    private String message;

    public Error() {
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

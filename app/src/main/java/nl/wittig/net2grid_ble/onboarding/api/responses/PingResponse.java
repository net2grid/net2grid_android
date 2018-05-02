package nl.wittig.net2grid_ble.onboarding.api.responses;

import com.google.gson.annotations.SerializedName;

public class PingResponse {

    @SerializedName("result")
    private String result;

    public String getResult() {
        return result;
    }

    public boolean isInternetAvailable() {
        return result != null && result.equals("ok");   //on no internet resposne is result:"error"
    }
}

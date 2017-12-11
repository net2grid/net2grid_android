package nl.wittig.net2grid.onboarding.api.requests;

import com.google.gson.annotations.SerializedName;

public class JoinNetworkRequest {
    public static final String TAG = JoinNetworkRequest.class.getSimpleName();

    @SerializedName("ssid")
    private String ssid;
    @SerializedName("key")
    private String password;

    public JoinNetworkRequest(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }

    public JoinNetworkRequest(String ssid) {
        this(ssid, null);
    }
}

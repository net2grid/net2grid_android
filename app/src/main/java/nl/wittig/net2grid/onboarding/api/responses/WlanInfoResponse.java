package nl.wittig.net2grid.onboarding.api.responses;

import com.google.gson.annotations.SerializedName;

public class WlanInfoResponse {
    public static final String TAG = WlanInfoResponse.class.getSimpleName();
    @SerializedName("mode")
    private String mode;
    @SerializedName("ap_ssid")
    private String ssid;
    @SerializedName("ap_key")
    private String key;
    @SerializedName("client_ssid")
    private String clientSsid;
    @SerializedName("client_key")
    private String clientKey;
    @SerializedName("ip_addr")
    private String ipAddress;
    @SerializedName("mac")
    private String mac;

    public WlanInfoResponse() {
    }

    public String getMode() {
        return mode;
    }

    public String getSsid() {
        return ssid;
    }

    public String getKey() {
        return key;
    }

    public String getClientSsid() {
        return clientSsid;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMac() {
        return mac;
    }
}

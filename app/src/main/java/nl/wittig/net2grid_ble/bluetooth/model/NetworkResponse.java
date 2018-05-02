package nl.wittig.net2grid_ble.bluetooth.model;

import com.google.gson.annotations.SerializedName;

public class NetworkResponse {
    public static final String TAG = NetworkResponse.class.getSimpleName();
    @SerializedName("rssi")
    private int rssi;
    @SerializedName("encryption")
    private Boolean encryption = true;
    @SerializedName("ssid")
    private String ssid;

    public NetworkResponse() {
    }

    public NetworkResponse(int rssi, boolean encryption, String ssid){
        this.rssi = rssi;
        this.encryption = encryption;
        this.ssid = ssid;
    }

    public Boolean getEncryption() {
        return encryption;
    }

    public int getRssi() {
        return rssi;
    }

    public String getSsid() {
        return ssid;
    }
}

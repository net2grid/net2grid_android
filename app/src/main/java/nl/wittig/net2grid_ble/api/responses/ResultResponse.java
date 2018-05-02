package nl.wittig.net2grid_ble.api.responses;

import com.google.gson.annotations.SerializedName;

public class ResultResponse {

    @SerializedName("value")
    private int value;

    @SerializedName("unit")
    private String unit;

    @SerializedName("time")
    private long time;

    public int getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public long getTime() {
        return time;
    }
}

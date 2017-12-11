package nl.wittig.net2grid.api.responses;

import com.google.gson.annotations.SerializedName;

public class ElectricityResponse {

    @SerializedName("power")
    private ResultSetResponse power;

    @SerializedName("consumption")
    private ResultSetResponse consumption;

    @SerializedName("production")
    private ResultSetResponse production;

    public ResultSetResponse getPower() {
        return power;
    }

    public ResultSetResponse getConsumption() {
        return consumption;
    }

    public ResultSetResponse getProduction() {
        return production;
    }
}

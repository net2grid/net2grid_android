package nl.wittig.net2grid_ble.api.responses;

import com.google.gson.annotations.SerializedName;

public class MeterResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("elec")
    public ElectricityResponse electricity;

    @SerializedName("gas")
    public ElectricityResponse gas;

    public String getStatus() {
        return status;
    }

    public ElectricityResponse getElectricity() {
        return electricity;
    }

    public ElectricityResponse getGas() {
        return gas;
    }
}

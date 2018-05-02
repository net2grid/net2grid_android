package nl.wittig.net2grid_ble.onboarding.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NetworkListResponse {
    public static final String TAG = NetworkListResponse.class.getSimpleName();

    @SerializedName("APList")
    private List<NetworkResponse> networkResponseList;

    public NetworkListResponse() {
    }

    public List<NetworkResponse> getNetworkResponseList() {
        return networkResponseList;
    }
}

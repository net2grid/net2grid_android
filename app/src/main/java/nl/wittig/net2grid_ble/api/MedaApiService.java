package nl.wittig.net2grid_ble.api;

import nl.wittig.net2grid_ble.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MedaApiService {

    @GET("wlan/info")
    Call<WlanInfoResponse> getWlanInfoResponse();
}

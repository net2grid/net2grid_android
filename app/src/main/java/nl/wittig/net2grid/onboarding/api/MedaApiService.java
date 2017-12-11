package nl.wittig.net2grid.onboarding.api;

import nl.wittig.net2grid.onboarding.api.requests.JoinNetworkRequest;
import nl.wittig.net2grid.onboarding.api.responses.NetworkListResponse;
import nl.wittig.net2grid.onboarding.api.responses.PingResponse;
import nl.wittig.net2grid.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MedaApiService {

    @POST("wlan/scan")
    Call<NetworkListResponse> postWlanScan();

    @GET("wlan/info")
    Call<WlanInfoResponse> getWlanInfoResponse();

    @POST("wlan/join")
    Call<Void> postJoinToNetwork(@Body JoinNetworkRequest request);

    /**
     * Checking if meda has internet connection
     */
    @POST("wlan/ping")
    Call<PingResponse> ping();

    @POST("wlan/disable")
    Call<Void> disableAccessPoint();
}

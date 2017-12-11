package nl.wittig.net2grid.onboarding.api;

import android.util.Log;

import nl.wittig.net2grid.onboarding.api.requests.JoinNetworkRequest;
import nl.wittig.net2grid.onboarding.api.responses.NetworkListResponse;
import nl.wittig.net2grid.onboarding.api.responses.PingResponse;
import nl.wittig.net2grid.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedaApiManager {

    public static final String TAG = MedaApiManager.class.getSimpleName();
    private static final int SERVICE_UNAVAILABLE_CODE = 503;
    private static final boolean FORCE_ONLY_HTTP = true;
    private static final String MEDA_DEFAULT_IP = "10.5.6.1";

    private final MedaRestClient medaRestClient;
    private final MedaApiService httpApiService;

    public static MedaApiManager getDefaultInstance() {
        return new MedaApiManager(MEDA_DEFAULT_IP);
    }

    public MedaApiManager(String medaIp) {

        medaRestClient = new MedaRestClient(medaIp);

        httpApiService = medaRestClient.getHttpApiService();
    }

    public void isConnectionPossible(final Callback<WlanInfoResponse> callback) {

        httpApiService.getWlanInfoResponse().enqueue(new Callback<WlanInfoResponse>() {
            @Override
            public void onResponse(Call<WlanInfoResponse> call, Response<WlanInfoResponse> response) {

                if (!response.isSuccessful() && response.code() == SERVICE_UNAVAILABLE_CODE) {
                    onFailure(call, new Exception(exceptionMessage(response.code())));
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<WlanInfoResponse> call, Throwable t) {

                callback.onFailure(call, t);
            }
        });
    }

    public void loadNetworksList(final Callback<NetworkListResponse> callback) {

        httpApiService.postWlanScan().enqueue(new Callback<NetworkListResponse>() {


            @Override
            public void onResponse(Call<NetworkListResponse> call, Response<NetworkListResponse> response) {

                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    if (response.code() == SERVICE_UNAVAILABLE_CODE) {
                        onFailure(call, new Exception(exceptionMessage(response.code())));
                    } else {
                        callback.onFailure(call, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<NetworkListResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void joinToNetwork(final JoinNetworkRequest request, final Callback<Void> callback) {
        httpApiService.postJoinToNetwork(request).enqueue(new Callback<Void>() {


            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    if (response.code() == SERVICE_UNAVAILABLE_CODE) {
                        onFailure(call, new Exception(exceptionMessage(response.code())));
                    } else {
                        callback.onFailure(call, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                callback.onFailure(call, t);
            }
        });
    }

    public void ping(final Callback<PingResponse> callback) {
        httpApiService.ping().enqueue(new Callback<PingResponse>() {


            @Override
            public void onResponse(Call<PingResponse> call, Response<PingResponse> response) {

                Log.d(TAG, "PING onResponse: " + response.isSuccessful());

                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                }
                else {
                    if (response.code() == SERVICE_UNAVAILABLE_CODE) {
                        onFailure(call, new Exception(exceptionMessage(response.code())));
                    } else {
                        callback.onFailure(call, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<PingResponse> call, Throwable t) {

                callback.onFailure(call, t);
            }
        });
    }

    public void disableAccessPoint(final Callback<Void> callback) {
        httpApiService.disableAccessPoint().enqueue(new Callback<Void>() {


            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    if (response.code() == SERVICE_UNAVAILABLE_CODE) {
                        onFailure(call, new Exception(exceptionMessage(response.code())));
                    } else {
                        callback.onFailure(call, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    private String exceptionMessage(int exceptionCode) {
      return "code :" + exceptionCode;
    }
}

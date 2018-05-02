package nl.wittig.net2grid_ble.onboarding.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.util.List;

import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.helpers.AlertHelper;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import nl.wittig.net2grid_ble.onboarding.api.MedaApiManager;
import nl.wittig.net2grid_ble.onboarding.api.model.NetworkItemModel;
import nl.wittig.net2grid_ble.onboarding.api.responses.NetworkListResponse;
import nl.wittig.net2grid_ble.onboarding.api.responses.PingResponse;
import nl.wittig.net2grid_ble.utils.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PingingNetworkFragment extends LoadingNetworkFragment {

    public static final String TAG = PingingNetworkFragment.class.getSimpleName();
    private final long MAX_RETRY_CALLS = 3;

    public enum FragmentResponseType {
        PING_SUCCES,
        PING_FAIL,
        ERROR;
    }

    private String SSID;

    private MedaApiManager apiManager;
    private Handler handler = new Handler();
    private List<NetworkItemModel> networks;

    private int pingingFailed = 0;
    private boolean pingStarted;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        apiManager = MedaApiManager.getDefaultInstance();
    }

    private void ping() {

        apiManager.ping(new Callback<PingResponse>() {

            @Override
            public void onResponse(Call<PingResponse> call, Response<PingResponse> response) {

                if(!response.isSuccessful() || response.body() == null){
                    onFailure(call, new Throwable("Unsuccessful response"));
                    return;
                }

                if(response.body().isInternetAvailable()) {

                    // internet available
                    PersistentHelper.setSSID(SSID);

                    readyListener.onFragmentReady(PingingNetworkFragment.this, new FragmentResponse(FragmentResponseType.PING_SUCCES, null));
                }
                else {

                    // internet not available
                    loadNetworkListAndProceed();
                }

                Log.i(TAG, "onResponse: " + response.body().getResult());
            }

            @Override
            public void onFailure(Call<PingResponse> call, Throwable t) {

                // when the device cant be reached
                handleFailure();
            }
        });
    }

    public void loadNetworkListAndProceed() {

        apiManager.loadNetworksList(new Callback<NetworkListResponse>() {
            @Override
            public void onResponse(Call<NetworkListResponse> call, Response<NetworkListResponse> response) {

                networks = Tools.castNetworkListResponseToViewModels(response.body().getNetworkResponseList());
                readyListener.onFragmentReady(PingingNetworkFragment.this, new FragmentResponse(FragmentResponseType.PING_FAIL, networks));
            }

            @Override
            public void onFailure(Call<NetworkListResponse> call, Throwable t) {

                handleFailure();
            }
        });
    }

    public void handleFailure() {

        if (pingingFailed < MAX_RETRY_CALLS) {

            pingingFailed++;
            ping();
        }
        else {

            AlertHelper.errorOccured(getContext(), getString(R.string.error_message_no_meter_connected)).show();

            pingingFailed = 0;
            readyListener.onFragmentReady(PingingNetworkFragment.this, new FragmentResponse(FragmentResponseType.ERROR, null));
        }
    }

    @Override
    public void onFragmentVisible() {

        Log.i(TAG, "onFragmentVisible: pinging network visible");

        if(!pingStarted) {

            ping();
            pingStarted = true;
        }
    }

    @Override
    public void onFragmentHidden() {

        Log.i(TAG, "onFragmentHidden: pinging network hidden");
        pingStarted = false;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public class FragmentResponse {

        private FragmentResponseType type;

        private List<NetworkItemModel> networkItems;

        public FragmentResponse(FragmentResponseType type, List<NetworkItemModel> networkItems) {

            this.type = type;
            this.networkItems = networkItems;
        }

        public FragmentResponseType getType() {
            return type;
        }

        public List<NetworkItemModel> getNetworkItems() {
            return networkItems;
        }
    }
}

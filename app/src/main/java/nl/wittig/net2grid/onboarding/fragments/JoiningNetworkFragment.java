package nl.wittig.net2grid.onboarding.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import nl.wittig.net2grid.R;
import nl.wittig.net2grid.helpers.AlertHelper;
import nl.wittig.net2grid.helpers.PersistentHelper;
import nl.wittig.net2grid.onboarding.api.MedaApiManager;
import nl.wittig.net2grid.onboarding.api.model.NetworkItemModel;
import nl.wittig.net2grid.onboarding.api.requests.JoinNetworkRequest;
import nl.wittig.net2grid.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static nl.wittig.net2grid.onboarding.fragments.SelectNetworkFragment.TAG;

public class JoiningNetworkFragment extends LoadingNetworkFragment {

    private final long MAX_RETRY_CALLS = 2;

    public enum FragmentResponseType {
        JOIN_SUCCES,
        JOIN_FAILED,
        GET_INFO_FAILED
    }

    private MedaApiManager apiManager;
    private NetworkItemModel network;
    private String password;

    private int joiningFailed = 0;
    private int getInfoFailed = 0;
    private boolean joiningStarted;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiManager = MedaApiManager.getDefaultInstance();
    }

    private void joinNetwork() {

        apiManager.joinToNetwork(new JoinNetworkRequest(network.getName(), password), new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if(!response.isSuccessful()){

                    onFailure(call, new Throwable("Unsuccesfull response"));
                } else {

                    getInfo();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (joiningFailed < MAX_RETRY_CALLS) {

                    joiningFailed++;
                    joinNetwork();
                }
                else {

                    joiningFailed = 0;
                    readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(FragmentResponseType.JOIN_FAILED));
                }
            }
        });
    }

    public void getInfo() {

        apiManager.isConnectionPossible(new Callback<WlanInfoResponse>() {
            @Override
            public void onResponse(Call<WlanInfoResponse> call, Response<WlanInfoResponse> response) {

                PersistentHelper.setSSID(response.body().getClientSsid());

                onJoined();
            }

            @Override
            public void onFailure(Call<WlanInfoResponse> call, Throwable t) {

                Log.e(TAG, "onFailure: ", t);

                if (getInfoFailed < MAX_RETRY_CALLS) {

                    getInfoFailed++;
                    getInfo();
                } else {

                    AlertHelper.errorOccured(getContext(), getString(R.string.error_message_no_meter_connected)).show();
                    readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(FragmentResponseType.GET_INFO_FAILED));
                }
            }
        });
    }

    private void onJoined() {

        readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(FragmentResponseType.JOIN_SUCCES));
    }

    public void setNetwork(NetworkItemModel network, String password) {

        this.network = network;
        this.password = password;
    }

    @Override
    public void onFragmentVisible() {

        verifyingtext.setText(getString(R.string.connecting));

        if(!joiningStarted) {

            joinNetwork();
            joiningStarted = true;
        }
    }

    @Override
    public void onFragmentHidden() {

        joiningStarted = false;
    }

    public class FragmentResponse {

        private FragmentResponseType type;

        public FragmentResponse(FragmentResponseType type) {

            this.type = type;
        }

        public FragmentResponseType getType() {
            return type;
        }
    }
}

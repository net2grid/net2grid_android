package nl.wittig.net2grid_ble.onboarding.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.bluetooth.BluetoothManager;
import nl.wittig.net2grid_ble.helpers.AlertHelper;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import nl.wittig.net2grid_ble.onboarding.api.MedaApiManager;
import nl.wittig.net2grid_ble.onboarding.api.model.NetworkItemModel;
import nl.wittig.net2grid_ble.onboarding.api.requests.JoinNetworkRequest;
import nl.wittig.net2grid_ble.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static nl.wittig.net2grid_ble.onboarding.fragments.SelectNetworkFragment.TAG;

public class JoiningNetworkFragment extends LoadingNetworkFragment {

    private final long MAX_RETRY_CALLS = 2;

    public enum FragmentResponseType {
        JOIN_SUCCES,
        JOIN_FAILED,
        GET_INFO_FAILED,
        JOIN_BLUETOOTH_SUCCES,
        JOIN_DISCONNECTED,
        INFO_STILL_REACHABLE
    }

    private MedaApiManager apiManager;
    private NetworkItemModel network;
    private String password;

    private String mode;

    private Handler infoTimeoutHandler;
    private boolean infoTimeoutExceeded;

    private int joiningFailed = 0;
    private int getInfoFailed = 0;
    private boolean joiningStarted;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiManager = MedaApiManager.getDefaultInstance();
    }

    private void joinNetwork() {

        if (mode.equals(SelectBluetoothDeviceFragment.BLUETOOTH_MODE)) {

            BluetoothManager.getInstance().registerAndJoinCall(new nl.wittig.net2grid_ble.bluetooth.model.JoinNetworkRequest(network.getName(), password), new BluetoothManager.JoinNetworkCallback() {
                @Override
                public void onNetworkJoined() {

                    readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(FragmentResponseType.JOIN_BLUETOOTH_SUCCES));
                }

                @Override
                public void onFailure(boolean disconnected) {

                    Toast.makeText(getContext(), R.string.bluetooth_join_error, Toast.LENGTH_SHORT).show();
                    readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(disconnected ? FragmentResponseType.JOIN_DISCONNECTED : FragmentResponseType.JOIN_FAILED));
                }
            });
        }
        else {

            apiManager.joinToNetwork(new JoinNetworkRequest(network.getName(), password), new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if(!response.isSuccessful()){

                        onFailure(call, new Throwable("Unsuccesfull response"));
                    } else {

                        startInfoCalls();
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
    }

    private void startInfoCalls(){

        infoTimeoutHandler = new Handler();
        infoTimeoutHandler.postDelayed(infoTimeoutRunnable, 30000);

        // Call info
        getInfo();
    }

    public void getInfo() {

        MedaApiManager.getDefaultInstance().isConnectionPossible(new Callback<WlanInfoResponse>() {
            @Override
            public void onResponse(Call<WlanInfoResponse> call, Response<WlanInfoResponse> response) {

                getInfoFailed++;

                // Delay
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(!infoTimeoutExceeded) {
                            getInfo();
                        }
                    }
                }, 5000);
            }

            @Override
            public void onFailure(Call<WlanInfoResponse> call, Throwable t) {

                Log.e(TAG, "onFailure: ", t);

                if(infoTimeoutHandler != null){
                    infoTimeoutHandler.removeCallbacks(infoTimeoutRunnable);
                }

                if (!infoTimeoutExceeded) {
                    readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(FragmentResponseType.JOIN_SUCCES));
                }
            }
        });
    }

    public void setJoiningMode(String mode) {

        this.mode = mode;
    }

    public void setNetwork(NetworkItemModel network, String password) {

        this.network = network;
        this.password = password;
    }

    @Override
    public void onFragmentVisible() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                verifyingtext.setText(getString(R.string.connecting));

                if (!joiningStarted) {

                    joinNetwork();
                    joiningStarted = true;
                }
            }
        }, 2000);
    }

    @Override
    public void onFragmentHidden() {

        joiningStarted = false;
    }

    protected Runnable infoTimeoutRunnable = new Runnable() {
        @Override
        public void run() {

            infoTimeoutExceeded = true;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.check_smartbridge_online)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {

                        infoTimeoutExceeded = false;
                        startInfoCalls();
                    })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> {

                        readyListener.onFragmentReady(JoiningNetworkFragment.this, new FragmentResponse(FragmentResponseType.INFO_STILL_REACHABLE));
                    }).show();
        }
    };

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

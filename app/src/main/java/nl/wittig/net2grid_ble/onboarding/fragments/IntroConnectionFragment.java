package nl.wittig.net2grid_ble.onboarding.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.onboarding.api.MedaApiManager;
import nl.wittig.net2grid_ble.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static nl.wittig.net2grid_ble.onboarding.OnBoardingActivity.RECONNECTING;

public class IntroConnectionFragment extends OnBoardingFragment {

    public static final String TAG = IntroConnectionFragment.class.getSimpleName();
    public static final int CHECK_FOR_CONNECTION_DELAY_MS = 3000;

    @BindView(R.id.fragment_connect_goto_wifi_btn) FrameLayout gotoWifiBtn;
    @BindView(R.id.fragment_intro_connection_title_icon) ImageView titleIcon;

    private boolean connectionCheckInProgress;
    private boolean connectionCheckStarted;

    private Handler handler;
    private MedaApiManager medaApiManager;

    public IntroConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        medaApiManager = MedaApiManager.getDefaultInstance();
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_intro_connection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        gotoWifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        final boolean isReconnecting = getActivity().getIntent().getBooleanExtra(RECONNECTING, false);
        if (isReconnecting) {
            titleIcon.setImageResource(R.drawable.icon_cross);
        } else {
            titleIcon.setImageResource(R.drawable.back_triangle);
        }

        titleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
                handler.removeCallbacks(checkConnection);
            }
        });
    }

    private Runnable checkConnection = new Runnable() {
        @Override
        public void run() {

            if (connectionCheckInProgress)
                return;

            handler.removeCallbacks(checkConnection);
            connectionCheckInProgress = true;

            medaApiManager.isConnectionPossible(new Callback<WlanInfoResponse>() {

                @Override
                public void onResponse(Call<WlanInfoResponse> call, Response<WlanInfoResponse> response) {

                    if(!response.isSuccessful()){
                        onFailure(call, new Throwable("Unsuccessful response"));
                        return;
                    }
                    else if(response.body() == null){
                        onFailure(call, new Throwable("Empty body"));
                        return;
                    }

                    connectionCheckInProgress = false;

                    readyListener.onFragmentReady(IntroConnectionFragment.this, response.body());
                    handler.removeCallbacks(checkConnection);
                }

                @Override
                public void onFailure(Call<WlanInfoResponse> call, Throwable t) {

                    connectionCheckInProgress = false;
                    handler.postDelayed(checkConnection, CHECK_FOR_CONNECTION_DELAY_MS);
                }
            });
        }
    };

    @Override
    public void onFragmentVisible() {

        Log.i(TAG, "onFragmentVisible: introconnection visible");

        if(!connectionCheckStarted) {

            checkConnection.run();
            connectionCheckStarted = true;
        }
    }

    @Override
    public void onFragmentHidden() {

        Log.i(TAG, "onFragmentHidden: introconnection hidden");

        if(connectionCheckStarted) {

            handler.removeCallbacks(checkConnection);
            connectionCheckStarted = false;
        }
    }
}

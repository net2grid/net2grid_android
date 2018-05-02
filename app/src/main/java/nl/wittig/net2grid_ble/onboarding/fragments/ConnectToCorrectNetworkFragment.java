package nl.wittig.net2grid_ble.onboarding.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.helpers.WifiHelper;
import nl.wittig.net2grid_ble.onboarding.OnBoardingActivity;

public class ConnectToCorrectNetworkFragment extends OnBoardingFragment {

    @BindView(R.id.fragment_connect_to_correct_network_connected_title) TextView title;
    @BindView(R.id.fragment_connect_to_correct_network_connected_goto_wifi_btn) FrameLayout gotoWifiBtn;

    public enum FragmentResponseType {
        CORRECT,
        WRONG
    }

    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_connect_to_correct_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        handler = new Handler();

        gotoWifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    private Runnable checkNetworkSSID = new Runnable() {
        @Override
        public void run() {

            checkWifiNetwork();
        }
    };

    public void checkWifiNetwork() {

        String name = WifiHelper.getCurrentNetworkName(getActivity());

        if (WifiHelper.onSmartBridgeNetwork(name)) {

            readyListener.onFragmentReady(ConnectToCorrectNetworkFragment.this, new FragmentResponse(FragmentResponseType.CORRECT));
            return;
        }

        if (name != null || name != null && !name.equals("")) {

            title.setText(getString(R.string.wrong_network_title, name));
        } else {

            title.setText(getString(R.string.no_network_title));
        }

        handler.postDelayed(checkNetworkSSID, 1000);
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();

        checkWifiNetwork();
    }

    @Override
    protected void onFragmentHidden() {
        super.onFragmentHidden();

        handler.removeCallbacks(checkNetworkSSID);
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

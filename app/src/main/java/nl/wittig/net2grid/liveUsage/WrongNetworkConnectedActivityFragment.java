package nl.wittig.net2grid.liveUsage;

import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid.R;
import nl.wittig.net2grid.helpers.WifiHelper;

public class WrongNetworkConnectedActivityFragment extends Fragment {

    @BindView(R.id.fragment_wrong_network_connected_title) TextView title;
    @BindView(R.id.fragment_wrong_network_connected_goto_wifi_btn) FrameLayout gotoWifiBtn;

    private Handler handler;

    public WrongNetworkConnectedActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_wrong_network_connected, container, false);
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

        handler = new Handler();
        checkWifiNetwork();

        String wifiName = WifiHelper.getCurrentNetworkName(getContext());

        if (wifiName != null) {
            title.setText(getString(R.string.wrong_network_title, WifiHelper.getCurrentNetworkName(getContext())));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        handler.postDelayed(checkNetworkSSID, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();

        handler.removeCallbacks(checkNetworkSSID);
    }

    private Runnable checkNetworkSSID = new Runnable() {
        @Override
        public void run() {

            checkWifiNetwork();
        }
    };

    public void checkWifiNetwork() {

        String name = WifiHelper.getCurrentNetworkName(getContext());

        if (WifiHelper.onSmartBridgeNetwork(name))
            getActivity().finish();


        if (name != null) {

            title.setText(getString(R.string.wrong_network_title, name));
        } else {

            title.setText(getString(R.string.no_network_title));
        }

        handler.postDelayed(checkNetworkSSID, 1000);
    }
}

package nl.wittig.net2grid.liveUsage;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.InetAddress;

import nl.wittig.net2grid.R;
import nl.wittig.net2grid.api.Api;
import nl.wittig.net2grid.api.ServiceDiscoveryHelper;
import nl.wittig.net2grid.helpers.AlertHelper;
import nl.wittig.net2grid.helpers.PersistentHelper;
import nl.wittig.net2grid.onboarding.fragments.ResolvingNetworkFragment;

import static nl.wittig.net2grid.YnniApplication.SMARTBRIDGE_SERVICE_NAME;

public class DiscoverNetworkActivityFragment extends Fragment {

    public static final int MAX_RETRY_FAIL = 3;

    private String TAG = DiscoverNetworkActivityFragment.class.getSimpleName();

    private int failCount = 0;

    public DiscoverNetworkActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resolveSmartBridge();
    }

    private void resolveSmartBridge() {

        ServiceDiscoveryHelper serviceDiscoveryHelper = new ServiceDiscoveryHelper(getActivity());
        serviceDiscoveryHelper.setCallback(new ServiceDiscoveryHelper.OnResultCallback() {
            @Override
            public void onIpFound(InetAddress ip) {

                failCount = 0;

                Log.i(TAG, "onIpFound: " + ip.toString());
                PersistentHelper.setSmartBridgeHost(ip.getHostAddress());
                Api.resetApi();

                getActivity().finish();
            }

            @Override
            public void onError(Throwable t) {

                Log.i(TAG, "on error discovering");

                if (failCount >= MAX_RETRY_FAIL) {

                    AlertHelper.errorOccured(getActivity().getApplicationContext(), "Couldn't connect to SmartBridge").show();

                    failCount = 0;
                    getActivity().finish();

                } else {


                    failCount++;

                    resolveSmartBridge();
                }
            }
        });

        serviceDiscoveryHelper.findIp(SMARTBRIDGE_SERVICE_NAME);
    }
}

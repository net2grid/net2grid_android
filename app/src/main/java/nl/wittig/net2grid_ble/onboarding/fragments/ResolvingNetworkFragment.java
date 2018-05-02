package nl.wittig.net2grid_ble.onboarding.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhargavms.dotloader.DotLoader;

import java.net.InetAddress;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.MainActivity;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.api.Api;
import nl.wittig.net2grid_ble.api.ServiceDiscoveryHelper;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import nl.wittig.net2grid_ble.helpers.WifiHelper;

import static nl.wittig.net2grid_ble.YnniApplication.SMARTBRIDGE_SERVICE_NAME;

public class ResolvingNetworkFragment extends OnBoardingFragment {

    private static final String TAG = ResolvingNetworkFragment.class.getSimpleName();

    public enum FragmentResponseType {
        RESOLVED,
        FAILED
    }

    public ResolvingNetworkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_resolving_network, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();

        Log.i(TAG, "onFragmentVisible: resolvingnetworkfragment");

        resolveSmartBridge();
    }

    public void startCompletedAnimation(final int duration) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(readyListener != null)
                    readyListener.onFragmentReady(ResolvingNetworkFragment.this, new FragmentResponse(FragmentResponseType.RESOLVED));
            }
        }, duration);
    }

    private void resolveSmartBridge() {

        ServiceDiscoveryHelper serviceDiscoveryHelper = new ServiceDiscoveryHelper();
        serviceDiscoveryHelper.setCallback(new ServiceDiscoveryHelper.OnResultCallback() {
            @Override
            public void onIpFound(InetAddress ip) {

                Log.i(TAG, "onIpFound: " + ip.toString());

                PersistentHelper.setSSID(WifiHelper.getCurrentNetworkName(getContext()));
                PersistentHelper.setSmartBridgeHost(ip.getHostAddress());
                Api.resetApi();

                startCompletedAnimation(2000);
            }

            @Override
            public void onError(Throwable t) {

                if(readyListener != null)
                    readyListener.onFragmentReady(ResolvingNetworkFragment.this, new FragmentResponse(FragmentResponseType.FAILED));
            }
        });

        serviceDiscoveryHelper.findIp(SMARTBRIDGE_SERVICE_NAME, getContext());
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

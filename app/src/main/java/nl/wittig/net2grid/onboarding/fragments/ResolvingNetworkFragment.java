package nl.wittig.net2grid.onboarding.fragments;


import android.os.Bundle;
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
import nl.wittig.net2grid.R;
import nl.wittig.net2grid.api.Api;
import nl.wittig.net2grid.api.ServiceDiscoveryHelper;
import nl.wittig.net2grid.helpers.PersistentHelper;

import static nl.wittig.net2grid.YnniApplication.SMARTBRIDGE_SERVICE_NAME;

public class ResolvingNetworkFragment extends LoadingNetworkFragment {

    private static final String TAG = ResolvingNetworkFragment.class.getSimpleName();

    @BindView(R.id.fragment_resolving_network_dotloader) DotLoader dotLoader;
    @BindView(R.id.fragment_resolving_network_title) TextView titleView;

    @BindView(R.id.fragment_resolving_network_loading_frame) LinearLayout loadingFrame;
    @BindView(R.id.fragment_resolving_network_completed_frame) LinearLayout completedFrame;


    public enum FragmentResponseType {
        RESOLVED,
        FAILED
    }

    public ResolvingNetworkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_resolving_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();

        resolveSmartBridge();
    }

    @Override
    public void startCompletedAnimation(final int duration) {

        Animation animation = new AlphaAnimation(1f, 0f);
        animation.setDuration(duration);
        loadingFrame.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                loadingFrame.setVisibility(View.GONE);
                completedFrame.setVisibility(View.VISIBLE);

                Animation animation2 = new AlphaAnimation(0f, 1f);
                animation2.setDuration(duration);
                completedFrame.startAnimation(animation2);

                animation2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        readyListener.onFragmentReady(ResolvingNetworkFragment.this, new FragmentResponse(FragmentResponseType.RESOLVED));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void resolveSmartBridge() {

        ServiceDiscoveryHelper serviceDiscoveryHelper = new ServiceDiscoveryHelper(getActivity());
        serviceDiscoveryHelper.setCallback(new ServiceDiscoveryHelper.OnResultCallback() {
            @Override
            public void onIpFound(InetAddress ip) {

                Log.i(TAG, "onIpFound: " + ip.toString());
                PersistentHelper.setSmartBridgeHost(ip.getHostAddress());
                Api.resetApi();

                startCompletedAnimation(300);
            }

            @Override
            public void onError(Throwable t) {

                readyListener.onFragmentReady(ResolvingNetworkFragment.this, new FragmentResponse(FragmentResponseType.FAILED));
            }
        });

        serviceDiscoveryHelper.findIp(SMARTBRIDGE_SERVICE_NAME);
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

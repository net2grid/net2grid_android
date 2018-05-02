package nl.wittig.net2grid_ble.onboarding.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;

public class LoadingNetworkFragment extends OnBoardingFragment {

    @BindView(R.id.id_verifying) TextView verifyingtext;
    @BindView(R.id.id_connectedframe) LinearLayout connectedframe;
    @BindView(R.id.id_verifyingframe) LinearLayout verifyingframe;
    @BindView(R.id.id_connected) TextView connectedtext;

    public LoadingNetworkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connecting_to_network, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    public void startCompletedAnimation(final int duration) {

        Animation animation = new AlphaAnimation(1f, 0f);
        animation.setDuration(duration);
        verifyingframe.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                verifyingframe.setVisibility(View.GONE);
                connectedframe.setVisibility(View.VISIBLE);

                Animation animation2 = new AlphaAnimation(0f, 1f);
                animation2.setDuration(duration);
                connectedframe.startAnimation(animation2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}

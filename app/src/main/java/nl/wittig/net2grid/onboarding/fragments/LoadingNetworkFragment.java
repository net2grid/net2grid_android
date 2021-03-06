package nl.wittig.net2grid.onboarding.fragments;


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
import nl.wittig.net2grid.R;

public class LoadingNetworkFragment extends OnBoardingFragment {

    @BindView(R.id.verifying) TextView verifyingtext;
    @BindView(R.id.connectedframe) LinearLayout connectedframe;
    @BindView(R.id.verifyingframe) LinearLayout verifyingframe;
    @BindView(R.id.connected) TextView connectedtext;

    public LoadingNetworkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connecting_to_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
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

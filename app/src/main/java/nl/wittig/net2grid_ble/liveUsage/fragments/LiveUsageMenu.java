package nl.wittig.net2grid_ble.liveUsage.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.MainActivity;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.onboarding.OnBoardingActivity;

public class LiveUsageMenu extends Fragment {

    @BindView(R.id.fragment_menu_live_usage_connect) TextView connectBtn;

    public LiveUsageMenu() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_live_usage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent onBoardingIntent = new Intent(getContext(), MainActivity.class);
                onBoardingIntent.putExtra(OnBoardingActivity.RECONNECTING, true);
                startActivity(onBoardingIntent);
            }
        });
    }
}

package nl.wittig.net2grid_ble.onboarding;

import android.support.v4.app.Fragment;

public interface OnBoardingReadyListener {

    void onFragmentReady(Fragment fragment, Object response);
    void onNavigateBack();
    void onNavigateBack(String mode);
}

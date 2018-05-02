package nl.wittig.net2grid_ble.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import nl.wittig.net2grid_ble.liveUsage.LiveUsageActivity;
import nl.wittig.net2grid_ble.onboarding.api.model.NetworkItemModel;
import nl.wittig.net2grid_ble.onboarding.api.responses.WlanInfoResponse;
import nl.wittig.net2grid_ble.onboarding.fragments.ConnectToCorrectNetworkFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.JoiningNetworkFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.IntroConnectionFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.OnBoardingFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.PingingNetworkFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.ResolvingNetworkFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.SelectBluetoothDeviceFragment;
import nl.wittig.net2grid_ble.onboarding.fragments.SelectNetworkFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OnBoardingActivity extends AppCompatActivity implements OnBoardingFragment.ReadyListener {

    public static final String TAG = OnBoardingActivity.class.getSimpleName();

    public static final String RECONNECTING = "reconnecting";

    public enum Page {
        INTRO_CONNECTION,
        BLUETOOTH_SELECT_DEVICE,
        PINGING_NETWORK,
        SELECT_NETWORK,
        JOINING_NETWORK,
        CONNECTING_TO_CORRECT_NETWORK,
        RESOLVING_NETWORK
    }

    @BindView(R.id.viewPager) ViewPager viewPager;

    private IntroConnectionFragment introConnectionFragment;
    private SelectNetworkFragment selectNetworkFragment;
    private PingingNetworkFragment pingingNetworkFragment;
    private JoiningNetworkFragment joiningNetworkFragment;
    private ConnectToCorrectNetworkFragment connectToCorrectNetworkFragment;
    private ResolvingNetworkFragment resolvingNetworkFragment;
    private SelectBluetoothDeviceFragment selectBluetoothDeviceFragment;

    private List<OnBoardingFragment> configurationFragments;

    private OnBoardingPagerAdapter onBoardingPagerAdapter;

    private boolean mode;
    private String deviceName;
    private NetworkItemModel selectedNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        ButterKnife.bind(this);

        mode = getIntent().getBooleanExtra("bluetoothMode", false);
        deviceName = getIntent().getStringExtra("deviceName");

        setupViewPager(mode);
    }

    @Override
    protected void onResume() {
        super.onResume();

        OnBoardingFragment currentFragment = configurationFragments.get(viewPager.getCurrentItem());
        currentFragment.reportFragmentVisible(true);
    }

    @Override
    protected void onPostResume() {
        try {
            super.onPostResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        OnBoardingFragment currentFragment = configurationFragments.get(viewPager.getCurrentItem());
        currentFragment.reportFragmentVisible(false);
    }

    protected void navigateToPage(final Page page) {

        OnBoardingFragment currentFragment = configurationFragments.get(viewPager.getCurrentItem());
        currentFragment.reportFragmentVisible(false);

        viewPager.setCurrentItem(page.ordinal());

        OnBoardingFragment nextFragment = configurationFragments.get(page.ordinal());
        nextFragment.reportFragmentVisible(true);

    }

    private void setupViewPager(boolean bluetoothMode) {

        this.configurationFragments = new ArrayList<>();

        introConnectionFragment = new IntroConnectionFragment();
        introConnectionFragment.setFragmentReadyListener(this);
        configurationFragments.add(introConnectionFragment);

        selectBluetoothDeviceFragment = SelectBluetoothDeviceFragment.newInstance();
        selectBluetoothDeviceFragment.setFragmentReadyListener(this);
        selectBluetoothDeviceFragment.setBluetoothDeviceName(deviceName);
        configurationFragments.add(selectBluetoothDeviceFragment);

        pingingNetworkFragment = new PingingNetworkFragment();
        pingingNetworkFragment.setFragmentReadyListener(this);
        configurationFragments.add(pingingNetworkFragment);

        selectNetworkFragment = new SelectNetworkFragment();
        selectNetworkFragment.setFragmentReadyListener(this);
        configurationFragments.add(selectNetworkFragment);

        joiningNetworkFragment = new JoiningNetworkFragment();
        joiningNetworkFragment.setFragmentReadyListener(this);
        configurationFragments.add(joiningNetworkFragment);

        connectToCorrectNetworkFragment = new ConnectToCorrectNetworkFragment();
        connectToCorrectNetworkFragment.setFragmentReadyListener(this);
        configurationFragments.add(connectToCorrectNetworkFragment);

        resolvingNetworkFragment = new ResolvingNetworkFragment();
        resolvingNetworkFragment.setFragmentReadyListener(this);
        configurationFragments.add(resolvingNetworkFragment);

        onBoardingPagerAdapter = new OnBoardingPagerAdapter(OnBoardingActivity.this.getSupportFragmentManager(), this.configurationFragments);
        viewPager.setAdapter(onBoardingPagerAdapter);

        viewPager.setCurrentItem(bluetoothMode ? Page.BLUETOOTH_SELECT_DEVICE.ordinal() : Page.INTRO_CONNECTION.ordinal(), false);
    }

    @Override
    public void onFragmentReady(OnBoardingFragment sender, Object response) {

        if (sender instanceof IntroConnectionFragment && response instanceof WlanInfoResponse) {

            pingingNetworkFragment.setSSID(((WlanInfoResponse) response).getClientSsid());
            navigateToPage(Page.PINGING_NETWORK);
        }
        else if(sender instanceof SelectBluetoothDeviceFragment && response instanceof Boolean) {

            if(((Boolean) response).booleanValue()) {

                selectNetworkFragment.setScanMode(SelectBluetoothDeviceFragment.BLUETOOTH_MODE);
                navigateToPage(Page.SELECT_NETWORK);
            }
            else {

                navigateToPage(Page.INTRO_CONNECTION);
            }
        }
        else if (sender instanceof PingingNetworkFragment && response instanceof PingingNetworkFragment.FragmentResponse) {

            PingingNetworkFragment.FragmentResponseType type = ((PingingNetworkFragment.FragmentResponse) response).getType();

            if (type.equals(PingingNetworkFragment.FragmentResponseType.PING_FAIL)) {
                selectNetworkFragment.setNetworkResponses(((PingingNetworkFragment.FragmentResponse) response).getNetworkItems());
                selectNetworkFragment.setScanMode(SelectBluetoothDeviceFragment.WIFI_MODE);
                navigateToPage(Page.SELECT_NETWORK);
            }
            else if (type.equals(PingingNetworkFragment.FragmentResponseType.PING_SUCCES)) {

                // completed
                this.finish();
                startActivity(new Intent(this, LiveUsageActivity.class));
            }
            else {

                // show error
                navigateToPage(Page.INTRO_CONNECTION);
            }
        }
        else if (sender instanceof SelectNetworkFragment && response instanceof SelectNetworkFragment.FragmentResponse) {

            SelectNetworkFragment.FragmentResponseType type = ((SelectNetworkFragment.FragmentResponse) response).getType();

            if(type.equals(SelectNetworkFragment.FragmentResponseType.SUCCESS)) {

                String mode = ((SelectNetworkFragment.FragmentResponse) response).getMode();
                selectedNetwork = ((SelectNetworkFragment.FragmentResponse) response).getNetwork();

                joiningNetworkFragment.setNetwork(selectedNetwork, ((SelectNetworkFragment.FragmentResponse) response).getPassword());
                joiningNetworkFragment.setJoiningMode(mode);
                navigateToPage(Page.JOINING_NETWORK);
            }
            else if(type.equals(SelectNetworkFragment.FragmentResponseType.TIMEOUT)) {

                //RESTART
                this.finish();
            }
        }
        else if (sender instanceof JoiningNetworkFragment && response instanceof JoiningNetworkFragment.FragmentResponse) {

            JoiningNetworkFragment.FragmentResponseType type = ((JoiningNetworkFragment.FragmentResponse) response).getType();

            if (type.equals(JoiningNetworkFragment.FragmentResponseType.JOIN_SUCCES) && selectedNetwork != null) {

                PersistentHelper.setSSID(selectedNetwork.getName());
                navigateToPage(Page.RESOLVING_NETWORK);
            }
            else if(type.equals(JoiningNetworkFragment.FragmentResponseType.JOIN_BLUETOOTH_SUCCES) && selectedNetwork != null) {

                PersistentHelper.setSSID(selectedNetwork.getName());
                navigateToPage(Page.RESOLVING_NETWORK);
            }
            else if (type.equals(JoiningNetworkFragment.FragmentResponseType.JOIN_FAILED) || type.equals(JoiningNetworkFragment.FragmentResponseType.INFO_STILL_REACHABLE)) {

                navigateToPage(Page.SELECT_NETWORK);
            }
            else if (type.equals(JoiningNetworkFragment.FragmentResponseType.JOIN_DISCONNECTED)) {

                this.finish();
                // RESTART
            }
        } else if (sender instanceof ConnectToCorrectNetworkFragment && response instanceof ConnectToCorrectNetworkFragment.FragmentResponse) {

            ConnectToCorrectNetworkFragment.FragmentResponseType type = ((ConnectToCorrectNetworkFragment.FragmentResponse) response).getType();

            if (type.equals(ConnectToCorrectNetworkFragment.FragmentResponseType.CORRECT)) {

                navigateToPage(Page.RESOLVING_NETWORK);
            }
        } else if (sender instanceof ResolvingNetworkFragment && response instanceof ResolvingNetworkFragment.FragmentResponse) {

            ResolvingNetworkFragment.FragmentResponseType type = ((ResolvingNetworkFragment.FragmentResponse) response).getType();

            if (type.equals(ResolvingNetworkFragment.FragmentResponseType.RESOLVED)) {

                this.finish();
                startActivity(new Intent(this, LiveUsageActivity.class));
            }
            else {

                navigateToPage(Page.CONNECTING_TO_CORRECT_NETWORK);
            }
        }
    }

    @Override
    public void onNavigateBack() {


    }

    @Override
    public void onNavigateBack(String mode) {

        viewPager.setAdapter(null);
        setupViewPager(mode.equals(SelectBluetoothDeviceFragment.BLUETOOTH_MODE));
    }


    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class OnBoardingPagerAdapter extends FragmentPagerAdapter {

        private List<OnBoardingFragment> fragments;

        public OnBoardingPagerAdapter(FragmentManager fm, List<OnBoardingFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }
}

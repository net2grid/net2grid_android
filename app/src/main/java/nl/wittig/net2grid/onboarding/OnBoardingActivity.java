package nl.wittig.net2grid.onboarding;

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
import nl.wittig.net2grid.R;
import nl.wittig.net2grid.liveUsage.LiveUsageActivity;
import nl.wittig.net2grid.onboarding.api.responses.WlanInfoResponse;
import nl.wittig.net2grid.onboarding.fragments.ConnectToCorrectNetworkFragment;
import nl.wittig.net2grid.onboarding.fragments.JoiningNetworkFragment;
import nl.wittig.net2grid.onboarding.fragments.IntroConnectionFragment;
import nl.wittig.net2grid.onboarding.fragments.OnBoardingFragment;
import nl.wittig.net2grid.onboarding.fragments.PingingNetworkFragment;
import nl.wittig.net2grid.onboarding.fragments.ResolvingNetworkFragment;
import nl.wittig.net2grid.onboarding.fragments.SelectNetworkFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OnBoardingActivity extends AppCompatActivity implements OnBoardingFragment.ReadyListener {

    public static final String TAG = OnBoardingActivity.class.getSimpleName();

    public static final String RECONNECTING = "reconnecting";

    public enum Page {
        INTRO_CONNECTION,
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

    private List<OnBoardingFragment> configurationFragments;

    private OnBoardingPagerAdapter onBoardingPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        ButterKnife.bind(this);

        setupViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        OnBoardingFragment currentFragment = configurationFragments.get(viewPager.getCurrentItem());
        currentFragment.reportFragmentVisible(true);
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

    private void setupViewPager() {

        introConnectionFragment = new IntroConnectionFragment();
        introConnectionFragment.setFragmentReadyListener(this);

        selectNetworkFragment = new SelectNetworkFragment();
        selectNetworkFragment.setFragmentReadyListener(this);

        pingingNetworkFragment = new PingingNetworkFragment();
        pingingNetworkFragment.setFragmentReadyListener(this);

        joiningNetworkFragment = new JoiningNetworkFragment();
        joiningNetworkFragment.setFragmentReadyListener(this);

        connectToCorrectNetworkFragment = new ConnectToCorrectNetworkFragment();
        connectToCorrectNetworkFragment.setFragmentReadyListener(this);

        resolvingNetworkFragment = new ResolvingNetworkFragment();
        resolvingNetworkFragment.setFragmentReadyListener(this);

        this.configurationFragments = new ArrayList<>();
        this.configurationFragments.addAll(Arrays.asList(
                introConnectionFragment,
                pingingNetworkFragment,
                selectNetworkFragment,
                joiningNetworkFragment,
                connectToCorrectNetworkFragment,
                resolvingNetworkFragment
        ));

        onBoardingPagerAdapter = new OnBoardingPagerAdapter(getSupportFragmentManager(), this.configurationFragments);
        viewPager.setAdapter(onBoardingPagerAdapter);
    }

    @Override
    public void onFragmentReady(OnBoardingFragment sender, Object response) {

        if (sender instanceof IntroConnectionFragment && response instanceof WlanInfoResponse) {

            pingingNetworkFragment.setSSID(((WlanInfoResponse) response).getClientSsid());
            navigateToPage(Page.PINGING_NETWORK);
        }
        else if (sender instanceof PingingNetworkFragment && response instanceof PingingNetworkFragment.FragmentResponse) {

            PingingNetworkFragment.FragmentResponseType type = ((PingingNetworkFragment.FragmentResponse) response).getType();

            if (type.equals(PingingNetworkFragment.FragmentResponseType.PING_FAIL)) {
                selectNetworkFragment.setNetworkResponses(((PingingNetworkFragment.FragmentResponse) response).getNetworkItems());
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

            Log.i(TAG, "onFragmentReady: " + ((SelectNetworkFragment.FragmentResponse)response).getNetwork().getName() + " " + ((SelectNetworkFragment.FragmentResponse) response).getPassword());
            joiningNetworkFragment.setNetwork(((SelectNetworkFragment.FragmentResponse) response).getNetwork(), ((SelectNetworkFragment.FragmentResponse) response).getPassword());
            navigateToPage(Page.JOINING_NETWORK);
        }
        else if (sender instanceof JoiningNetworkFragment && response instanceof JoiningNetworkFragment.FragmentResponse) {

            JoiningNetworkFragment.FragmentResponseType type = ((JoiningNetworkFragment.FragmentResponse) response).getType();

            if (type.equals(JoiningNetworkFragment.FragmentResponseType.JOIN_SUCCES)) {

                navigateToPage(Page.CONNECTING_TO_CORRECT_NETWORK);

            } else if (type.equals(JoiningNetworkFragment.FragmentResponseType.JOIN_FAILED)){

                navigateToPage(Page.SELECT_NETWORK);
            } else {

                navigateToPage(Page.INTRO_CONNECTION);
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
            } else {

                navigateToPage(Page.CONNECTING_TO_CORRECT_NETWORK);
            }
        }
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

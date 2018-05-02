package nl.wittig.net2grid_ble.liveUsage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.api.Api;
import nl.wittig.net2grid_ble.api.ServiceDiscoveryHelper;
import nl.wittig.net2grid_ble.api.responses.MeterResponse;
import nl.wittig.net2grid_ble.api.responses.ResultSetResponse;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import nl.wittig.net2grid_ble.helpers.WifiHelper;
import nl.wittig.net2grid_ble.liveUsage.fragments.EnergyDayBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.EnergyMonthBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.EnergyYearBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.GasDayBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.GasMonthBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.GasYearBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.LiveBarChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.LiveChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.LiveLineChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.LiveUsageMenu;
import nl.wittig.net2grid_ble.liveUsage.fragments.PowerDayLineChartFragment;
import nl.wittig.net2grid_ble.liveUsage.fragments.PowerHourLineChartFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LiveUsageActivity extends AppCompatActivity implements LiveChartFragment.ReadyListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "LiveUsageActivity";
    private static final long INTERVAL_CHECK_NETWORK = TimeUnit.SECONDS.toMillis(5);
    private static final long INTERVAL_FETCH_LIVE_DATA = TimeUnit.SECONDS.toMillis(10);
    private static final int LIVE_BAR_COUNT = 14;
    private static final int MAX_NOW_FAIL_COUNT = 3;

    private static final double LIVE_SIGMA = 1.3;
    private static final double LIVE_MULTIPLIER = 9.0;
    private static final double LIVE_BASE = 5.0;
    private static final double LIVE_ROOT_SQR = Math.sqrt(2.0 * Math.PI);
    private static final long INTERVAL_UPDATE_VIEW = TimeUnit.SECONDS.toMillis(1);

    @BindView(R.id.activity_live_usage_viewpager) ViewPager viewPager;
    @BindView(R.id.power_usage_chart) BarChart powerChart;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.w24) TextView wat24;
    @BindView(R.id.activity_live_usage_min_value) TextView minValue;
    @BindView(R.id.activity_live_usage_interval) TextView intervalText;
    @BindView(R.id.activity_live_usage_max_value) TextView maxValue;

    @BindView(R.id.activity_live_usage_chart_placeholder) LinearLayout chartPlaceholder;

    @BindView(R.id.activity_live_usage_drawer) DrawerLayout drawerLayout;

    @BindView(R.id.fragment_live_chart_title) TextView title;
    @BindView(R.id.fragment_live_chart_previous) ImageView previousBtn;
    @BindView(R.id.fragment_live_chart_next) ImageView nextBtn;
    @BindView(R.id.activity_live_usage_menu_btn) ImageButton menuBtn;

    private LiveBarChartFragment liveBarGasYearChartFragment;
    private LiveBarChartFragment liveBarGasMonthChartFragment;
    private LiveBarChartFragment liveBarGasDayChartFragment;
    private LiveBarChartFragment liveBarEnergyYearChartFragment;
    private LiveBarChartFragment liveBarEnergyMonthChartFragment;
    private LiveBarChartFragment liveBarEnergyDayChartFragment;
    private LiveLineChartFragment liveLineHourChartFragment;
    private LiveLineChartFragment liveLineDayChartFragment;

    private LiveChartPagerAdapter liveChartPagerAdapter;

    private LiveUsageMenu menuFragment;

    private int currentPagePosition;
    private int secondsAgo = 10;
    public boolean isConnectedToSmartBridge;

    private List<LiveChartFragment> configurationFragments;

    private Handler handler;
    private boolean startedFetching;
    private int nowFailCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_usage);

        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        setupBarChart();

        menuFragment = (LiveUsageMenu) LiveUsageActivity.this.getSupportFragmentManager().findFragmentById(R.id.activity_live_usage_menu);

        handler = new Handler();

        Log.i(TAG, "onCreate: " + PersistentHelper.getSSID());

        viewPager.addOnPageChangeListener(this);
        setupViewPager();

        currentPagePosition = viewPager.getCurrentItem();

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(menuFragment.getView());
            }
        });
    }

    private void setupViewPager() {
        
        liveBarGasYearChartFragment = new GasYearBarChartFragment();
        liveBarGasYearChartFragment.setFragmentReadyListener(this);

        liveBarGasMonthChartFragment = new GasMonthBarChartFragment();
        liveBarGasMonthChartFragment.setFragmentReadyListener(this);

        liveBarGasDayChartFragment = new GasDayBarChartFragment();
        liveBarGasDayChartFragment.setFragmentReadyListener(this);
        
        liveLineHourChartFragment = new PowerHourLineChartFragment();
        liveLineHourChartFragment.setFragmentReadyListener(this);

        liveLineDayChartFragment = new PowerDayLineChartFragment();
        liveLineDayChartFragment.setFragmentReadyListener(this);

        liveBarEnergyYearChartFragment = new EnergyYearBarChartFragment();
        liveBarEnergyYearChartFragment.setFragmentReadyListener(this);

        liveBarEnergyMonthChartFragment = new EnergyMonthBarChartFragment();
        liveBarEnergyMonthChartFragment.setFragmentReadyListener(this);

        liveBarEnergyDayChartFragment = new EnergyDayBarChartFragment();
        liveBarEnergyDayChartFragment.setFragmentReadyListener(this);

        configurationFragments = new ArrayList<>();
        configurationFragments.addAll(Arrays.asList(
                liveLineHourChartFragment,
                liveLineDayChartFragment,
                liveBarEnergyDayChartFragment,
                liveBarEnergyMonthChartFragment,
                liveBarEnergyYearChartFragment,
                liveBarGasDayChartFragment,
                liveBarGasMonthChartFragment,
                liveBarGasYearChartFragment
        ));

//          This is the standard viewpager
        liveChartPagerAdapter = new LiveChartPagerAdapter(LiveUsageActivity.this.getSupportFragmentManager(), configurationFragments);

        viewPager.setOffscreenPageLimit(configurationFragments.size());
        viewPager.setAdapter(liveChartPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkNetworkRunnable.run();
        updateInterval.run();

        LiveChartFragment currentFragment = configurationFragments.get(viewPager.getCurrentItem());
        currentFragment.reportFragmentVisible(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(checkNetworkRunnable);
        handler.removeCallbacks(fetchRunnable);
        handler.removeCallbacks(updateInterval);

        startedFetching = false;

        LiveChartFragment currentFragment = configurationFragments.get(viewPager.getCurrentItem());
        currentFragment.reportFragmentVisible(false);
    }

    @Override
    public void onFragmentReady(LiveChartFragment sender) {

        if (sender instanceof PowerDayLineChartFragment) {

            title.setText(R.string.chart_power_day_chart);
        } else if (sender instanceof PowerHourLineChartFragment) {

            title.setText(R.string.chart_power_hour_chart);
        } else if (sender instanceof GasYearBarChartFragment) {

            title.setText(R.string.chart_gas_year_chart);
        } else if (sender instanceof GasMonthBarChartFragment) {

            title.setText(getString(R.string.chart_gas_month_chart));
        } else if (sender instanceof GasDayBarChartFragment) {

            title.setText(getString(R.string.chart_gas_day_chart));
        } else if (sender instanceof EnergyYearBarChartFragment) {

            title.setText(getString(R.string.chart_energy_year_chart));
        } else if (sender instanceof EnergyMonthBarChartFragment) {

            title.setText(getString(R.string.chart_energy_month_chart));
        } else if (sender instanceof EnergyDayBarChartFragment) {

            title.setText(getString(R.string.chart_energy_day_chart));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


    }

    @Override
    public void onPageSelected(int position) {

        LiveChartFragment currentFragment = configurationFragments.get(currentPagePosition);
        currentFragment.reportFragmentVisible(false);

        currentPagePosition = position;

        LiveChartFragment nextFragment = configurationFragments.get(position);
        nextFragment.reportFragmentVisible(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setupBarChart() {

        powerChart.setPinchZoom(false);
        powerChart.setDoubleTapToZoomEnabled(false);
        powerChart.setScaleEnabled(false);
        powerChart.setTouchEnabled(false);

        powerChart.setNoDataText(getString(R.string.chart_loading_data));
        powerChart.setNoDataTextColor(Color.WHITE);

        Description desc = new Description();
        desc.setText("");
        powerChart.setDescription(desc);
        powerChart.getLegend().setEnabled(false);

        powerChart.getXAxis().setTextColor(Color.WHITE);
        powerChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        powerChart.getXAxis().setDrawLabels(false);
        powerChart.getXAxis().setDrawGridLines(false);
        powerChart.getXAxis().setDrawAxisLine(false);
        powerChart.getAxisLeft().setDrawAxisLine(false);
        powerChart.getAxisLeft().setDrawGridLines(false);
        powerChart.getAxisLeft().setDrawLabels(false);
        powerChart.getAxisLeft().setAxisMinimum(0);
        powerChart.getAxisLeft().setAxisMaximum(8);
        powerChart.getAxisRight().setDrawGridLines(false);
        powerChart.getAxisRight().setDrawAxisLine(false);
        powerChart.getAxisRight().setDrawLabels(false);

        powerChart.animateY(3000);
    }

    private void fetchLiveData() {

        Api.getDefaultInstance().meter.now().enqueue(new Callback<MeterResponse>() {
            @Override
            public void onResponse(Call<MeterResponse> call, Response<MeterResponse> response) {

                nowFailCount = 0;

                MeterResponse meterResponse = response.body();

                if (response.isSuccessful() && meterResponse != null && meterResponse.status != null && meterResponse.status.equals("ok")) {

                    ResultSetResponse powerResponse = response.body().electricity.getPower();

                    int max = powerResponse.getMax().getValue();
                    int now = powerResponse.getNow().getValue();
                    int min = powerResponse.getMin().getValue();

                    if(max < now || max < min){
                        max = now;
                    }

                    updateLiveData(now, min, max);
                    updateUsage(powerResponse);

                    secondsAgo = 10;
                }
            }

            @Override
            public void onFailure(Call<MeterResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);

                if (WifiHelper.onSmartBridgeNetwork(WifiHelper.getCurrentNetworkName(LiveUsageActivity.this))) {

                    nowFailCount++;

                    if (nowFailCount >= MAX_NOW_FAIL_COUNT) {

                        Log.i(TAG, "now request failed max times, starting service discovery");
                        startActivity(new Intent(LiveUsageActivity.this, DiscoverNetworkActivity.class));

                        nowFailCount = 0;
                    }
                }
            }
        });
    }

    private void updateLiveData(int now, int min, int max) {

        Log.i(TAG, "updateLiveData: " + now + " " + min + " " + max);

        float perc = (float)(now - min) / (float)(max - min);
        int activeBars = Math.round(LIVE_BAR_COUNT * perc);

        // Active
        List<BarEntry> activeData = new ArrayList<>();

        for (int i = 0; i < activeBars; i++) {

            int step = i - activeBars + 1;

            activeData.add(new BarEntry(i, (float)translateLiveValue(step)));
        }


        // Inactive
        List<BarEntry> inActiveData = new ArrayList<>();

        for (int i = activeBars; i < LIVE_BAR_COUNT; i++) {

            int step = i - activeBars + 1;

            inActiveData.add(new BarEntry(i, (float)translateLiveValue(step)));
        }

        if (activeData.size() > 0 || inActiveData.size() > 0) {

            chartPlaceholder.setVisibility(View.GONE);
            powerChart.setVisibility(View.VISIBLE);

            BarDataSet activeDataSet = new BarDataSet(activeData, "active");
            activeDataSet.setColor(Color.parseColor("#37d1bb"));
            activeDataSet.setDrawValues(false);

            BarDataSet inActiveDataSet = new BarDataSet(inActiveData, "inactive");
            inActiveDataSet.setColor(Color.parseColor("#1a2d3b"));
            inActiveDataSet.setDrawValues(false);

            BarData barData = new BarData();
            barData.addDataSet(activeDataSet);
            barData.addDataSet(inActiveDataSet);
            barData.setBarWidth(0.5f);

            powerChart.setData(barData);
            powerChart.invalidate();
        } else {

            chartPlaceholder.setVisibility(View.VISIBLE);
            powerChart.setVisibility(View.GONE);
        }
    }

    private double translateLiveValue(int step) {

        return ((1.0 / (LIVE_SIGMA * LIVE_ROOT_SQR)) * Math.exp(-0.5 * Math.pow(step / LIVE_SIGMA, 2.0)) * LIVE_MULTIPLIER) + LIVE_BASE;
    }

    public void checkWifiNetwork() {

        String name = WifiHelper.getCurrentNetworkName(getBaseContext());

        if (!WifiHelper.onSmartBridgeNetwork(name)) {
            // activity starten die aangeeft verkeerde netwerk
            isConnectedToSmartBridge = false;
            startActivity(new Intent(getBaseContext(), WrongNetworkConnectedActivity.class));
        } else {

            isConnectedToSmartBridge = true;

            if (!startedFetching) {
                startFetching();
                startedFetching = true;
            }
        }
    }

    private void startFetching() {

        fetchRunnable.run();
    }

    private Runnable updateInterval = new Runnable() {
        @Override
        public void run() {

            updateIntervalView();
            secondsAgo--;

            handler.postDelayed(updateInterval, INTERVAL_UPDATE_VIEW);
        }
    };

    private void updateIntervalView() {

        intervalText.setText(getString(R.string.dashboard_updated_label, Math.max(secondsAgo, 0)));
    }

    private Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {

            fetchLiveData();
            handler.postDelayed(fetchRunnable, INTERVAL_FETCH_LIVE_DATA);
        }
    };

    private void updateUsage(ResultSetResponse usage) {

        wat24.setText(usage.getNow().getValue() + usage.getNow().getUnit());

        minValue.setText("" + usage.getMin().getValue());
        maxValue.setText("" + usage.getMax().getValue());
    }

    private Runnable checkNetworkRunnable = new Runnable() {
        @Override
        public void run() {

            checkWifiNetwork();
            handler.postDelayed(checkNetworkRunnable, INTERVAL_CHECK_NETWORK);
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class LiveChartPagerAdapter extends FragmentPagerAdapter {

        private List<LiveChartFragment> fragments;

        public LiveChartPagerAdapter(FragmentManager fm, List<LiveChartFragment> fragments) {
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

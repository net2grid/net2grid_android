package nl.wittig.net2grid_ble.liveUsage.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.api.model.MeterResult;

public abstract class LiveBarChartFragment extends LiveChartFragment {

    public static final String TAG = LiveBarChartFragment.class.getSimpleName();

    @BindView(R.id.fragment_live_bar_chart_bar) BarChart barChart;
    @BindView(R.id.fragment_live_bar_chart_no_data_frame) LinearLayout noDataFrame;

    protected List<MeterResult> data;

    public LiveBarChartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_bar_chart, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        setupChart();
    }

    @Override
    protected void updateData()
    {
        super.updateData();

        List<BarEntry> chartData = getChartData();

        if (chartData.size() > 0) {

            barChart.setVisibility(View.VISIBLE);
            noDataFrame.setVisibility(View.GONE);

            BarDataSet barDataSet = new BarDataSet(chartData, "powerEntries");
            barDataSet.setColor(ResourcesCompat.getColor(getResources(), getChartColorResource(), null));
            barDataSet.setDrawValues(false);

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.4f);

            barChart.setData(barData);
            barChart.invalidate();
        } else {

            barChart.setVisibility(View.GONE);
            noDataFrame.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void setupChart() {

        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setTouchEnabled(false);

        barChart.setNoDataText(getString(R.string.chart_loading_data));
        barChart.setNoDataTextColor(Color.WHITE);

        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);
        barChart.getLegend().setEnabled(false);

        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setDrawAxisLine(false);

        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);

        barChart.animateY(3000);
        barChart.invalidate();
    }

    protected List<BarEntry> getChartData() {

        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).getValue()));
        }

        Collections.sort(entries, new EntryXComparator());

        return entries;
    }
}

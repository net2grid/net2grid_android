package nl.wittig.net2grid_ble.liveUsage.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.api.model.MeterResult;

public abstract class LiveLineChartFragment extends LiveChartFragment {

    public static final String TAG = LiveLineChartFragment.class.getSimpleName();

    @BindView(R.id.fragment_live_line_chart_line) LineChart lineChart;
    @BindView(R.id.fragment_live_line_chart_no_data_frame) LinearLayout noDataFrame;

    protected List<MeterResult> data;

    public LiveLineChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_line_chart, container, false);
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

        List<Entry> chartData = getChartData();

        if (chartData.size() > 0) {

            lineChart.setVisibility(View.VISIBLE);
            noDataFrame.setVisibility(View.GONE);

            LineDataSet dataSet = new LineDataSet(chartData, "label2");
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(1f);
            dataSet.setColor(ResourcesCompat.getColor(getResources(), getChartColorResource(), null));

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate();
        } else {

            lineChart.setVisibility(View.GONE);
            noDataFrame.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void setupChart() {

        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);

        lineChart.setNoDataText(getString(R.string.chart_loading_data));
        lineChart.setNoDataTextColor(Color.WHITE);

        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setCenterAxisLabels(true);
        lineChart.getXAxis().setLabelCount(4);
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;
                if (index < 0 || index >= data.size())
                    return "";

                MeterResult item = data.get(index);
                DateTime date = new DateTime(item.getDate());

                DateTimeFormatter fmt = DateTimeFormat.forPattern("k:mm");

                return fmt.print(date);
            }
        });

        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);

        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.getAxisRight().setDrawLabels(false);

        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        lineChart.getLegend().setEnabled(false);
        lineChart.animateY(3000);
        lineChart.invalidate();
    }

    protected List<Entry> getChartData() {

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {

            entries.add(new Entry(i, data.get(i).getValue()));
        }

        Collections.sort(entries, new EntryXComparator());

        return entries;
    }
}

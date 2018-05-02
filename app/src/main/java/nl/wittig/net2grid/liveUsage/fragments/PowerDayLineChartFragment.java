package nl.wittig.net2grid.liveUsage.fragments;

import android.os.Handler;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.concurrent.TimeUnit;

import nl.wittig.net2grid.api.Api;
import nl.wittig.net2grid.api.responses.MeterResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PowerDayLineChartFragment extends LiveLineChartFragment {

    private static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(5);

    private String unit = "";

    @Override
    protected void setupChart() {

        lineChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return (int)value + unit;
            }
        });

        super.setupChart();
    }

    @Override
    protected void fetchData() {

        Api.getDefaultInstance().meter.powerDay().enqueue(new Callback<MeterResponse>() {
            @Override
            public void onResponse(Call<MeterResponse> call, Response<MeterResponse> response) {

                MeterResponse meterResponse = response.body();

                if (response.isSuccessful() && meterResponse.status != null && meterResponse.status.equals("ok")) {

                    data = meterResponse.electricity.getPower().getMeterResults();
                    unit = meterResponse.electricity.getPower().getUnit();
                    updateData();
                }

                scheduleFetchIfNeeded(REFRESH_INTERVAL);
            }

            @Override
            public void onFailure(Call<MeterResponse> call, Throwable t) {

                scheduleFetchIfNeeded(REFRESH_INTERVAL);
            }
        });
    }
}

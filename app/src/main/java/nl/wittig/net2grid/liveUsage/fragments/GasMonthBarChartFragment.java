package nl.wittig.net2grid.liveUsage.fragments;

import android.os.Handler;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.TimeUnit;

import nl.wittig.net2grid.R;
import nl.wittig.net2grid.api.Api;
import nl.wittig.net2grid.api.model.MeterResult;
import nl.wittig.net2grid.api.responses.MeterResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GasMonthBarChartFragment extends LiveBarChartFragment {

    private static final long REFRESH_INTERVAL = TimeUnit.DAYS.toMillis(1);

    private String unit = "";
    @Override
    protected void setupChart() {

        barChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return Math.round(value) + unit;
            }
        });

        barChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;
                if (index < 0 || index >= data.size())
                    return "";

                MeterResult item = data.get(index);
                DateTime date = new DateTime(item.getDate());

                DateTimeFormatter fmt = DateTimeFormat.forPattern("d");

                return fmt.print(date);
            }
        });

        barChart.getXAxis().setLabelCount(6);

        super.setupChart();
    }

    @Override
    protected void fetchData() {

        Api.getDefaultInstance().meter.consumptionMonth().enqueue(new Callback<MeterResponse>() {
            @Override
            public void onResponse(Call<MeterResponse> call, Response<MeterResponse> response) {

                MeterResponse meterResponse = response.body();

                if (response.isSuccessful() && meterResponse.status != null && meterResponse.status.equals("ok")) {
                    data = response.body().gas.getConsumption().getMeterResults();
                    unit = meterResponse.gas.getConsumption().getUnit();
                    updateData(R.color.bar_gas_chart);
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

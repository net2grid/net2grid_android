package nl.wittig.net2grid_ble.liveUsage.fragments;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.TimeUnit;

import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.api.Api;
import nl.wittig.net2grid_ble.api.model.MeterResult;
import nl.wittig.net2grid_ble.api.responses.MeterResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnergyMonthBarChartFragment extends LiveBarChartFragment {

    private String unit = "";

    @Override
    public long getRefreshInterval() {

        return TimeUnit.DAYS.toMillis(30);
    }

    @Override
    protected int getChartColorResource() {

        return R.color.elec_chart_color;
    }

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

        Api.getDefaultInstance().meter.consumptionElecMonth().enqueue(new Callback<MeterResponse>() {
            @Override
            public void onResponse(Call<MeterResponse> call, Response<MeterResponse> response) {

                MeterResponse meterResponse = response.body();

                if (response.isSuccessful() && meterResponse.status != null && meterResponse.status.equals("ok")) {
                    data = response.body().electricity.getConsumption().getMeterResults();
                    unit = meterResponse.electricity.getConsumption().getUnit();
                    updateData();
                }

                scheduleFetchIfNeeded();
            }

            @Override
            public void onFailure(Call<MeterResponse> call, Throwable t) {

                scheduleFetchIfNeeded();
            }
        });
    }
}

package nl.wittig.net2grid.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.wittig.net2grid.api.model.MeterResult;

public class ResultSetResponse {

    @SerializedName("now")
    private ResultResponse now;

    @SerializedName("min")
    private ResultResponse min;

    @SerializedName("max")
    private ResultResponse max;

    @SerializedName("interval")
    private int interval;

    @SerializedName("start_time")
    private long startTime;

    @SerializedName("entry_count")
    private int entryCount;

    @SerializedName("unit")
    private String unit;

    @SerializedName("results")
    private List<Integer> results;

    public ResultResponse getNow() {
        return now;
    }

    public ResultResponse getMin() {
        return min;
    }

    public ResultResponse getMax() {
        return max;
    }

    public int getInterval() {
        return interval;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public String getUnit() {
        return unit;
    }

    public List<Integer> getResults() {
        return results;
    }

    public void setMax(ResultResponse max) {
        this.max = max;
    }

    public List<MeterResult> getMeterResults() {

        List<MeterResult> meterResults = new ArrayList<>();

        int counter = 0;

        for (Integer result : results) {

            Date date = new Date((startTime + (interval * counter)) * 1000);

            meterResults.add(new MeterResult(result, date, unit));
            counter++;

        }

        return meterResults;
    }
}

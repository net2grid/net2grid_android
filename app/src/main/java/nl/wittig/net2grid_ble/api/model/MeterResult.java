package nl.wittig.net2grid_ble.api.model;

import java.util.Date;

public class MeterResult {

    protected Integer value;

    protected Date date;
    protected String unit;

    public MeterResult(Integer value, Date date, String unit) {

        this.value = value;
        this.date = date;
        this.unit = unit;
    }

    public Integer getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }

    public String getUnit() {
        return unit;
    }
}

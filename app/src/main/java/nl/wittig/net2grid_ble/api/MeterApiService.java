package nl.wittig.net2grid_ble.api;

import nl.wittig.net2grid_ble.api.responses.MeterResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MeterApiService {

    @GET("meter/now")
    Call<MeterResponse> now();

    @GET("meter/elec/power/hour")
    Call<MeterResponse> powerHour();

    @GET("meter/elec/power/day")
    Call<MeterResponse> powerDay();

    @GET("meter/gas/consumption/year")
    Call<MeterResponse> consumptionYear();

    @GET("meter/gas/consumption/month")
    Call<MeterResponse> consumptionMonth();

    @GET("meter/gas/consumption/day")
    Call<MeterResponse> consumptionDay();

    @GET("meter/elec/consumption/year")
    Call<MeterResponse> consumptionElecYear();

    @GET("meter/elec/consumption/month")
    Call<MeterResponse> consumptionElecMonth();

    @GET("meter/elec/consumption/day")
    Call<MeterResponse> consumptionElecDay();
}

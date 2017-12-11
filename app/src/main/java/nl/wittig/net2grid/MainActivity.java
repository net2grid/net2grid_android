package nl.wittig.net2grid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.net.InetAddress;

import nl.wittig.net2grid.api.Api;
import nl.wittig.net2grid.api.ServiceDiscoveryHelper;
import nl.wittig.net2grid.helpers.PersistentHelper;
import nl.wittig.net2grid.liveUsage.LiveUsageActivity;
import nl.wittig.net2grid.onboarding.OnBoardingActivity;
import nl.wittig.net2grid.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static nl.wittig.net2grid.YnniApplication.SMARTBRIDGE_SERVICE_NAME;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String host = PersistentHelper.getSmartBridgeHost();

        if (host != null) {
            goToLive();
        }
        else {
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

            discoverSmartBridge();
    }

    private void discoverSmartBridge() {

        ServiceDiscoveryHelper serviceDiscoveryHelper = new ServiceDiscoveryHelper(this);
        serviceDiscoveryHelper.setCallback(new ServiceDiscoveryHelper.OnResultCallback() {
            @Override
            public void onIpFound(InetAddress ip) {

                PersistentHelper.setSmartBridgeHost(ip.getHostAddress());
                Api.resetApi();

                getSmartBridgeInfo();
            }

            @Override
            public void onError(Throwable t) {

                Log.e(TAG, "discover smartbridge onerror: ", t);

                goToOnBoarding();
            }
        });

        serviceDiscoveryHelper.findIp(SMARTBRIDGE_SERVICE_NAME);
    }

    public void getSmartBridgeInfo() {

        Api.getDefaultInstance().meda.getWlanInfoResponse().enqueue(new Callback<WlanInfoResponse>() {
            @Override
            public void onResponse(Call<WlanInfoResponse> call, Response<WlanInfoResponse> response) {

                if (response.isSuccessful()) {

                    PersistentHelper.setSSID(response.body().getClientSsid());
                    goToLive();
                } else {

                    goToOnBoarding();
                }
            }

            @Override
            public void onFailure(Call<WlanInfoResponse> call, Throwable t) {

                goToOnBoarding();
            }
        });
    }

    private void goToLive() {

        Intent liveUsageIntent = new Intent(this, LiveUsageActivity.class);
        liveUsageIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(liveUsageIntent);
    }

    private void goToOnBoarding() {

        Intent onBoardingIntent = new Intent(this, OnBoardingActivity.class);
        onBoardingIntent.putExtra(OnBoardingActivity.RECONNECTING, false);
        startActivity(onBoardingIntent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


}

package nl.wittig.net2grid_ble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.net.InetAddress;

import nl.wittig.net2grid_ble.api.Api;
import nl.wittig.net2grid_ble.api.ServiceDiscoveryHelper;
import nl.wittig.net2grid_ble.bluetooth.BluetoothManager;
import nl.wittig.net2grid_ble.bluetooth.model.BluetoothDevice;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import nl.wittig.net2grid_ble.helpers.WifiHelper;
import nl.wittig.net2grid_ble.liveUsage.LiveUsageActivity;
import nl.wittig.net2grid_ble.onboarding.OnBoardingActivity;
import nl.wittig.net2grid_ble.onboarding.api.responses.WlanInfoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static nl.wittig.net2grid_ble.YnniApplication.SMARTBRIDGE_SERVICE_NAME;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothManager manager;
    private BluetoothDevice bluetoothDevice;

    private Boolean bluetoothSmartBridgeFound = null;
    private Boolean wifiInfoCallSucceeded = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isReconnecting = getIntent().getBooleanExtra(OnBoardingActivity.RECONNECTING, false);
        if(!isReconnecting) {
            String host = PersistentHelper.getSmartBridgeHost();
            if (host != null) {
                goToLive();
            }
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

            discoverSmartBridge();
            discoverBluetoothSmartBridge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(manager != null) {
            manager.disconnect();
        }
    }

    private void discoverSmartBridge() {

        ServiceDiscoveryHelper serviceDiscoveryHelper = new ServiceDiscoveryHelper();
        serviceDiscoveryHelper.setCallback(new ServiceDiscoveryHelper.OnResultCallback() {
            @Override
            public void onIpFound(InetAddress ip) {

                PersistentHelper.setSSID(WifiHelper.getCurrentNetworkName(MainActivity.this));
                PersistentHelper.setSmartBridgeHost(ip.getHostAddress());
                Api.resetApi();

                getSmartBridgeInfo();
            }

            @Override
            public void onError(Throwable t) {

                Log.e(TAG, "discover smartbridge onerror: ", t);
                wifiInfoCallSucceeded = false;
                determineNextStep();
            }
        });

        serviceDiscoveryHelper.findIp(SMARTBRIDGE_SERVICE_NAME, this);
    }

    public void discoverBluetoothSmartBridge() {

        manager = BluetoothManager.getInstance();

        Boolean permissionGranted = manager.ensurePermissions(this);

        if(permissionGranted) {

            toggleBluetooth();
        }
    }

    public void toggleBluetooth() {

        Boolean bluetoothOn = BluetoothManager.getInstance().startBluetoothService(this);

        if(bluetoothOn != null && bluetoothOn) {

            getBluetoothSmartBridgeInfo();
        }
        else if(bluetoothOn != null && !bluetoothOn) {

            bluetoothSmartBridgeFound = false;
            determineNextStep();
        }
    }

    public void getSmartBridgeInfo() {

        Api.getDefaultInstance().meda.getWlanInfoResponse().enqueue(new Callback<WlanInfoResponse>() {
            @Override
            public void onResponse(Call<WlanInfoResponse> call, Response<WlanInfoResponse> response) {

//                if (response.isSuccessful()) {
//
//                    PersistentHelper.setSSID(response.body().getClientSsid());
//                    goToLive();
//                } else {
//
//                    goToOnBoarding();
//                }

                wifiInfoCallSucceeded = true;
                determineNextStep();
            }

            @Override
            public void onFailure(Call<WlanInfoResponse> call, Throwable t) {

                wifiInfoCallSucceeded = false;
                determineNextStep();
            }
        });
    }

    public void getBluetoothSmartBridgeInfo() {

        manager.discoverSmartBridge(getApplicationContext(), new BluetoothManager.DiscoverSmartBridgeCallback() {
            @Override
            public void onBluetoothDeviceFound(BluetoothDevice device) {

                bluetoothDevice = device;

                manager.discoverMacAddress(new BluetoothManager.DiscoverMacAddressCallback() {
                    @Override
                    public void onMacAddressFound(String address) {

                        bluetoothSmartBridgeFound = true;
                        determineNextStep();
                    }

                    @Override
                    public void onFailure() {

                        bluetoothSmartBridgeFound = false;
                        determineNextStep();
                    }
                });
            }

            @Override
            public void onFailure() {

                bluetoothSmartBridgeFound = false;
                determineNextStep();
            }
        });
    }

    public void determineNextStep() {

        if(wifiInfoCallSucceeded == null && bluetoothSmartBridgeFound == null){

            Log.i(TAG, "No response yet, waiting...");
            return;
        }

        if(wifiInfoCallSucceeded != null && wifiInfoCallSucceeded) {

            Log.i(TAG, "Wifi Info call succeeded, finishing");

            goToLive();
        }
        else if(wifiInfoCallSucceeded != null && bluetoothSmartBridgeFound != null) {

            if(!wifiInfoCallSucceeded && !bluetoothSmartBridgeFound) {

                Log.i(TAG, "Wifi Info call failed and no bluetooth SmartBridge found. setup standard process");
                goToOnBoarding(false);
            }
            else if(bluetoothSmartBridgeFound) {

                Log.i(TAG, "Found bluetooth SmartBridge, setting up onboarding using bluetooth");
                goToOnBoarding(true);
            }
        }
    }

    private void goToLive() {

        Intent liveUsageIntent = new Intent(this, LiveUsageActivity.class);
        liveUsageIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(liveUsageIntent);
    }

    private void goToOnBoarding(boolean bluetoothMode) {

        boolean isReconnecting = getIntent().getBooleanExtra(OnBoardingActivity.RECONNECTING, false);
        Intent onBoardingIntent = new Intent(this, OnBoardingActivity.class);
        onBoardingIntent.putExtra(OnBoardingActivity.RECONNECTING, isReconnecting);
        onBoardingIntent.putExtra("bluetoothMode", bluetoothMode);
        if(bluetoothMode) {
            onBoardingIntent.putExtra("deviceName", bluetoothDevice.getName());
        }
        startActivity(onBoardingIntent);
        if(isReconnecting) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case BluetoothManager.PERMISSION_COARSE_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    toggleBluetooth();
                }
                else {

                    Log.i(TAG, "onRequestPermissionsResult: Bluetooth permission not granted!");

                    bluetoothSmartBridgeFound = false;
                    determineNextStep();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BluetoothManager.REQUEST_ENABLE_BT && resultCode == RESULT_OK) {

            getBluetoothSmartBridgeInfo();
        }
        else if (requestCode == BluetoothManager.REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){

            bluetoothSmartBridgeFound = false;
            determineNextStep();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

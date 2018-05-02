package nl.wittig.net2grid_ble.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nl.wittig.net2grid_ble.YnniApplication;
import nl.wittig.net2grid_ble.bluetooth.model.JoinNetworkRequest;
import nl.wittig.net2grid_ble.onboarding.api.responses.NetworkResponse;
import nl.wittig.net2grid_ble.utils.Tools;

public class BluetoothManager extends BluetoothGattCallback {

    private static final String TAG = BluetoothManager.class.getSimpleName();

    private static final String BLUETOOTH_DEVICE_PREFIX = "sbwf-";

    public static final int REQUEST_ENABLE_BT = 2018;

    public static final int PERMISSION_COARSE_LOCATION = 2019;

    private static final int DISCOVER_TIME_OUT = 30000;
    private static final int REPORT_NETWORKS_TIME_OUT = 2000;
    private static final int JOIN_NETWORK_TIME_OUT = 5000;

    private static final int BLUETOOTH_SERVICE_YNNI = 0x4E01;
    private static final int BLUETOOTH_CHAR_SSID = 0x4E02;
    private static final int BLUETOOTH_CHAR_KEY = 0x4E03;
    private static final int BLUETOOTH_CHAR_COMMAND = 0x4E04;
    private static final int BLUETOOTH_CHAR_JOIN = 0x4E05;
    private static final int BLUETOOTH_CHAR_SCAN = 0x4E06;
    private static final int BLUETOOTH_CHAR_INFO = 0x4E07;
    private static final int BLUETOOTH_CHAR_DESCRIPTOR = 0x2902;

    private Gson gson = new Gson();

    private static BluetoothManager manager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattService gattService;
    private BluetoothGatt gatt;

    private boolean isDiscovering = false;
    private boolean isJoining = false;

    private JoinNetworkRequest joinNetworkRequest;

    private List<NetworkResponse> networks = new ArrayList<>();

    private Handler handler = new Handler();

    private nl.wittig.net2grid_ble.bluetooth.model.BluetoothDevice bluetoothDevice;

    private DiscoverSmartBridgeCallback discoverCallback;
    private ScanNetworksCallback scanNetworksCallback;
    private JoinNetworkCallback joinNetworkCallback;
    private DiscoverMacAddressCallback discoverMacAddressCallback;

    protected BluetoothManager() {
        // Exists only to defeat instantiation.
    }

    public static BluetoothManager getInstance() {

        if(manager == null) {
            manager = new BluetoothManager();
        }

        return manager;
    }

    public Boolean ensurePermissions(Activity activity) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                activity.requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
                return false;
            }
        }

        return true;
    }

    public Boolean startBluetoothService(Activity activity) {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) {
            
            Log.i(TAG, "Device doesn't support bluetooth");
            return false;
        }
        else {

            if(bluetoothAdapter.isEnabled()) {
                
                return true;
            }
            else {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                
                return null;
            }
        }
    }
    
    public void discoverMacAddress(DiscoverMacAddressCallback callback) {
        
        this.discoverMacAddressCallback = callback;

        BluetoothGattCharacteristic infoChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_INFO));
        gatt.readCharacteristic(infoChar);
    }
    
    public void discoverSmartBridge(Context context, DiscoverSmartBridgeCallback callback) {

        this.discoverCallback = callback;
        isDiscovering = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        context.registerReceiver(bluetoothReceiver, filter);
        bluetoothAdapter.startDiscovery();

        handler.postDelayed(discoverTimeOut, DISCOVER_TIME_OUT);
    }

    public void cancelDiscoveringSmartBridge() {

        discoverCallback = null;
        bluetoothDevice = null;
        isDiscovering = false;

        if(bluetoothAdapter != null) {

            bluetoothAdapter.cancelDiscovery();
        }
    }
    
    private void reportSmartBridgeDevice(BluetoothDevice device, String deviceName) {

        bluetoothDevice = new nl.wittig.net2grid_ble.bluetooth.model.BluetoothDevice(deviceName, device.getAddress());

        Log.i(TAG, "Stop discovering devices");
        bluetoothAdapter.cancelDiscovery();

        Log.i(TAG, "Connecting device to Gatt");
        device.connectGatt(YnniApplication.getContext(), false, this);
    }

    private void reportDiscoverFailure() {

        YnniApplication.getInstance().runOnUiThread(() -> {

            if(discoverCallback != null) {
                discoverCallback.onFailure();
                discoverCallback = null;
            }

            handler.removeCallbacks(discoverTimeOut);
            isDiscovering = false;
        });
    }

    private void reportNetworkFound(byte[] value) {

        try {

            NetworkResponse network = gson.fromJson(new String(value, "UTF-8"), NetworkResponse.class);
            if(network != null && network.getSsid() != null){

                networks.add(network);
            }

            handler.removeCallbacks(reportNetworksTimeOut);
            handler.postDelayed(reportNetworksTimeOut, REPORT_NETWORKS_TIME_OUT);
        }
        catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
    }

    public void registerToScanCall(ScanNetworksCallback callback) {

        if(gattService == null || gatt == null){
            return;
        }

        // Set notification for scan results
        BluetoothGattCharacteristic scanChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_SCAN));
        gatt.setCharacteristicNotification(scanChar, true);

        // Start searching to WiFi networks: result will come in -> onCharacteristicChanged
        BluetoothGattCharacteristic cmdChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_COMMAND));
        cmdChar.setValue(new byte[]{1});
        gatt.writeCharacteristic(cmdChar);

        this.scanNetworksCallback = callback;
    }

    public void registerAndJoinCall(JoinNetworkRequest joinNetworkRequest, JoinNetworkCallback callback) {

        if(gattService == null || gatt == null){

            callback.onFailure(true);
            return;
        }

        this.joinNetworkRequest = joinNetworkRequest;
        this.joinNetworkCallback = callback;
        isJoining = true;

        // Write chosen SSID to ssidChar -> onCharacteristicWrite
        BluetoothGattCharacteristic ssidChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_SSID));
        ssidChar.setValue(joinNetworkRequest.getSsid());
        gatt.writeCharacteristic(ssidChar);
    }

    private void reportJoinResult(boolean success, boolean disconnected){


        YnniApplication.getInstance().runOnUiThread(() -> {

            if(joinNetworkCallback != null) {

                if (success) {

                    Log.i(TAG, "JOINED SUCCESSFULLY");
                    joinNetworkCallback.onNetworkJoined();
                } else {

                    Log.e(TAG, "JOIN FAILED");
                    joinNetworkCallback.onFailure(disconnected);
                }

                joinNetworkCallback = null;
            }

            isJoining = false;
        });
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {

            Log.i(TAG, "Gatt connected, requesting MTU");
            gatt.requestMtu(64);
        }
        else if(newState == BluetoothProfile.STATE_DISCONNECTED) {

            Log.i(TAG, "Gatt disconnected");

            if(isDiscovering) {
                reportDiscoverFailure();
            }
            else if(isJoining){
                reportJoinResult(false, true);
            }

            this.gatt = null;
            this.gattService = null;
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        String uuid = characteristic.getUuid().toString();
        String infoUuid = convertFromInteger(BLUETOOTH_CHAR_INFO).toString();
        if(uuid.equals(infoUuid)) {

            BluetoothGattCharacteristic infoChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_INFO));
            String mac = Tools.createMacAddressFromByteArray(infoChar.getValue());
            YnniApplication.getInstance().runOnUiThread(() -> {

                if(discoverMacAddressCallback != null) {

                    discoverMacAddressCallback.onMacAddressFound(mac);
                    discoverMacAddressCallback = null;
                }
            });
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);

        if(status == BluetoothGatt.GATT_SUCCESS) {

            Log.i(TAG, "MTU Changed, discovering services");
            gatt.discoverServices();
        }
        else {

            Log.i(TAG, "MTU Change failed");
            reportDiscoverFailure();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        if(status == BluetoothGatt.GATT_SUCCESS) {

            BluetoothGattService service = gatt.getService(convertFromInteger(BLUETOOTH_SERVICE_YNNI));
            if(service != null && bluetoothDevice != null) {

                this.gatt = gatt;
                this.gattService = service;

                Log.i(TAG, "Discover finished found ynni service");

                handler.removeCallbacks(discoverTimeOut);

                YnniApplication.getInstance().runOnUiThread(() -> {

                    if(discoverCallback != null) {

                        discoverCallback.onBluetoothDeviceFound(bluetoothDevice);
                        discoverCallback = null;
                    }

                    isDiscovering = false;
                });
            }
        } else {

            Log.i(TAG, "Service discovery failed");
            reportDiscoverFailure();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

        String uuid = characteristic.getUuid().toString();
        String ssidUuid = convertFromInteger(BLUETOOTH_CHAR_SSID).toString();
        String keyUuid = convertFromInteger(BLUETOOTH_CHAR_KEY).toString();

        if(uuid.equals(ssidUuid)) {

            if(status == BluetoothGatt.GATT_SUCCESS){

                // Write chosen password to keyChar -> onCharacteristicWrite
                BluetoothGattCharacteristic keyChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_KEY));
                keyChar.setValue(joinNetworkRequest.getPassword());
                gatt.writeCharacteristic(keyChar);
            }
            else {

                reportJoinResult(false, true);
            }
        }
        else if(uuid.equals(keyUuid)) {

            if(status == BluetoothGatt.GATT_SUCCESS) {

                // Set notification for join result
                BluetoothGattCharacteristic joinChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_JOIN));
                gatt.setCharacteristicNotification(joinChar, true);

                // Start joining to WiFi network: result will come in -> onCharacteristicChanged
                BluetoothGattCharacteristic cmdChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_COMMAND));
                cmdChar.setValue(new byte[]{2});
                gatt.writeCharacteristic(cmdChar);
            }
            else {

                reportJoinResult(false, true);
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        String uuid = characteristic.getUuid().toString();
        String scanUuid = convertFromInteger(BLUETOOTH_CHAR_SCAN).toString();
        String joinUuid = convertFromInteger(BLUETOOTH_CHAR_JOIN).toString();

        if(uuid.equals(scanUuid)) {

            reportNetworkFound(characteristic.getValue());
        }
        else if(uuid.equals(joinUuid)) {

            // Join result not received, because smartbridge disconnects after join command
            byte[] arr = characteristic.getValue();
            int state = arr[0];
            if(state == 1) {

                Log.i(TAG, "JOINING IN PROGRESS");
            }
            else if(state == 0) {

                reportJoinResult(true, false);
            }
            else {

                reportJoinResult(false, false);
            }
        }
    }

    private Runnable discoverTimeOut = new Runnable() {
        @Override
        public void run() {

            Log.i(TAG, "Discover timed out!");
            reportDiscoverFailure();
        }
    };

    private Runnable reportNetworksTimeOut = new Runnable() {
        @Override
        public void run() {

            if(scanNetworksCallback != null) {
                scanNetworksCallback.onNetworksFound(networks);
            }

            // Remove notifications for scan results
            if(gatt != null && gattService != null) {

                BluetoothGattCharacteristic scanChar = gattService.getCharacteristic(convertFromInteger(BLUETOOTH_CHAR_SCAN));
                gatt.setCharacteristicNotification(scanChar, false);
            }

            scanNetworksCallback = null;

            handler.removeCallbacks(reportNetworksTimeOut);
        }
    };

    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice receivedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                
                if(deviceName != null && deviceName.startsWith(BLUETOOTH_DEVICE_PREFIX)) {
                    Log.i(TAG, "SBWF bluetooth device found: " + deviceName);
                    reportSmartBridgeDevice(receivedDevice, deviceName);
                }
            }
        }
    };

    public void disconnect() {

        Log.i(TAG, "@disconnect!");

        if(gatt != null) {
            gatt.disconnect();
        }
    }

    public interface ScanNetworksCallback {

        void onNetworksFound(List<NetworkResponse> networks);
    }

    public interface DiscoverMacAddressCallback {

        void onMacAddressFound(String address);
        void onFailure();
    }

    public interface JoinNetworkCallback {

        void onNetworkJoined();
        void onFailure(boolean disconnected);
    }

    public interface DiscoverSmartBridgeCallback {

        void onBluetoothDeviceFound(nl.wittig.net2grid_ble.bluetooth.model.BluetoothDevice device);
        void onFailure();
    }
}

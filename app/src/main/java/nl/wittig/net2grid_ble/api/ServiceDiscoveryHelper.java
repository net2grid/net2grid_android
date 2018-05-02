package nl.wittig.net2grid_ble.api;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.net.InetAddress;

import javax.jmdns.ServiceEvent;

import nl.wittig.net2grid_ble.zeroconf.client.ZeroConfClient;

public class ServiceDiscoveryHelper implements ZeroConfClient.Listener
{
    private static final String SERVICE_TYPE = "_http._tcp";
    private static final int TIMEOUT_DURATION = 5000;
    private static final String TAG = ServiceDiscoveryHelper.class.getName();

    protected Handler handler;

    protected OnResultCallback callback;
    protected String service;
    protected ZeroConfClient client;

    public ServiceDiscoveryHelper() {

    }

    public void findIp(final String service, Context context) {

        this.service = service;

        handler = new Handler();
        handler.postDelayed(callOnError, TIMEOUT_DURATION);

        client = new ZeroConfClient(context);
        client.registerListener(this);
        client.connectToService();
    }

    private Runnable callOnError = new Runnable() {
        @Override
        public void run() {

            stopDiscovering();
            callback.onError(new Throwable("Timeout"));
        }
    };

    public void setCallback(OnResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void serviceUpdated(ServiceEvent record) {

        Log.i(TAG, "serviceUpdated: " + record.getName());
        if (record.getName().equals(service)) {

            stopDiscovering();

            if (record.getInfo() != null && record.getInfo().getInet4Addresses().length > 0) {
                handler.removeCallbacks(callOnError);
                callback.onIpFound(record.getInfo().getInet4Addresses()[0]);
            }
        }
    }

    private void stopDiscovering() {

        client.unregisterListener(this);
        client.disconnectFromService();
    }

    @Override
    public void serviceRemoved(ServiceEvent record) {

        Log.i(TAG, "serviceRemoved: " + record.getName());
    }

    public interface OnResultCallback {

        void onIpFound(InetAddress ip);
        void onError(Throwable t);
    }
}

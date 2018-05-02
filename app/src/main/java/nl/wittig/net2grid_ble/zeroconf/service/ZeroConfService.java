package nl.wittig.net2grid_ble.zeroconf.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class ZeroConfService extends Service implements Runnable {

	public final static String TAG = ZeroConfService.class.toString();

	public final static String MULTICAST_LOCK_TAG = ZeroConfService.class.toString();

	public final IBinder binder = new ZeroConfServiceBinder();

	WifiManager wifiManager;

	ConnectionStateListener connectionStateListener;

	MulticastLock multicastLock;

	JmDNS mDNS;

	Thread serviceThread;
	boolean serviceShutdownRequested = false;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating service");

		Log.d(TAG, "Getting wifi manager");
		wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

		Log.d(TAG, "Registering connection state listener");
		connectionStateListener = new ConnectionStateListener();
		registerReceiver(connectionStateListener,
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		Log.d(TAG, "Starting service thread");
		serviceThread = new Thread(this);
		serviceThread.start();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying service");

		Log.d(TAG, "Shutting down service thread");
		serviceShutdownRequested = true;
		boolean joined = false;
		while(!joined) {
			try {
				serviceThread.interrupt();
				serviceThread.join();
				joined = true;
			} catch (InterruptedException e) {
			}
		}
		
		Log.d(TAG, "Unregistering connection state listener");
		unregisterReceiver(connectionStateListener);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Binding connection " + intent);
		return binder;
	}

	private void onWifiChange() {
		serviceThread.interrupt();
	}

	public void run() {
		int lastWifiState = WifiManager.WIFI_STATE_UNKNOWN;
		// loop until shutdown
		while(!serviceShutdownRequested) {
			// depending on wifi connection
			int currentWifiState = wifiManager.getWifiState();
			if(currentWifiState != lastWifiState) {
				if(currentWifiState == WifiManager.WIFI_STATE_ENABLED) {
					startDiscovery();
				} else {
					stopDiscovery();
				}
			}
			lastWifiState = currentWifiState;
			// idle wait
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// ignored
			}
		}
	}

	private void startDiscovery() {
		Log.d(TAG, "Attempting to start discovery");

		JmDNS cur = mDNS;
		if(cur != null) {
			Log.d(TAG, "Discovery already running");
			return;
		}
		
		Log.d(TAG, "Creating multicast lock");
		multicastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_TAG);
		multicastLock.setReferenceCounted(true);

		Log.d(TAG, "Getting wifi state");
		if(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			Log.d(TAG, "Wifi state is not enabled");
			return;
		}

		Log.d(TAG, "Getting connection info");
		WifiInfo connInfo = wifiManager.getConnectionInfo();
		if(connInfo == null) {
			Log.d(TAG, "Failed to get connection info");
			return;
		}

		// XXX ugly
		int myRawAddress = connInfo.getIpAddress();
		byte[] myAddressBytes = new byte[] {
				(byte) (myRawAddress & 0xff),
				(byte) (myRawAddress >> 8 & 0xff),
				(byte) (myRawAddress >> 16 & 0xff),
				(byte) (myRawAddress >> 24 & 0xff)
		};

		InetAddress myAddress;
		try {
			myAddress = InetAddress.getByAddress(myAddressBytes);
		} catch (UnknownHostException e) {
			Log.d(TAG, "Failed to get address: " + e.toString());
			return;
		}

		Log.d(TAG, "My address is " + myAddress.toString());

		Log.d(TAG, "Acquiring multicast lock");
		multicastLock.acquire();

		Log.d(TAG, "Starting discovery on address " + myAddress);
		try {
			mDNS = JmDNS.create(myAddress);
			mDNS.addServiceTypeListener(new SrvTypeListener());
		} catch (IOException e) {
			Log.d(TAG, "Failed to start discovery: " + e.toString());
			mDNS = null;
			multicastLock.release();
		}
	}

	private void stopDiscovery() {
		Log.d(TAG, "Attempting to stop discovery");

		JmDNS cur = mDNS;
		if(cur == null) {
			Log.d(TAG, "Discovery not running");
			return;
		}
		mDNS = null;

		Log.d(TAG, "Shutting down listener");
		try {
			cur.close();
		} catch (IOException e) {
			// XXX do we care?
		}

		Log.d(TAG, "Releasing multicast lock");
		multicastLock.release();
	}

	/**
	 * Broadcast receiver watching connection state
	 */
	class ConnectionStateListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean status = (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
			Log.d(TAG, "Connection state is now " + (status ? "enabled" : "disabled"));
			onWifiChange();
		}
	}

	/**
	 * Debug wrapper
	 * 
	 * @param message
	 */
	private final void debugListener(String message) {
//		Log.d(TAG, "listener " + message);
	}

	public class ZeroConfServiceBinder extends Binder {

		public ZeroConfService getService() {

			return ZeroConfService.this;
		}
	}

	public static class ServiceUpdatedEvent {

		protected ServiceEvent event;

		public ServiceUpdatedEvent(ServiceEvent event) {
			this.event = event;
		}

		public ServiceEvent getEvent() {
			return event;
		}
	}

	public static class ServiceRemovedEvent {

		protected ServiceEvent event;

		public ServiceRemovedEvent(ServiceEvent event) {
			this.event = event;
		}

		public ServiceEvent getEvent() {
			return event;
		}
	}

	/**
	 * Internal listener for service type events.
	 * 
	 * This gets called by JmDNS when services are removed,
	 * added or resolved. This class is responsible for forwarding
	 * these events to the main thread in a synchronized fashion.
	 */
	private class SrvListener implements ServiceListener {

		/** Service type for this listener */
		private final String serviceType;

		/** Simple constructor */
		public SrvListener(String serviceType) {
			this.serviceType = serviceType;
		}

		/** Callback method for adding services */
		@Override
		public void serviceAdded(ServiceEvent event) {
			debugListener("serviceAdded(" + serviceType + " | " + event.getName() + ")");

			// request resolution of service details
			mDNS.requestServiceInfo(event.getType(), event.getName(), true);
		}

		/** Callback method for removing services */
		@Override
		public void serviceRemoved(ServiceEvent event) {
			debugListener("serviceRemoved(" + serviceType + " | " + event.getName() + ")");

			// notify the main thread
			EventBus.getDefault().post(new ServiceRemovedEvent(event));
		}

		/** Callback method for resolution results */
		@Override
		public void serviceResolved(ServiceEvent event) {
			debugListener("serviceResolved(" + serviceType + " | "+ event.getName() + ")");
			if (event.getInfo().getInet4Addresses().length > 0)
			debugListener("IP" + event.getInfo().getInet4Addresses()[0]);

			// notify the main thread
			EventBus.getDefault().post(new ServiceUpdatedEvent(event));
		}
	}

	/**
	 * Internal listener for service type events.
	 * 
	 * This gets called by JmDNS when new service types
	 * get discovered. This class is responsible for forwarding
	 * these events to the main thread in a synchronized fashion.
	 */
	private class SrvTypeListener implements ServiceTypeListener {

		/** Set of all types seen during listener lifetime */
		private final HashSet<String> seenTypes = new HashSet<String>();

		/** Callback method for adding service types */
		@Override
		public void serviceTypeAdded(ServiceEvent event) {
			debugListener("serviceTypeAdded(" + event.getType() + ")");
			if(mDNS != null) {
				String type = event.getType();

				// if we have never seen this type
				if(!seenTypes.contains(type)) {
					// add a service listener for the type
					mDNS.addServiceListener(type, new SrvListener(type));
					// remember the type
					seenTypes.add(type);
				}
			}
		}

		/** Callback method for adding service subtypes */
		@Override
		public void subTypeForServiceTypeAdded(ServiceEvent event) {
		}

	}

}

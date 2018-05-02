package nl.wittig.net2grid_ble.zeroconf.client;

import java.util.Enumeration;
import java.util.Vector;

import nl.wittig.net2grid_ble.zeroconf.service.ZeroConfService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.jmdns.ServiceEvent;

public class ZeroConfClient {

	/** Log tag */
	public final static String TAG = ZeroConfClient.class.toString();

	/* Notification message types */
	private static final int NOTIFY_UPDATED = 1;
	private static final int NOTIFY_REMOVED = 2;
	
	/** Context of this client, used for service binding */
	private Context clientContext;

	/** Bound reference to service */
	private ZeroConfService service;

	/** Vector of all our listeners */
	private Vector<Listener> listeners = new Vector<Listener>();

	/**
	 * Public constructor
	 * 
	 * Creates a zeroconf client to be used for
	 * access to the shared zeroconf service.
	 * 
	 * @param context must be provided
	 */
	public ZeroConfClient(Context context) {
		this.clientContext = context;
	}
	
	/**
	 * Debug function
	 * 
	 * @param message
	 */
	private final void debugClient(String message) {
		Log.d(TAG, message);
	}

	/**
	 * Connect to the zeroconf service
	 */
	public void connectToService() {
		debugClient("Connecting to service");

		EventBus.getDefault().register(this);

		// create intent using the service
		Intent serviceIntent = new Intent(clientContext, ZeroConfService.class);

		// perform the bind
		boolean result = clientContext.bindService(
				serviceIntent, serviceConnection,
				Context.BIND_AUTO_CREATE);

		// report on the results
		if(result) {
			debugClient("Bound to service, expecting connection");
		} else {
			debugClient("Bind failed");
		}
	}

	/**
	 * Disconnect from the zeroconf service
	 */
	public void disconnectFromService() {
		debugClient("Disconnecting from service");

		EventBus.getDefault().unregister(this);

		// unbind from service
		clientContext.unbindService(serviceConnection);
	}

	/**
	 * Convenient listener interface
	 * 
	 * To be implemented by clients.
	 */
	public interface Listener {
		void serviceUpdated(ServiceEvent record);
		void serviceRemoved(ServiceEvent record);
	}
	
	/**
	 * Register a client-level listener
	 * 
	 * @param listener
	 */
	public void registerListener(Listener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Unregister a client-level listener
	 * 
	 * @param listener
	 */
	public void unregisterListener(Listener listener) {
		listeners.remove(listener);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onServiceUpdated(ZeroConfService.ServiceUpdatedEvent event) {

		Enumeration<Listener> e = listeners.elements();

		while(e.hasMoreElements()) {
			Listener l = e.nextElement();
			l.serviceUpdated(event.getEvent());
		}
	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onServiceRemoved(ZeroConfService.ServiceRemovedEvent event) {

		Enumeration<Listener> e = listeners.elements();

		while(e.hasMoreElements()) {
			Listener l = e.nextElement();
			l.serviceRemoved(event.getEvent());
		}
	}

	/**
	 * Internal service connection
	 * 
	 * Used to track service connection state.
	 * 
	 */
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			debugClient("Connected to service");
			service = ((ZeroConfService.ZeroConfServiceBinder) binder).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			debugClient("Disconnected from service");
			service = null;
		}
	};
}

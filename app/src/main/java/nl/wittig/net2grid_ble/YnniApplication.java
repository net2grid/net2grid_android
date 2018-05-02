package nl.wittig.net2grid_ble;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;

public class YnniApplication extends Application {

    public static final String SMARTBRIDGE_SERVICE_NAME = "smartbridge";

    private static YnniApplication instance;

    private Handler mainHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        instance = this;
        mainHandler = new Handler();

        PersistentHelper.initialize(getApplicationContext());
    }

    public static Context getContext() {
        return context;
    }

    private static Context context;

    public static YnniApplication getInstance() {
        return instance;
    }

    public void runOnUiThread(final Runnable runnable) {

        mainHandler.post(runnable);
    }
}

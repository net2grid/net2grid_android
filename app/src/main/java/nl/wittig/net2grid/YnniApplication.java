package nl.wittig.net2grid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import nl.wittig.net2grid.helpers.PersistentHelper;

public class YnniApplication extends Application {

    public static final String SMARTBRIDGE_SERVICE_NAME = "smartbridge";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        PersistentHelper.initialize(getApplicationContext());
    }
}

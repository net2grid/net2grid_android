package nl.wittig.net2grid.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistentHelper {

    public static final String PREFERENCES_NAME = "NET2GRID-Android";

    public static final String KEY_SSID = "ssid";
    public static final String KEY_SMARTBRIDGE_HOST = "smartbridgehost";

    private static SharedPreferences preferences;

    public static void initialize(Context context) {

        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void setSSID(String ssid) {

        preferences.edit().putString(KEY_SSID, ssid).apply();
    }

    public static String getSSID() {

        return preferences.getString(KEY_SSID, null);
    }

    public static void setSmartBridgeHost(String host) {

        preferences.edit().putString(KEY_SMARTBRIDGE_HOST, host).apply();
    }

    public static String getSmartBridgeHost() {

        return preferences.getString(KEY_SMARTBRIDGE_HOST, null);
    }
}

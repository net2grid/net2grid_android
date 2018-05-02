package nl.wittig.net2grid_ble.helpers;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiHelper {

    public static String getCurrentNetworkName(Context context) {

        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String name = wifiInfo.getSSID();

        if (wifiInfo.getSupplicantState().equals(SupplicantState.DISCONNECTED) || name.equals("<unknow ssid>") || name.equals("")) {
            return null;
        }

        name = name.replace("\"", "");

        return name;
    }

    public static boolean onSmartBridgeNetwork(String name) {

        String ssid = PersistentHelper.getSSID();
        if (ssid == null || name == null)
            return false;

        if (ssid.equals(name))
            return true;

        return false;
    }
}

package nl.wittig.net2grid_ble.bluetooth.model;

public class WifiNetwork {

    private String ssid;
    private String rssi;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}

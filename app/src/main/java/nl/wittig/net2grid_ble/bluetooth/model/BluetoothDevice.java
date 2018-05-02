package nl.wittig.net2grid_ble.bluetooth.model;

/**
 * Created by joeykieboom on 20-02-18.
 */

public class BluetoothDevice {

    private String name;
    private String address;

    public BluetoothDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

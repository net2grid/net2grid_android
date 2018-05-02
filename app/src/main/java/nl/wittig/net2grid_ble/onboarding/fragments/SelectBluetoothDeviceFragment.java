package nl.wittig.net2grid_ble.onboarding.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.bluetooth.BluetoothManager;
import nl.wittig.net2grid_ble.onboarding.OnBoardingReadyListener;

public class SelectBluetoothDeviceFragment extends OnBoardingFragment {

    public static final String BLUETOOTH_MODE = "BlUETOOTH";
    public static final String WIFI_MODE = "WIFI";

    @BindView(R.id.select_bluetooth_device_yes)
    LinearLayout btnYes;
    @BindView(R.id.select_bluetooth_device_no)
    LinearLayout btnNo;

    @BindView(R.id.select_bluetooth_device_title)
    TextView deviceTitle;

    private OnBoardingFragment.ReadyListener listener;
    private String bluetoothDeviceName;

    public SelectBluetoothDeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_bluetooth_device, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        deviceTitle.setText(bluetoothDeviceName);

        btnYes.setOnClickListener(view1 -> {

            if (listener != null)
                listener.onFragmentReady(SelectBluetoothDeviceFragment.this, true);
        });

        btnNo.setOnClickListener(view12 -> {

            if (listener != null) {
                listener.onFragmentReady(SelectBluetoothDeviceFragment.this, false);
                BluetoothManager.getInstance().disconnect();
            }
        });
    }

    public void setBluetoothDeviceName(String deviceName) {

        this.bluetoothDeviceName = deviceName;
    }

    public static SelectBluetoothDeviceFragment newInstance() {

        Bundle args = new Bundle();

        SelectBluetoothDeviceFragment fragment = new SelectBluetoothDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setFragmentReadyListener(OnBoardingFragment.ReadyListener listener) {

        this.listener = listener;
    }
}

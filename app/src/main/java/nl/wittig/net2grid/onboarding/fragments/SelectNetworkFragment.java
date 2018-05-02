package nl.wittig.net2grid.onboarding.fragments;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.wittig.net2grid.onboarding.api.model.NetworkItemModel;
import nl.wittig.net2grid.R;
import nl.wittig.net2grid.listeners.RecyclerItemClickListener;
import nl.wittig.net2grid.onboarding.api.MedaApiManager;
import nl.wittig.net2grid.onboarding.api.responses.NetworkListResponse;
import nl.wittig.net2grid.utils.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectNetworkFragment extends OnBoardingFragment {

    public static final String TAG = SelectNetworkFragment.class.getSimpleName();

    private final long CHECK_FOR_CONNECTION_DELAY_MS = 10000;

    @BindView(R.id.recycler_wifi_networks) RecyclerView wifiNetworkList;

    @BindView(R.id.blabla) TextView titleView;
    private List<NetworkItemModel> networkList;

    private NetworkListAdapter networkAdapter;
    private MedaApiManager apiManager;
    private Handler handler;

    private boolean firstCall = true;

    private String password = "";

    public SelectNetworkFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        apiManager = MedaApiManager.getDefaultInstance();
        handler = new Handler();

        wifiNetworkList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        createNetworkAdapterIfNeeded();
        wifiNetworkList.setAdapter(networkAdapter);

        wifiNetworkList.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                selectNetwork(networkList.get(position));
            }
        }));
    }

    private void selectNetwork(NetworkItemModel network) {

        String networkPassword = "";

        // ask for password
        if (network.isPasswordProtected()) {
            startPasswordDialog(network);
        }

        // send complete
    }

    private void startPasswordDialog(final NetworkItemModel network) {

        AlertDialog.Builder passwordDialog = new AlertDialog.Builder(getActivity());
        passwordDialog.setTitle(network.getName());
        passwordDialog.setMessage("Please enter your password");

        FrameLayout container = new FrameLayout(getContext());

        final EditText passwordInput = new EditText(getContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordInput.setSingleLine();

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordInput.setSelection(passwordInput.getText().length());
            }
        });

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        lp.leftMargin = 60;
        lp.rightMargin = 60;

        passwordInput.setLayoutParams(lp);
        container.addView(passwordInput);
        passwordDialog.setView(container);

        passwordDialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                password = passwordInput.getText().toString().trim();
                readyListener.onFragmentReady(SelectNetworkFragment.this, new FragmentResponse(network, password));
            }
        });

        passwordDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });

        passwordDialog.create();

        passwordDialog.show();
    }

    private void createNetworkAdapterIfNeeded() {
        if(networkAdapter == null) {
            networkList = new ArrayList<>();
            networkAdapter = new NetworkListAdapter(networkList);
        }
    }

    public void setNetworkResponses(List<NetworkItemModel> networkItemModels) {
        createNetworkAdapterIfNeeded();

        List<NetworkItemModel> result = new ArrayList<>();
        Set<String> titles = new HashSet<>();

        for(NetworkItemModel item : networkItemModels ) {
            if(!titles.contains(item.getName())) {
                titles.add(item.getName());
                result.add(item);
            }
        }

        Collections.sort(result, new Comparator<NetworkItemModel>() {
            @Override
            public int compare(NetworkItemModel networkItemModel, NetworkItemModel t1) {
                return networkItemModel.getName().compareToIgnoreCase(t1.getName());
            }
        });

        networkAdapter.setModelList(result);
        networkList.clear();
        networkList.addAll(result);
        networkAdapter.notifyDataSetChanged();
    }

    public void loadNetworkList() {

        apiManager.loadNetworksList(new Callback<NetworkListResponse>() {
            @Override
            public void onResponse(Call<NetworkListResponse> call, Response<NetworkListResponse> response) {

                if(!response.isSuccessful() || response.body() == null){
                    onFailure(call, new Throwable("Unsuccessful response"));
                    return;
                }

                firstCall = false;

                setNetworkResponses(Tools.castNetworkListResponseToViewModels(response.body().getNetworkResponseList()));

                handler.postDelayed(refreshNetworks, CHECK_FOR_CONNECTION_DELAY_MS);
            }

            @Override
            public void onFailure(Call<NetworkListResponse> call, Throwable t) {

                handler.postDelayed(refreshNetworks, CHECK_FOR_CONNECTION_DELAY_MS);
            }
        });
    }

    @Override
    public void onFragmentVisible() {

        refreshNetworks.run();
    }

    @Override
    public void onFragmentHidden() {

        handler.removeCallbacks(refreshNetworks);
    }

    private Runnable refreshNetworks = new Runnable() {
        @Override
        public void run() {

            loadNetworkList();
        }
    };

    public class NetworkListAdapter extends RecyclerView.Adapter<NetworkViewHolder> {

        private List<NetworkItemModel> modelList;

        public NetworkListAdapter(List<NetworkItemModel> networkList) {
            modelList = networkList;
        }

        public void setModelList(List<NetworkItemModel> modelList) {
            this.modelList = modelList;
            notifyDataSetChanged();
        }

        @Override
        public NetworkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_network_view, parent, false);
            return new NetworkViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(NetworkViewHolder holder, int position) {

            final NetworkItemModel networkItemModel = modelList.get(position);

            holder.textNetworkName.setText(networkItemModel.getName());
        }

        @Override
        public int getItemCount() {
            return modelList != null ? modelList.size() : 0;
        }
    }

    public class NetworkViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_network_name) TextView textNetworkName;
        @BindView(R.id.separator_bottom) View sepBottom;

        public NetworkViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class FragmentResponse {

        private NetworkItemModel network;
        private String password;

        public FragmentResponse(NetworkItemModel network, String password) {

            this.network = network;
            this.password = password;
        }

        public NetworkItemModel getNetwork() {
            return network;
        }

        public String getPassword() {
            return password;
        }
    }
}

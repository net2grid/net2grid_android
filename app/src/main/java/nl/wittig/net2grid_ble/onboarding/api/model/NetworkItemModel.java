package nl.wittig.net2grid_ble.onboarding.api.model;

public class NetworkItemModel {
    public static final String TAG = NetworkItemModel.class.getSimpleName();

    private boolean passwordProtected;
    private String name;

    public NetworkItemModel(boolean passwordProtected, String name) {
        this.passwordProtected = passwordProtected;
        this.name = name;
    }

    public boolean isPasswordProtected() {

        return passwordProtected;
    }

    public String getName() {
        return name;
    }
}

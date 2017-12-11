package nl.wittig.net2grid.liveUsage;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import nl.wittig.net2grid.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WrongNetworkConnectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_network_connected);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

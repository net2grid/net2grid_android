package nl.wittig.net2grid.liveUsage.fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public abstract class LiveChartFragment extends Fragment
{
    private static final String TAG = LiveChartFragment.class.getSimpleName();
    protected ReadyListener readyListener;

    private long DEFAULT_REFRESH_INTERVAL = TimeUnit.SECONDS.toMillis(10);

    protected boolean fragmentVisible;
    protected boolean fragmentStarted;
    private boolean isCreated = false;
    protected boolean hadFirstData = false;

    private Handler handler = new Handler();

    public void reportFragmentVisible(boolean visible){

        if(fragmentVisible == visible){
            return;
        }

        fragmentVisible = visible;

        if(fragmentVisible && fragmentStarted){
            onFragmentVisible();
        }
        else if(!fragmentVisible && fragmentStarted){
            onFragmentHidden();
        }
    }

    protected abstract void fetchData();
    protected abstract void setupChart();

    protected abstract long getRefreshInterval();
    protected abstract int getChartColorResource();

    protected void updateData() {

        hadFirstData = true;
    }

    protected void onFragmentVisible() {

        long randomDelay = Math.round(Math.random() * 2000);

        Log.i(TAG, "Fetching in " + randomDelay);

        handler.postDelayed(fetchRunnable, randomDelay);

        readyListener.onFragmentReady(this);

        if (!isCreated) {

            setupChart();
            isCreated = true;
        }
    }

    protected void onFragmentHidden() {

        handler.removeCallbacks(fetchRunnable);
    }

    @Override
    public void onResume() {

        super.onResume();

        fragmentStarted = true;

        if(fragmentVisible){
            onFragmentVisible();
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        fragmentStarted = false;

        if(fragmentVisible){
            onFragmentHidden();
        }
    }

    public void setFragmentReadyListener(ReadyListener listener) {

        this.readyListener = listener;
    }

    protected void scheduleFetchIfNeeded() {

        if (fragmentVisible) {
            handler.postDelayed(fetchRunnable, hadFirstData ? getRefreshInterval() : DEFAULT_REFRESH_INTERVAL);
        }
    }

    protected Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {

            fetchData();
        }
    };

    public interface ReadyListener {
        void onFragmentReady(LiveChartFragment sender);
    }
}

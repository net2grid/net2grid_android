package nl.wittig.net2grid.liveUsage.fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;

public abstract class LiveChartFragment extends Fragment
{
    protected ReadyListener readyListener;

    protected boolean fragmentVisible;
    protected boolean fragmentStarted;
    private boolean isCreated = false;

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

    protected void onFragmentVisible() {

        fetchData();

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

    protected void scheduleFetchIfNeeded(long timeOut) {

        if (fragmentVisible) {
            handler.postDelayed(fetchRunnable, timeOut);
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

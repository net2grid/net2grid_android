package nl.wittig.net2grid.onboarding.fragments;


import android.support.v4.app.Fragment;

public abstract class OnBoardingFragment extends Fragment
{
    protected ReadyListener readyListener;

    protected boolean fragmentVisible;
    protected boolean fragmentStarted;

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

    protected void onFragmentVisible() {

    }

    protected void onFragmentHidden() {

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

    public interface ReadyListener {
        void onFragmentReady(OnBoardingFragment sender, Object response);
    }
}

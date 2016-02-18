package views;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.offerland.app.R;

import utils.ViewUtil;

public abstract class ProgressFragment extends Fragment {
    // constants for view states
    private static final int VIEW_STATE_MAIN = 1;
    private static final int VIEW_STATE_PROGRESS = 2;
    private static final int VIEW_STATE_ERROR = 3;
    private static final int VIEW_STATE_EMPTY = 4;

    private int viewState; // used to save current visible view state

    private Activity activity;
    private Typeface typeface;

    // main views
    private View mainView;
    private View progressView;
    private View errorView;
    private View emptyView;
    private SwipeRefreshLayout swipeLayout;

    // error view components
    private TextView textError;
    private TextView textTapToRetry;
    private ImageButton buttonRefresh;
    private String errorMsg;

    // empty view components
    private TextView textEmpty;
    private String emptyMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);

        // init components
        activity = getActivity();
        typeface = Typeface.createFromAsset(activity.getAssets(), "roboto_l.ttf");
        mainView = rootView.findViewById(R.id.view_main);
        progressView = rootView.findViewById(R.id.view_progress);
        errorView = rootView.findViewById(R.id.view_error);
        emptyView = rootView.findViewById(R.id.view_empty);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeLayout);
        textError = (TextView) errorView.findViewById(R.id.text_error);
        textTapToRetry = (TextView) errorView.findViewById(R.id.text_tapToRetry);
        buttonRefresh = (ImageButton) errorView.findViewById(R.id.button_refresh);
        textEmpty = (TextView) emptyView.findViewById(R.id.text_empty);

        // customize swipe layout colors
        swipeLayout.setColorSchemeResources(
                R.color.primary_dark,
                R.color.primary,
                R.color.hint);

        // customize fonts
        textError.setTypeface(typeface);
        textTapToRetry.setTypeface(typeface);
        textEmpty.setTypeface(typeface);

        // add action listeners
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ProgressFragment.this.onRefresh();
            }
        });

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefresh();
            }
        });


        return rootView;
    }

    /**
     * abstract method to pass layout resource id from children
     */
    protected abstract int getLayoutResource();

    /*
    * method used to show main viewState
    */
    protected void showMain() {
        // hide error viewState if it is visible
        ViewUtil.showView(errorView, false);
        // hide empty viewState if it is visible
        ViewUtil.showView(emptyView, false);
        // hide all AppMsgs
        AppMsg.cancelAll(activity);

        // hide progress viewState if it is visible
        ViewUtil.showView(progressView, false);
        // stop swipe layout refreshing if it is
        if (swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(false);

        // show main viewState
        ViewUtil.showView(mainView, true);
        // update viewState state
        viewState = VIEW_STATE_MAIN;
    }

    /*
     * method used to show progress if it is possible
     */
    protected void showProgress() {
        // check to ensure main view is not visible
        if (viewState != VIEW_STATE_MAIN) {
            // not visible, so hide all views
            ViewUtil.showView(mainView, false);
            ViewUtil.showView(errorView, false);
            ViewUtil.showView(emptyView, false);
            // and show progress view
            ViewUtil.showView(progressView, true);

            // update view state
            viewState = VIEW_STATE_PROGRESS;
        } else {
            // main view is visible >> hide all AppMsgs and swipe layout will show its progress
            AppMsg.cancelAll(activity);
        }
    }

    /*
     * overloaded method used to show error with default msg
     */
    protected void showError() {
        errorMsg = getString(R.string.error_loading_data);
        // set error text
        textError.setText(errorMsg);

        // show the suitable error style
        showTheError();
    }

    /*
     * overloaded method used to show error with String msg
     */
    protected void showError(String errorMsg) {
        this.errorMsg = errorMsg;
        // set error text
        textError.setText(errorMsg);

        // show the suitable error style
        showTheError();
    }

    /*
     * overloaded method used to show error with msg resource id
     */
    protected void showError(int errorMsgResource) {
        errorMsg = getString(errorMsgResource);
        // set error text
        textError.setText(errorMsgResource);

        // show the suitable error style
        showTheError();
    }

    /*
     * method used to show the suitable error msg
     */
    private void showTheError() {
        // check if main view is visible
        if (viewState == VIEW_STATE_MAIN) {
            // visible, so stop swipe layout refreshing if it is
            if (swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);

            // and hide all other AppMsgs
            AppMsg.cancelAll(activity);
            // and just show error in AppMsg
            AppMsg appMsg = AppMsg.makeText(activity, errorMsg, AppMsg.STYLE_CONFIRM);
            appMsg.setParent((ViewGroup) mainView);
            appMsg.show();
        } else {
            // main view is not visible, so hide all views
            ViewUtil.showView(mainView, false);
            ViewUtil.showView(progressView, false);
            ViewUtil.showView(emptyView, false);

            // and show error view
            ViewUtil.showView(errorView, true);

            // update view state
            viewState = VIEW_STATE_ERROR;
        }
    }

    /*
     * overloaded method used to show empty with default msg
     */
    protected void showEmpty() {
        emptyMsg = getString(R.string.no_data_found);
        // set empty text
        textEmpty.setText(emptyMsg);

        // show the suitable empty style
        showTheEmpty();
    }

    /*
     * overloaded method used to show empty with String msg
     */
    protected void showEmpty(String emptyMsg) {
        this.emptyMsg = emptyMsg;
        // set empty text
        textEmpty.setText(emptyMsg);

        // show the suitable empty style
        showTheEmpty();
    }

    /*
     * overloaded method used to show empty with msg resource id
     */
    protected void showEmpty(int emptyMsgResource) {
        emptyMsg = getString(emptyMsgResource);
        // set empty text
        textEmpty.setText(emptyMsgResource);

        // show the suitable empty style
        showTheEmpty();
    }

    /*
     * method used to show the suitable empty msg
     */
    private void showTheEmpty() {
        // hide all views
        ViewUtil.showView(mainView, false);
        ViewUtil.showView(progressView, false);
        ViewUtil.showView(errorView, false);

        // and show empty view
        ViewUtil.showView(emptyView, true);

        // update view state
        viewState = VIEW_STATE_EMPTY;
    }

    /**
     * abstract method to override slide_in children to do refresh operation
     */
    protected abstract void onRefresh();
}

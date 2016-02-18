package com.offerland.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import components.LocationFinder;
import datamodels.Constants;
import datamodels.Store;
import datamodels.User;
import json.JsonReader;
import json.StoresHandler;
import utils.InternetUtil;
import utils.ViewUtil;

/**
 * Created by Shamyyoun on 2/24/2015.
 */
public class StoresMapFragment extends Fragment {
    // main objects
    private ActionBarActivity activity;
    private User user;
    private Typeface typeface;

    // main views
    private View progressView;
    private View errorView;
    private View mainView;
    private TextView textError;
    private TextView textTapToRetry;
    private ImageButton buttonRefresh;
    private SupportMapFragment mapFragment;

    private ArrayList<AsyncTask> runningTasks; // used to hold running tasks

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stores_map, container, false);
        initComponents(rootView);

        return rootView;
    }

    /**
     * method used to initialize components
     */
    private void initComponents(View rootView) {
        activity = (ActionBarActivity) getActivity();
        user = AppController.getInstance(activity).getActiveUser();
        typeface = Typeface.createFromAsset(activity.getAssets(), "roboto_l.ttf");
        progressView = rootView.findViewById(R.id.view_progress);
        mainView = rootView.findViewById(R.id.view_main);
        errorView = rootView.findViewById(R.id.view_error);
        textError = (TextView) errorView.findViewById(R.id.text_error);
        textTapToRetry = (TextView) errorView.findViewById(R.id.text_tapToRetry);
        buttonRefresh = (ImageButton) errorView.findViewById(R.id.button_refresh);
        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));

        runningTasks = new ArrayList<>();

        // customize fonts
        textError.setTypeface(typeface);
        textTapToRetry.setTypeface(typeface);

        // add listeners
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StoresTask().execute();
            }
        });

        // load stores
        new StoresTask().execute();
    }

    /**
     * async task used to load offers
     */
    private class StoresTask extends AsyncTask<Void, Void, Void> {
        private Location location;
        private String response;

        private StoresTask() {
            // save reference to this task, to destroy it if required
            runningTasks.add(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);
                cancel(true);
                return;
            }

            // try to get location
            LocationFinder locationFinder = new LocationFinder(activity);
            location = locationFinder.getLocation();

            showProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create url
            String url = AppController.END_POINT + "/nearstores/" + user.getId();
            if (location != null) {
                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    url += "/" + location.getLatitude() + "/" + location.getLongitude();
                }
            }

            // create and execute request
            JsonReader jsonReader = new JsonReader(url);
            response = jsonReader.sendGetRequest();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // validate response
            if (response == null) {
                // show error msg
                showError(R.string.connection_error_try_again);
                return;
            }

            // ---response is valid---
            // handle it in stores array
            StoresHandler handler = new StoresHandler(response);
            final List<Store> stores = handler.handle();

            // check handling operation result
            if (stores == null) {
                // show error msg
                showError(R.string.connection_error_try_again);
                return;
            }

            // get GoogleMap object when loaded
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // customize map
                    googleMap.setMyLocationEnabled(true);

                    // check size
                    if (stores.size() == 0) {
                        // show msg
                        showEmpty(R.string.no_stores_near_you);
                        return;
                    }

                    // --there's one or more store >> add markers on map--
                    final List<Marker> markers = new ArrayList<>();
                    for (Store store : stores) {
                        LatLng coordinate = new LatLng(store.getLat(), store.getLng());
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .title(store.getFirstName() + " " + store.getLastName())
                                .position(coordinate));

                        markers.add(marker);
                    }

                    // zoom in showing all markers
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                    googleMap.animateCamera(cameraUpdate);

                    // add markers click listeners
                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            for (Store store : stores) {
                                if ((store.getFirstName() + " " + store.getLastName()).equals(marker.getTitle())) {
                                    Intent intent = new Intent(activity, StoreOffersActivity.class);
                                    intent.putExtra(Constants.KEY_STORE, intent);
                                    startActivity(intent);
                                }
                            }

                            return false;
                        }
                    });

                    showMain();
                }
            });
        }
    }

    /**
     * method, used to show progress view
     */
    private void showProgress() {
        ViewUtil.showView(progressView, true);
        ViewUtil.showView(errorView, false);
        ViewUtil.showView(mainView, false);
    }

    /**
     * method, used to show error view
     */
    private void showError(int errorMsgRes) {
        textError.setText(errorMsgRes);
        ViewUtil.showView(errorView, true);
        ViewUtil.showView(progressView, false);
        ViewUtil.showView(mainView, false);
    }

    /**
     * method, used to show main view
     */
    private void showMain() {
        ViewUtil.showView(mainView, true);
        ViewUtil.showView(errorView, false);
        ViewUtil.showView(progressView, false);
    }

    /**
     * method used to show empty msg
     */
    private void showEmpty(int msgResId) {
        showMain();
        AppMsg appMsg = AppMsg.makeText(activity, msgResId, AppMsg.STYLE_CONFIRM);
        appMsg.setParent(R.id.view_main);
        AppMsg.cancelAll(activity);
        appMsg.show();
    }

    /*
     * overriden method
     */
    @Override
    public void onDestroy() {
        // cancel all appmsgs
        AppMsg.cancelAll(activity);

        // stop all running tasks
        if (runningTasks != null) {
            for (AsyncTask task : runningTasks) {
                task.cancel(true);
            }
        }

        super.onDestroy();
    }
}

package com.offerland.app;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devspark.appmsg.AppMsg;

import java.util.ArrayList;
import java.util.List;

import adapters.OffersAdapter;
import components.LocationFinder;
import datamodels.Constants;
import datamodels.Offer;
import datamodels.User;
import json.JsonReader;
import json.OffersHandler;
import utils.InternetUtil;
import views.ProgressFragment;

/**
 * Created by Shamyyoun on 2/24/2015.
 */
public class OffersNearMeFragment extends ProgressFragment {
    // main objects
    private Activity activity;
    private User user;
    private RecyclerView recyclerOffers;

    private ArrayList<AsyncTask> runningTasks; // used to hold running tasks

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        initComponents(rootView);

        return rootView;
    }

    /**
     * overriden abstract method, used to set content layout resource
     */
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_offers;
    }

    /**
     * method used to initialize components
     */
    private void initComponents(View rootView) {
        activity = getActivity();
        user = AppController.getInstance(activity).getActiveUser();
        recyclerOffers = (RecyclerView) rootView.findViewById(R.id.recycler_offers);
        runningTasks = new ArrayList<>();

        // customize recycler view
        recyclerOffers.setLayoutManager(new LinearLayoutManager(activity));
        recyclerOffers.setItemAnimator(new DefaultItemAnimator());

        // load offers
        new OffersTask().execute();
    }

    /**
     * async task used to load offers
     */
    private class OffersTask extends AsyncTask<Void, Void, Void> {
        private Location location;
        private String response;

        private OffersTask() {
            // save reference to this task, to destroy it if required
            runningTasks.add(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                // show error
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
            String url = AppController.END_POINT + "/nearoffers/" + user.getId();
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
            // handle it in offers array
            OffersHandler handler = new OffersHandler(response);
            final List<Offer> offers = handler.handle();

            // check handling operation result
            if (offers == null) {
                // show error msg
                showError(R.string.connection_error_try_again);
                return;
            }

            // check size
            if (offers.size() == 0) {
                // show msg
                showEmpty(R.string.no_offers_near_you_now);
                return;
            }

            // update recycler adapter
            OffersAdapter adapter = new OffersAdapter(activity, offers, R.layout.recycler_offers_item1);
            adapter.setOnItemClickListener(new OffersAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Offer offer = offers.get(position);
                    Intent intent = new Intent(activity, OfferActivity.class);
                    intent.putExtra(Constants.KEY_OFFER, offer);
                    startActivity(intent);
                }
            });
            recyclerOffers.setAdapter(adapter);

            showMain();
        }
    }

    /**
     * overriden method, used to refresh content
     */
    @Override
    protected void onRefresh() {
        new OffersTask().execute();
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

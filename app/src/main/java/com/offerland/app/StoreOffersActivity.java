package com.offerland.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.devspark.appmsg.AppMsg;

import java.util.ArrayList;
import java.util.List;

import adapters.OffersAdapter;
import components.LocationFinder;
import datamodels.Constants;
import datamodels.Offer;
import datamodels.Store;
import json.JsonReader;
import json.OffersHandler;
import utils.InternetUtil;
import views.ProgressActivity;

/**
 * Created by Shamyyoun on 6/27/2015.
 */
public class StoreOffersActivity extends ProgressActivity {
    // main objects
    private Activity activity;
    private Store store;
    private RecyclerView recyclerOffers;

    private ArrayList<AsyncTask> runningTasks; // used to hold running tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initComponents();
    }

    /**
     * overriden abstract method, used to set content layout resource
     */
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_store_offers;
    }

    /**
     * method used to initialize components
     */
    private void initComponents() {
        store = (Store) getIntent().getSerializableExtra(Constants.KEY_STORE);
        recyclerOffers = (RecyclerView) findViewById(R.id.recycler_offers);
        runningTasks = new ArrayList<>();

        // customize recycler view
        recyclerOffers.setLayoutManager(new GridLayoutManager(activity, 2));
        recyclerOffers.setItemAnimator(new DefaultItemAnimator());

        // load offers
        new OffersTask().execute();
    }

    /**
     * async task used to load offers
     */
    private class OffersTask extends AsyncTask<Void, Void, Void> {
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

            showProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/store_offers/" + store.getId();
            JsonReader jsonReader = new JsonReader(url);

            // execute request
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
                String msg = activity.getString(R.string.no_offers_in) + " " + store.getFirstName() + " " + store.getLastName();
                showEmpty(msg);
                return;
            }

            // update recycler adapter
            OffersAdapter adapter = new OffersAdapter(activity, offers, R.layout.recycler_offers_item2);
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

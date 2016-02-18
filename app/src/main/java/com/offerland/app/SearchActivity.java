package com.offerland.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import adapters.OffersAdapter;
import datamodels.Constants;
import datamodels.Offer;
import json.JsonReader;
import json.OffersHandler;
import utils.InternetUtil;
import utils.ViewUtil;

/**
 * Created by Shamyyoun on 6/27/2015.
 */
public class SearchActivity extends ActionBarActivity {
    // main views
    private View mainView;
    private View progressView;
    private View errorView;
    private View emptyView;

    // other views
    private TextView textError;
    private ImageButton buttonRefresh;
    private TextView textEmpty;

    // main screen objects
    private EditText textSearch;
    private ImageButton buttonSearch;
    private RecyclerView recyclerResults;
    private OffersAdapter adapter;

    private List<AsyncTask> tasks; // used to hold running tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        mainView = findViewById(R.id.view_main);
        progressView = findViewById(R.id.view_progress);
        errorView = findViewById(R.id.view_error);
        emptyView = findViewById(R.id.view_empty);

        textError = (TextView) errorView.findViewById(R.id.text_error);
        buttonRefresh = (ImageButton) errorView.findViewById(R.id.button_refresh);
        textEmpty = (TextView) emptyView.findViewById(R.id.text_empty);

        textSearch = (EditText) findViewById(R.id.text_search);
        buttonSearch = (ImageButton) findViewById(R.id.button_search);
        recyclerResults = (RecyclerView) findViewById(R.id.recycler_results);
        LayoutInflater inflater = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE));

        tasks = new ArrayList<>();

        // customize textSearch
        String color = String.format("#%06X", 0xFFFFFF & getResources().getColor(R.color.hint));
        textSearch.setHint(Html.fromHtml("<font color='" + color + "'>Search offers...</font>"));

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textSearch.setTypeface(typeface);
        textError.setTypeface(typeface);
        textEmpty.setTypeface(typeface);

        // add listeners
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validate inputs
                String searchText = textSearch.getText().toString();
                searchText = searchText.trim();
                if (searchText.isEmpty()) {
                    // show error
                    textSearch.setText("");
                    textSearch.setError("Enter search text");

                    return;
                }

                // hide keyboard
                InputMethodManager imm = (InputMethodManager) SearchActivity.this.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textSearch.getWindowToken(), 0);

                // run search task
                new SearchTask(searchText).execute();
            }
        });
    }

    /**
     * sub class, used to get search results from server
     */
    private class SearchTask extends AsyncTask<Void, Void, Void> {
        private String searchText;
        private SearchActivity activity;

        private String response;

        private SearchTask(final String searchText) {
            this.searchText = searchText;
            activity = SearchActivity.this;

            // add listener to refresh button, to re run this task if required
            buttonRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SearchTask(searchText).execute();
                }
            });

            cancelRunningTasks(); // cancel running tasks before run this
            tasks.add(this); // hold  reference to this task, to destroy it if required
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

            showProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = AppController.END_POINT + "/findoffer";
            JsonReader jsonReader = new JsonReader(url);

            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("search", searchText));

            response = jsonReader.sendPostRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tasks.remove(this); // remove from running tasks

            // validate response
            if (response == null) {
                // show error msg
                showError(R.string.connection_error);

                return;
            }

            // ---response is valid---
            // handle it
            OffersHandler handler = new OffersHandler(response);
            final List<Offer> offers = handler.handle();

            // check handling operation result
            if (offers == null) {
                // show error msg
                showError(R.string.connection_error);

                return;
            }

            // check if data is empty
            if (offers.size() == 0) {
                // show empty msg
                showEmpty(R.string.no_offers_found);

                return;
            }

            // set adapter
            adapter = new OffersAdapter(getApplicationContext(), offers, R.layout.recycler_offers_item1);
            adapter.setOnItemClickListener(new OffersAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(activity, OfferActivity.class);
                    intent.putExtra(Constants.KEY_OFFER, offers.get(position));
                    startActivity(intent);
                }
            });
            recyclerResults.setAdapter(adapter);

            showMain();
        }
    }

    /**
     * method, used to show main view
     */
    private void showMain() {
        // hide all other
        ViewUtil.showView(progressView, false);
        ViewUtil.showView(errorView, false);
        ViewUtil.showView(emptyView, false);

        // show main view
        ViewUtil.showView(mainView, true);
    }

    /**
     * method, used to show progress view
     */
    private void showProgress() {
        // hide all other
        ViewUtil.showView(mainView, false);
        ViewUtil.showView(errorView, false);
        ViewUtil.showView(emptyView, false);

        // show progress view
        ViewUtil.showView(progressView, true);
    }

    /**
     * method, used to show error view
     */
    private void showError(int msgRes) {
        // hide all other
        ViewUtil.showView(progressView, false);
        ViewUtil.showView(mainView, false);
        ViewUtil.showView(emptyView, false);

        // show error view
        textError.setText(msgRes);
        ViewUtil.showView(errorView, true);
    }

    /**
     * method, used to show empty view
     */
    private void showEmpty(int msgRes) {
        // hide all other
        ViewUtil.showView(progressView, false);
        ViewUtil.showView(errorView, false);
        ViewUtil.showView(mainView, false);

        // show empty view
        textEmpty.setText(msgRes);
        ViewUtil.showView(emptyView, true);
    }

    /**
     * method, used to cancel running tasks
     */
    private void cancelRunningTasks() {
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }
    }

    /**
     * overriden method
     */
    @Override
    public void onDestroy() {
        cancelRunningTasks();
        super.onDestroy();
    }
}

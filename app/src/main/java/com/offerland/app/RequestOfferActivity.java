package com.offerland.app;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.devspark.appmsg.AppMsg;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import components.LocationFinder;
import datamodels.Constants;
import datamodels.User;
import json.JsonReader;
import utils.InternetUtil;

/**
 * Created by Shamyyoun on 6/27/2015.
 */
public class RequestOfferActivity extends ActionBarActivity {
    // main objects
    private User user;
    private Typeface typeface;

    // main views
    private EditText textTitle;
    private EditText textDesc;
    private EditText textBudget;
    private Button buttonRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_offer);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        user = AppController.getInstance(this).getActiveUser();
        typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textTitle = (EditText) findViewById(R.id.text_title);
        textDesc = (EditText) findViewById(R.id.text_desc);
        textBudget = (EditText) findViewById(R.id.text_budget);
        buttonRequest = (Button) findViewById(R.id.button_request);

        // customize fonts
        textTitle.setTypeface(typeface);
        textDesc.setTypeface(typeface);
        textBudget.setTypeface(typeface);
        buttonRequest.setTypeface(typeface);

        // add listeners
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RequestOfferTask().execute();
            }
        });
    }

    /**
     * async task, used to request offer in server
     */
    private class RequestOfferTask extends AsyncTask<Void, Void, Void> {
        private RequestOfferActivity activity;
        private ProgressDialog progressDialog;

        private Location location;
        private String title;
        private String desc;
        private String budget;

        private String response;

        private RequestOfferTask() {
            activity = RequestOfferActivity.this;

            // create and customize progress dialog
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.please_wait));

            // try to get location
            LocationFinder locationFinder = new LocationFinder(activity);
            location = locationFinder.getLocation();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // get inputs
            title = textTitle.getText().toString();
            desc = textDesc.getText().toString();
            budget = textBudget.getText().toString();

            // validate inputs
            if (title.isEmpty() || desc.isEmpty() || budget.isEmpty()) {
                showError(R.string.invalid_inputs);
                cancel(true);
                return;
            }

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);
                cancel(true);
                return;
            }

            // all conditions is okay >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/makeRequest";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("user_id", "" + AppController.getInstance(activity).getActiveUser().getId()));
            parameters.add(new BasicNameValuePair("title", title));
            parameters.add(new BasicNameValuePair("desc", desc));
            parameters.add(new BasicNameValuePair("price", budget));
            if (location != null) {
                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    parameters.add(new BasicNameValuePair("lat", "" + location.getLatitude()));
                    parameters.add(new BasicNameValuePair("long", "" + location.getLongitude()));
                }
            }

            // execute request
            response = jsonReader.sendPostRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // hide progress
            showProgress(false);

            // validate response
            if (response == null) {
                showError(R.string.connection_error_try_again);
                return;
            }

            // check result
            if (response.equals(Constants.JSON_MSG_TRUE)) {
                // --successful request--
                // finish activity
                setResult(RESULT_OK);
                finish();
            } else {
                showError(R.string.connection_error_try_again);
            }
        }

        private void showProgress(boolean show) {
            if (show)
                progressDialog.show();
            else
                progressDialog.dismiss();
        }

        private void showError(int errorMsgRes) {
            AppMsg appMsg = AppMsg.makeText(activity, errorMsgRes, AppMsg.STYLE_CONFIRM);
            appMsg.setParent(R.id.view_main);
            AppMsg.cancelAll(activity);
            appMsg.show();
        }
    }
}

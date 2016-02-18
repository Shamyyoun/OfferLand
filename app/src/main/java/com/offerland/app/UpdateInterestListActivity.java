package com.offerland.app;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;

import java.util.ArrayList;
import java.util.List;

import adapters.CategoriesAdapter;
import database.CategoryDAO;
import database.InterestListDAO;
import datamodels.Category;
import datamodels.Constants;
import datamodels.User;
import json.JsonReader;
import utils.InternetUtil;

/**
 * Created by Shamyyoun on 6/27/2015.
 */
public class UpdateInterestListActivity extends ActionBarActivity {
    // main objects
    private User user;
    private CategoryDAO categoryDAO;
    private InterestListDAO interestListDAO;
    private Typeface typeface;

    // main views
    private CategoriesAdapter adapter;
    private ListView listCategories;
    private Button buttonSave;

    private ArrayList<AsyncTask> tasks; // used to hold running tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_interest_list);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        user = AppController.getInstance(this).getActiveUser();
        categoryDAO = new CategoryDAO(this);
        interestListDAO = new InterestListDAO(this);
        typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        listCategories = (ListView) findViewById(R.id.list_categories);
        buttonSave = (Button) findViewById(R.id.button_save);
        tasks = new ArrayList<>();

        // customize fonts
        buttonSave.setTypeface(typeface);

        // load categories from database
        categoryDAO.open();
        final List<Category> categories = categoryDAO.getAll();
        categoryDAO.close();

        // load old interest list
        interestListDAO.open();
        List<Category> oldInterestList = interestListDAO.getAll();
        interestListDAO.close();

        // check categories in interest list
        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < oldInterestList.size(); j++) {
                Category category = categories.get(i);
                Category oldInterestListItem = oldInterestList.get(j);
                if (category.getId() == oldInterestListItem.getId()) {
                    category.setChecked(true);
                    break;
                }
            }
        }

        // set list view adapter
        adapter = new CategoriesAdapter(this, R.layout.list_categories_item, categories);
        listCategories.setAdapter(adapter);

        // add listeners
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateInterestListTask().execute();
            }
        });
    }

    /**
     * async task, used to update interest list in server
     */
    private class UpdateInterestListTask extends AsyncTask<Void, Void, Void> {
        private UpdateInterestListActivity activity;
        private List<Category> newInterestList;
        private ProgressDialog progressDialog;
        private String response;

        private UpdateInterestListTask() {
            activity = UpdateInterestListActivity.this;
            newInterestList = adapter.getCheckedItems();

            // create and customize progress dialog
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.please_wait));

            tasks.add(this); // save reference to this task to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check new interest list length
            if (newInterestList.size() == 0) {
                showError(R.string.choose_one_category_at_least);
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
            // create url
            String url = AppController.END_POINT + "/addfavorite/" + user.getId() + "/";
            for (int i = 0; i < newInterestList.size(); i++) {
                if (i != 0) {
                    // not first item
                    url += ",";
                }
                url += newInterestList.get(i).getId();
            }

            // create and execute request
            JsonReader jsonReader = new JsonReader(url);
            response = jsonReader.sendGetRequest();

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
                // --updated successfully--
                // save them in database
                interestListDAO.open();
                interestListDAO.deleteAll();
                interestListDAO.add(newInterestList);
                interestListDAO.close();

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

    /**
     * overridden method
     */
    @Override
    protected void onDestroy() {
        // stop all running tasks
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }

        super.onDestroy();
    }
}

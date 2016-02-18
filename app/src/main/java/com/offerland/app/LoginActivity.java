package com.offerland.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.UserDAO;
import datamodels.Constants;
import datamodels.User;
import json.JsonReader;
import json.UserHandler;
import utils.InternetUtil;
import utils.ViewUtil;


public class LoginActivity extends ActionBarActivity {
    private View layoutLogin;
    private EditText textEmail;
    private EditText textPassword;
    private CheckBox checkRememberMe;
    private Button buttonLogin;
    private TextView textSignUp;
    private ProgressBar progressBar;

    private ArrayList<AsyncTask> tasks; // used to hold running tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        layoutLogin = findViewById(R.id.layout_login);
        textEmail = (EditText) findViewById(R.id.text_email);
        textPassword = (EditText) findViewById(R.id.text_password);
        checkRememberMe = (CheckBox) findViewById(R.id.check_rememberMe);
        buttonLogin = (Button) findViewById(R.id.button_login);
        textSignUp = (TextView) findViewById(R.id.text_signup);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        tasks = new ArrayList<>();

        // customize hints
        String color = String.format("#%06X", 0xFFFFFF & getResources().getColor(R.color.hint));
        textEmail.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.email_address) + "</font>"));
        textPassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.password) + "</font>"));

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textEmail.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        checkRememberMe.setTypeface(typeface);
        buttonLogin.setTypeface(typeface);
        textSignUp.setTypeface(typeface);

        // add listeners
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute();
            }
        });
        textPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    new LoginTask().execute();
                    return true;
                }
                return false;
            }
        });
        textSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO sign up
            }
        });
    }

    /**
     * sub class, used to send login request
     */
    private class LoginTask extends AsyncTask<Void, Void, Void> {
        private String email;
        private String password;

        private LoginActivity activity;
        private String response;

        private LoginTask() {
            email = textEmail.getText().toString().trim();
            password = textPassword.getText().toString().trim();

            activity = LoginActivity.this;
            tasks.add(this); // save reference to this task to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // validate inputs
            if (email.isEmpty()) {
                textEmail.setText("");
                textEmail.setError(getString(R.string.email_cant_be_empty));
                cancel(true);
                return;
            }

            String pattern = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(email);
            if (!m.find()) {
                textEmail.setError(getString(R.string.email_isnt_valid));
                cancel(true);
                return;
            }

            if (password.isEmpty()) {
                textPassword.setText("");
                textPassword.setError(getString(R.string.password_cant_be_empty));
                cancel(true);
                return;
            }

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textEmail.getWindowToken(), 0);

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
            String url = AppController.END_POINT + "/login";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(2);
            parameters.add(new BasicNameValuePair("email", email));
            parameters.add(new BasicNameValuePair("password", password));

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
            if (response.equals(Constants.JSON_MSG_FALSE)) {
                // invalid email address or password
                showError(R.string.invalid_email_address_or_password);
                return;
            }

            // --response is valid, handle it--
            UserHandler handler = new UserHandler(response);
            User user = handler.handle();

            // check handling operation
            if (user == null) {
                showError(R.string.connection_error_try_again);
                return;
            }

            // --user object is valid--
            user.setPassword(password);
            // check remember me check box
            if (checkRememberMe.isChecked()) {
                // save it in database
                UserDAO dao = new UserDAO(activity);
                dao.open();
                dao.add(user);
                dao.close();

                // register user to GCM
                AppController.getInstance(getApplicationContext()).registerToGCM();
            }
            // save it in runtime
            AppController.getInstance(activity.getApplicationContext()).setActiveUser(user);

            // start update location receiver to update location right now
            long when = System.currentTimeMillis();
            AppController.startLocationUpdater(activity, when);

            // goto main activity
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra(Constants.KEY_FORGET_ME, !checkRememberMe.isChecked());
            startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBar, show);
            ViewUtil.showView(layoutLogin, !show, View.INVISIBLE);
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();
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

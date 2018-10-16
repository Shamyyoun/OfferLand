package com.offerland.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import database.UserDAO;
import datamodels.Constants;
import datamodels.User;
import json.JsonReader;
import receivers.LocationUpdaterReceiver;

/**
 * Created by Shamyyoun on 3/15/2015.
 */
public class AppController extends Application {
    public static final String END_POINT = "http://mahmoudelshamy.com/offerland";
    private static final String PROJECT_NUMBER = "1041277976833";
    public static final long LOCATION_UPDATER_DELAY = 30 * 60 * 1000; // time in milli seconds (default 30 minutes)
    public static final int MAX_LOCATION_UPDATER_TRIES = 3;

    private User activeUser;

    public AppController() {
        super();
    }

    /**
     * method, used to getAll active user from runtime or from SP
     */
    public User getActiveUser() {
        if (activeUser == null) {
            // getAll saved user if exists
            UserDAO userDAO = new UserDAO(getApplicationContext());
            userDAO.open();
            activeUser = userDAO.get();
            userDAO.close();
        }

        return activeUser;
    }

    /**
     * method, used to set active user
     */
    public void setActiveUser(User user) {
        this.activeUser = user;
    }

    /**
     * method used to return current application instance
     */
    public static AppController getInstance(Context context) {
        return (AppController) context.getApplicationContext();
    }

    /**
     * method, used to register user to GCM and send his reg_id to server
     */
    public void registerToGCM() {
        // execute operation in async task
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // getAll reg_id
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    String regId = gcm.register(PROJECT_NUMBER);

                    // check reg_id
                    if (regId != null) {
                        // send reg_id to server
                        JsonReader jsonReader = new JsonReader(AppController.END_POINT + "/update_reg_id");
                        List<NameValuePair> parameters = new ArrayList<>(2);
                        parameters.add(new BasicNameValuePair("user_id", "" + getInstance(getApplicationContext()).getActiveUser().getId()));
                        parameters.add(new BasicNameValuePair("reg_id", regId));
                        jsonReader.sendAsyncPostRequest(parameters);

                        // update user's object in DB
                        getInstance(getApplicationContext()).getActiveUser().setRegId(regId);
                        UserDAO userDAO = new UserDAO(getApplicationContext());
                        userDAO.open();
                        userDAO.update(getInstance(getApplicationContext()).getActiveUser());
                        userDAO.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    /**
     * method, used to start alarm manager to update location
     */
    public static void startLocationUpdater(Context context, long when) {
        Intent mIntent = new Intent(context, LocationUpdaterReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                Constants.RECEIVER_LOCATION_UPDATER, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }
}

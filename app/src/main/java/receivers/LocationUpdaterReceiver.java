package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

import com.offerland.app.AppController;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import components.LocationFinder;
import datamodels.Constants;
import datamodels.User;
import json.JsonReader;
import utils.InternetUtil;

public class LocationUpdaterReceiver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        // check if internet is enabled
        if (InternetUtil.isConnected(context)) {
            // getAll active user
            final User user = AppController.getInstance(context).getActiveUser();

            // validate user
            if (user != null) {
                // getAll location using LocationFinder
                LocationFinder locationFinder = new LocationFinder(context);
                Location location = locationFinder.getLocation();
                locationFinder.stop();


                // send update location request to server
                new UpdateLocationTask(user.getId(), location, AppController.MAX_LOCATION_UPDATER_TRIES).execute();

                // start location updater after static time
                long updateTime = System.currentTimeMillis() + AppController.LOCATION_UPDATER_DELAY;
                AppController.startLocationUpdater(context, updateTime);
            }
        }
    }

    /*
     * async task, used to update user's location in server
     */
    private class UpdateLocationTask extends AsyncTask<Void, Void, Void> {
        private int userId;
        private Location location;
        private int triesCount;

        private double latitude;
        private double longitude;
        private String response;

        private UpdateLocationTask(int userId, Location location, int triesCount) {
            this.userId = userId;
            this.location = location;
            this.triesCount = triesCount;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // getAll lat & long
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

//            Toast.makeText(context, "LAT: " + latitude + " && LNG: " + longitude, Toast.LENGTH_LONG).show();

            //  validate lat & long
            if (latitude == 0 || longitude == 0) {
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json parser
            String url = AppController.END_POINT + "/update_location";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(3);
            parameters.add(new BasicNameValuePair("user_id", "" + userId));
            parameters.add(new BasicNameValuePair("lat", "" + latitude));
            parameters.add(new BasicNameValuePair("long", "" + longitude));

            // execute request
            response = jsonReader.sendPostRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // --check response--
            if (response == null || Constants.JSON_MSG_FALSE.equals(response)) {
                // request has problems, retry if possible
                if (triesCount > 0) {
                    // decrement tries count
                    triesCount--;
                    // retry
                    new UpdateLocationTask(userId, location, triesCount).execute();
                }
            }
        }
    }
}

package components;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationFinder extends Service implements LocationListener {

    private final Context mContext;
    private Location location;
    protected LocationManager locationManager;

    public LocationFinder(Context context) {
        this.mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
    }

    public Location getLocation() {
        try {
            // check if can getAll location using GPS
            if (canGetLocationUsingGPS()) {
                // getAll location using gps provider
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // validate location
                if (location == null) {
                    // getAll location using network provider
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else if (location.getLatitude() == 0 && location.getLongitude() == 0) {
                    // getAll location using network provider
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } else {
                // getAll location using network provider
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception e) {
            // getAll location using network provider
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return location;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocationUsingGPS() {
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // getting both states
        return (isGPSEnabled && isNetworkEnabled);
    }

    /**
     * Function to check GPS enabled
     *
     * @return boolean
     */
    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stop() {
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}

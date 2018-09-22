package com.google.ar.sceneform.samples.solarsystem.Helper;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.sceneform.samples.solarsystem.Activity.PinActivity;
import com.google.ar.sceneform.samples.solarsystem.Service.PlaceService;

import java.util.Locale;

public class LocationHelper {

    private static LocationHelper mInstance;

    private Activity mActivity;

    private Location lastLocation;
    
    private LocationManager locationManager;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    private boolean isGPSEnabled;

    private boolean isNetworkEnabled;

    private boolean locationServiceAvailable;


    private LocationHelper(Activity activity) {
        mActivity = activity;
    }

    public static LocationHelper getInstance(Activity activity) {
        if (mInstance == null)
            mInstance = new LocationHelper(activity);

        return mInstance;
    }

    public LocationListener getLocationListener(LocationHelper locationHelper) {

        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(locationHelper.mActivity, String.format(Locale.getDefault(), "Lat: %f", location.getLatitude()), Toast.LENGTH_SHORT).show();
                locationHelper.lastLocation = location;
                //TODO
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    public void initLocationService() {

        if (ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DemoUtils.requestLocationPermission(mActivity, PinActivity.RC_PERMISSIONS);
        }

        try {
            
            this.locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
                DemoUtils.requestLocationPermission(mActivity, PinActivity.RC_PERMISSIONS);
                return;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, getLocationListener(this));
                if (locationManager != null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, getLocationListener(this));

                if (locationManager != null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        } catch (Exception ex) {
            Log.e(this.getClass().getSimpleName(), ex.getMessage());

        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}

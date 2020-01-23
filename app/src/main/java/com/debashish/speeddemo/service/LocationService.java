package com.debashish.speeddemo.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.debashish.speeddemo.activity.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static long INTERVAL = 1000 * 2;
    private final static long FASTEST_INTERVAL = 1000;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Location myCurrentLocation, lStart, lEnd;
    static double distance = 0;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        return mBinder;
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    locationRequest, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(final Location location) {
        myCurrentLocation = location;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        location.getLongitude() + " " + location.getLatitude(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        if (lStart == null) {
            lStart = lEnd = myCurrentLocation;
        } else {
            lEnd = myCurrentLocation;
        }
        //update information
        updateUI();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        lStart = lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        distance = 0;
    }

    private void updateUI() {
        if (MainActivity.p == 0) {
            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            Log.e("time difference is: ", diff + "");
            lStart = lEnd;
        }
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}
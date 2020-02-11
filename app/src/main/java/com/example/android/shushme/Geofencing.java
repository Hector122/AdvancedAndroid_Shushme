package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

// TODO (1) Create a Geofencing class with a Context and GoogleApiClient constructor that
// initializes a private member ArrayList of Geofences called mGeofenceList
public class Geofencing implements ResultCallback {
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;

    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
        this.mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    // TODO (2) Inside Geofencing, implement a public method called updateGeofencesList that
    // given a PlaceBuffer will create a Geofence object for each Place using Geofence.Builder
    // and add that Geofence to mGeofenceList

    public void updateGeofencesList(PlaceBuffer places) {
        if (places == null || places.getCount() == 0) return;
        for (Place place : places) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setExpirationDuration(1_440_000)
                    .setCircularRegion(place.getLatLng().latitude, place.getLatLng().latitude, 50.0f)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);
        }
    }

    // TODO (3) Inside Geofencing, implement a private helper method called getGeofencingRequest that
    // uses GeofencingRequest.Builder to return a GeofencingRequest object from the Geofence list

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    // TODO (5) Inside Geofencing, implement a private helper method called getGeofencePendingIntent that
    // returns a PendingIntent for the GeofenceBroadcastReceiver class

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofenceList != null) {
            return mGeofencePendingIntent;
        }


        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;

    }
    // TODO (6) Inside Geofencing, implement a public method called registerAllGeofences that
    // registers the GeofencingRequest by calling LocationServices.GeofencingApi.addGeofences
    // using the helper functions getGeofencingRequest() and getGeofencePendingIntent()

    public void registerAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException ex) {
            Log.e("TAG", "security exception: " + ex.getMessage());
        }
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e("TAG", "on Result: " + result.getStatus());
    }

    // TODO (7) Inside Geofencing, implement a public method called unRegisterAllGeofences that
    // unregisters all geofences by calling LocationServices.GeofencingApi.removeGeofences
    // using the helper function getGeofencePendingIntent()
    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException ex) {
            Log.e("TAG", ex.getMessage());
        }
    }
}




package com.example.koki.myplayservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity
implements OnConnectionFailedListener, ConnectionCallbacks {
    private final String TAG = "MyPlayServices";

    // Reference to the Google Play Services client
    protected GoogleApiClient mGoogleApiClient;

    /**
     *  Standard Activity lifecycle methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the GoogleApiClient object. There has to be at
        // least one API added otherwise it throws an error, so
        // we add LocationServices even though this example doesn't
        // actually use it

        Log.i(TAG,"onCreate: Building the GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApiIfAvailable(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart: Connecting to Google Play Services");

        // Connect to Play Services
        GoogleApiAvailability gAPI = GoogleApiAvailability.getInstance();
        int resultCode = gAPI.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            gAPI.getErrorDialog(this, resultCode,1).show();
        }
        else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG,"onStop: Disconnecting from Google Play Services ");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause method called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume method called");
    }

    /**
     * There are the methods that get called by the Play Services library
     * to indicate various lifecycle events.
     */

    @Override
    public void onConnected (Bundle connectionHint) {
        Log.i(TAG,"onConnected: Play services onConnected called");
        if(mGoogleApiClient.hasConnectedApi(Wearable.API)){
            Log.i(TAG,"Wearable Present!");
        }
        else {
            Log.i(TAG,"No Wearable! Ah well, let's continue");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG,"onConnectionFailed: Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: Connection was suspended, cause code is " + cause);
        // Something caused the connection to be lost - usually a network issue
        // Re-establish the connection by calling the connect method again
        mGoogleApiClient.connect();
    }
}

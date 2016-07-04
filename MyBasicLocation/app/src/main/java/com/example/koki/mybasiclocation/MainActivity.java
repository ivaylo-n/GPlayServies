package com.example.koki.mybasiclocation;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

// Import the Play Services namespaces we will need use the Location API
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity
    implements ConnectionCallbacks, OnConnectionFailedListener {
    private  final String TAG = "LOC_SAMPLE_START";

    // member to hold reference to the Play Services client
    protected GoogleApiClient mGoogleApiClient;

    private TextView mLatVal;
    private TextView mLongVal;
    private TextView mAlt;
    private TextView mAccuracy;

    /**
     * Update the location field values in the Activity with the given
     * values in the supplied Location object
     */

    public void setLocationFields(Location loc) {
        if (loc != null) {
            mLatVal.setText(String.format("%f", loc.getLatitude()));
            mLongVal.setText(String.format("%f",loc.getLongitude()));

            if(loc.hasAltitude()) {
                mAlt.setText(String.format("%f",loc.getAltitude()));
            }
            if(loc.hasAccuracy()) {
                mAccuracy.setText(String.format("%f0", loc.getAccuracy()));
            }
        }
    }

    /**
     * Retrieves the last known location. Assumes that permissions are granted.
     */

    private Location getLocation() {
        // TODO: get and return the last know location
        try {
            Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return loc;
        }
        catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Activity Lifecycle methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // build the Play Services client object
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // get references to the user interface fields
        mLatVal = (TextView)findViewById(R.id.latValue);
        mLongVal = (TextView)findViewById(R.id.longValue);
        mAlt = (TextView)findViewById(R.id.altValue);
        mAccuracy = (TextView)findViewById(R.id.accValue);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Called when the user has been prompted at runtime to grand permissions
     */

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] results){
        if (reqCode == 1) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                Location locData = getLocation();
                setLocationFields(locData);
            }
        }
    }

    /**
     *  Google Play Services Lifecycle methods
     */
    public void onConnected(Bundle connectionHint) {
        // If we're running on API 23 or above, we need to ask permission at runtime
        int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            Location locData = getLocation();
            setLocationFields(locData);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode() );
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }
}

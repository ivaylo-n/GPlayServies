package com.example.koki.addresslocationname;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener {
    protected final String TAG = "GET_ADDRESS";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected String mAddress;
    protected boolean mHaveLocPerm;
    private TextView mAddressView;
    private EditText mPlaceName;

    protected AddressReceiver mResultReceiver;

    protected void getAddressFromLoc() {
        if (mGoogleApiClient.isConnected() && mHaveLocPerm) {
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            catch (SecurityException e) {
                Log.d(TAG, "Could not access LocationServices");
            }

            if (mLastLocation != null) {
                // Create the intent service responsible for getting the address.
                Intent intent = new Intent(this, GeocodeService.class);

                intent.setAction(Constants.ACTION_ADDRESS_FROM_LOC);
                intent.putExtra(Constants.RECEIVER_KEY, mResultReceiver);
                intent.putExtra(Constants.LOCATION_KEY, mLastLocation);

                // Start the service. If the service isn't already running, it is instantiated and started
                // (creating a process for it if needed); if it is running then it remains running. The
                // service kills itself automatically once all intents are processed.
                startService(intent);
            }
        }
    }

    protected void getAddressFromName(String name) {
        if (name != null && !name.isEmpty()) {
            // Create the intent service responsible for getting the address.
            Intent intent = new Intent(this, GeocodeService.class);

            intent.setAction(Constants.ACTION_LOC_FROM_ADDR);
            intent.putExtra(Constants.RECEIVER_KEY, mResultReceiver);
            intent.putExtra(Constants.PLACE_NAME_KEY, name);

            // Start the service.
            startService(intent);
        }
    }

    protected void updateUI() {
        mAddressView.setText(mAddress);
    }

    /**
     * On API Level 23 and above, we ask for permissions at runtime. This method is called
     * when the user has approved or denied the permission.
     */
    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] results){
        if (reqCode == 1) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                mHaveLocPerm = true;
            }
        }
    }

    /**
     * Google Play Services Lifecycle methods
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mHaveLocPerm = true;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection was suspended for some reason");
        mGoogleApiClient.connect();
    }

    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLastLocation = null;
        mHaveLocPerm = false;
        mAddress = "";
        mAddressView = (TextView)findViewById(R.id.tvAddress);
        mPlaceName = (EditText)findViewById(R.id.etNamedLoc);

        mResultReceiver = new AddressReceiver(new Handler());

        // Listen for clicks on our Get Address From Location Button
        findViewById(R.id.btnGetAddress).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        getAddressFromLoc();
                    }
                }
        );

        //Listen for clicks on the Get Address From Name
        findViewById(R.id.btnNamedLoc).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String theName = mPlaceName.getText().toString();
                        getAddressFromName(theName);
                    }
                }
        );

        // build the Play Services client object
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Only enable the button if we have a Geocoder installed
        findViewById(R.id.btnGetAddress).setEnabled(Geocoder.isPresent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    class AddressReceiver extends ResultReceiver {
        public AddressReceiver(Handler handler) {
            super(handler);
        }
        /**
         *  Receives data sent from GeocoderService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the intent service.
            mAddress = resultData.getString(Constants.ADDRESS_RESULT_KEY);
            updateUI();
        }
    }
}

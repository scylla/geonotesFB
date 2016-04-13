package com.wordpress.techdoors.geonotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.Response;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenRequest;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.amazonaws.mobileconnectors.lambdainvoker.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;


import org.json.JSONException;
import org.json.JSONObject;


import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private CognitoCachingCredentialsProvider credentialsProvider;
    String LOG = "geonotes.logs";
    String POOL_ID = "ap-northeast-1:cb4c9356-4ab9-4abb-80bf-6b6dc4c69b3e";
    String CUSTOM_PROVIDER_NAME = "Facebook";
    String ROLE_ARN = "arn:aws:iam::276570401165:role/georole";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean isReceiverRegistered;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    // Since this activity is SingleTop, there can only ever be one instance. This variable corresponds to this instance.
    public static Boolean inBackground = true;


    public enum ProviderType{
        NETWORK,
        GPS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        info = (TextView)findViewById(R.id.info);
        loginButton = (LoginButton)findViewById(R.id.login_button);

        Button lambdaButton = (Button)findViewById(R.id.lambda_button);
        Button snsRegButton = (Button)findViewById(R.id.sns_reg);

        lambdaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeLambda();
            }
        });

        snsRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeLambda2();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d("GCM Register", "Token Sent");
                } else {
                    Log.d("GCM Register", "Token Error");
                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken != null) {
            Log.d("Check Login Status", "Already logged in");
//            setAmazonToken();
            getLocation();
            InstanceID instanceID = InstanceID.getInstance(this);
            try {
                String token = instanceID.getToken(getString(R.string.project_number),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i("sas", "GCM Registration Token: " + token);
            } catch (Exception e) {

            }

        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {

                String textStr = "User ID: " + loginResult.getAccessToken().getUserId() + "\n" + "Auth Token: " + loginResult.getAccessToken().getToken();
                info.setText(textStr);

//                new AmazonAsyncCalls().execute();
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
            }

        });



    }

    private class MyLocationListener implements LocationListener {

        String TAG = "From My Loacation Class";
        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }


    private class AmazonAsyncCalls extends AsyncTask<URL, Integer, Long> {

        protected Long doInBackground(URL... urls) {

            String identityId = credentialsProvider.getIdentityId();
            Log.d(LOG, "my ID is " + identityId);

            // Create the request object
            GetOpenIdTokenRequest tokenRequest = new GetOpenIdTokenRequest();
            tokenRequest.setIdentityId(identityId);


            // Initialize the Cognito Sync client
            CognitoSyncManager syncClient = new CognitoSyncManager(
                    getApplicationContext(),
                    Regions.AP_NORTHEAST_1, // Region
                    credentialsProvider);


            // Create a record in a dataset and synchronize with the server
            Dataset dataset = syncClient.openOrCreateDataset("myDataset");
            dataset.put("name", AccessToken.getCurrentAccessToken().getUserId().toString());
            dataset.synchronize(new DefaultSyncCallback() {
                @Override
                public void onSuccess(Dataset dataset, List newRecords) {
                    Log.d(LOG, "added successfully");
                }
            });

            return new Long(0);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            Log.d(LOG, "onPostExecute async class");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        registerReceiver();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isReceiverRegistered = false;
        // Logs 'app deactivate' App Event.
//        AppEventsLogger.deactivateApp(this);
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    protected void getLocation() {
        // get user location
        String provider;

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.equals("NETWORK")){
            Log.d("LocationManager", "My location manager is NETWORK");
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
            Log.d("LocationManager", "My location manager is GPS");
            LocationListener locationListener = new MyLocationListener();

            try {

                GPSTracker gps = new GPSTracker(this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            } catch (SecurityException e) {
                Log.d("LocationManager", "An error occured with location service");
            }

        }

    }

    private void setAmazonToken() {
        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-1:cb4c9356-4ab9-4abb-80bf-6b6dc4c69b3e", // Identity Pool ID
                Regions.AP_NORTHEAST_1 // Region
        );

        Map<String, String> logins = new HashMap<String, String>();
        logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
        credentialsProvider.setLogins(logins);

        CredentialHolder ch = CredentialHolder.getInstance();
        ch.setCredentialHolder(credentialsProvider);

        new AmazonAsyncCalls().execute();
    }

    private void invokeLambda() {



        try {

            new AsyncTask<RequestClass, Void, ResponseClass>() {
            @Override
            protected ResponseClass doInBackground(RequestClass... params) {
                    LambdaInvokerFactory factory = new LambdaInvokerFactory(getApplicationContext(),
                            Regions.AP_NORTHEAST_1, credentialsProvider);

                    LambdaFuncInterface invoker = factory.build(LambdaFuncInterface.class);

                    String result;
                    NameInfo nameInfo = new NameInfo("King", "Kai");

                    result = invoker.androidTest(nameInfo);
                    Log.d("Lambda Execution ", result);

                    return null;

            }

            @Override
            protected void onPostExecute(ResponseClass result) {
                if (result == null) {
                    return;
                }
            }
        }.execute();



        } catch (LambdaFunctionException lfe) {
            Log.e("Lambda Execution ", "Failed to execute echo", lfe);
        }

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("Check Play Services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerForSNS() {

    }


    private void invokeLambda2() {
        try {

            new AsyncTask<RequestClass, Void, ResponseClass>() {
                @Override
                protected ResponseClass doInBackground(RequestClass... params) {


                    LambdaInvokerFactory factory = new LambdaInvokerFactory(getApplicationContext(),
                            Regions.AP_NORTHEAST_1, credentialsProvider);

                    LambdaFuncInterface invoker = factory.build(LambdaFuncInterface.class);

                    String result;
                    GeoTopic geoTopic = new GeoTopic("testTopic");

                    result = invoker.createTopic(geoTopic);
                    Log.d("Lambda Execution ", result);

                    return null;

                }

                @Override
                protected void onPostExecute(ResponseClass result) {
                    if (result == null) {
                        return;
                    }
                }
            }.execute();
        } catch (LambdaFunctionException lfe) {
            Log.e("Lambda Execution ", "Failed to execute echo", lfe);
        }
    }

}

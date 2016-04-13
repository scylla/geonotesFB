package com.wordpress.techdoors.geonotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;

/**
 * Created by amit on 03/04/16.
 */
public class AWSCreateEndPointTask extends AsyncTask<String, Void,  CreatePlatformEndpointResult> {

    Context context;

    public AWSCreateEndPointTask(Context context ){

        super();

        this.context = context;

    }

    @Override

    protected CreatePlatformEndpointResult doInBackground(String[] params ) {

        if(params.length < 3){

            return null;

        }

        String arn = params[0];

        String gcmToken = params[1];

        String userData = params[2];

        try {

            CreatePlatformEndpointRequest request = new CreatePlatformEndpointRequest();

            request.setCustomUserData(userData);

            request.setToken(gcmToken);

            request.setPlatformApplicationArn(arn);

            return AWSManager.getSNSClient().createPlatformEndpoint(request);

        }catch(Exception ex){

            return null;

        }

    }

    @Override

    protected void onPostExecute(CreatePlatformEndpointResult result) {

        if(result != null) {

            SharedPreferences prefs = context.getSharedPreferences( "my_prefs" , Context.MODE_PRIVATE );

            String endpointArn = result.getEndpointArn();

            prefs.edit().putString( context.getString(R.string.endpoint_arn_sns), endpointArn ).apply();

        }

    }

}
package com.wordpress.techdoors.geonotes;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;

import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by amit on 03/04/16.
 */
public class CredentialHolder {

    private static CredentialHolder mInstance = null;
    CognitoCachingCredentialsProvider credentialsProvider = null;

    private CredentialHolder(){

    }

    public static CredentialHolder getInstance(){
        if(mInstance == null)
        {
            mInstance = new CredentialHolder();
        }
        return mInstance;
    }

    public CognitoCachingCredentialsProvider getCredentialHolder(){
        return this.credentialsProvider;
    }

    public void setCredentialHolder(CognitoCachingCredentialsProvider value){
        credentialsProvider = value;
    }

}

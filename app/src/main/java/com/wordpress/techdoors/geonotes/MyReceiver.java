package com.wordpress.techdoors.geonotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "MyReceiver Started..",Toast.LENGTH_SHORT).show();
        Log.v("Debug", "MyReceiver Started..");
        Intent myIntent=new Intent(context,GPSTracker.class);
        context.startService(myIntent);
    }
}

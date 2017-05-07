package com.avaya.sdksampleapp.commpackage;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
//import android.support.v7.app.AppCompatActivity;

import com.avaya.clientservices.media.gui.PlaneViewGroup;


public class BaseActivity extends  Activity{

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();
    private final int REQUEST_PERMISSION_NETWORK_STATE=1;

    private ToastMessageReceiver toastMessageReceiver;
    private PlaneViewGroup planeViewGroup = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Hi","hi");
        //checkPermissions();
        // Configure and create Client when application started
        SDKManager.getInstance(this).setupClientConfiguration(getApplication());

        // Configure and create User
        SDKManager.getInstance(this).setupUserConfiguration();

        // Initialize broadcast receiver for event messages that will be shown in toast
        toastMessageReceiver = new ToastMessageReceiver();
        Log.d(LOG_TAG, "Application started. Opening start fragment 1");
        // Client and User initialization logic implemented inside super class
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Application started. Opening start fragment");
        StartFragment startFragment = new StartFragment();
        getFragmentManager().beginTransaction().replace(R.id.dynamic_view, startFragment).commit();

        // Initialize video view
        planeViewGroup = new PlaneViewGroup(this);
        planeViewGroup.setVisibility(ViewGroup.GONE);

        //Add render on BaseActivity
        addContentView(planeViewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    public PlaneViewGroup getPlaneViewGroup() {
        return planeViewGroup;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        planeViewGroup.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        planeViewGroup.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(toastMessageReceiver, new IntentFilter(SDKManager.MESSAGE_RECEIVER));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(toastMessageReceiver);
    }

    // Android do not guarantee that this method will be called
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");

        // Shutdown client when application destroyed
        SDKManager.getInstance(this).shutdownClient();
    }

    private void showMessageInToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        Fragment currentCallActiveFragment = getFragmentManager().findFragmentByTag(SDKManager.ACTIVE_CALL_FRAGMENT_TAG);
        if (currentCallActiveFragment instanceof CallActiveFragment) {
            // We are on Active call screen and the call is active. We will back from it when the call is ended.
            Log.d(LOG_TAG, "To enable this action add code to support multiple active calls in your application.");
            return;
        }

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            // Navigate to the previous screen if fragment back stack is not empty
            getFragmentManager().popBackStack();
            return;
        } else if (getFragmentManager().getBackStackEntryCount() == 0) {
            // Move app to background instead of closing when tap BACK on start screen
            moveTaskToBack(true);
            return;
        }
        // Use default handler to close keyboard
        super.onBackPressed();
    }

   /* public void checkPermissions(){
        //ACCESS NETWORK STATE
        int permissionCheck = Context.checkSelfPermission(
                this, Manifest.permission.ACCESS_NETWORK_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (Activity.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_NETWORK_STATE)) {
                Toast.makeText(BaseActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();
                //showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE);
            } else {
                Activity.requestPermissions(BaseActivity.this,new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, REQUEST_PERMISSION_NETWORK_STATE);
            }
        } else {
            Toast.makeText(BaseActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }

        //ACCESS WIFI STATE

        //CAMERA

        //INTERNET

        //READ CONTACTS

        //
    }*/

    // Receiver of Broadcast messages. Used to show received messages in TOAST.
    class ToastMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(SDKManager.TOAST_TAG);
            showMessageInToast(message);
        }
    }

}


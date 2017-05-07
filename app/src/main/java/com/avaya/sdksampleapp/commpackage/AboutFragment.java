package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avaya.clientservices.client.Client;

/**
 * AboutFragment is used to display about screen
 */
public class AboutFragment extends Fragment {
    private static final String LOG_TAG = AboutFragment.class.getSimpleName();
    private TextView sdkVersion;
    private TextView sampleAppVersion;
    private String version;
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreateView()");

        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        sdkVersion = (TextView) view.findViewById(R.id.sdk_version_value);
        sampleAppVersion = (TextView) view.findViewById(R.id.sample_app_version_value);
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "Fragment#onStart()");
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "Fragment#onDestroyView()");
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "Fragment#onResume()");
        super.onResume();
        parseVersionInfo();
        sampleAppVersion.setText(version);
        sdkVersion.setText(Client.getVersion());
        getActivity().setTitle(R.string.about);
    }

    private void parseVersionInfo()
    {
        String sdkVersion = Client.getVersion();
        int firstPoint = sdkVersion.indexOf("(");
        int secondPoint =  sdkVersion.indexOf(" ", firstPoint);
        if(firstPoint > -1 && secondPoint > -1) {
            version = sdkVersion.substring(firstPoint + 1, secondPoint);
        }
        else
        {
            version = Client.getVersion();
        }
    }
}
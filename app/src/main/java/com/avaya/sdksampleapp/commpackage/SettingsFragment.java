package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * SettingsFragment is displaying different settings sections and manage navigation between them.
 * For now we have only one option "Call Service" to be configured.
 */
public class SettingsFragment extends Fragment {

    private SettingsCallServiceFragment settingsCallServiceFragment;
    private SettingsAmmServiceFragment settingsAmmServiceFragment;
    private AboutFragment aboutFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adding control listeners
        Button callServiceButton = (Button) view.findViewById(R.id.call_service);
        callServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsCallServiceFragment == null) {
                    // Initializing SettingsCallServiceFragment that is used to display signaling server and user configuration
                    settingsCallServiceFragment = new SettingsCallServiceFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, settingsCallServiceFragment).addToBackStack(null).commit();
            }
        });
        Button ammServiceButton = (Button) view.findViewById(R.id.amm_service);
        ammServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsAmmServiceFragment == null) {
                    // Initializing SettingsAmmServiceFragment that is used to display signaling server and user configuration
                    settingsAmmServiceFragment = new SettingsAmmServiceFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, settingsAmmServiceFragment).addToBackStack(null).commit();
            }
        });
        Button aboutButton = (Button) view.findViewById(R.id.about_button);

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, aboutFragment).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set fragment title
        getActivity().setTitle(R.string.settings_item);
    }
}

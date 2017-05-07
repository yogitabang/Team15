package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.BroadcastReceiver;


/**
 * StartFragment is showing main application menu.
 */
public class StartFragment extends Fragment {

    private CallInitFragment callInitFragment;
    private ContactsFragment contactsFragment;
    private SettingsFragment settingsFragment;
    private CallLogListFragment callLogListFragment;
    private ConversationListFragment conversationListFragment;

    private SDKManager sdkManagerInstance;
    private FragmentReceiver fragmentReceiver;
    private Button contactsButton;
    private Button callFeaturesButton;
    private Button callLogButton;
    private Button messagingButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdkManagerInstance = SDKManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initializing all controls on screen when it ready
        callFeaturesButton = (Button) view.findViewById(R.id.call_features_button);
        //contactsButton = (Button) view.findViewById(R.id.contacts_button);
       // Button settingsButton = (Button) view.findViewById(R.id.settings_button);
       // callLogButton = (Button) view.findViewById(R.id.callLogs_button);
        messagingButton = (Button) view.findViewById(R.id.messaging_button);

        // Adding control listeners
        callFeaturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callInitFragment == null) {
                    callInitFragment = new CallInitFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, callInitFragment,SDKManager.INIT_CALL_FRAGMENT_TAG).addToBackStack(null).commit();
            }
        });

       /* contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactsFragment == null) {
                    contactsFragment = new ContactsFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, contactsFragment).addToBackStack(null).commit();
            }
        });*/

        /*settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, settingsFragment).addToBackStack(null).commit();
            }
        });*/


        /*callLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callLogListFragment == null) {
                    callLogListFragment = new CallLogListFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, callLogListFragment).addToBackStack(null).commit();
            }
        });*/

        messagingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversationListFragment == null) {
                    conversationListFragment = new ConversationListFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, conversationListFragment).addToBackStack(null).commit();
            }
        });

    }

    private void setButtonToggle(boolean status) {
        // Disable buttons for preventing actions if user is not logged in
        callFeaturesButton.setEnabled(status);
        //contactsButton.setEnabled(status);
        //callLogButton.setEnabled(status);
        /////***************??????
        if((status &&
                sdkManagerInstance.isAMMEnabled() &&
                sdkManagerInstance.getUser().getMessagingService().isServiceAvailable())) {
            messagingButton.setEnabled(status);
            sdkManagerInstance.getMessagingManager().retrieveActiveConversations();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initialize broadcast receiver for login status and register receiver when resume to the screen
        fragmentReceiver = new FragmentReceiver();
        getActivity().registerReceiver(fragmentReceiver, new IntentFilter(SDKManager.LOGIN_RECEIVER));
        // Initialize broadcast receiver for Messaging service status and register receiver when resume to the screen
        getActivity().registerReceiver(fragmentReceiver, new IntentFilter(SDKManager.MESSAGING_SERVICE_STATUS_RECEIVER));

        // Check user login status and update UI when resume to the SettingsCallService screen
        setButtonToggle(SDKManager.getInstance(getActivity()).isUserLoggedIn());

        // Set fragment title
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister broadcast receiver each time we are leaving current screen
        getActivity().unregisterReceiver(fragmentReceiver);
    }

    // Receiver of Broadcast messages.
    class FragmentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean loginStatus;
            if(action.equals(SDKManager.LOGIN_RECEIVER)) {
                //Used to update login status when settingsFragment is active.
                loginStatus = intent.getBooleanExtra("loginStatus", false);
                setButtonToggle(loginStatus);
            }
            else if(action.equals(SDKManager.MESSAGING_SERVICE_STATUS_RECEIVER))
            {
                //Used to update the messaging Button status when settingsFragment is active.
                // AMM button should be disabled in the following cases:
                // isAMMEnabled set as false, because AMM settings was not provided by user.
                // isServiceAvailable returns false. Settings is correct but messaging server is unavailable for any reasons.
                loginStatus = intent.getBooleanExtra(SDKManager.MESSAGING_SERVICE_TAG, false);
                if((loginStatus &&
                        SDKManager.getInstance(getActivity()).isAMMEnabled() &&
                        SDKManager.getInstance(getActivity()).getUser().getMessagingService().isServiceAvailable())) {
                    messagingButton.setEnabled(loginStatus);
                    setButtonToggle(loginStatus);
                }
            }

        }
    }
}
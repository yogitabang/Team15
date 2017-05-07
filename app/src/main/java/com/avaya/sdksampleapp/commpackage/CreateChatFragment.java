package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * CreateChatFragmentis used to show "create new chat" screen
 */
public class CreateChatFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private EditText participantAddressEditText;
    private boolean isAbleToResume = true;
    private MessagingManager messagingManager;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        messagingManager = SDKManager.getInstance(getActivity()).getMessagingManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView()");
        return inflater.inflate(R.layout.create_chat_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button createChatButton;
        Log.d(LOG_TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        participantAddressEditText = (EditText) view.findViewById(R.id.enter_participant_address);
        createChatButton = (Button) view.findViewById(R.id.create_chat_button);
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConversationItem newChat = messagingManager.createNewConversation(participantAddressEditText.getText().toString());
                participantAddressEditText.getText().clear();
                ConversationFragment chat = new ConversationFragment();
                chat.setConversation(newChat);
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, chat).addToBackStack(null).commit();
                isAbleToResume = false;
            }
        });
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "onDestroyView()");
        // removing this fragment after closing to prevent the following situation :
        // user tap to Back button and create "new chat screen" is displayed again
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume()");
        super.onResume();
        // Set fragment title
        if (!isAbleToResume) {
            getFragmentManager().popBackStack();
        }
        getActivity().setTitle("Create new chat");
    }
}

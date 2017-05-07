package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.avaya.clientservices.messaging.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * ConversationFragment used to show list of the messages of the active conversation
 */
public class ConversationFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private List<Message> conversationList = new ArrayList<>();
    private ConversationItem item;
    private EditText messageEditText;
    private ChatMessagesListAdapter chatMessagesListAdapter;
    private MessagingManager messagingManager;

    public void setConversation(ConversationItem item) {
        this.item = item;
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        messagingManager = SDKManager.getInstance(getActivity()).getMessagingManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView()");
        return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        messageEditText = (EditText) view.findViewById(R.id.editTextMessage);
        conversationList = item.getMessages();
        ListView conversationListView = (ListView) view.findViewById(R.id.messages_list);
//        // Initializing ListView adapter
        chatMessagesListAdapter = new ChatMessagesListAdapter(getActivity(), R.layout.chat_item, conversationList);
        conversationListView.setAdapter(chatMessagesListAdapter);
        item.setAdapter(chatMessagesListAdapter);
        Button sentButton = (Button) view.findViewById(R.id.send_message_button);
        sentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageEditText.getText().toString();
                if (!text.isEmpty() && messagingManager != null) {
                    messagingManager.createMessage(item.getConversationItem().getId(), text);
                    messageEditText.getText().clear();
                    chatMessagesListAdapter.notifyDataSetChanged();
                }
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
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume()");
        super.onResume();
        // Set fragment title
        getActivity().setTitle("conversation with " + item.name);
    }
}


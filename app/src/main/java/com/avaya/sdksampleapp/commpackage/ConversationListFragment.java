package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * ConversationListFragment used to show list of the active conversations
 */
public class ConversationListFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private List<ConversationItem> conversationItemList;
    private CreateChatFragment createChatFragment;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreateView()");
        conversationItemList = new ArrayList<>();

        return inflater.inflate(R.layout.conversation_list_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        ConversationListAdapter conversationListAdapter;
        Log.d(LOG_TAG, "Fragment#onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        conversationItemList = SDKManager.getInstance(getActivity()).getMessagingManager().getConversationItemList();
        ListView  conversationListView = (ListView) view.findViewById(R.id.converation_list);
        // Initializing ListView adapter
        conversationListAdapter = new ConversationListAdapter(getActivity(), R.layout.conversation_list_item, conversationItemList);
        // set ListView adapter instance to the Messaging Manager,
        // to be able to request update the UI form during handling reports from CSDK
        SDKManager.getInstance(getActivity()).getMessagingManager().setConversationListAdapter(conversationListAdapter);
        conversationListView.setAdapter(conversationListAdapter);
        Button newChatButton = (Button) view.findViewById(R.id.new_chat_button);
        newChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChatFragment = new CreateChatFragment();
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, createChatFragment).addToBackStack(null).commit();
            }
        });
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
        // Set fragment title
        getActivity().setTitle(R.string.messaging);
    }
}

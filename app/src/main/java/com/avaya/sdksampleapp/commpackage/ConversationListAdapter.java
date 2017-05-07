package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.avaya.clientservices.messaging.MessagingCompletionHandler;
import com.avaya.clientservices.messaging.MessagingException;

import java.util.List;

class ConversationListAdapter extends ArrayAdapter<ConversationItem> {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private final List<ConversationItem> conversationItem;
    private final int layoutResourceId;
    private final Activity activity;


    public ConversationListAdapter(Activity activity, int layoutResourceId, List<ConversationItem> items) {
        super(activity, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.conversationItem = items;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ConversationHolder holder;
        if(view == null) {
            //initialise the holder item once for better performance.
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
            holder = new ConversationHolder();
            fillHolder(holder, position, view);
        }
        else
        {
            holder = (ConversationHolder) view.getTag();
            if(holder == null)
            {
                holder = new ConversationHolder();
                fillHolder(holder, position, view);
            }
        }
        holder.item = conversationItem.get(position);
        String temp;
        temp = holder.item.name;
        holder.remoteName.setText(temp);
        return view;
    }

    private void fillHolder(ConversationHolder holder, int position, View view)
    {
        holder.remoteName = (TextView) view.findViewById(R.id.conversation_name);
        // Delete button initialization
        holder.openButton = (Button) view.findViewById(R.id.openchatbutton);
        holder.openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View conversationItem) {
                ConversationItem item = getItem((Integer) conversationItem.getTag());
                ConversationFragment chat = new ConversationFragment();
                chat.setConversation(item);
                activity.getFragmentManager().beginTransaction().replace(R.id.dynamic_view, chat).addToBackStack(null).commit();
            }
        });
        holder.openButton.setTag(position);
        holder.removeButton = (ImageButton) view.findViewById(R.id.delete_conversation);
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View conversationItem) {
                ConversationItem item = getItem((Integer) conversationItem.getTag());
                MessagingManager messagingManager = SDKManager.getInstance(activity).getMessagingManager();
                if (messagingManager != null) {
                    // request the messaging service to leave this conversation via the messaging manager
                    messagingManager.removeConversationFromList(item);
                    item.getConversationItem().leave(new MessagingCompletionHandler() {
                        @Override
                        public void onSuccess() {
                            Log.d(LOG_TAG, "leaveConversation - onSuccess");
                        }

                        @Override
                        public void onError(MessagingException e) {
                            Log.e(LOG_TAG, "leaveConversation - onSuccess");
                        }
                    });
                    notifyDataSetChanged();
                }
            }
        });
        holder.removeButton.setTag(position);
    }

    public static class ConversationHolder {
        ConversationItem item;
        TextView remoteName;
        Button openButton;
        ImageButton removeButton;
    }
}

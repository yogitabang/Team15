package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.avaya.clientservices.common.DataCollectionChangeType;
import com.avaya.clientservices.common.DataRetrievalWatcher;
import com.avaya.clientservices.common.DataRetrievalWatcherListener;
import com.avaya.clientservices.messaging.AddParticipantAddressesCompletionHandler;
import com.avaya.clientservices.messaging.Conversation;
import com.avaya.clientservices.messaging.ConversationListener;
import com.avaya.clientservices.messaging.Message;
import com.avaya.clientservices.messaging.MessagingCompletionHandler;
import com.avaya.clientservices.messaging.MessagingException;
import com.avaya.clientservices.messaging.MessagingLimits;
import com.avaya.clientservices.messaging.MessagingParticipant;
import com.avaya.clientservices.messaging.MessagingService;
import com.avaya.clientservices.messaging.MessagingServiceListener;
import com.avaya.clientservices.messaging.enums.ConversationStatus;
import com.avaya.clientservices.messaging.enums.ConversationType;
import com.avaya.clientservices.messaging.enums.SensitivityLevel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// MessagingManager class is created to implement the basic functionality of the messaging
// and handling of all related events.

public class MessagingManager implements ConversationListener, DataRetrievalWatcherListener<Conversation>, MessagingServiceListener {
    private final List<ConversationItem> conversationItemList;
    private final MessagingService messagingService;
    private final List<Conversation> conversations = new ArrayList<>();
    private final Activity activity;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private DataRetrievalWatcher<Conversation> watcher;
    private ConversationListAdapter conversationListAdapter;


    public MessagingManager(MessagingService service, Activity activity) {
        this.activity = activity;
        conversationItemList = new ArrayList<>();
        messagingService = service;
        messagingService.addListener(this);
        retrieveActiveConversations();
    }
    //Using the SDK messaging service to get list of active conversations
    public void retrieveActiveConversations() {
        //need to create the instance of the DataRetrievalWatcher to get callbacks
        watcher = new DataRetrievalWatcher<>();
        // MessagingManager is implements the DataRetrievalWatcherListener and my be set as listener
        watcher.addListener(this);
        messagingService.retrieveActiveConversations(watcher);
    }

    public List<ConversationItem> getConversationItemList() {
        return conversationItemList;
    }

    // instance of the conversationListAdapter is needed to be able to update UI forms during handling callbacks from SDK.
    public void setConversationListAdapter(ConversationListAdapter adapter)
    {
        conversationListAdapter = adapter;
    }

    public ConversationItem createNewConversation(final String address) {
        Log.d(LOG_TAG, "createNewConversation");
        final ConversationItem item;
        item = new ConversationItem(address);
        conversationItemList.add(item);
        // request the SDK messaging service to create new conversation
        final Conversation newConversation = messagingService.createConversation();
        item.setConversationItem(newConversation);
        if (newConversation != null) {
            newConversation.start(new MessagingCompletionHandler() {
                @Override
                public void onSuccess() {
                    Log.d(LOG_TAG, " newConversation.start - onSuccess");
                    addToConversations(item);
                    final List<String> addresses = new ArrayList<>();
                    addresses.add(address);
                    /*
                    add participant to new conversation using parameter address
                    address should be in the following format - extension@domain
                     */
                    newConversation.addParticipantAddresses(addresses, new AddParticipantAddressesCompletionHandler() {
                        @Override
                        public void onSuccess(List<MessagingParticipant> list) {
                            Log.d(LOG_TAG, " addParticipantAddresses - onSuccess");

                        }

                        @Override
                        public void onError(MessagingException e) {
                            // for example - error returns in case when participant is online(in SIP) but AMM service is not configured
                            Log.d(LOG_TAG, " addParticipantAddresses - onError");
                        }
                    });
                }

                @Override
                public void onError(MessagingException e) {
                    Log.e(LOG_TAG, " newConversation.start - onError");
                }
            });


        }
        return item;
    }

    public void removeConversationFromList(ConversationItem removedConversationItem) {
        if (conversationItemList.contains(removedConversationItem)) {
            conversationItemList.remove(removedConversationItem);
        }
    }


    private Conversation findConversation(String conversationId) {
        Conversation conversation = null;
        for (Conversation item : conversations) {
            if (item.getId().equals(conversationId)) {
                conversation = item;
                break;
            }
        }


        if ((conversation == null)) {
            Log.e(LOG_TAG, "findConversation result is null, conversationId: " + conversationId);
        }
        return conversation;
    }

    private void addToConversations(ConversationItem conversation) {
        if (!conversations.contains(conversation.getConversationItem())) {
            conversations.add(conversation.getConversationItem());
            conversation.getConversationItem().addListener(this);
            conversation.getConversationItem().retrieveMessages(conversation.getDataRetrievalWatcher());
        }
    }

    public void createMessage(String conversationID, String newMessage) {
        Conversation conversation = findConversation(conversationID);
        if(conversation != null) {
            // request the SDK to create instance of the class Message.
            final Message message = conversation.createMessage();
            // set message text and report typing to participant
            message.setBodyAndReportTyping(newMessage, new MessagingCompletionHandler() {
                @Override
                public void onSuccess() {
                    //if participant is ready to receive message - send it.
                    message.send(new MessagingCompletionHandler() {
                        @Override
                        public void onSuccess() {
                            Log.d(LOG_TAG, "sending new Message: " + message.getBody() + " is Success");
                        }

                        @Override
                        public void onError(MessagingException error) {
                            Log.e(LOG_TAG, "sending is failed,  onError was return. Error is: " + error.getMessage());
                        }
                    });
                }

                @Override
                public void onError(MessagingException error) {
                    Log.e(LOG_TAG, "createMessage is failed,  onError was return. Error is: " + error.getMessage());
                }
            });
        }
        else
        {
            Log.d(LOG_TAG, "createMessage is failed, conversation with conversationId: " + conversationID + "  is null");
        }
    }

    private ConversationItem findConversationById(String id) {
        ConversationItem result = null;
        for (ConversationItem item : conversationItemList) {
            if (item.getConversationItem().getId().equals(id)) {
                result = item;
                break;
            }
        }
        return result;
    }
    /*
     * ConversationListener section
     */
    @Override
    public void onConversationActiveStatusChanged(Conversation conversation, boolean newStatus) {
        Log.d(LOG_TAG, "onConversationActiveStatusChanged - " + newStatus);
    }

    @Override
    public void onConversationClosedStatusChanged(Conversation conversation, boolean newStatus) {
        Log.d(LOG_TAG, "onConversationClosedStatusChanged - " + newStatus);
    }

    @Override
    public void onConversationMultiPartyStatusChanged(Conversation conversation, boolean newStatus) {
        Log.d(LOG_TAG, "onConversationMultiPartyStatusChanged - " + newStatus);
    }

    @Override
    public void onConversationLastAccessTimeChanged(Conversation conversation, Date date) {
        Log.d(LOG_TAG, "onConversationLastAccessTimeChanged - " + date);
    }

    @Override
    public void onConversationLastUpdatedTimeChanged(Conversation conversation, Date date) {
        Log.d(LOG_TAG, "onConversationLastUpdatedTimeChanged - " + date);
    }

    @Override
    public void onConversationLatestEntryTimeChanged(Conversation conversation, Date date) {
        Log.d(LOG_TAG, "onConversationLatestEntryTimeChanged - " + date);
    }

    @Override
    public void onConversationStatusChanged(Conversation conversation, ConversationStatus conversationStatus) {
        Log.d(LOG_TAG, "onConversationLatestEntryTimeChanged - " + conversationStatus);
    }

    @Override
    public void onConversationPreviewTextChanged(Conversation conversation, String text) {
        Log.d(LOG_TAG, "onConversationPreviewTextChanged - " + text);
    }

    @Override
    public void onConversationTotalMessageCountChanged(Conversation conversation, int messageCount) {
        Log.d(LOG_TAG, "onConversationTotalMessageCountChanged - " + messageCount);
    }

    @Override
    public void onConversationTotalAttachmentCountChanged(Conversation conversation, int attachmentCount) {
        Log.d(LOG_TAG, "onConversationTotalAttachmentCountChanged - " + attachmentCount);
    }

    @Override
    public void onConversationTotalUnreadMessageCountChanged(Conversation conversation, int unreadMessageCount) {
        Log.d(LOG_TAG, "onConversationTotalUnreadMessageCountChanged - " + unreadMessageCount);
    }

    @Override
    public void onConversationTotalUnreadAttachmentCountChanged(Conversation conversation, int unreadAttachmentCount) {
        Log.d(LOG_TAG, "onConversationTotalUnreadAttachmentCountChanged - " + unreadAttachmentCount);
    }

    @Override
    public void onConversationSensitivityChanged(Conversation conversation, SensitivityLevel sensitivityLevel) {
        Log.d(LOG_TAG, "onConversationSensitivityChanged - " + sensitivityLevel);
    }

    @Override
    public void onConversationSubjectChanged(Conversation conversation, String subject) {
        Log.d(LOG_TAG, "onConversationSubjectChanged - " + subject);
    }

    @Override
    public void onConversationTypeChanged(Conversation conversation, ConversationType conversationType) {
        Log.d(LOG_TAG, "onConversationTypeChanged - " + conversationType);
    }

    @Override
    public void onConversationMessagesAdded(Conversation conversation, List<Message> list) {
        Log.d(LOG_TAG, "onConversationMessagesAdded");
        // report from CSDK - new message was added to existing conversation
        ConversationItem item = findConversationById(conversation.getId());
        if (item != null) {
            for (Message message : list) {
                item.addNewMessage(message);
                Log.d(LOG_TAG, "onConversationMessages List Item - " + message.getBody());
            }
        }
    }

    @Override
    public void onConversationMessagesRemoved(Conversation conversation, List<Message> list) {
        Log.d(LOG_TAG, "onConversationMessagesRemoved - " + list);
    }

    @Override
    public void onConversationParticipantsAdded(Conversation conversation, List<MessagingParticipant> list) {
        Log.d(LOG_TAG, "onConversationParticipantsAdded - " + list);
    }

    @Override
    public void onConversationParticipantsRemoved(Conversation conversation, List<MessagingParticipant> list) {
        Log.d(LOG_TAG, "onConversationParticipantsRemoved - " + list);
    }


    @Override
    public void onConversationCapabilitiesChanged(Conversation conversation) {
        Log.d(LOG_TAG, "onConversationCapabilitiesChanged");
    }


    /*
     * DataRetrievalWatcherListener section
     */
    @Override
    public void onRetrievalProgress(DataRetrievalWatcher<Conversation> dataRetrievalWatcher, boolean b, int i, int i1) {
        Log.d(LOG_TAG, "onRetrievalProgress");
    }

    @Override
    public void onRetrievalCompleted(DataRetrievalWatcher<Conversation> dataRetrievalWatcher) {
        Log.d(LOG_TAG, "onRetrievalCompleted");
    }

    @Override
    public void onRetrievalFailed(DataRetrievalWatcher<Conversation> dataRetrievalWatcher, Exception e) {
        Log.d(LOG_TAG, "onRetrievalFailed");
    }

    @Override
    public void onCollectionChanged(DataRetrievalWatcher<Conversation> watcher, DataCollectionChangeType changeType, List<Conversation> changedItems) {
        Log.d(LOG_TAG, "onCollectionChanged");
        // report from SDK about changes in list of active conversations
        // need to update local conversation list and notify the UI forms about it.
        switch (changeType){
            case ITEMS_ADDED:
            case ITEMS_UPDATED:
                for (Conversation conversation : changedItems) {
                    ConversationItem updatedItem = findConversationById(conversation.getId());
                    if (updatedItem != null) {
                        updatedItem.setConversationItem(conversation);
                    }
                    else
                    {
                        ConversationItem newItem = new ConversationItem(conversation.getId());
                        newItem.setConversationItem(conversation);
                        conversationItemList.add(newItem);
                        addToConversations(newItem);
                        if(conversationListAdapter != null)
                        {
                            conversationListAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
            case ITEMS_DELETED:
                for (Conversation conversation : changedItems) {

                    conversationItemList.remove(conversation);
                }
                if(conversationListAdapter != null) {
                    conversationListAdapter.notifyDataSetChanged();
                }
                break;
            case COLLECTION_CLEARED:
                conversationItemList.clear();
                if(conversationListAdapter != null) {
                    conversationListAdapter.notifyDataSetChanged();
                }
                break;
        }
    }


    /*
     * MessagingServiceListener section
     */
    @Override
    public void onMessagingServiceAvailable(MessagingService messagingService) {
        Log.d(LOG_TAG, "onMessagingServiceAvailable");
        activity.sendBroadcast(new Intent(SDKManager.MESSAGING_SERVICE_STATUS_RECEIVER).putExtra(SDKManager.MESSAGING_SERVICE_TAG, true));
    }

    @Override
    public void onMessagingServiceUnavailable(MessagingService messagingService) {
        Log.d(LOG_TAG, "onMessagingServiceUnavailable");
    }

    @Override
    public void onMessagingServiceCapabilitiesChanged(MessagingService messagingService) {
        Log.d(LOG_TAG, "onMessagingServiceCapabilitiesChanged");
    }

    @Override
    public void onMessagingLimitsChanged(MessagingService messagingService, MessagingLimits messagingLimits) {
        Log.d(LOG_TAG, "onMessagingLimitsChanged");
    }

    @Override
    public void onRoutableDomainsChanged(MessagingService messagingService, List<String> list) {
        Log.d(LOG_TAG, "onRoutableDomainsChanged");
    }

    @Override
    public void onNumberOfConversationsWithUnreadContentChanged(MessagingService messagingService, int i) {
        Log.d(LOG_TAG, "onNumberOfConversationsWithUnreadContentChanged");
    }

    @Override
    public void onNumberOfConversationsWithUnreadContentSinceLastAccessChanged(MessagingService messagingService, int i) {
        Log.d(LOG_TAG, "onNumberOfConversationsWithUnreadContentSinceLastAccessChanged");
        messagingService.retrieveActiveConversations(watcher);
    }

    @Override
    public void onMessagingServiceMatchedContactsChanged(MessagingService messagingService) {
        // report from CSDK. Receives in case when conversation with this user was created by anyone else and new message was sent.
        Log.d(LOG_TAG, "onMessagingServiceMatchedContactsChanged");
    }

    @Override
    public void onMessagingServiceFailed(MessagingService messagingService, MessagingException e) {
        Log.d(LOG_TAG, "onMessagingServiceFailed");
    }
}

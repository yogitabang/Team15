package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.avaya.clientservices.common.DataCollectionChangeType;
import com.avaya.clientservices.common.DataRetrievalWatcher;
import com.avaya.clientservices.common.DataRetrievalWatcherListener;
import com.avaya.clientservices.contact.AccessControlBehavior;
import com.avaya.clientservices.contact.Contact;
import com.avaya.clientservices.contact.ContactService;
import com.avaya.clientservices.contact.ContactSourceType;
import com.avaya.clientservices.presence.Presence;
import com.avaya.clientservices.presence.PresenceCompletionHandler;
import com.avaya.clientservices.presence.PresenceException;
import com.avaya.clientservices.presence.PresenceService;
import com.avaya.clientservices.presence.PresenceSubscriptionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactsFragment is used to show local contacts
 */
public class ContactsFragment extends Fragment implements DataRetrievalWatcherListener<Contact>, PresenceSubscriptionListener {

    private static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private final List<Contact> contacts = new ArrayList<>();
    private ContactService contactService;
    private PresenceService presenceService;
    private ContactsListAdapter contactListAdapter;
    private DataRetrievalWatcher<Contact> contactsWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreate()");
        super.onCreate(savedInstanceState);

        // SDK API call. Getting contact service
        contactService = SDKManager.getInstance(getActivity()).getUser().getContactService();

        // SDK API call. Getting presence service
        presenceService = SDKManager.getInstance(getActivity()).getUser().getPresenceService();

        // Set DataRetrievalWatcher object that will receive all contacts
        contactsWatcher = new DataRetrievalWatcher<>();
        contactsWatcher.addListener(this);
        contactService.getContacts(contactsWatcher, ContactSourceType.ENTERPRISE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreateView()");
        return inflater.inflate(R.layout.contacts_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        // Initializing ListView that is used to show contact list
        ListView contactsListView = (ListView) view.findViewById(R.id.contacts_list);
        // Initializing ListView adapter
        contactListAdapter = new ContactsListAdapter(getActivity(), R.layout.contact_list_item, contacts, contactService);
        contactsListView.setAdapter(contactListAdapter);

        // Initializing add contact button
        Button addContact = (Button) view.findViewById(R.id.add_contact);
        addContact.setEnabled(contactService.getAddContactCapability().isAllowed());
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactDetailsFragment contactDetailsFragment = new ContactDetailsFragment();
                getFragmentManager().beginTransaction().replace(R.id.dynamic_view, contactDetailsFragment).addToBackStack(null).commit();
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

    /*
     * DataRetrievalWatcherListener section
     */

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "Fragment#onResume()");
        super.onResume();
        // Set fragment title
        getActivity().setTitle(R.string.contacts_item);
    }

    @Override
    public void onRetrievalProgress(DataRetrievalWatcher<Contact> dataRetrievalWatcher, boolean b, int i, int i1) {

    }

    @Override
    public void onRetrievalCompleted(DataRetrievalWatcher<Contact> dataRetrievalWatcher) {
        Log.d(LOG_TAG, "onRetrievalCompleted");
        contacts.clear();
        contacts.addAll(dataRetrievalWatcher.getSnapshot());
        contactListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRetrievalFailed(DataRetrievalWatcher<Contact> dataRetrievalWatcher, Exception e) {

    }

    @Override
    public void onCollectionChanged(DataRetrievalWatcher<Contact> dataRetrievalWatcher, DataCollectionChangeType dataCollectionChangeType, List<Contact> list) {
        if (dataRetrievalWatcher == contactsWatcher) {
            Log.d(LOG_TAG, "onCollectionChanged. " + dataCollectionChangeType);
            switch (dataCollectionChangeType) {
                case COLLECTION_CLEARED:
                    stopPresence(contacts);
                    contacts.clear();
                    break;
                case ITEMS_ADDED:
                    startPresence(list);
                    contacts.addAll(list);
                    break;
                case ITEMS_DELETED:
                    stopPresence(list);
                    for (Contact contact : list) {
                        contacts.remove(contact);
                    }
                    break;
                case ITEMS_UPDATED:
                    break;
            }
        } else {
            if (dataCollectionChangeType == DataCollectionChangeType.ITEMS_ADDED) {
                startPresence(list);
                contacts.addAll(list);
            }
        }
        contactListAdapter.notifyDataSetChanged();
    }

    void startPresence(List<Contact> list) {
        for (Contact contact : list) {
            if (presenceService != null && presenceService.isServiceAvailable()) {
                contact.addPresenceListener(this);
                contact.startPresence(AccessControlBehavior.NONE, new PresenceCompletionHandler() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Presence for the contact has been started");
                    }

                    @Override
                    public void onError(PresenceException e) {
                        Log.e(LOG_TAG, "Presence cannot be started. " + e);
                    }
                });
            }
        }
    }

    void stopPresence(List<Contact> list) {
        for (Contact contact : list) {
            if (presenceService != null && presenceService.isServiceAvailable()) {
                contact.removePresenceListener(this);
                contact.stopPresence(new PresenceCompletionHandler() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Presence for the contact has been stopped");
                    }

                    @Override
                    public void onError(PresenceException e) {
                        Log.e(LOG_TAG, "Presence cannot be stopped. " + e);
                    }
                });
            }
        }
    }

    @Override
    public void onPresenceUpdated(Contact contact, Presence presence) {
        Log.d(LOG_TAG, "Presence for the contact has been updated");
        contactListAdapter.notifyDataSetChanged();
    }
}
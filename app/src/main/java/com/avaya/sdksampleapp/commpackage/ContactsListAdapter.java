package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avaya.clientservices.contact.Contact;
import com.avaya.clientservices.contact.ContactCompletionHandler;
import com.avaya.clientservices.contact.ContactException;
import com.avaya.clientservices.contact.ContactService;
import com.avaya.clientservices.contact.fields.ContactEmailAddressField;
import com.avaya.clientservices.contact.fields.ContactPhoneField;
import com.avaya.clientservices.contact.fields.ContactPhoneNumberType;
import com.avaya.clientservices.presence.PresenceState;

import java.util.ArrayList;
import java.util.List;

public class ContactsListAdapter extends ArrayAdapter<Contact> {

    private static final String LOG_TAG = ContactsListAdapter.class.getSimpleName();

    private List<Contact> items = new ArrayList<>();
    private int layoutResourceId;

    private ContactService contactService;

    public ContactsListAdapter(Activity activity, int layoutResourceId, List<Contact> items, ContactService contactService) {
        super(activity, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.items = items;
        this.contactService = contactService;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ContactHolder holder;
        if (view == null) {
            // Initialise the holder item once for better perfomance.
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
            holder = new ContactHolder();
            initContactHolder(holder, view);
        } else {
            holder = (ContactHolder) view.getTag();
            if (holder == null) {
                holder = new ContactHolder();
                initContactHolder(holder, view);
            }
        }
        int size = items.size();
        if (position > size - 1) {
            return view;
        }
        holder.item = items.get(position);
        String temp;

        temp = holder.item.getNativeLastName().getValue() + ", " + holder.item.getNativeFirstName().getValue();
        holder.displayName.setText(temp);

        // Find phones and set default or work number or first one
        List<ContactPhoneField> phoneNumberList = holder.item.getPhoneNumbers().getValues();
        ContactPhoneField firstWorkNumberItem = null;
        for (final ContactPhoneField phoneNumberItem : phoneNumberList) {
            if (phoneNumberItem.isDefault()) {
                holder.contactNumber.setText(phoneNumberItem.getPhoneNumber());
            }

            if ((firstWorkNumberItem == null) && (phoneNumberItem.getType() == ContactPhoneNumberType.WORK)) {
                firstWorkNumberItem = phoneNumberItem;
            }
        }

        if (firstWorkNumberItem != null) {
            holder.contactNumber.setText(firstWorkNumberItem.getPhoneNumber());
        }

        if (!phoneNumberList.isEmpty()) {
            holder.contactNumber.setText(phoneNumberList.get(0).getPhoneNumber());
        } else {
            holder.contactNumber.setText("");
        }

        // Set email
        List<ContactEmailAddressField> emailAddressesList = holder.item.getEmailAddresses().getValues();
        if (!emailAddressesList.isEmpty()) {
            temp = emailAddressesList.get(0).getAddress();
            holder.email.setText(temp);
        } else {
            holder.email.setText("");
        }

        // Delete button initialization
        if (holder.item.getDeleteContactCapability().isAllowed()) {
            holder.deleteContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View deleteContact) {
                    final Contact item = getItem((Integer) deleteContact.getTag());
                    // uses the function "deleteContact" via contactService to remove the contact
                    contactService.deleteContact(item, new ContactCompletionHandler() {
                        // local implementation of the ContactCompletionHandler
                        @Override
                        public void onSuccess() {
                            Log.d(LOG_TAG, "Contact has been deleted");
                        }

                        @Override
                        public void onError(ContactException e) {
                            Log.e(LOG_TAG, "Contact cannot be deleted. " + e);
                        }
                    });
                }
            });
            holder.deleteContact.setTag(position);
        } else {
            holder.deleteContact.setVisibility(View.INVISIBLE);
        }

        // set presence
        PresenceState presenceState = holder.item.getPresence().getOverallState();
        holder.presenceState.setImageDrawable(getPresenceImage(presenceState));

        return view;
    }

    public static class ContactHolder {
        Contact item;
        TextView displayName;
        TextView email;
        TextView contactNumber;
        ImageButton deleteContact;
        ImageView presenceState;
    }

    private void initContactHolder(ContactHolder holder, View view) {
        holder.displayName = (TextView) view.findViewById(R.id.contact_name);
        holder.contactNumber = (TextView) view.findViewById(R.id.contact_number);
        holder.email = (TextView) view.findViewById(R.id.contact_email);
        holder.deleteContact = (ImageButton) view.findViewById(R.id.delete_contact);
        holder.presenceState = (ImageView) view.findViewById(R.id.contact_presence);
    }

    private Drawable getPresenceImage(PresenceState presenceState) {
        int status = 0;
        switch (presenceState) {
            case AVAILABLE:
                status = R.drawable.available;
                break;
            case AWAY:
                status = R.drawable.away;
                break;
            case BUSY:
                status = R.drawable.busy;
                break;
            case DO_NOT_DISTURB:
                status = R.drawable.dnd;
                break;
            case OFFLINE:
                status = R.drawable.offline;
                break;
            case ON_A_CALL:
                status = R.drawable.onacall;
                break;
            case OUT_OF_OFFICE:
                status = R.drawable.outofoffice;
                break;
            case UNKNOWN:
                status = R.drawable.unavailable;
                break;
            case UNSPECIFIED:
                status = R.drawable.def;
                break;
        }
        return getContext().getResources().getDrawable(status);
    }
}

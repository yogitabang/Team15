package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.avaya.clientservices.contact.AddContactCompletionHandler;
import com.avaya.clientservices.contact.Contact;
import com.avaya.clientservices.contact.ContactException;
import com.avaya.clientservices.contact.ContactService;
import com.avaya.clientservices.contact.EditableContact;
import com.avaya.clientservices.contact.fields.ContactEmailAddressType;
import com.avaya.clientservices.contact.fields.ContactPhoneNumberType;
import com.avaya.clientservices.contact.fields.EditableContactEmailAddressField;
import com.avaya.clientservices.contact.fields.EditableContactPhoneField;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactDetailsFragment is used to add contact
 */
public class ContactDetailsFragment extends Fragment {

    private static final String LOG_TAG = ContactDetailsFragment.class.getSimpleName();

    private ContactService contactService;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private Button addContact;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contactService = SDKManager.getInstance(getActivity()).getUser().getContactService();

        firstNameEditText = (EditText) view.findViewById(R.id.contact_first_name);
        lastNameEditText = (EditText) view.findViewById(R.id.contact_last_name);
        phoneEditText = (EditText) view.findViewById(R.id.contact_phone);
        emailEditText = (EditText) view.findViewById(R.id.contact_email_address);

        // Adding control listeners
        addContact = (Button) view.findViewById(R.id.contact_add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do not enable button when adding in progress
                addContact.setEnabled(false);

                addContactFromInfo();
            }
        });
    }

    private void addContactFromInfo() {
        // Create new editable contact
        EditableContact editableContact = contactService.createEditableContact();

        // Set name
        editableContact.getNativeFirstName().setValue(firstNameEditText.getText().toString());
        editableContact.getNativeLastName().setValue(lastNameEditText.getText().toString());

        // Create phone item as work phone
        EditableContactPhoneField contactPhoneField = new EditableContactPhoneField();
        contactPhoneField.setPhoneNumber(phoneEditText.getText().toString());
        contactPhoneField.setType(ContactPhoneNumberType.WORK);
        contactPhoneField.setDefault(true);

        // Add created phone item to array and set it for editable contact
        ArrayList<EditableContactPhoneField> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(contactPhoneField);
        editableContact.getPhoneNumbers().setValues(phoneNumbers);

        // Create email item as work email
        EditableContactEmailAddressField contactEmailAddressField = new EditableContactEmailAddressField();
        contactEmailAddressField.setAddress(emailEditText.getText().toString());
        contactEmailAddressField.setType(ContactEmailAddressType.WORK);

        // Add created email item to array and set it for editable contact
        List<EditableContactEmailAddressField> emailAddresses = new ArrayList<>();
        emailAddresses.add(contactEmailAddressField);
        editableContact.getEmailAddresses().setValues(emailAddresses);

        contactService.addContact(editableContact, new AddContactCompletionHandler() {
            @Override
            public void onSuccess(Contact contact, boolean b) {
                Log.d(LOG_TAG, "Contact has been added");
                getFragmentManager().popBackStack();
            }

            @Override
            public void onError(ContactException e) {
                Log.e(LOG_TAG, "Contact cannot be added. " + e);
                addContact.setEnabled(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set fragment title
        getActivity().setTitle(R.string.add_contact);
    }
}

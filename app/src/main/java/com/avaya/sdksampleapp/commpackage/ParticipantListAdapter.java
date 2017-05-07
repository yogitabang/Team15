package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.avaya.clientservices.call.CallCompletionHandler;
import com.avaya.clientservices.call.CallException;
import com.avaya.clientservices.call.conference.ActiveParticipant;

import java.util.ArrayList;
import java.util.List;

/**
 * ParticipantListAdapter is used to show participants in conference call
 */
class ParticipantListAdapter extends ArrayAdapter<ActiveParticipant> {

    private static final String LOG_TAG = ParticipantListAdapter.class.getSimpleName();

    private List<ActiveParticipant> items = new ArrayList<>();
    private final int layoutResourceId;

    public ParticipantListAdapter(Activity activity, int layoutResourceId, List<ActiveParticipant> items) {
        super(activity, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.items = items;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        view = inflater.inflate(layoutResourceId, parent, false);

        ParticipantHolder holder = new ParticipantHolder();
        holder.activeParticipant = items.get(position);
        holder.name = (TextView) view.findViewById(R.id.participant_name);
        holder.name.setText(holder.activeParticipant.getDisplayName());

        holder.muteState = (Switch) view.findViewById(R.id.mute_state);
        // Set state for the mute button
        if (holder.activeParticipant.getMuteAudioCapability().isAllowed()) {
            // Mute is allowed
            holder.muteState.setChecked(false);
        } else if (holder.activeParticipant.getUnmuteAudioCapability().isAllowed()) {
            // Unmute is allowed
            holder.muteState.setChecked(true);
        } else {
            // Nothing is allowed
            holder.muteState.setEnabled(false);
        }

        // Uses for handling of mute/unmute participant action
        final CallCompletionHandler callCompletionHandler = new CallCompletionHandler() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Participant audio state changing started");
            }

            @Override
            public void onError(CallException e) {
                Log.e(LOG_TAG, "Participant audio state changing cannot be done. Exception: " + e.getError());
            }
        };

        holder.muteState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton mute, boolean isChecked) {
                ActiveParticipant activeParticipant = getItem((Integer) mute.getTag());
                // Mute/unmute participants
                if (activeParticipant != null && isChecked) {
                    activeParticipant.mute(callCompletionHandler);
                } else {
                    activeParticipant.unmute(callCompletionHandler);
                }
            }
        });
        holder.muteState.setTag(position);

        return view;
    }

    public static class ParticipantHolder {
        ActiveParticipant activeParticipant;
        TextView name;
        Switch muteState;
    }
}

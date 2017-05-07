package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.avaya.clientservices.call.Call;
import com.avaya.clientservices.call.CallCompletionHandler;
import com.avaya.clientservices.call.CallException;
import com.avaya.clientservices.call.ConferenceConnectionError;
import com.avaya.clientservices.call.conference.ActiveParticipant;
import com.avaya.clientservices.call.conference.ActiveParticipantListener;
import com.avaya.clientservices.call.conference.Conference;
import com.avaya.clientservices.call.conference.ConferenceEncryptionStatus;
import com.avaya.clientservices.call.conference.ConferenceListener;
import com.avaya.clientservices.call.conference.ConferenceRecordingStatus;
import com.avaya.clientservices.call.conference.ConferenceStreamingStatus;
import com.avaya.clientservices.call.conference.DroppedParticipant;
import com.avaya.clientservices.call.conference.Participant;
import com.avaya.clientservices.call.conference.ParticipantConnectionStatus;
import com.avaya.clientservices.call.conference.ParticipantMediaStatus;
import com.avaya.clientservices.call.conference.PendingParticipant;
import com.avaya.clientservices.call.conference.VideoLayout;
import com.avaya.clientservices.collaboration.Collaboration;
import com.avaya.clientservices.collaboration.CollaborationFailure;
import com.avaya.clientservices.collaboration.CollaborationService;
import com.avaya.clientservices.collaboration.CollaborationServiceListener;
import com.avaya.clientservices.collaboration.contentsharing.ContentSharing;
import com.avaya.clientservices.collaboration.contentsharing.ContentSharingListener;
import com.avaya.clientservices.collaboration.contentsharing.ContentSharingRenderer;
import com.avaya.clientservices.collaboration.drawing.Point;
import com.avaya.clientservices.collaboration.drawing.Size;
import com.avaya.clientservices.common.DataCollectionChangeType;
import com.avaya.clientservices.media.AudioDevice;
import com.avaya.clientservices.media.AudioInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * CallActiveFragment is used to display active call
 */
public class CallActiveFragment extends Fragment implements ActiveParticipantListener, CollaborationServiceListener, ContentSharingListener {

    private static final String LOG_TAG = CallActiveFragment.class.getSimpleName();

    private int callId;
    private boolean isVideoCall;
    private final List<ActiveParticipant> participantsDataSet = new ArrayList<>();
    private ParticipantListAdapter conferenceParticipantListAdapter;
    private AudioDevice defaultAudioDevice;
    private AudioDevice speakerAudioDevice;

    private Timer durationTimer;
    private static int count = 0;

    private static final long TIMER_INITIAL_DELAY_MS = 1000;
    private static final long TIMER_STEP_PERIOD_MS = 1000;
    private static final long CALL_ENDED_EXIT_DELAY_MS = 2000;

    private TextView callState;
    private TextView callDuration;
    private ListView participantList;

    private ViewGroup videoFrame;

    private Button endCall;
    private CheckBox muteAudio;
    private CheckBox speakerphone;
    private Switch lockMeeting;
    private LinearLayout lockMeetingLayout;
    private CompoundButton.OnCheckedChangeListener mLockMeetingListener;
    private EditText dtmfDigits;
    private SDKManager sdkManagerInstance;
    private AudioInterface audioInterface;

    private VideoFrameFragment videoFrameFragment;
    private CallEventsReceiver callEventsReceiver;

    private boolean isFragmentPaused;

    private Collaboration collaborationInstance;
    private ContentSharingRenderer contentSharingRenderer;
    private CollaborationService collaborationService;
    private ContentSharingFragment contentSharingFragment;
    private boolean isWebCollaborationActive = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Fragment#onCreate");
        // Initialize broadcast receiver for event messages that will be shown in toast
        callEventsReceiver = new CallEventsReceiver();

        callId = getArguments().getInt(SDKManager.CALL_ID);
        isVideoCall = getArguments().getBoolean(SDKManager.IS_VIDEO_CALL, false);
        // Get instance of SDKManager
        sdkManagerInstance = SDKManager.getInstance(getActivity());
        audioInterface = sdkManagerInstance.getMediaServiceInstance()
                .getAudioInterface();
        collaborationService = SDKManager.getInstance(getActivity()).getUser().getCollaborationService();
        if (collaborationService != null) {
            collaborationService.addListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment#onCreateView");
        return inflater.inflate(R.layout.active_call_fragment, container, false);
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "Fragment#onDestroyView");
        // We should end all active calls if application destroys. It should be done in VOIP
        // service. Remove this code when you implement it in your application
        if (!isWebCollaborationActive) {
            endCall(callId);
        }
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "Fragment#onViewCreated");

        callDuration = (TextView) view.findViewById(R.id.call_duration);
        participantList = (ListView) view.findViewById(R.id.participants_list);
        callState = (TextView) view.findViewById(R.id.call_state);
        videoFrame = (ViewGroup) view.findViewById(R.id.video_frame);

        if (isVideoCall) {
            upgradeToVideo();
        }

        // Send DTMF view initialization
        dtmfDigits = (EditText) view.findViewById(R.id.dtmf_digits);
        dtmfDigits.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                final int length = s.length();
                if (length > 0) {
                    // get last inputted symbol from EditText
                    char digit = s.charAt(length - 1);
                    // get the call
                    Call call = sdkManagerInstance.getCallWrapperByCallId(callId).getCall();
                    // Send the digit to the call
                    sdkManagerInstance.sendDTMF(call, digit);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Mute microphone button initialization
        muteAudio = (CheckBox) view.findViewById(R.id.mute_call_switch);
        muteAudio.setChecked(isMuted(callId));
        muteAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View muteAudio) {
                muteCall(callId, ((CheckBox) muteAudio).isChecked());
            }
        });

        // Speakerphone button initialization
        speakerphone = (CheckBox) view.findViewById(R.id.speakerphone_switch);
        //save active device to set it again when the call is ended
        defaultAudioDevice = audioInterface.getActiveDevice();
        //check that speaker is available from the list of supported devices
        boolean isSpeakerAvailable = false;
        List<AudioDevice> audioDevices = audioInterface.getDevices();
        for (AudioDevice audioDevice : audioDevices) {
            if (audioDevice.getType() == AudioDevice.Type.SPEAKER) {
                // check that current device is not speaker
                if (audioDevice != defaultAudioDevice) {
                    isSpeakerAvailable = true;
                    speakerAudioDevice = audioDevice;
                    break;
                }
                break;
            }
        }

        speakerphone.setEnabled(isSpeakerAvailable);
        speakerphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View speakerphone) {
                if (((CheckBox) speakerphone).isChecked()) {
                    audioInterface.setUserRequestedDevice(speakerAudioDevice);
                } else {
                    audioInterface.setUserRequestedDevice(defaultAudioDevice);
                }
            }
        });

        // Lock meeting button initialization
        lockMeetingLayout = (LinearLayout) view.findViewById(R.id.lock_meeting_layout);
        lockMeeting = (Switch) view.findViewById(R.id.lock_meeting_switch);
        mLockMeetingListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton lockMeeting, boolean isChecked) {
                lockConference(callId, isChecked);
            }
        };
        lockMeeting.setOnCheckedChangeListener(mLockMeetingListener);

        // End call button initialization
        endCall = (Button) view.findViewById(R.id.end_call);
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall(callId);
            }
        });
    }

    private void muteCall(int callId, boolean isChecked) {
        // Check if the call is not removed yet
        CallWrapper callWrapper = sdkManagerInstance.getCallWrapperByCallId(callId);
        if (callWrapper != null) {
            Call call = callWrapper.getCall();
            if (call != null) {
                // Mute the call if it is checked and unmute the call if it is unchecked
                call.muteAudio(isChecked, new CallCompletionHandler() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Audio mute started");
                        // Audio mute status chnaging handled in onCallAudioMuteStatusChanged
                    }

                    @Override
                    public void onError(CallException e) {
                        Log.e(LOG_TAG, "Audio mute cannot be done. Exception: " + e.getError());
                        muteAudio.toggle();
                    }
                });
            }
        }
    }

    private boolean isMuted(int callId) {
        // Check if the call is not removed yet
        CallWrapper callWrapper = sdkManagerInstance.getCallWrapperByCallId(callId);
        if (callWrapper != null) {
            Call call = callWrapper.getCall();
            return call.isAudioMuted();
        }
        return false;
    }

    private void lockConference(int callId, boolean isChecked) {
        // Check if the call is not removed yet
        CallWrapper callWrapper = sdkManagerInstance.getCallWrapperByCallId(callId);
        if (callWrapper != null) {
            Call call = callWrapper.getCall();
            if (call != null) {
                call.getConference().setLocked(isChecked, new CallCompletionHandler() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Lock conference started");
                    }

                    @Override
                    public void onError(CallException e) {
                        Log.e(LOG_TAG, "Lock conference cannot be done. Exception: " + e.getError());
                        lockMeeting.setOnCheckedChangeListener(null);
                        lockMeeting.toggle();
                        lockMeeting.setOnCheckedChangeListener(mLockMeetingListener);
                    }
                });
            }
        }
    }

    private void endCall(int callId) {
        // Clean up video resources
        destroyVideoFragment();

        // Check if the call is not removed yet
        CallWrapper callWrapper = sdkManagerInstance.getCallWrapperByCallId(callId);
        if (callWrapper != null) {
            Call call = callWrapper.getCall();
            if (call != null) {
                // End the call
                call.end();
            }
        }
    }

    private void updateRemoteDisplayName(Call call) {
        // Update far-end name in the Participants view for P2P call
        if (call != null && !call.isConference()) {
            participantList.setAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    Collections.singletonList(call.getRemoteDisplayName())));
        }
    }

    private void updateConferenceParticipants(Conference conference) {
        if (conferenceParticipantListAdapter == null) {
            conferenceParticipantListAdapter = new ParticipantListAdapter(getActivity(), R.layout.participant_list_item, participantsDataSet);
        }
        participantList.setAdapter(conferenceParticipantListAdapter);
        // Get all participants and update participant view
        participantsDataSet.clear();
        List<ActiveParticipant> activeParticipants = conference.getParticipants();
        participantsDataSet.addAll(activeParticipants);
        conferenceParticipantListAdapter.notifyDataSetChanged();
    }

    private void addListenersForParticipants(Collection<ActiveParticipant> list) {
        // Subscribe to get participants updates and update participants list if participant's audio
        // status will be changed (onParticipantAudioStatusChanged)
        for (ActiveParticipant participant : list) {
            participant.addListener(this);
        }
    }

    private void removeListenersForParticipants(Collection<ActiveParticipant> list) {
        for (ActiveParticipant participant : list) {
            participant.removeListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Fragment#onResume");
        isFragmentPaused = false;
        // Set fragment title
        getActivity().setTitle(R.string.active_call);
        // Register receiver for call events
        getActivity().registerReceiver(callEventsReceiver,
                new IntentFilter(SDKManager.CALL_EVENTS_RECEIVER));

        // Destroy video frame if both video streams were stopped while the fragment was not active
        CallWrapper callWrapper = sdkManagerInstance.getCallWrapperByCallId(callId);
        if (callWrapper != null && !callWrapper.isLocalVideoActive() && !callWrapper.isRemoteVideoActive()) {
            destroyVideoFragment();
        }
        // Check if call ended to close current fragment if it was finished while fragment was not active.
        if (sdkManagerInstance.getCallWrapperByCallId(callId) == null) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Fragment#onPause");
        isFragmentPaused = true;
        // Unregister broadcast receiver when leaving current screen
        getActivity().unregisterReceiver(callEventsReceiver);
    }

    // Upgrade call from Audio to Video
    private void upgradeToVideo() {
        Log.d(LOG_TAG, "Upgrading audio call to video");
        videoFrame.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putInt(SDKManager.CALL_ID, callId);
        videoFrameFragment = new VideoFrameFragment();
        videoFrameFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.video_frame, videoFrameFragment).commit();
    }

    // Downgrade call from Video to Audio
    private void destroyVideoFragment() {
        Log.d(LOG_TAG, "Destroying video frame");
        if (videoFrameFragment != null) {
            getFragmentManager().beginTransaction().detach(videoFrameFragment).commit();
            videoFrameFragment = null;
        }
        videoFrame.setVisibility(View.GONE);
    }

    //CollaborationServiceListener section
    @Override
    public void onCollaborationServiceCollaborationRemoved(CollaborationService collaborationService, Collaboration collaboration) {
        Log.d(LOG_TAG, "onCollaborationServiceCollaborationRemoved");
        if (collaborationInstance.getCallId() == collaboration.getCallId()) {
            collaborationInstance.getContentSharing().removeListener(this);
            collaborationInstance.getContentSharing().removeListener(contentSharingRenderer);
            collaborationInstance = null;
            isWebCollaborationActive = false;
        }
    }

    @Override
    public void onCollaborationServiceCollaborationCreationSucceeded(CollaborationService collaborationService, Collaboration collaboration) {
        Log.d(LOG_TAG, "onCollaborationServiceCollaborationCreationSucceeded");
        collaborationInstance = collaboration;
        SDKManager manger = SDKManager.getInstance(getActivity());
        contentSharingRenderer = manger.getContentSharingListener();
        if(contentSharingRenderer == null) {
            contentSharingRenderer = new ContentSharingRenderer();
            collaborationInstance.getContentSharing().addListener(contentSharingRenderer);
        }
        collaborationInstance.getContentSharing().addListener(this);
        manger.addContentSharingListener(contentSharingRenderer);
        isWebCollaborationActive = true;
    }

    @Override
    public void onCollaborationServiceCollaborationCreationFailed(CollaborationService collaborationService, CollaborationFailure collaborationFailure) {
        Log.d(LOG_TAG, "onCollaborationServiceCollaborationCreationFailed");
    }

    @Override
    public void onCollaborationServiceCollaborationCreationFailed(CollaborationService collaborationService) {
        Log.d(LOG_TAG, "onCollaborationServiceCollaborationCreationFailed");
    }


    //ContentSharingListener section
    @Override
    public void onContentSharingStarted(ContentSharing contentSharing, Participant participant) {
        if (contentSharingFragment == null) {
            contentSharingFragment = new ContentSharingFragment();
        }
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager != null) {
            fragmentManager.beginTransaction().replace(R.id.dynamic_view, contentSharingFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void onContentSharingEnded(ContentSharing contentSharing) {
        Log.d(LOG_TAG, "onContentSharingEnded");
    }

    @Override
    public void onContentSharingPaused(ContentSharing contentSharing) {
        Log.d(LOG_TAG, "onContentSharingPaused");
    }

    @Override
    public void onContentSharingResumed(ContentSharing contentSharing) {
        Log.d(LOG_TAG, "onContentSharingResumed");
    }

    @Override
    public void onCursorReceived(ContentSharing contentSharing, Point point) {
        Log.d(LOG_TAG, "onCursorReceived");
    }

    @Override
    public void onSharingFrameReceived(ContentSharing contentSharing, Size size) {
        Log.d(LOG_TAG, "onSharingFrameReceived");
    }

    // Receiver of Broadcast messages. Used to handle call events.
    class CallEventsReceiver extends BroadcastReceiver implements ConferenceListener {
        @Override
        public void onReceive(Context context, Intent intent) {
            Call call = null;
            CallWrapper callWrapper = sdkManagerInstance.getCallWrapperByCallId(callId);
            if (callWrapper != null) {
                call = callWrapper.getCall();
            }

            String message = intent.getStringExtra(SDKManager.CALL_EVENT_TAG);
            Log.d(LOG_TAG, "Received event: " + message);
            switch (message) {
                case SDKManager.CALL_EVENT_RINGING:
                    // Update call state
                    callState.setText(R.string.ringing);
                    // Update participants view
                    updateRemoteDisplayName(call);
                    break;
                case SDKManager.CALL_EVENT_ESTABLISHED:
                    // Update call state
                    callState.setText(R.string.established);
                    // Allow input DTMF
                    dtmfDigits.setEnabled(true);
                    // Start call duration timer
                    durationTimer = new Timer();
                    durationTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String result = String
                                                .format("%02d:%02d", (count / 60) % 60, count % 60);
                                        callDuration.setText(result);
                                        count++;
                                    }

                                });
                            }
                        }
                    }, TIMER_INITIAL_DELAY_MS, TIMER_STEP_PERIOD_MS);
                    // Update participants view
                    if (call != null) {
                        if (call.isConference()) {
                            Conference conference = call.getConference();
                            // Subscribe to conference events
                            conference.addListener(this);
                            updateConferenceParticipants(conference);
                        } else {
                            updateRemoteDisplayName(call);
                        }
                    }
                    // Enable the mute microphone button
                    muteAudio.setEnabled(true);
                    break;
                case SDKManager.CALL_EVENT_ENDED:
                    // Update call state
                    callState.setText(R.string.ended);
                    // Stop duration timer
                    if (durationTimer != null) {
                        durationTimer.cancel();
                    }
                    count = 0;
                    // Disable the mute microphone button
                    muteAudio.setEnabled(false);
                    // Hide the end button
                    endCall.setVisibility(View.GONE);
                    // Disallow input new DTMF
                    dtmfDigits.setEnabled(false);
                    // Call has been stopped. Stop capturing and destroy video
                    // fragment. Video will be destroyed in VideoFrameFragment#onStop()
                    destroyVideoFragment();
                    // Stop speakerphone
                    if (speakerphone.isChecked()) {
                        audioInterface.setUserRequestedDevice(defaultAudioDevice);
                    }
                    // Unsubscribe from conference events
                    if (call != null && call.isConference()) {
                        call.getConference().removeListener(this);
                    }
                    // We are on Active call screen and the call is ended. Let's close this screen after 2 sec
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Check if fragment not paused to prevent popBackStack() execution in background
                            if (!isFragmentPaused) {
                                getFragmentManager().popBackStack();
                            }
                        }
                    }, CALL_ENDED_EXIT_DELAY_MS);
                    break;
                case SDKManager.CALL_EVENT_FAILED:
                    // Update call state and show the error
                    String error = intent.getStringExtra(SDKManager.EXCEPTION_TAG);
                    callState.setText(error);
                    break;
                case SDKManager.CALL_EVENT_CAPABILITIES_CHANGED:
                    updateRemoteDisplayName(call);
                    break;
                case SDKManager.CALL_EVENT_REMOTE_ADDRESS_CHANGED:
                    updateRemoteDisplayName(call);
                    break;
                case SDKManager.CALL_EVENT_REDIRECTED:
                    updateRemoteDisplayName(call);
                    break;
                case SDKManager.CALL_EVENT_VIDEO_CHANNELS_UPDATED:
                    int channelId = intent.getIntExtra(SDKManager.CHANNEL_ID_TAG, -1);
                    Log.d(LOG_TAG, "Video channel update received for channelId = " + channelId);
                    if (channelId != -1) {
                        // Starting video fragment as video channel is not empty now
                        // VideoFrameFragment will get start params from SDKManager and register
                        // it's own event receiver once started.
                        if (videoFrameFragment == null) {
                            upgradeToVideo();
                        }
                    }
                    if (intent.getBooleanExtra(SDKManager.STOP_VIDEO_TAG, false)) {
                        // Video has been removed from the call. Stop capturing and destroy video
                        // fragment. Video will be destroyed in VideoFrameFragment#onStop()
                        destroyVideoFragment();
                    }
                    break;
                case SDKManager.CALL_EVENT_CONFERENCE_STATUS_CHANGED:
                    if (call != null && call.isConference()) {
                        Conference conference = call.getConference();
                        // Subscribe to conference events
                        conference.addListener(this);
                        // Create adapter for show conference participants
                        updateConferenceParticipants(conference);
                    }
                    break;
                case SDKManager.CALL_EVENT_AUDIO_MUTE_STATUS_CHANGED:
                    // Update mute button state if it is not the same as mute state
                    muteAudio.setChecked(intent.getBooleanExtra(SDKManager.MUTE_TAG, false));
                    break;
                default:
                    break;
            }
        }

        /*
         * ConferenceListener section. This section is handling events that are received for conference.
         */
        @Override
        public void onConferenceCapabilitiesChanged(Conference conference) {
            Log.d(LOG_TAG, "onConferenceCapabilitiesChanged");
            if (conference.getUpdateLockStatusCapability().isAllowed()) {
                // Show lock meeting button
                lockMeetingLayout.setVisibility(View.VISIBLE);
            } else {
                lockMeetingLayout.setVisibility(View.GONE);
            }

        }

        @Override
        public void onConferenceLockStatusChanged(Conference conference, boolean isLocked) {
            Log.d(LOG_TAG, "onConferenceLockStatusChanged isLocked: " + isLocked);
            if (isLocked != lockMeeting.isChecked()) {
                // Update mute button state if it is not the same as mute state
                lockMeeting.setOnCheckedChangeListener(null);
                lockMeeting.toggle();
                lockMeeting.setOnCheckedChangeListener(mLockMeetingListener);
            }
        }

        @Override
        public void onConferenceParticipantsChanged(Conference conference, DataCollectionChangeType dataCollectionChangeType, List<ActiveParticipant> list) {
            Log.d(LOG_TAG, "onConferenceParticipantsChanged");
            switch (dataCollectionChangeType) {
                case COLLECTION_CLEARED:
                    removeListenersForParticipants(list);
                    participantsDataSet.clear();
                    break;
                case ITEMS_ADDED:
                    addListenersForParticipants(list);
                    participantsDataSet.addAll(list);
                    break;
                case ITEMS_DELETED:
                    removeListenersForParticipants(list);
                    for (ActiveParticipant participant : list) {
                        participantsDataSet.remove(participant);
                    }
                    break;
                case ITEMS_UPDATED:
                default:
                    break;
            }
            // Update participants view in accordance with new info
            conferenceParticipantListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConferenceDroppedParticipantsChanged(Conference conference, DataCollectionChangeType dataCollectionChangeType, List<DroppedParticipant> list) {
            Log.d(LOG_TAG, "onConferenceDroppedParticipantsChanged");
        }

        @Override
        public void onConferencePendingParticipantsChanged(Conference conference, DataCollectionChangeType dataCollectionChangeType, List<PendingParticipant> list) {
            Log.d(LOG_TAG, "onConferencePendingParticipantsChanged");
        }

        @Override
        public void onConferenceActiveTalkersChanged(Conference conference, DataCollectionChangeType dataCollectionChangeType, List<ActiveParticipant> list) {
            Log.d(LOG_TAG, "onConferenceActiveTalkersChanged");
        }

        @Override
        public void onConferenceRecentTalkersChanged(Conference conference, DataCollectionChangeType dataCollectionChangeType, List<ActiveParticipant> list) {
            Log.d(LOG_TAG, "onConferenceRecentTalkersChanged");
        }

        @Override
        public void onConferenceContinuationStatusChanged(Conference conference, boolean b) {
            Log.d(LOG_TAG, "onConferenceContinuationStatusChanged");
        }

        @Override
        public void onConferenceVideoStatusChanged(Conference conference, boolean b) {
            Log.d(LOG_TAG, "onConferenceVideoStatusChanged");
        }

        @Override
        public void onConferenceLectureModeStatusChanged(Conference conference, boolean b) {
            Log.d(LOG_TAG, "onConferenceLectureModeStatusChanged");
        }

        @Override
        public void onConferenceEntryExitToneStatusChanged(Conference conference, boolean b) {
            Log.d(LOG_TAG, "onConferenceEntryExitToneStatusChanged");
        }

        @Override
        public void onConferenceSubjectChanged(Conference conference, String s) {
            Log.d(LOG_TAG, "onConferenceSubjectChanged");
        }

        @Override
        public void onConferenceBrandNameChanged(Conference conference, String s) {
            Log.d(LOG_TAG, "onConferenceBrandNameChanged");
        }

        @Override
        public void onConferenceVideoSelfSeeChanged(Conference conference, boolean selfSee) {
            Log.d(LOG_TAG, "onConferenceVideoSelfSeeChanged");
        }

        @Override
        public void onConferenceDisplayVideoParticipantNameChanged(Conference conference, boolean displayVideoParticipantName) {
            Log.d(LOG_TAG, "onConferenceDisplayVideoParticipantNameChanged");
        }

        @Override
        public void onConferenceAlwaysDisplayActiveSpeakerVideoChanged(Conference conference, boolean alwaysDisplayActiveSpeakerVideo) {
            Log.d(LOG_TAG, "onConferenceAlwaysDisplayActiveSpeakerVideoChanged");
        }

        @Override
        public void onConferenceVideoLayoutChanged(Conference conference, VideoLayout layout) {
            Log.d(LOG_TAG, "onConferenceVideoLayoutChanged");
        }

        @Override
        public void onConferenceAvailableVideoLayoutsChanged(Conference conference, VideoLayout[] layouts) {
            Log.d(LOG_TAG, "onConferenceAvailableVideoLayoutsChanged");
        }

        @Override
        public void onConferenceActiveTalkersChanged(Conference conference, List<Participant> participants) {
            Log.d(LOG_TAG, "onConferenceActiveTalkersChanged");
        }

        @Override
        public void onConferenceHandLowered(Conference conference) {
            Log.d(LOG_TAG, "onConferenceHandLowered");
        }

        @Override
        public void onConferenceHandRaised(Conference conference) {
            Log.d(LOG_TAG, "onConferenceHandRaised");
        }

        @Override
        public void onConferencePendingParticipant(Conference conference, PendingParticipant pendingParticipant) {
            Log.d(LOG_TAG, "onConferencePendingParticipant");
        }

        @Override
        public void onConferenceServiceAvailable(Conference conference) {
            Log.d(LOG_TAG, "onConferenceServiceAvailable");
        }

        @Override
        public void onConferenceServiceUnavailable(Conference conference, ConferenceConnectionError conferenceConnectionError) {
            Log.d(LOG_TAG, "onConferenceServiceUnavailable");
        }

        @Override
        public void onConferenceExternalAdmissionStatusChanged(Conference conference, boolean b) {
            Log.d(LOG_TAG, "onConferenceExternalAdmissionStatusChanged");
        }

        @Override
        public void onConferenceWaitingToStart(Conference conference) {
            Log.d(LOG_TAG, "onConferenceWaitingToStart");
        }

        @Override
        public void onConferenceStarted(Conference conference) {
            Log.d(LOG_TAG, "onConferenceStarted");
        }

        @Override
        public void onConferenceRecordingStatusChanged(Conference conference, ConferenceRecordingStatus conferenceRecordingStatus) {
            Log.d(LOG_TAG, "onConferenceRecordingStatusChanged");
        }

        @Override
        public void onConferenceEncryptionStatusChanged(Conference conference, ConferenceEncryptionStatus conferenceEncryptionStatus) {
            Log.d(LOG_TAG, "onConferenceEncryptionStatusChanged");
        }

        @Override
        public void onConferenceStreamingStatusChanged(Conference conference, ConferenceStreamingStatus conferenceStreamingStatus) {
            Log.d(LOG_TAG, "onConferenceStreamingStatusChanged");
        }

        @Override
        public void onConferenceEndTimeChanged(Conference conference, Date date) {
            Log.d(LOG_TAG, "onConferenceEndTimeChanged");
        }

        @Override
        public void onConferencePasscodeRequired(Conference conference, boolean b) {
            Log.d(LOG_TAG, "onConferencePasscodeRequired");
        }

        @Override
        public void onConferencePermissionToEnterLockedConferenceRequired(Conference conference) {
            Log.d(LOG_TAG, "onConferencePermissionToEnterLockedConferenceRequired");
        }
    }

    /*
     * ActiveParticipantListener section. This section is handling events that are received for active participant of conference.
     */
    @Override
    public void onParticipantAudioStatusChanged(Participant participant, ParticipantMediaStatus participantMediaStatus) {
        Log.d(LOG_TAG, "onParticipantAudioStatusChanged");
        // Update participants view in accordance with new info
        conferenceParticipantListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onParticipantApplicantSharingStatusChanged(Participant participant) {
        Log.d(LOG_TAG, "onParticipantApplicantSharingStatusChanged");
    }

    @Override
    public void onParticipantConnectionStatusChanged(Participant participant, ParticipantConnectionStatus participantConnectionStatus) {
        Log.d(LOG_TAG, "onParticipantConnectionStatusChanged");
    }

    @Override
    public void onParticipantSetAsModerator(Participant participant) {
        Log.d(LOG_TAG, "onParticipantSetAsModerator");
    }

    @Override
    public void onParticipantUnsetAsModerator(Participant participant) {
        Log.d(LOG_TAG, "onParticipantUnsetAsModerator");
    }

    @Override
    public void onParticipantSetAsLecturer(Participant participant) {
        Log.d(LOG_TAG, "onParticipantSetAsLecturer");
    }

    @Override
    public void onParticipantUnsetAsLecturer(Participant participant) {
        Log.d(LOG_TAG, "onParticipantUnsetAsLecturer");
    }

    @Override
    public void onParticipantSetAsPresenter(Participant participant) {
        Log.d(LOG_TAG, "onParticipantSetAsPresenter");
    }

    @Override
    public void onParticipantUnsetAsPresenter(Participant participant) {
        Log.d(LOG_TAG, "onParticipantUnsetAsPresenter");
    }

    @Override
    public void onParticipantVideoStatusChanged(Participant participant, ParticipantMediaStatus participantMediaStatus) {
        Log.d(LOG_TAG, "onParticipantVideoStatusChanged");
    }

    @Override
    public void onParticipantHandRaised(Participant participant) {
        Log.d(LOG_TAG, "onParticipantHandRaised");
    }

    @Override
    public void onParticipantHandLowered(Participant participant) {
        Log.d(LOG_TAG, "onParticipantHandLowered");
    }

    @Override
    public void onParticipantCameraRemoteControlSupportChanged(Participant participant) {
        Log.d(LOG_TAG, "onParticipantCameraRemoteControlSupportChanged");
    }

    @Override
    public void onParticipantMatchedContactsChanged(Participant participant) {
        Log.d(LOG_TAG, "onParticipantMatchedContactsChanged");
    }
}

package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.avaya.clientservices.call.Call;
import com.avaya.clientservices.call.CallCompletionHandler;
import com.avaya.clientservices.call.CallEndReason;
import com.avaya.clientservices.call.CallException;
import com.avaya.clientservices.call.CallListener;
import com.avaya.clientservices.call.CallService;
import com.avaya.clientservices.call.CallServiceListener;
import com.avaya.clientservices.call.DTMFType;
import com.avaya.clientservices.call.MediaDirection;
import com.avaya.clientservices.call.VideoChannel;
import com.avaya.clientservices.call.VideoMode;
import com.avaya.clientservices.client.Client;
import com.avaya.clientservices.client.ClientConfiguration;
import com.avaya.clientservices.client.ClientListener;
import com.avaya.clientservices.client.CreateUserCompletionHandler;
import com.avaya.clientservices.client.UserCreatedException;
import com.avaya.clientservices.collaboration.CollaborationService;
import com.avaya.clientservices.collaboration.WCSConfiguration;
import com.avaya.clientservices.collaboration.contentsharing.ContentSharingRenderer;
import com.avaya.clientservices.common.ConnectionPolicy;
import com.avaya.clientservices.common.ServerInfo;
import com.avaya.clientservices.common.SignalingServer;
import com.avaya.clientservices.credentials.Challenge;
import com.avaya.clientservices.credentials.CredentialCompletionHandler;
import com.avaya.clientservices.credentials.CredentialProvider;
import com.avaya.clientservices.credentials.UserCredential;
import com.avaya.clientservices.media.MediaServicesInstance;
import com.avaya.clientservices.media.VoIPConfigurationAudio;
import com.avaya.clientservices.media.VoIPConfigurationVideo;
import com.avaya.clientservices.media.capture.VideoCamera;
import com.avaya.clientservices.media.capture.VideoCaptureController;
import com.avaya.clientservices.media.capture.VideoCaptureException;
import com.avaya.clientservices.presence.PresenceConfiguration;
import com.avaya.clientservices.provider.amm.AMMConfiguration;
import com.avaya.clientservices.provider.media.MediaConfiguration;
import com.avaya.clientservices.provider.ppm.PPMConfiguration;
import com.avaya.clientservices.provider.sip.SIPUserConfiguration;
import com.avaya.clientservices.user.LocalContactConfiguration;
import com.avaya.clientservices.user.User;
import com.avaya.clientservices.user.UserConfiguration;
import com.avaya.clientservices.user.UserRegistrationListener;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * SDKManager class is created to handle all library API calls related to Client and User management
 */
public class SDKManager implements UserRegistrationListener, ClientListener, CredentialProvider, CallServiceListener, CallListener {

    private static final String LOG_TAG = SDKManager.class.getSimpleName();

    public static final String CLIENTSDK_TEST_APP_PREFS = "com.avaya.android.prefs";
    public static final String ACTIVE_CALL_FRAGMENT_TAG = "com.avaya.sdksampleapp.activeCallFragment";
    public static final String INIT_CALL_FRAGMENT_TAG = "com.avaya.sdksampleapp.initCallFragment";

    public static final String CALL_EVENTS_RECEIVER = "callEventsReceiver";
    public static final String LOGIN_RECEIVER = "loginReceiver";
    public static final String MESSAGE_RECEIVER = "messageReceiver";
    public static final String MESSAGING_SERVICE_STATUS_RECEIVER = "messagingServiceReceiver";

    public static final String CALL_EVENT_TAG = "callEvent";
    public static final String TOAST_TAG = "toastMessage";
    public static final String LOGIN_TAG = "loginStatus";
    public static final String EXCEPTION_TAG = "exceptionString";
    public static final String START_LOCAL_VIDEO_TAG = "startLocalVideo";
    public static final String START_REMOTE_VIDEO_TAG = "startRemoteVideo";
    public static final String STOP_VIDEO_TAG = "stopVideo";
    public static final String CHANNEL_ID_TAG = "videoChannelID";
    public static final String CONFERENCE_TAG = "isConferenceCall";
    public static final String MUTE_TAG = "isMuted";
    public static final String MESSAGING_SERVICE_TAG = "isMessaginServiceAvalible";

    public static final String ADDRESS = "address";
    public static final String PORT = "port";
    public static final String DOMAIN = "domain";
    public static final String USE_TLS = "useTls";
    public static final String EXTENSION = "extension";
    public static final String PASSWORD = "password";


    public static final String AMM_ADDRESS = "amm_address";
    public static final String AMM_PORT = "amm_port";
    public static final String AMM_REFRESH = "amm_refresh";


    public static final String CALL_ID = "callId";
    public static final String IS_VIDEO_CALL = "isVideoCall";

    public static final String CALL_EVENT_STARTED = "onCallStarted";
    public static final String CALL_EVENT_RINGING = "onCallRemoteAlerting";
    public static final String CALL_EVENT_ESTABLISHED = "onCallEstablished";
    public static final String CALL_EVENT_ENDED = "onCallEnded";
    public static final String CALL_EVENT_FAILED = "onCallCallFailed";
    public static final String CALL_EVENT_CAPABILITIES_CHANGED = "onCallCapabilitiesChanged";
    public static final String CALL_EVENT_REMOTE_ADDRESS_CHANGED = "onCallRemoteAddressChanged";
    public static final String CALL_EVENT_REDIRECTED = "onCallRedirected";
    public static final String CALL_EVENT_QUEUED = "onCallQueued";
    public static final String CALL_EVENT_HELD = "onCallHeld";
    public static final String CALL_EVENT_UNHELD = "onCallUnheld";
    public static final String CALL_EVENT_HELD_REMOTELY = "onCallHeldRemotely";
    public static final String CALL_EVENT_UNHELD_REMOTELY = "onCallUnheldRemotely";
    public static final String CALL_EVENT_JOINED = "onCallJoined";
    public static final String CALL_EVENT_DENIED = "onCallDenied";
    public static final String CALL_EVENT_IGNORED = "onCallIgnored";
    public static final String CALL_EVENT_SPEAKER_STATUS_CHNAGED = "onCallSpeakerSilenceStatusChanged";
    public static final String CALL_EVENT_AUDIO_MUTE_STATUS_CHANGED = "onCallAudioMuteStatusChanged";
    public static final String CALL_EVENT_VIDEO_CHANNELS_UPDATED = "onCallVideoChannelsUpdated";
    public static final String CALL_EVENT_INCOMING_VIDEO_REQUEST_RECEIVED = "onCallIncomingVideoAddRequestReceived";
    public static final String CALL_EVENT_INCOMING_VIDEO_REQUEST_ACCEPTED = "onCallIncomingVideoAddRequestAccepted";
    public static final String CALL_EVENT_INCOMING_VIDEO_REQUEST_DENIED = "onCallIncomingVideoAddRequestDenied";
    public static final String CALL_EVENT_INCOMING_VIDEO_REQUEST_TIMEDOUT = "onCallIncomingVideoAddRequestTimedout";
    public static final String CALL_EVENT_CONFERENCE_STATUS_CHANGED = "onCallConferenceStatusChanged";
    public static final String CALL_EVENT_SERVICE_AVAILABLE = "onCallServiceAvailable";
    public static final String CALL_EVENT_SERVICE_UNAVAILABLE = "onCallServiceUnavailable";
    public static final String CALL_EVENT_PARTICIPANT_MATCHED_CONTACTS_CHANGED = "onCallParticipantMatchedContactsChanged";
    public static final String CALL_DIGIT_COLLECTION_PLAY_DIAL_TONE = "onCallDigitCollectionPlayDialTone";
    public static final String CALL_DIGIT_COLLECTION_COMPLETED = "onCallDigitCollectionCompleted";

    // Singleton instance of SDKManager
    private static volatile SDKManager instance;

    private final Activity activity;
    private SharedPreferences settings;
    private AlertDialog incomingCallDialog;

    private UserConfiguration userConfiguration;
    private static volatile VideoCaptureController videoCaptureController;
    private Client mClient;
    private User mUser;
    private CallWrapper incomingCallWrapper;

    public static VideoCamera currentCamera;

    private static int activeVideoChannel = -1;

    // Store all active calls with callId key
    private final SparseArray<CallWrapper> callsMap;

    private boolean isAMMEnabled = false;

    private boolean isUserLoggedIn = false;

    private MessagingManager messagingManager;

    private ContentSharingRenderer contentSharingRenderer;


    private SDKManager(Activity activity) {
        this.activity = activity;
        callsMap = new SparseArray<>();
    }

    public static SDKManager getInstance(Activity activity) {
        if (instance == null) {
            synchronized (SDKManager.class) {
                if (instance == null) {
                    instance = new SDKManager(activity);
                }
            }
        }
        return instance;
    }

    public MessagingManager getMessagingManager() {
        return messagingManager;
    }

    // Configure and create mClient
    public void setupClientConfiguration(Application application) {
        // Create client configuration
        String productName = activity.getResources().getString(R.string.productName);
        String productVersion = activity.getResources().getString(R.string.productVersion);
        String buildNumber = activity.getResources().getString(R.string.buildNumber);
        String vendorName = activity.getResources().getString(R.string.vendorName);

        String dataDirectory = activity.getResources().getString(R.string.dataDirectory);
        ClientConfiguration clientConfiguration = new ClientConfiguration(dataDirectory, productName,
                productVersion, Build.MODEL, Build.VERSION.RELEASE, buildNumber, vendorName);
        // Set user agent name
        clientConfiguration.setUserAgentName(productName + '(' + Build.MODEL + ')');

        // A unique instance id of the user agent defined in RFC 5626.
        // For the real applications please generate unique value once (e.g. UUID [RFC4122]) and
        // then save this in persistent storage for all future use.
        clientConfiguration.setUserAgentInstanceId(GenerateUserAgentInstanceId(application));

        // Set media configuration
        final MediaConfiguration mediaConfiguration = new MediaConfiguration();
        mediaConfiguration.setVoIPConfigurationAudio(new VoIPConfigurationAudio());
        mediaConfiguration.setVoIPConfigurationVideo(new VoIPConfigurationVideo());
        clientConfiguration.setMediaConfiguration(mediaConfiguration);
        // Create Client
        mClient = new Client(clientConfiguration, application, this);
        mClient.setLogLevel(Client.LogLevel.DEBUG);
    }

    // Generates unique UserAgentInstanceId value
    private static String GenerateUserAgentInstanceId(Context context) {
        String androidID = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        if (androidID.length() > 0) {
            //deterministic generation (based on ANDROID_ID)
            return UUID.nameUUIDFromBytes(androidID.getBytes()).toString();
        }
        else {
            //random generation (if ANDROID_ID isn't available)
            return UUID.randomUUID().toString();
        }
    }

    // Configure and create mUser
    public void setupUserConfiguration() {
        // Initialize shared preferences
        settings = activity.getSharedPreferences(CLIENTSDK_TEST_APP_PREFS, Context.MODE_PRIVATE);
        // Getting SIP configuration details from settings
        String address = "svsm01.avaya.com";//settings.getString(ADDRESS, "");
        int port = 5061;//settings.getInt(PORT, 5061);
        String domain ="svucacloud.com";// settings.getString(DOMAIN, "");
        boolean useTls =true;// settings.getBoolean(USE_TLS, true);
        String extension = "66078141";//settings.getString(EXTENSION, "");

        // Create SIP configuration
        userConfiguration = new UserConfiguration();
        SIPUserConfiguration sipConfig = userConfiguration.getSIPUserConfiguration();

        // Set SIP service enabled and configure userID and domain
        sipConfig.setEnabled(true);
        sipConfig.setUserId(extension);
        sipConfig.setDomain(domain);

        // Configure Session Manager connection details
        SignalingServer.TransportType transportType =
                useTls ? SignalingServer.TransportType.TLS : SignalingServer.TransportType.TCP;
        SignalingServer sipSignalingServer = new SignalingServer(transportType, address, port,
                SignalingServer.FailbackPolicy.AUTOMATIC);
        sipConfig.setConnectionPolicy(new ConnectionPolicy(sipSignalingServer));

        // Set CredentialProvider
        sipConfig.setCredentialProvider(this);
        // Configuring local contacts to be enabled
        LocalContactConfiguration localContactConfiguration = new LocalContactConfiguration();
        localContactConfiguration.setEnabled(true);
        userConfiguration.setLocalContactConfiguration(localContactConfiguration);

        // Configure PPM service for Send All calls feature. PPM service can be stand alone server
        // as well as part of Session Manager. In the code below SM server details will be used to
        // access PPM. Use ppmConfiguration.setServerInfo() to configure stand alone PPM server
        PPMConfiguration ppmConfiguration = userConfiguration.getPPMConfiguration();
        ppmConfiguration.setEnabled(true);

        // Configure presence service to get presence for contacts
        PresenceConfiguration presenceConfiguration = userConfiguration.getPresenceConfiguration();
        presenceConfiguration.setEnabled(true);

        // Using SIP credential provider. You can use SIP credential provider for PPM if you have
        // same login details. Please create another credential provider in case PPM authentication
        // details are different from SIP user configuration details.
        ppmConfiguration.setCredentialProvider(this);
        userConfiguration.setLocalCallLogFilePath(Environment.getExternalStorageDirectory().getPath()
                + File.separatorChar + "call_logs_sampleApps.xml");

        setupWCS();

        /*
        Configure AMM service. Creating the instance of AMMConfiguration for provide amm settings to CSDK.
        Should contain the separate CredentialProvider for messaging, instance of the ServerInfo, amm server IP,
        port for amm connection.
        */
        //AMMConfiguration ammConfiguration = new AMMConfiguration();
        AMMConfiguration ammConfiguration = userConfiguration.getAMMConfiguration();
        Log.d("AMM","Before Cred");
        CredentialProvider credentialProvider=new CredentialProvider() {
            @Override
            public void onAuthenticationChallenge(Challenge challenge, CredentialCompletionHandler credentialCompletionHandler) {
                credentialCompletionHandler.onCredentialProvided(new UserCredential("hackathon66078141","Hack#2017"));
            }

            @Override
            public void onCredentialAccepted(Challenge challenge) {
                Toast.makeText(activity,"AMM SUCCESS",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationChallengeCancelled(Challenge challenge) {
                Toast.makeText(activity,"AMM ERROR",Toast.LENGTH_SHORT).show();
            }
        };

        String ammServerAddress = settings.getString(SDKManager.AMM_ADDRESS, "svamm.avaya.com");
        int ammPort = settings.getInt(SDKManager.AMM_PORT, 8443);
        int pollIntervalInMinutes = settings.getInt(SDKManager.AMM_REFRESH, 0);
        if (!ammServerAddress.isEmpty()) {
            isAMMEnabled = true;
        }
        ammConfiguration.setEnabled(isAMMEnabled);
        boolean useAmmTls = true;
        ServerInfo serverInfo = new ServerInfo(ammServerAddress, ammPort, useAmmTls);
        ammConfiguration.setServerInfo(serverInfo);
        ammConfiguration.setPollIntervalInMinutes(pollIntervalInMinutes);
        //MessagingCredentialProvider messagingCredentialProvider = new MessagingCredentialProvider(settings);
        ammConfiguration.setCredentialProvider(credentialProvider);
        userConfiguration.setAMMConfiguration(ammConfiguration);
        // Finally create and login a user
        register();
    }

    private void setupWCS() {
        WCSConfiguration wcsConfiguration = new WCSConfiguration();
        wcsConfiguration.setEnabled(true);
        userConfiguration.setWCSConfiguration(wcsConfiguration);
    }

    public ContentSharingRenderer getContentSharingListener() {
        return contentSharingRenderer;
    }

    public void addContentSharingListener(ContentSharingRenderer contentSharingRenderer) {
        this.contentSharingRenderer = contentSharingRenderer;
    }

    public boolean isAMMEnabled() {
        return isAMMEnabled;
    }

    public CollaborationService getCollaborationService() {
        return mUser.getCollaborationService();
    }

    private void register() {
        Log.d(LOG_TAG, "Register user");
        if (mUser != null) {
            // Login if user already exist
            mUser.start();
        } else {
            // Create user if not created yet
            mClient.createUser(userConfiguration, new CreateUserCompletionHandler() {
                @Override
                public void onSuccess(User user) {
                    Log.d(LOG_TAG, "createUser onSuccess");
                    // Initialize class member mUser if we created user successfully
                    mUser = user;
                    Log.d(LOG_TAG, "User Id = " + mUser.getUserId());
                    mUser.addRegistrationListener(SDKManager.this);

                    CallService callService = mUser.getCallService();
                    if (callService != null) {
                        Log.d(LOG_TAG, "CallService is ready to use");
                        // Subscribe to CallService events for incoming call handling
                        callService.addListener(getInstance(activity));
                    }
                    // Initialize class member MessagingManager if we created user successfully and AMM parameters is configured.
                    if (isAMMEnabled) {
                        messagingManager = new MessagingManager(mUser.getMessagingService(), activity);
                    }
                    // And login
                    mUser.start();
                }

                @Override
                public void onError(UserCreatedException e) {
                    Log.e(LOG_TAG, "createUser onError " + e.getFailureReason());

                    //Send broadcast to notify BaseActivity to show message to the user
                    activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG,
                            "ERROR: " + e.getFailureReason().toString()));
                }
            });
        }
    }

    public User getUser() {
        return mUser;
    }

    public MediaServicesInstance getMediaServiceInstance() {
        return mClient.getMediaEngine();
    }

    public static VideoCaptureController getVideoCaptureController() {
        if (videoCaptureController == null) {
            synchronized (VideoCaptureController.class) {
                if (videoCaptureController == null) {
                    videoCaptureController = new VideoCaptureController();
                }
            }
        }

        return videoCaptureController;
    }

    public void shutdownClient() {
        Log.d(LOG_TAG, "Shutdown client");

        //Remove call service listener as we are not going to receive calls anymore
        if (mUser != null) {
            CallService callService = mUser.getCallService();
            if (callService != null) {
                callService.removeListener(getInstance(activity));
            }
            mUser.stop();
        }

        // gracefulShutdown true will try to disconnect the user from servers
        if (mClient != null) {
            mClient.shutdown(true);
        }
    }

    public void delete(boolean loginStatus) {
        Log.d(LOG_TAG, "Delete user");
        if (mUser != null) {
            Log.d(LOG_TAG, "User exist. Deleting...");
            mClient.removeUser(mUser, loginStatus);
            mUser = null;
        }
    }

    public boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }

    /*
     * UserRegistrationListener section
     */
    @Override
    public void onUserRegistrationInProgress(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserRegistrationInProgress");
    }


    // onUserRegistrationSuccessful is called when signaling server respond that provided user
    // credentials are fine and user successfully registered on the server.
    @Override
    public void onUserRegistrationSuccessful(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserRegistrationSuccessful");
        isUserLoggedIn = true;

        // Send broadcast to notify SettingsCallServiceFragments that login label may changed
        activity.sendBroadcast(new Intent(LOGIN_RECEIVER).putExtra(LOGIN_TAG, isUserLoggedIn));
        // Send broadcast to notify BaseActivity to show message to the user
        activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG,
                "Successfully logged in: "
                        + userConfiguration.getSIPUserConfiguration().getUserId()));
    }

    // onUserRegistrationFailed is called when connection error occurred or signaling server
    // respond that provided user credentials are incorrect.
    @Override
    public void onUserRegistrationFailed(User user, SignalingServer signalingServer, Exception e) {
        Log.d(LOG_TAG, "onUserRegistrationFailed " + e.toString());
        isUserLoggedIn = false;

        // Send broadcast to notify SettingsCallServiceFragments that login label may changed
        activity.sendBroadcast(new Intent(LOGIN_RECEIVER).putExtra(LOGIN_TAG, isUserLoggedIn));
        // Send broadcast to notify BaseActivity to show message to the user
        activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG, "Failed to login: "
                + e.getLocalizedMessage()));
    }

    @Override
    public void onUserAllRegistrationsSuccessful(User user) {
        Log.d(LOG_TAG, "onUserAllRegistrationsSuccessful");
    }

    @Override
    public void onUserAllRegistrationsFailed(User user, boolean bWillRetry) {
        Log.d(LOG_TAG, "onUserRegistrationFailed ");
    }

    @Override
    public void onUserUnregistrationInProgress(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserUnregistrationInProgress");
    }

    @Override
    public void onUserUnregistrationSuccessful(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserUnregistrationSuccessful");
    }

    @Override
    public void onUserUnregistrationFailed(User user, SignalingServer signalingServer,
                                           Exception e) {
        Log.d(LOG_TAG, "onUserUnregistrationFailed " + e.toString());
    }

    // onUserUnregistrationComplete is called when server respond that user successfully
    // unregistered.
    @Override
    public void onUserUnregistrationComplete(User user) {
        Log.d(LOG_TAG, "onUserUnregistrationComplete");
        isUserLoggedIn = false;

        // Send broadcast to notify SettingsCallServiceFragments that login label may changed
        activity.sendBroadcast(new Intent(LOGIN_RECEIVER).putExtra(LOGIN_TAG, isUserLoggedIn));
        // Send broadcast to notify BaseActivity to show message to the user
        activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG,
                "Successfully logged off: "
                        + userConfiguration.getSIPUserConfiguration().getUserId()));
    }

    /*
     * ClientListener section
     */
    @Override
    public void onClientShutdown(Client client) {
        Log.d(LOG_TAG, "onClientShutdown");
    }

    @Override
    public void onClientUserCreated(Client client, User user) {
        Log.d(LOG_TAG, "onClientUserCreated");
    }

    // onClientUserRemoved executed when Client.removeUser() is called and successfully completed
    @Override
    public void onClientUserRemoved(Client client, User user) {
        Log.d(LOG_TAG, "onClientUserRemoved");
        // User was deleted due to settings update. Let's create new user with updated
        // configuration.
        setupUserConfiguration();
    }

    /*
     * Credential provider listener section
     */
    // onAuthenticationChallenge executed when we call User.start(). It is passing login
    // credentials to signaling server.
    @Override
    public void onAuthenticationChallenge(Challenge challenge,
                                          CredentialCompletionHandler credentialCompletionHandler) {
        Log.d(LOG_TAG, "UserCredentialProvider.onAuthenticationChallenge : Challenge = "
                + challenge);

        // Getting login information from settings
        String extension = "66078141";//settings.getString(EXTENSION, "");
        // Note: Although this sample application manages passwords as clear text this application
        // is intended as a learning tool to help users become familiar with the Avaya SDK.
        // Managing passwords as clear text is not illustrative of a secure process to protect
        // passwords in an enterprise quality application.
        String password = "123456";//settings.getString(PASSWORD, "");
        String domain = "svucacloud.com";//settings.getString(DOMAIN, "");

        // Login with saved credentials
        UserCredential userCredential = new UserCredential(extension, password, domain);
        credentialCompletionHandler.onCredentialProvided(userCredential);
    }

    @Override
    public void onCredentialAccepted(Challenge challenge) {
    }

    @Override
    public void onAuthenticationChallengeCancelled(Challenge challenge) {
    }

    /*
     * CallServiceListener listener section
     */
    @Override
    public void onIncomingCallReceived(CallService callService, Call call) {
        Log.d(LOG_TAG, "onIncomingCall");
        // Dismiss active dialog if any
        if (incomingCallDialog != null) {
            incomingCallDialog.dismiss();
        }
        // Save the call into incomingCallWrapper for access from onClick event. Assume incoming call is
        // video call. We will destroy video frame if no video stream for the call
        incomingCallWrapper = new CallWrapper(call, true);
        // Add the call to call Map
        callsMap.put(call.getCallId(), incomingCallWrapper);

        // Initialize incoming call dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.incoming_call);

        Fragment currentCallActiveFragment = activity.getFragmentManager()
                .findFragmentByTag(ACTIVE_CALL_FRAGMENT_TAG);
        if (currentCallActiveFragment instanceof CallActiveFragment) {
            // We are on active call screen. Only one call is allowed. Do nothing.
            alert.setMessage("<To enable this feature add code to support multiple active calls " +
                    "in your application>");
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(LOG_TAG, "To enable this feature add code to support multiple active " +
                            "calls in your application");
                    incomingCallWrapper.getCall().ignore();
                }
            });
        } else {
            // We are not on active call screen. Set message with far-end info and add two buttons
            // for accept/ignore incoming call
            alert.setMessage(call.getRemoteNumber() + " \"" + call.getRemoteDisplayName() + '\"');
            alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    // Create active call fragment
                    CallActiveFragment callActiveFragment = new CallActiveFragment();

                    Bundle bundle = new Bundle();
                    bundle.putInt(SDKManager.CALL_ID, incomingCallWrapper.getCall().getCallId());
                    // Don't add video frame. Let's add it once video channel update received.
                    bundle.putBoolean(SDKManager.IS_VIDEO_CALL, false);
                    callActiveFragment.setArguments(bundle);

                    // Open active call fragment
                    activity.getFragmentManager().beginTransaction().replace(R.id.dynamic_view,
                            callActiveFragment, ACTIVE_CALL_FRAGMENT_TAG)
                            .addToBackStack(null).commit();
                    // Trying to start video call by default
                    startCall(incomingCallWrapper);
                }
            });
            alert.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(LOG_TAG, "Ignore incoming call");
                    incomingCallWrapper.getCall().ignore();
                }
            });
        }

        incomingCallDialog = alert.create();
        incomingCallDialog.show();
    }

    @Override
    public void onCallCreated(CallService callService, Call call) {
        Log.d(LOG_TAG, "onCallCreated");

    }

    @Override
    public void onIncomingCallUndelivered(CallService callService, Call call) {
        Log.d(LOG_TAG, "onIncomingCallUndelivered");
    }

    @Override
    public void onActiveCallChanged(CallService callService, Call call) {
        Log.d(LOG_TAG, "onActiveCallChanged");
    }

    @Override
    public void onCallRemoved(CallService callService, Call call) {
        Log.d(LOG_TAG, "onCallRemoved");
        // Hide incoming call dialog
        if (incomingCallDialog != null) {
            incomingCallDialog.dismiss();
        }
        // Remove the call from call map for prevent any actions with it
        int callId = call.getCallId();
        callsMap.remove(callId);

        // Unsubscribe from call state events
        call.removeListener(this);
    }

    @Override
    public void onCallServiceCapabilityChanged(CallService callService) {
        Log.d(LOG_TAG, "onCallServiceCapabilityChanged");
    }

    /*
     * Call management section
     */

    public static int getActiveVideoChannel() {
        return activeVideoChannel;
    }

    public static void setActiveVideoChannel(int activeVideoChannel) {
        SDKManager.activeVideoChannel = activeVideoChannel;
    }

    // Return the call with specified call id if it is not removed yet
    public CallWrapper getCallWrapperByCallId(int callId) {
        return callsMap.get(callId);
    }

    // Create call object, set the number, add to call map and return call object
    public CallWrapper createCall(String calledParty) {
        // Create call
        CallService callService = mUser.getCallService();
        Call call = callService.createCall();
        // Set far-end's number
        call.setRemoteAddress(calledParty);
        // Get unique call id specified for created call
        int callId = call.getCallId();

        CallWrapper callWrapper = new CallWrapper(call);

        // Add the call to call Map
        callsMap.put(callId, callWrapper);
        return callWrapper;
    }


    public void startCall(CallWrapper callWrapper) {

        Call call = callWrapper.getCall();
        // Subscribe to call state events
        call.addListener(this);

        // Add video to the call
        if (callWrapper.isVideoCall()) {
            addVideo(call);
        }

        if (call.isIncoming()) {
            Log.d(LOG_TAG, "Incoming call accepted");
            // Accept the call if it is incoming call
            call.accept();
        } else {
            Log.d(LOG_TAG, "Outgoing call started");
            // Start the call if it is outgoing call
            call.start();
        }
    }

    // Create video chanel and set it for the call
    private void addVideo(Call call) {
        // Check if video supported
        if (!call.getUpdateVideoModeCapability().isAllowed()) {
            Log.d(LOG_TAG, "Don't add video. Video isn't supported");
            return;
        } else if (call.isIncoming() && call.getIncomingVideoStatus()
                != Call.IncomingVideoStatus.SUPPORTED) {
            Log.d(LOG_TAG, "Don't add video. Far-end didn't send video information");
            return;
        }

        // Set video mode for the call depending on camera device
        call.setVideoMode(setupCamera(), new CallCompletionHandler() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Video mode has been set");
            }

            @Override
            public void onError(CallException e) {
                Log.e(LOG_TAG, "Video mode can't be set. Exception: " + e.getError());
            }
        });
    }

    // Set the camera and return video mode for initializing video
    private VideoMode setupCamera() {
        // Check if device has camera
        VideoCaptureController videoCaptureController = getVideoCaptureController();
        try {
            if (videoCaptureController.hasVideoCamera(VideoCamera.Front)) {
                currentCamera = VideoCamera.Front;
                return VideoMode.SEND_RECEIVE;
            } else if (videoCaptureController.hasVideoCamera(VideoCamera.Back)) {
                currentCamera = VideoCamera.Back;
                return VideoMode.SEND_RECEIVE;
            }
        } catch (VideoCaptureException e) {
            Log.e(LOG_TAG, "Camera can't be set. Exception: " + e.getLocalizedMessage());
            return VideoMode.RECEIVE_ONLY;
        }
        // No cameras found
        return VideoMode.RECEIVE_ONLY;
    }

    // Send one DTMF digit to the call
    public void sendDTMF(Call call, char char_digit) {
        //Convert char to DTMF
        DTMFType DTMF_digit = parseToDTMF(char_digit);
        call.sendDTMF(DTMF_digit);
    }

    // Function for convert char to DTMF
    private DTMFType parseToDTMF(char digit) {
        switch (digit) {
            case '1':
                return DTMFType.ONE;
            case '2':
                return DTMFType.TWO;
            case '3':
                return DTMFType.THREE;
            case '4':
                return DTMFType.FOUR;
            case '5':
                return DTMFType.FIVE;
            case '6':
                return DTMFType.SIX;
            case '7':
                return DTMFType.SEVEN;
            case '8':
                return DTMFType.EIGHT;
            case '9':
                return DTMFType.NINE;
            case '0':
                return DTMFType.ZERO;
            case '#':
                return DTMFType.POUND;
            case '*':
                return DTMFType.STAR;
            default:
                return null;
        }
    }

    /*
     * CallListener section. This section is handling events that are received for calls.
     */
    @Override
    public void onCallStarted(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_STARTED);
    }

    @Override
    public void onCallRemoteAlerting(Call call, boolean b) {
        Log.d(LOG_TAG, CALL_EVENT_RINGING);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_RINGING));
    }

    @Override
    public void onCallEstablished(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_ESTABLISHED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_ESTABLISHED));
    }


    @Override
    public void onCallFailed(Call call, CallException e) {
        Log.d(LOG_TAG, CALL_EVENT_FAILED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_FAILED)
                .putExtra(EXCEPTION_TAG, e.getLocalizedMessage()));

    }

    @Override
    public void onCallCapabilitiesChanged(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_CAPABILITIES_CHANGED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_CAPABILITIES_CHANGED));
    }

    @Override
    public void onCallRemoteAddressChanged(Call call, String s, String s1) {
        Log.d(LOG_TAG, CALL_EVENT_REMOTE_ADDRESS_CHANGED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_REMOTE_ADDRESS_CHANGED));

    }

    @Override
    public void onCallRedirected(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_REDIRECTED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_REDIRECTED));
    }

    @Override
    public void onCallQueued(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_QUEUED);
    }

    @Override
    public void onCallHeld(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_HELD);
    }

    @Override
    public void onCallUnheld(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_UNHELD);
    }

    @Override
    public void onCallHeldRemotely(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_HELD_REMOTELY);
    }

    @Override
    public void onCallUnheldRemotely(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_UNHELD_REMOTELY);
    }

    @Override
    public void onCallJoined(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_JOINED);
    }

    @Override
    public void onCallEnded(Call call, CallEndReason callEndReason) {
        Log.d(LOG_TAG, CALL_EVENT_ENDED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_ENDED));
    }

    @Override
    public void onCallDenied(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_DENIED);
    }

    @Override
    public void onCallIgnored(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_IGNORED);
    }

    @Override
    public void onCallAudioMuteStatusChanged(Call call, boolean isMuted) {
        Log.d(LOG_TAG, "onCallAudioMuteStatusChanged isMuted: " + isMuted);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_AUDIO_MUTE_STATUS_CHANGED)
                .putExtra(MUTE_TAG, isMuted));
    }

    @Override
    public void onCallSpeakerSilenceStatusChanged(Call call, boolean b) {
        Log.d(LOG_TAG, CALL_EVENT_SPEAKER_STATUS_CHNAGED);
    }

    @Override
    public void onCallVideoChannelsUpdated(Call call, List<VideoChannel> list) {
        Log.d(LOG_TAG, CALL_EVENT_VIDEO_CHANNELS_UPDATED);
        // Get call id
        int callId = call.getCallId();

        CallWrapper callWrapper = getCallWrapperByCallId(callId);

        if (!list.isEmpty()) {
            // Get video channel id
            int channelId = list.get(0).getChannelId();
            setActiveVideoChannel(channelId);

            // Get negotiated media direction
            MediaDirection mediaDirection = list.get(0).getNegotiatedDirection();
            Log.d(LOG_TAG, "Negotiated media direction: " + mediaDirection);
            if (mediaDirection == MediaDirection.SEND_RECEIVE
                    || mediaDirection == MediaDirection.SEND_ONLY) {
                Log.d(LOG_TAG, START_LOCAL_VIDEO_TAG);
                activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                        .putExtra(CALL_EVENT_TAG, CALL_EVENT_VIDEO_CHANNELS_UPDATED)
                        .putExtra(START_LOCAL_VIDEO_TAG, true)
                        .putExtra(CHANNEL_ID_TAG, channelId));
                callWrapper.setLocalVideoActive(true);
            }
            if (mediaDirection == MediaDirection.SEND_RECEIVE
                    || mediaDirection == MediaDirection.RECEIVE_ONLY) {
                Log.d(LOG_TAG, START_REMOTE_VIDEO_TAG);
                activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                        .putExtra(CALL_EVENT_TAG, CALL_EVENT_VIDEO_CHANNELS_UPDATED)
                        .putExtra(START_REMOTE_VIDEO_TAG, true)
                        .putExtra(CHANNEL_ID_TAG, channelId));
                callWrapper.setRemoteVideoActive(true);
            }
        } else {
            Log.d(LOG_TAG, STOP_VIDEO_TAG);
            activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                    .putExtra(CALL_EVENT_TAG, CALL_EVENT_VIDEO_CHANNELS_UPDATED)
                    .putExtra(STOP_VIDEO_TAG, true));
            callWrapper.setLocalVideoActive(false);
            callWrapper.setRemoteVideoActive(false);
            setActiveVideoChannel(-1);
        }
    }

    @Override
    public void onCallIncomingVideoAddRequestReceived(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_INCOMING_VIDEO_REQUEST_RECEIVED);
    }

    @Override
    public void onCallIncomingVideoAddRequestAccepted(Call call, VideoChannel videoChannel) {
        Log.d(LOG_TAG, CALL_EVENT_INCOMING_VIDEO_REQUEST_ACCEPTED);
    }

    @Override
    public void onCallIncomingVideoAddRequestDenied(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_INCOMING_VIDEO_REQUEST_DENIED);
    }

    @Override
    public void onCallIncomingVideoAddRequestTimedOut(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_INCOMING_VIDEO_REQUEST_TIMEDOUT);
    }

    @Override
    public void onCallConferenceStatusChanged(Call call, boolean isConference) {
        Log.d(LOG_TAG, CALL_EVENT_CONFERENCE_STATUS_CHANGED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_CONFERENCE_STATUS_CHANGED)
                .putExtra(CONFERENCE_TAG, isConference));
    }

    @Override
    public void onCallServiceAvailable(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_SERVICE_AVAILABLE);
    }

    @Override
    public void onCallServiceUnavailable(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_SERVICE_UNAVAILABLE);
    }

    @Override
    public void onCallParticipantMatchedContactsChanged(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_PARTICIPANT_MATCHED_CONTACTS_CHANGED);
    }

    @Override
    public void onCallDigitCollectionPlayDialTone(Call call) {
        Log.d(LOG_TAG, CALL_DIGIT_COLLECTION_PLAY_DIAL_TONE);
    }

    @Override
    public void onCallDigitCollectionCompleted(Call call) {
        Log.d(LOG_TAG, CALL_DIGIT_COLLECTION_COMPLETED);
    }
}

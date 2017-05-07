package com.avaya.sdksampleapp.commpackage;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.avaya.clientservices.calllog.CallLogCompletionHandler;
import com.avaya.clientservices.calllog.CallLogItem;
import com.avaya.clientservices.calllog.CallLogService;
import com.avaya.clientservices.calllog.CallLogServiceListener;

import java.util.ArrayList;
import java.util.List;

public class CallLogListFragment extends Fragment implements CallLogServiceListener, CallLogCompletionHandler {

    private static final String LOG_TAG = CallLogListFragment.class.getSimpleName();
    private List<CallLogItem> callLogItems = new ArrayList<>();
    private CallLogService callLogService;
    private CallLogsListAdapter callLogsListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        // getting the instance of the CSDK callLogService
        // it need to make available the engine  functions and events.
        if (callLogService == null) {
            callLogService = SDKManager.getInstance(getActivity()).getUser().getCallLogService();
            // CallLogListFragment is implementing the CallLogServiceListener and may be set as listener of the callLogService
            callLogService.addListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView()");
        return inflater.inflate(R.layout.call_logs_list_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        callLogItems = callLogService.getCallLogs();
        ListView callLogsListListView = (ListView) view.findViewById(R.id.calllogs_list);
        // Initializing ListView adapter
        callLogsListAdapter = new CallLogsListAdapter(getActivity(), R.layout.call_log_list_item, callLogItems, callLogService);
        callLogsListListView.setAdapter(callLogsListAdapter);
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
        getActivity().setTitle(R.string.callLogs_item);
    }

    @Override
    // implementation of the CallLogServiceListener
    public void onCallLogServiceCallLogItemsAdded(CallLogService callLogService, List<CallLogItem> list) {
        Log.d(LOG_TAG, "onCallLogServiceCallLogItemsAdded()");
        updateCallLogs();
    }

    @Override
    // implementation of the CallLogServiceListener
    public void onCallLogServiceCallLogItemsResynchronized(CallLogService callLogService, List<CallLogItem> list) {
        Log.d(LOG_TAG, "onCallLogServiceCallLogItemsResynchronized()");
        callLogItems = callLogService.getCallLogs();
        callLogsListAdapter.notifyDataSetChanged();
    }

    @Override
    // implementation of the CallLogServiceListener
    public void onCallLogServiceCallLogItemsRemoved(CallLogService callLogService, List<CallLogItem> list) {
        Log.d(LOG_TAG, "Fragment#onCallLogServiceCallLogItemsRemoved()");
        updateCallLogs();
    }

    @Override
    // implementation of the CallLogServiceListener
    public void onCallLogServiceCallLogItemsUpdated(CallLogService callLogService, List<CallLogItem> list) {
        Log.d(LOG_TAG, "onCallLogServiceCallLogItemsUpdated()");
        updateCallLogs();
    }

    @Override
    // implementation of the CallLogServiceListener
    public void onCallLogServiceLoaded(CallLogService callLogService, List<CallLogItem> list) {
        Log.d(LOG_TAG, "onCallLogServiceLoaded()");
        updateCallLogs();
    }

    @Override
    // implementation of the CallLogServiceListener
    public void onCallLogServiceLoadFailed(CallLogService callLogService) {
        Log.d(LOG_TAG, "onCallLogServiceLoadFailed");
    }

    @Override
    // implementation of the CallLogCompletionHandler
    public void onSuccess() {
        updateCallLogs();
    }

    @Override
    // implementation of the CallLogCompletionHandler
    public void onError() {
        Log.e(LOG_TAG, "CallLogCompletionHandler - onError");
    }

    private void updateCallLogs() {
        // getting the call log from service and notify the adapter about it to update the view.
        callLogItems = callLogService.getCallLogs();
        callLogsListAdapter.notifyDataSetChanged();
    }
}

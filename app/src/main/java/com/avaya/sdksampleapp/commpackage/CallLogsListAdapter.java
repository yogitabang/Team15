package com.avaya.sdksampleapp.commpackage;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.avaya.clientservices.calllog.CallLogCompletionHandler;
import com.avaya.clientservices.calllog.CallLogItem;
import com.avaya.clientservices.calllog.CallLogService;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class CallLogsListAdapter extends ArrayAdapter<CallLogItem> {

    private static final String LOG_TAG = CallLogsListAdapter.class.getSimpleName();

    private final List<CallLogItem> items;
    private final int layoutResourceId;
    private CallLogService callLogService;

    public CallLogsListAdapter(Activity activity, int layoutResourceId, List<CallLogItem> items, CallLogService callLogService) {
        super(activity, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.items = items;
        this.callLogService = callLogService;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        CallLogHolder holder;
        if (view == null) {
            //initialise the holder item once for better performance.
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
            holder = new CallLogHolder();
            initCallLogHolder(holder, view);
        } else {
            holder = (CallLogHolder) view.getTag();
            if (holder == null) {
                holder = new CallLogHolder();
                initCallLogHolder(holder, view);
            }
        }
        int size = items.size();
        if (position > size - 1) {
            return view;
        }
        holder.item = items.get(position);
        String temp;

        temp = holder.item.getRemoteNumber();
        holder.remoteName.setText(temp);


        temp = (new Time(holder.item.getEndTime().getTime())).toString();
        holder.end_time.setText(temp);


        long duration = holder.item.getDurationInSeconds();
        Date date = new Date(duration * 1000);
        SimpleDateFormat simpleTime = new SimpleDateFormat("mm:ss");
        holder.duration.setText(simpleTime.format(date));

        // Delete button initialization

        holder.deleteCallLogItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View deleteCallLog) {
                List<CallLogItem> romovedList = new ArrayList<>();
                romovedList.add(getItem((Integer) deleteCallLog.getTag()));
                // uses the CSDK function "removeCallLogs" via callLogService to remove the call log entry
                callLogService.removeCallLogs(romovedList, new CallLogCompletionHandler() {
                    // local implementation of the CallLogCompletionHandler
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Call log item has been deleted");
                    }

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG, "Call log item cannot be deleted. ");
                    }
                });
            }
        });
        holder.deleteCallLogItem.setTag(position);
        return view;
    }

    public static class CallLogHolder {
        CallLogItem item;
        TextView remoteName;
        TextView end_time;
        TextView duration;
        ImageButton deleteCallLogItem;
    }

    private void initCallLogHolder(CallLogHolder holder, View view) {
        holder.remoteName = (TextView) view.findViewById(R.id.remoteUser_name);
        holder.end_time = (TextView) view.findViewById(R.id.end_time);
        holder.duration = (TextView) view.findViewById(R.id.duration);
        holder.deleteCallLogItem = (ImageButton) view.findViewById(R.id.delete_calllog);
    }
}

package com.avaya.sdksampleapp.commpackage;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.avaya.clientservices.collaboration.ZoomableImageView;
import com.avaya.clientservices.collaboration.contentsharing.ContentSharingRenderer;

public class ContentSharingFragment extends Fragment implements ContentSharingRenderer.RendererPrivateListener {
    private LinearLayout layout;
    private ContentSharingRenderer mContentSharingRenderer;
    private Activity mFragmentActivity;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Button closeButton;

    @Override
    public void onAttach(Activity activity) {
        Log.d(LOG_TAG, "onAttach");
        super.onAttach(activity);
        mFragmentActivity = activity;
        SDKManager manger = SDKManager.getInstance(getActivity());
        mContentSharingRenderer = manger.getContentSharingListener();
        if (mContentSharingRenderer == null)
        {
            mContentSharingRenderer = new ContentSharingRenderer();
            manger.addContentSharingListener(mContentSharingRenderer);
        }
        mContentSharingRenderer.addListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        layout = (LinearLayout) inflater.inflate(R.layout.content_sharing_fragment, container, false);

        mContentSharingRenderer.setCursorIcon(BitmapFactory.decodeResource(getResources(), R.drawable.cursor));
        mContentSharingRenderer.setPauseIcon(BitmapFactory.decodeResource(getResources(), R.drawable.pause));
        ZoomableImageView zoomableImageView = new ZoomableImageView(mFragmentActivity);
        layout.addView(mContentSharingRenderer.getContentSharingView(zoomableImageView));
        return layout;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        closeButton = (Button) view.findViewById(R.id.close_sharing_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onDetach() {
        Log.d(LOG_TAG, "onDetach");
        super.onDetach();
        mContentSharingRenderer.removeListener(this);
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

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "Fragment#onResume()");
        super.onResume();
        // Set fragment title
        getActivity().setTitle(R.string.webCollaboration);
    }



    //ContentSharingRenderer.RendererPrivateListener section
    @Override
    public void onContentSharingStarted() {
        Log.d(LOG_TAG, "onContentSharingStarted");
    }

    @Override
    public void onContentSharingEnded() {

        Log.d(LOG_TAG, "onContentSharingEnded");
        getFragmentManager().popBackStack();
    }

    @Override
    public void onNewBitmapImage(Bitmap bitmap) {
        Log.d(LOG_TAG, "onNewBitmapImage");
    }

    @Override
    public void onChangeCursorPosition(int x, int y) {
        Log.d(LOG_TAG, "onChangeCursorPosition");
    }

    @Override
    public void onContentSharingPaused(boolean paused) {
        Log.d(LOG_TAG, "onContentSharingPaused");
    }
}

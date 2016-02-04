package com.pits.smbbrowse.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.adapters.ContentListAdapter;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirection;

import java.net.MalformedURLException;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class BrowseDirectoryTask extends AsyncTask<Void, Void, ContentListAdapter> implements
        SwipeActionAdapter.SwipeActionListener, ListView.OnItemLongClickListener {

    private NtlmPasswordAuthentication mCredentials;
    private String mSambaShareAddress;
    private ListView mItemsListView;
    private Activity mActivity;
    private ContentListAdapter mContentListAdapter;
    private SwipeActionAdapter mSwipeActionAdapter;

    public BrowseDirectoryTask(Activity activity, String sambaShareAddress,
                               NtlmPasswordAuthentication credentials, ListView itemsListView) {
        super();
        mCredentials = credentials;
        mSambaShareAddress = sambaShareAddress;
        mItemsListView = itemsListView;
        mActivity = activity;
    }

    @Override
    protected ContentListAdapter doInBackground(Void... params) {

        try {
            SmbFile directory = new SmbFile(mSambaShareAddress, mCredentials);
            SmbFile[] files = directory.listFiles();
            List<SmbFile> filteredFiles = Helpers.filterFilesLargerThan(files, 10);
            mContentListAdapter = new ContentListAdapter(
                    mActivity.getApplicationContext(),
                    R.layout.list_row,
                    filteredFiles
            );
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return mContentListAdapter;
    }

    @Override
    protected void onPostExecute(final ContentListAdapter contentListAdapter) {
        super.onPostExecute(contentListAdapter);
        mSwipeActionAdapter = new SwipeActionAdapter(contentListAdapter);
        mItemsListView.setAdapter(mSwipeActionAdapter);
        AppGlobals.setCurrentBrowsedLocation(mSambaShareAddress);
        mSwipeActionAdapter.addBackground(
                SwipeDirection.DIRECTION_NORMAL_LEFT, R.layout.row_bg_left);
        mSwipeActionAdapter.addBackground(
                SwipeDirection.DIRECTION_NORMAL_RIGHT, R.layout.row_bg_right);
        mSwipeActionAdapter.setListView(mItemsListView);
        mSwipeActionAdapter.setSwipeActionListener(this);
        mItemsListView.setOnItemLongClickListener(this);
    }

    @Override
    public boolean hasActions(int position, SwipeDirection direction) {
        return direction.isLeft() || direction.isRight();
    }

    @Override
    public boolean shouldDismiss(int position, SwipeDirection direction) {
        return direction == SwipeDirection.DIRECTION_NORMAL_LEFT;
    }

    @Override
    public void onSwipe(int[] positionList, SwipeDirection[] directionList) {
        UiHelpers uiHelpers = new UiHelpers(mContentListAdapter);
        for (int i = 0; i < positionList.length; i++) {
            SwipeDirection direction = directionList[i];
            int position = positionList[i];
            SmbFile file = mContentListAdapter.getItem(position);

            switch (direction) {
                case DIRECTION_NORMAL_LEFT:
                case DIRECTION_FAR_LEFT:
                    new FileRenameTask(
                            mActivity.getApplicationContext(), mCredentials,
                            mContentListAdapter, file, null).execute();
                    break;
                case DIRECTION_NORMAL_RIGHT:
                case DIRECTION_FAR_RIGHT:
                    uiHelpers.showDeleteConfirmationDialog(mActivity, mCredentials, file);
                    break;
            }
            mSwipeActionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        SmbFile selectedFile = mContentListAdapter.getItem(position);
        UiHelpers uiHelpers = new UiHelpers(mContentListAdapter);
        uiHelpers.showFileRenameDialog(mActivity, mCredentials, selectedFile);
        return true;
    }
}
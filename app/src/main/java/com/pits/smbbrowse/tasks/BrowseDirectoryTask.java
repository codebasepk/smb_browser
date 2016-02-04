package com.pits.smbbrowse.tasks;

import android.app.Activity;
import android.os.AsyncTask;
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

public class BrowseDirectoryTask extends AsyncTask<Void, Void, ContentListAdapter> {

    private NtlmPasswordAuthentication mCredentials;
    private String mSambaShareAddress;
    private ListView mItemsListView;
    private Activity mActivity;
    private ContentListAdapter contentListAdapter = null;
    private SwipeActionAdapter swipeActionAdapter;

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
            contentListAdapter = new ContentListAdapter(
                    mActivity.getApplicationContext(),
                    R.layout.list_row,
                    filteredFiles
            );
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return contentListAdapter;
    }

    @Override
    protected void onPostExecute(final ContentListAdapter contentListAdapter) {
        super.onPostExecute(contentListAdapter);
        swipeActionAdapter = new SwipeActionAdapter(contentListAdapter);
        mItemsListView.setAdapter(swipeActionAdapter);
        AppGlobals.setCurrentBrowsedLocation(mSambaShareAddress);
        mActivity.registerForContextMenu(mItemsListView);
        swipeActionAdapter.addBackground(SwipeDirection.DIRECTION_NORMAL_LEFT, R.layout.row_bg_left)
                .addBackground(SwipeDirection.DIRECTION_NORMAL_RIGHT, R.layout.row_bg_right);
        swipeActionAdapter.setListView(mItemsListView);
        swipeActionAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position, SwipeDirection direction) {
                if (direction.isLeft()) return true;
                if (direction.isRight()) return true;
                return false;
            }

            @Override
            public boolean shouldDismiss(int position, SwipeDirection direction) {
                return direction == SwipeDirection.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, SwipeDirection[] directionList) {
                UiHelpers uiHelpers = new UiHelpers(contentListAdapter);
                for (int i = 0; i < positionList.length; i++) {
                    SwipeDirection direction = directionList[i];
                    int position = positionList[i];

                    switch (direction) {
                        case DIRECTION_NORMAL_LEFT:
                        case DIRECTION_FAR_LEFT:
                            try {
                                new FileRenameTask(mActivity.getApplicationContext(), mCredentials,
                                        contentListAdapter,
                                        new SmbFile(String.valueOf(
                                                swipeActionAdapter.getItem(position))),
                                        null).execute();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            break;
                        case DIRECTION_NORMAL_RIGHT:
                        case DIRECTION_FAR_RIGHT:
                            try {
                                uiHelpers.showDeleteConfirmationDialog(mActivity, mCredentials,
                                        new SmbFile(String.valueOf(swipeActionAdapter.getItem(position))));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    swipeActionAdapter.notifyDataSetChanged();
                }
            }
        });

    }
}
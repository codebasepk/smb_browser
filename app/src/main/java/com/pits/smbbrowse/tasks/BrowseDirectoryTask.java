package com.pits.smbbrowse.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.adapters.ContentListAdapter;
import com.pits.smbbrowse.utils.Helpers;

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
        ContentListAdapter contentListAdapter = null;
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
    protected void onPostExecute(ContentListAdapter contentListAdapter) {
        super.onPostExecute(contentListAdapter);
        mItemsListView.setAdapter(contentListAdapter);
        mActivity.registerForContextMenu(mItemsListView);
    }
}

package com.pits.smbbrowse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pits.smbbrowse.adapters.ContentListAdapter;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Constants;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileRenameTask extends AsyncTask<Void, Void, String> {

    private Context mContext;
    private NtlmPasswordAuthentication mAuth;
    private SmbFile mFileToRename;
    private String mNewName;
    private ContentListAdapter mContentAdapter;
    private SmbFile mNewFile;

    public FileRenameTask(Context context, NtlmPasswordAuthentication auth,
                          ContentListAdapter contentAdapter,  SmbFile fileToRename,
                          String newName) {
        super();
        mContext = context;
        mAuth = auth;
        mFileToRename = fileToRename;
        mNewName = newName;
        mContentAdapter = contentAdapter;
    }

    @Override
    protected String doInBackground(Void... params) {
        String doneMessage;
        if (mNewName == null) {
            // Its a file MOVE request
            String moved_directory;
            String smbHost = AppGlobals.getSambaHostAddress();
            if (!smbHost.endsWith("/")) {
                moved_directory = smbHost + "/" + Constants.DIRECTORY_MOVED;
            } else {
                moved_directory = smbHost + Constants.DIRECTORY_MOVED;
            }
            String newLocationAbs = moved_directory + "/" + mFileToRename.getName();
            SmbFile newFile = reallyRenameFile(newLocationAbs, false);
            if (newFile == null) {
                doneMessage = "Operation failed";
            } else {
                doneMessage = String.format("Moved to %s", newFile.getCanonicalPath());
            }
        } else {
            // Its a rename request
            String newNameAbs = mFileToRename.getParent() + mNewName;
            SmbFile newFile = reallyRenameFile(newNameAbs, true);
            if (newFile == null) {
                doneMessage = "Operation failed";
            } else {
                doneMessage = String.format("Renamed to %s", newFile.getName());
            }
        }
        return doneMessage;
    }

    @Override
    protected void onPostExecute(String doneMessage) {
        super.onPostExecute(doneMessage);
        if (doneMessage.startsWith("Renamed")) {
            int itemPosition = mContentAdapter.getPosition(mFileToRename);
            UiHelpers.removeItemFromAdapter(mContentAdapter, mFileToRename);

            // FIXME: move to a better location
            mContentAdapter.insert(mNewFile, itemPosition);
            mContentAdapter.notifyDataSetChanged();
        } else if (doneMessage.startsWith("Moved")) {
            UiHelpers.removeItemFromAdapter(mContentAdapter, mFileToRename);
        }
        UiHelpers.showLongToast(mContext, doneMessage);
    }

    private SmbFile reallyRenameFile(String newPath, boolean log) {
        try {
            mNewFile = new SmbFile(newPath, mAuth);
            mFileToRename.renameTo(mNewFile);
            if (log) {
                Helpers.createFileLog(
                        mAuth, mFileToRename.getName(), Helpers.getRenamedLogLocation());
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return mNewFile;
    }
}

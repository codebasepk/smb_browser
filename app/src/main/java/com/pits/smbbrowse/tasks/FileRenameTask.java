package com.pits.smbbrowse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Constants;
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

    public FileRenameTask(Context context, NtlmPasswordAuthentication auth,
                          SmbFile fileToRename, String newName) {
        super();
        mContext = context;
        mAuth = auth;
        mFileToRename = fileToRename;
        mNewName = newName;
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
            SmbFile newFile = reallyRenameFile(newLocationAbs);
            if (newFile == null) {
                doneMessage = "Operation failed";
            } else {
                doneMessage = String.format("Moved to %s", newFile.getCanonicalPath());
            }
        } else {
            // Its a rename request
            String newNameAbs = mFileToRename.getParent() + mNewName;
            SmbFile newFile = reallyRenameFile(newNameAbs);
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
        UiHelpers.showLongToast(mContext, doneMessage);
    }

    private SmbFile reallyRenameFile(String newPath) {
        SmbFile newFile = null;
        try {
            newFile = new SmbFile(newPath, mAuth);
            mFileToRename.renameTo(newFile);
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return newFile;
    }
}

package com.pits.smbbrowse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pits.smbbrowse.utils.UiHelpers;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileRenameTask extends AsyncTask<Void, Void, Void> {

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
    protected Void doInBackground(Void... params) {
        String newNameAbs = mFileToRename.getParent() + mNewName;
        try {
            SmbFile newFile = new SmbFile(newNameAbs, mAuth);
            mFileToRename.renameTo(newFile);
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        String toastText = String.format("Renamed to: %s", mNewName);
        UiHelpers.showLongToast(mContext, toastText);
    }
}

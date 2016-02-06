package com.pits.smbbrowse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.jcraft.jsch.JSchException;
import com.pits.smbbrowse.adapters.ContentListAdapter;
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
            String pathCanonical = mFileToRename.getCanonicalPath();
            String server = mFileToRename.getServer();
            String relativePath = pathCanonical.substring(
                    (Constants.HOST_PREFIX + server + "/").length());
            String fileSystemPath = Constants.DIRECTORY_ROOT + relativePath;

            String command = String.format("mv %s %s", fileSystemPath, Constants.LOCATION_MOVED);
            try {
                System.out.println(command);
                Helpers.runRemoteCommand(command);
                doneMessage = String.format("Moved to %s", Constants.LOCATION_MOVED);
            } catch (JSchException e) {
                e.printStackTrace();
                doneMessage = "Operation failed";
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
                String logFileName = Helpers.changeFileExtension(mFileToRename.getName());
                String command = "touch " + Constants.LOCATION_RENAME_LOG + logFileName;
                new RemoteCommandTask().execute(command);
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return mNewFile;
    }
}

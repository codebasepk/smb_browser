package com.pits.smbbrowse.tasks;

import android.os.AsyncTask;

import com.jcraft.jsch.JSchException;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;


public class RemoteCommandTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            Helpers.runRemoteCommand(params[0]);
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (!success) {
            UiHelpers.showLongToast(AppGlobals.getContext(), "Failed to run command");
        }
    }
}
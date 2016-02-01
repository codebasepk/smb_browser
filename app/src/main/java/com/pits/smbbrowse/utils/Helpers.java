package com.pits.smbbrowse.utils;

import android.app.Activity;
import android.widget.Toast;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class Helpers {

    public static void renameRemoteFile(
            final Activity context,
            final SmbFile file,
            final NtlmPasswordAuthentication auth,
            String newName) {

        final String newNameAbs = file.getParent() + newName;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    file.renameTo(new SmbFile(newNameAbs, auth));
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    context,
                                    String.format("Renamed to: %s", file.getName()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                } catch (SmbException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}

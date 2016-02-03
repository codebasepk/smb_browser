package com.pits.smbbrowse.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.tasks.FileRenameTask;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class UiHelpers implements AlertDialog.OnClickListener {

    private SmbFile mFileToDelete;
    private EditText mFileNameField;
    private NtlmPasswordAuthentication mAuth;

    public void showDeleteConfirmationDialog(Activity context, NtlmPasswordAuthentication auth,
                                             SmbFile fileToDelete) {
        mFileToDelete = fileToDelete;
        mAuth = auth;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Do you really want to delete ?");
        builder.setNegativeButton("No", this);
        builder.setPositiveButton("Yes", this);
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE:
                // Just dismiss the dialog, when "No" button is pressed.
                dialog.dismiss();
                break;

            case DialogInterface.BUTTON_POSITIVE:
                // Delete the file and dismiss the dialog.
                try {
                    String filename = mFileToDelete.getName();
                    mFileToDelete.delete();
                    Helpers.createFileLog(mAuth, filename, Helpers.getDeletedLogLocation());
                } catch (SmbException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
        }
    }

    public static void showLongToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void showFileRenameDialog(final Activity activity, final NtlmPasswordAuthentication auth,
                                     final SmbFile fileToRename) {
        mFileNameField = new EditText(activity);
        mFileNameField.setText(fileToRename.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(Constants.DIALOG_TEXT_RENAME);
        builder.setView(mFileNameField);
        builder.setCancelable(true);
        builder.setMessage("Type in the new name");
        builder.setPositiveButton(Constants.DIALOG_TEXT_RENAME,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new FileRenameTask(
                        activity.getApplicationContext(), auth,
                        fileToRename, mFileNameField.getText().toString()).execute();
            }
        });
        builder.setNegativeButton("Cancel", this);
        builder.create();
        builder.show();
    }

    public static void showWifiNotConnectedDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Wifi not connected");
        builder.setMessage(
                "Please ensure wifi is enabled and on the same network as your Samba host.");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.finish();
            }
        });
        builder.create();
        builder.show();
    }
}

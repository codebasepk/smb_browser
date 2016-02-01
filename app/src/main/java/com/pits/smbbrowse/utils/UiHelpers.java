package com.pits.smbbrowse.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.pits.smbbrowse.R;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class UiHelpers implements AlertDialog.OnClickListener {

    private SmbFile mFileToDelete;

    public void showDeleteConfirmationDialog(Activity context, SmbFile fileToDelete) {
        mFileToDelete = fileToDelete;

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
                    mFileToDelete.delete();
                } catch (SmbException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
        }
    }

    public static void showLongToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}

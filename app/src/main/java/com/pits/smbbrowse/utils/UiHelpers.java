package com.pits.smbbrowse.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.pits.smbbrowse.R;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class UiHelpers {

    public static void showDeleteConfirmationDialog(
            final Activity context, final SmbFile fileToDelete) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Do you really want to delete ?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    fileToDelete.delete();
                    Toast.makeText(
                            context,
                            String.format("File: %s deleted", fileToDelete.getName()),
                            Toast.LENGTH_LONG
                    ).show();
                } catch (SmbException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }
}

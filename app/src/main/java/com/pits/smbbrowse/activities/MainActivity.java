package com.pits.smbbrowse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.tasks.BrowseDirectoryTask;
import com.pits.smbbrowse.tasks.FileRenameTask;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Constants;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    private ListView mListView;
    private NtlmPasswordAuthentication mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (AppGlobals.isRunningForTheFirstTime()) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        String sambaHostAddress = AppGlobals.getSambaHostAddress();
        mAuth = Helpers.getAuthenticationCredentials();

        mListView = (ListView) findViewById(R.id.content_list);
        mListView.setOnItemClickListener(this);

        new BrowseDirectoryTask(MainActivity.this, sambaHostAddress, mAuth, mListView).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final SmbFile file = (SmbFile) parent.getItemAtPosition(position);
        try {
            if (file.isFile()) {
                UiHelpers.showLongToast(getApplicationContext(), "Cannot browse a file");
            } else {
                mListView.setAdapter(null);
                new BrowseDirectoryTask(
                        MainActivity.this, file.getCanonicalPath(), mAuth, mListView).execute();
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        int itemIndex = contextMenuInfo.position;
        SmbFile selectedFile = (SmbFile) mListView.getAdapter().getItem(itemIndex);
        UiHelpers uiHelpers = new UiHelpers();

        switch ((String) item.getTitle()) {
            case Constants.DIALOG_TEXT_DELETE:
                uiHelpers.showDeleteConfirmationDialog(MainActivity.this, selectedFile);
                break;
            case Constants.DIALOG_TEXT_RENAME:
                uiHelpers.showFileRenameDialog(MainActivity.this, mAuth, selectedFile);
                break;
            case Constants.DIALOG_TEXT_MOVE:
                new FileRenameTask(
                        getApplicationContext(), mAuth, selectedFile, null).execute();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ListView list = (ListView) v;
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        SmbFile file = (SmbFile) list.getAdapter().getItem(position);

        menu.setHeaderTitle(file.getName());
        menu.add(0, v.getId(), 0, Constants.DIALOG_TEXT_MOVE);
        menu.add(0, v.getId(), 0, Constants.DIALOG_TEXT_RENAME);
        menu.add(0, v.getId(), 0, Constants.DIALOG_TEXT_DELETE);
    }
}

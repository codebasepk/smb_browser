package com.pits.smbbrowse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import com.pits.smbbrowse.R;
import com.pits.smbbrowse.tasks.FileRenameTask;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Constants;
import com.pits.smbbrowse.adapters.ContentListAdapter;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;

import java.net.MalformedURLException;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private NtlmPasswordAuthentication mAuth;
    private String mSambaShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (AppGlobals.isRunningForTheFirstTime()) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        mSambaShare = AppGlobals.getSambaHostAddress();
        mAuth = Helpers.getAuthenticationCredentials();

        mListView = (ListView) findViewById(R.id.content_list);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final SmbFile directory = new SmbFile(mSambaShare, mAuth);
                    SmbFile[] files = directory.listFiles();
                    final List<SmbFile> filteredFiles = Helpers.filterFilesLargerThan(files, 10);
                    final ContentListAdapter adapter = new ContentListAdapter(
                            getApplicationContext(),
                            R.layout.list_item,
                            filteredFiles
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListView.setAdapter(adapter);
                            registerForContextMenu(mListView);
                            final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                                    new SwipeToDismissTouchListener<>(
                                            new ListViewAdapter(mListView),
                                            new SwipeToDismissTouchListener.
                                                    DismissCallbacks<ListViewAdapter>() {
                                                @Override
                                                public boolean canDismiss(int position) {
                                                    return true;
                                                }

                                                @Override
                                                public void onDismiss(ListViewAdapter view, int position) {
                                                }
                                            });
                            mListView.setOnTouchListener(touchListener);
                            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (touchListener.existPendingDismisses()) {
                                        touchListener.undoPendingDismiss();
                                    } else {
                                        final SmbFile file = (SmbFile) parent.getItemAtPosition(position);
                                        try {
                                            if (file.isFile()) {
                                                UiHelpers.showLongToast(getApplicationContext(), "Cannot browse a file");
                                            } else {
                                                mListView.setAdapter(null);
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            SmbFile directory = new SmbFile(file.getCanonicalPath(), mAuth);
                                                            SmbFile[] files = directory.listFiles();
                                                            List<SmbFile> filteredFiles = Helpers.filterFilesLargerThan(files, 10);
                                                            final ContentListAdapter adapter = new ContentListAdapter(
                                                                    getApplicationContext(),
                                                                    R.layout.list_item,
                                                                    filteredFiles
                                                            );
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    mListView.setAdapter(adapter);
                                                                    registerForContextMenu(mListView);
                                                                }
                                                            });
                                                        } catch (MalformedURLException e) {
                                                            e.printStackTrace();
                                                        } catch (SmbException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).start();

                                            }
                                        } catch (SmbException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (SmbException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

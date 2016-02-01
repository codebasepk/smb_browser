package com.pits.smbbrowse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pits.smbbrowse.activities.LoginActivity;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    private ListView mListView;
    private NtlmPasswordAuthentication mAuth;
    private String mSambaShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (AppGlobals.isRunningForTheFirstTime()) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            return;
        }

        String username = AppGlobals.getUsername();
        String password = AppGlobals.getPassword();
        mSambaShare = AppGlobals.getSambaHostAddress();
        mAuth = new NtlmPasswordAuthentication("", username, password);
        mListView = (ListView) findViewById(R.id.content_list);
        mListView.setOnItemClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SmbFile directory = new SmbFile(mSambaShare, mAuth);
                    final ContentListAdapter adapter = new ContentListAdapter(
                            getApplicationContext(),
                            R.layout.list_row,
                            directory.listFiles()
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final SmbFile file = (SmbFile) parent.getItemAtPosition(position);
        try {
            if (file.isFile()) {
                Toast.makeText(
                        getApplicationContext(), "Cannot browse a file", Toast.LENGTH_LONG).show();
            } else {
                mListView.setAdapter(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SmbFile directory = new SmbFile(file.getCanonicalPath(), mAuth);
                            final ContentListAdapter adapter = new ContentListAdapter(
                                    getApplicationContext(),
                                    R.layout.list_row,
                                    directory.listFiles()
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

    private class ContentListAdapter extends ArrayAdapter<SmbFile> {

        public ContentListAdapter(Context context, int resource, SmbFile[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_row, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.file_title);
                holder.size = (TextView) convertView.findViewById(R.id.file_size);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SmbFile file = getItem(position);
            holder.title.setText(file.getName());
            try {
                holder.size.setText(String.valueOf((double) file.length() / 100000) + "mb");
            } catch (SmbException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }

    static class ViewHolder {
        public TextView title;
        public TextView size;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch ((String) item.getTitle()) {
            case "Delete":
                UiHelpers.showDeleteConfirmationDialog(
                        MainActivity.this,
                        (SmbFile) mListView.getAdapter().getItem(index)
                );
                break;
            case "Rename":
                Helpers.renameRemoteFile(
                        MainActivity.this,
                        (SmbFile) mListView.getAdapter().getItem(index),
                        mAuth,
                        "test"
                );
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ListView list = (ListView) v;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        SmbFile file = (SmbFile) list.getAdapter().getItem(position);

        menu.setHeaderTitle(file.getName());
        menu.add(0, v.getId(), 0, "Move");
        menu.add(0, v.getId(), 0, "Rename");
        menu.add(0, v.getId(), 0, "Delete");
    }
}

package com.pits.smbbrowse.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.utils.AppGlobals;

import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;


public class ContentListAdapter extends ArrayAdapter<SmbFile> {

    public ContentListAdapter(Context context, int resource, List<SmbFile> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            Context ctx = AppGlobals.getContext();
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.file_title);
            holder.size = (TextView) convertView.findViewById(R.id.file_size);
            ViewHolder.background = (TextView) convertView.findViewById(R.id.background);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SmbFile file = getItem(position);
        holder.title.setText(file.getName());
        try {
            if (file.isFile()) {
                float sizeToMbs = (float) file.length() / 100000;
                String sizeString = String.format("%fmb", sizeToMbs);
                holder.size.setText(String.valueOf(sizeString));
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView title;
        public TextView size;
        public static TextView background;
    }
}

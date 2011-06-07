/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import org.eclipse.egit.github.core.GistFile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Map;

public class GistFilesListAdapter extends BaseAdapter {
    public static class ViewHolder {
        public TextView text;
    }

    private final Context mContext;

    private final LayoutInflater mInflater;

    private final Map<String, GistFile> mFileMap;

    public GistFilesListAdapter(final Context pContext, final Map<String, GistFile> pFileMap) {
        mContext = pContext;
        mInflater = LayoutInflater.from(mContext);
        mFileMap = pFileMap;
    }

    public int getCount() {
        return mFileMap.size();
    }

    public Object getItem(final int i) {
        return mFileMap.get(mFileMap.keySet().toArray()[i]);
    }

    public long getItemId(final int i) {
        return i;
    }

    public View getView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText((String)mFileMap.keySet().toArray()[index]);
        return convertView;
    }
}

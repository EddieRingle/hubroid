/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BranchListAdapter extends BaseAdapter {
    public static class ViewHolder {
        public TextView text;
    }

    private final Context mContext;

    private JSONObject mJson;

    private final LayoutInflater mInflater;

    public BranchListAdapter(final Context context, final JSONObject jsonobject) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mJson = jsonobject;
    }

    public int getCount() {
        return mJson.length();
    }

    public Object getItem(final int i) {
        try {
            return mJson.names().get(i);
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(final int i) {
        return i;
    }

    public View getView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.branch_list_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.tv_branchListItem_branchName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            holder.text.setText(mJson.names().getString(index));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

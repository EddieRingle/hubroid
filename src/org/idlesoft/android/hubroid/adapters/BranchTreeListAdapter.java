/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.adapters;

import org.idlesoft.android.hubroid.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BranchTreeListAdapter extends BaseAdapter {
    public static class ViewHolder {
        public ImageView icon;

        public TextView name;
    }

    private final Context mContext;

    private JSONArray mJson = new JSONArray();

    private final LayoutInflater mInflater;

    /**
     * Create a new ActivityFeedAdapter
     * 
     * @param context
     * @param jsonarray
     * @param single - whether this is a public activity feed or not
     */
    public BranchTreeListAdapter(final Context context, final JSONArray json) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mJson = json;
    }

    public int getCount() {
        return mJson.length();
    }

    public Object getItem(final int i) {
        try {
            return mJson.get(i);
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
            convertView = mInflater.inflate(R.layout.branch_tree_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_branch_tree_list_item_name);
            holder.icon = (ImageView) convertView.findViewById(R.id.iv_branch_tree_list_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            final JSONObject entry = mJson.getJSONObject(index);

            final String type = entry.getString("type");
            if (type.equals("tree")) {
                holder.icon.setImageResource(R.drawable.folder);
            } else if (type.equals("blob")) {
                holder.icon.setImageResource(R.drawable.file);
            }

            holder.name.setText(entry.getString("name"));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

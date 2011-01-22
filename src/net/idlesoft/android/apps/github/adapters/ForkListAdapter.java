/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
public class ForkListAdapter extends BaseAdapter {
    public static class ViewHolder {
        public TextView text;
    }

    private final Context m_context;

    private JSONArray m_data = new JSONArray();

    private final LayoutInflater m_inflater;

    public ForkListAdapter(final Context context, final JSONArray jsonarray) {
        m_context = context;
        m_inflater = LayoutInflater.from(m_context);
        m_data = jsonarray;
    }

    public int getCount() {
        return m_data.length();
    }

    public Object getItem(final int i) {
        try {
            return m_data.get(i);
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
            convertView = m_inflater.inflate(R.layout.network_list_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.tv_network_username);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            holder.text.setText(m_data.getJSONObject(index).getString("owner"));
            holder.text.setEllipsize(TruncateAt.END);
            holder.text.setTextColor(R.color.textColor);
            holder.text.setMaxLines(1);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

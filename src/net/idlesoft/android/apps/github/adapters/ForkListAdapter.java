/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.json.JSONException;

import android.app.Activity;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

public class ForkListAdapter extends JsonListAdapter {
    public static class ViewHolder {
        public TextView text;
    }

    public ForkListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.network_list_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.tv_network_username);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            holder.text.setText(mJson.getJSONObject(index).getString("owner"));
            holder.text.setEllipsize(TruncateAt.END);
            holder.text.setTextColor(R.color.textColor);
            holder.text.setMaxLines(1);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

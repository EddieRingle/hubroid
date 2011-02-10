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
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

public class RepositoriesListAdapter extends JsonListAdapter {
    public static class ViewHolder {
        public TextView repo_description;

        public TextView repo_fork;

        public TextView repo_fork_count;

        public TextView repo_name;

        public TextView repo_owner;

        public TextView repo_owner_label;

        public TextView repo_watch_count;
    }

    public RepositoriesListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.repository_list_item, null);
            holder = new ViewHolder();
            holder.repo_name = (TextView) convertView.findViewById(R.id.repository_list_item_name);
            holder.repo_owner = (TextView) convertView
                    .findViewById(R.id.repository_list_item_owner);
            holder.repo_owner_label = (TextView) convertView
                    .findViewById(R.id.repository_list_item_owner_label);
            holder.repo_description = (TextView) convertView
                    .findViewById(R.id.repository_list_item_description);
            holder.repo_fork = (TextView) convertView.findViewById(R.id.repository_list_item_fork);
            holder.repo_watch_count = (TextView) convertView
                    .findViewById(R.id.repository_list_item_watch_count);
            holder.repo_fork_count = (TextView) convertView
                    .findViewById(R.id.repository_list_item_fork_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            final JSONObject object = (JSONObject) getData().get(index);
            String owner = "";
            owner = object.getString("owner");
            holder.repo_name.setText(object.getString("name"));
            holder.repo_owner.setText(owner);
            holder.repo_description.setText(object.getString("description"));
            holder.repo_fork_count.setText(object.getString("forks"));
            holder.repo_watch_count.setText(object.getString("watchers"));

            if (object.getBoolean("fork")) {
                holder.repo_fork.setText("(Fork) ");
            } else {
                holder.repo_fork.setText("");
            }
        } catch (final JSONException e) {
            holder.repo_owner.setVisibility(View.GONE);
            holder.repo_owner_label.setVisibility(View.GONE);
            holder.repo_description.setVisibility(View.GONE);
        }
        return convertView;
    }
}

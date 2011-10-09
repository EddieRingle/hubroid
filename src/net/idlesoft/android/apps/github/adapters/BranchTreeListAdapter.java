/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.TreeEntry;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class BranchTreeListAdapter extends ArrayListAdapter<TreeEntry> {
    public static class ViewHolder {
        public ImageView icon;

        public TextView name;
    }

    public BranchTreeListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
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
        final TreeEntry entry = mData.get(index);
		final String type = entry.getType();
		if (type.equals("tree")) {
		    holder.icon.setImageResource(R.drawable.folder);
		} else if (type.equals("blob")) {
		    holder.icon.setImageResource(R.drawable.file);
		}
		holder.name.setText(entry.getPath());
        return convertView;
    }
}

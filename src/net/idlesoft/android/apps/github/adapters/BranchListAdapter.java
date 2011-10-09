/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.Reference;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

public class BranchListAdapter extends ArrayListAdapter<Reference> {
    public static class ViewHolder {
        public TextView text;
    }

    public BranchListAdapter(final Activity pActivity, final AbsListView pListView) {
    	super(pActivity, pListView);
    }

	@Override
	protected View doGetView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.branch_list_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.tv_branchListItem_branchName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(mData.get(position).getRef().replace("refs/heads/", ""));
        return convertView;
	}
}

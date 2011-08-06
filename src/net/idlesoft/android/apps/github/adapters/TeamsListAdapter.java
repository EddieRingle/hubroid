/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import com.github.droidfu.adapters.ListAdapterWithProgress;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.GistListAdapter.ViewHolder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.eclipse.egit.github.core.Team;

public class TeamsListAdapter extends ArrayListAdapter<Team> {
	public static class ViewHolder {
		TextView name;
		TextView other;
	};

	public TeamsListAdapter(Activity pActivity, AbsListView pListView) {
		super(pActivity, pListView);
	}

	@Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.info_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_title);
            holder.other = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(mData.get(index).getName());
        holder.other.setText("Team ID: " + mData.get(index).getId());

        return convertView;
    }
}

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RepositoriesListAdapter extends BaseAdapter {
	private JSONArray m_data = new JSONArray();
	private Context m_context;
	private LayoutInflater m_inflater;

	public static class ViewHolder {
		public TextView repo_name;
		public TextView repo_owner;
		public TextView repo_owner_label;
		public TextView repo_description;
		public TextView repo_fork;
		public TextView repo_watch_count;
		public TextView repo_fork_count;
	}

	public RepositoriesListAdapter(final Context context, JSONArray jsonarray) {
		m_context = context;
		m_inflater = LayoutInflater.from(m_context);
		m_data = jsonarray;
	}

	public int getCount() {
		return m_data.length();
	}

	public Object getItem(int i) {
		try {
			return m_data.get(i);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public long getItemId(int i) {
		return i;
	}

	public View getView(int index, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.repository_list_item, null);
			holder = new ViewHolder();
			holder.repo_name = (TextView) convertView.findViewById(R.id.repository_list_item_name);
			holder.repo_owner = (TextView) convertView.findViewById(R.id.repository_list_item_owner);
			holder.repo_owner_label = (TextView) convertView.findViewById(R.id.repository_list_item_owner_label);
			holder.repo_description = (TextView) convertView.findViewById(R.id.repository_list_item_description);
			holder.repo_fork = (TextView) convertView.findViewById(R.id.repository_list_item_fork);
			holder.repo_watch_count = (TextView) convertView.findViewById(R.id.repository_list_item_watch_count);
			holder.repo_fork_count = (TextView) convertView.findViewById(R.id.repository_list_item_fork_count);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String owner = "";
		try {
			owner = m_data.getJSONObject(index).getString("username");
		} catch (JSONException e) {
			try {
				owner = m_data.getJSONObject(index).getString("owner");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			holder.repo_name.setText(m_data.getJSONObject(index).getString("name"));
			holder.repo_owner.setText(owner);
			holder.repo_description.setText(m_data.getJSONObject(index).getString("description"));
			holder.repo_fork_count.setText(m_data.getJSONObject(index).getString("forks"));
			holder.repo_watch_count.setText(m_data.getJSONObject(index).getString("watchers"));
			if(m_data.getJSONObject(index).getBoolean("fork"))
				holder.repo_fork.setText("(Fork) ");
			else
				holder.repo_fork.setText("");
		} catch (JSONException e) {
			holder.repo_owner.setVisibility(TextView.GONE);
			holder.repo_owner_label.setVisibility(TextView.GONE);
			holder.repo_description.setVisibility(TextView.GONE);
		}
		return convertView;
	}
}
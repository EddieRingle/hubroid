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
	private int m_currentIndex;
	private Context m_context;
	private LayoutInflater m_inflater;

	public static class ViewHolder {
		public TextView repo_name;
		public TextView repo_owner;
		public TextView repo_owner_label;
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
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		try {
			holder.repo_name.setText(m_data.getJSONObject(index).getString("name"));
			holder.repo_owner.setText(m_data.getJSONObject(index).getString("username"));
		} catch (JSONException e) {
			holder.repo_owner.setVisibility(TextView.GONE);
			holder.repo_owner_label.setVisibility(TextView.GONE);
		}
		return convertView;
	}
}
package org.idlesoft.android.hubroid;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class UsersListAdapter extends BaseAdapter {
	protected JSONArray m_data = new JSONArray();
	protected int m_currentIndex;
	protected Context m_context;
	protected LayoutInflater m_inflater;

	public static class ViewHolder {
		public TextView text;
	}

	public UsersListAdapter(final Context context, JSONArray jsonarray) {
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
			convertView = m_inflater.inflate(android.R.layout.simple_list_item_1, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		try {
			holder.text.setText(m_data.getJSONObject(index).getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
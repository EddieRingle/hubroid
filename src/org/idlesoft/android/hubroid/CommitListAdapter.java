package org.idlesoft.android.hubroid;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CommitListAdapter extends BaseAdapter {
	private JSONArray m_data = new JSONArray();
	private int m_currentIndex;
	private Context m_context;
	private LayoutInflater m_inflater;

	public static class ViewHolder {
		public TextView commit_shortdesc;
		public TextView commit_sha;
	}

	public CommitListAdapter(final Context context, JSONArray jsonarray) {
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
			convertView = m_inflater.inflate(R.layout.commit_list_item, null);
			holder = new ViewHolder();
			holder.commit_shortdesc = (TextView) convertView.findViewById(R.id.commit_list_item_shortdesc);
			holder.commit_sha = (TextView) convertView.findViewById(R.id.commit_list_item_sha);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		try {
			holder.commit_shortdesc.setText(m_data.getJSONObject(index).getString("message"));
			holder.commit_sha.setText(m_data.getJSONObject(index).getString("id"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
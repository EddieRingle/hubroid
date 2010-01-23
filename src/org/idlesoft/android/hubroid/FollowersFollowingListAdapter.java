package org.idlesoft.android.hubroid;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FollowersFollowingListAdapter extends UsersListAdapter {

	public FollowersFollowingListAdapter(Context context, JSONArray jsonarray) {
		super(context, jsonarray);
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
		holder.text.setTextColor(R.color.textColor);
		try {
			holder.text.setText(m_data.getString(index));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
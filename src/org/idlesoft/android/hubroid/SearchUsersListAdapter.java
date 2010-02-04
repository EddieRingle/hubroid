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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchUsersListAdapter extends UsersListAdapter {

	public SearchUsersListAdapter(Context context, JSONArray jsonarray) {
		super(context, jsonarray);
		this.loadGravatars();
	}

	@Override
	public void loadGravatars()
	{
		for (int i = 0; !m_data.isNull(i); i++) {
			try {
				Log.d("debug_gravatars", "Loading gravatar #" + i);
				m_gravatars[i] = Hubroid.getGravatar(Hubroid.getGravatarID(m_data.getJSONObject(i).getString("username")), 30);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		m_currentIndex = index;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.user_list_item, null);
			m_currentViewHolder = new ViewHolder();
			m_currentViewHolder.text = (TextView) convertView.findViewById(R.id.tv_user_list_item_name);
			m_currentViewHolder.gravatar = (ImageView) convertView.findViewById(R.id.iv_user_list_gravatar);
			convertView.setTag(m_currentViewHolder);
		} else {
			m_currentViewHolder = (ViewHolder) convertView.getTag();
		}
		m_currentViewHolder.text.setTextColor(R.color.textColor);
		try {
			m_currentViewHolder.text.setText(m_data.getJSONObject(m_currentIndex).getString("username"));
			m_currentViewHolder.gravatar.setImageBitmap(m_gravatars[m_currentIndex]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
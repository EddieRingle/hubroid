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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FollowersFollowingListAdapter extends UsersListAdapter {

	public FollowersFollowingListAdapter(Context context, JSONArray jsonarray) {
		super(context, jsonarray);
		this.loadGravatars();
	}

	public void loadGravatars()
	{
		int length = m_data.length();
		for (int i = 0; i < length; i++) {
			try {
				String username = m_data.getString(i);
				m_gravatars.put(username, Hubroid.getGravatar(Hubroid.getGravatarID(username), 30));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

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
			m_currentViewHolder.text.setText(m_data.getString(m_currentIndex));
			m_currentViewHolder.gravatar.setImageBitmap(m_gravatars.get(m_data.getString(m_currentIndex)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
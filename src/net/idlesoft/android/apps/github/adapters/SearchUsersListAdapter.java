/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
public class SearchUsersListAdapter extends UsersListAdapter {

    public SearchUsersListAdapter(final Context context, final JSONArray jsonarray) {
        super(context, jsonarray);
        loadGravatars();
    }

    public View getView(final int index, View convertView, final ViewGroup parent) {
        m_currentIndex = index;
        if (convertView == null) {
            convertView = m_inflater.inflate(R.layout.user_list_item, null);
            m_currentViewHolder = new ViewHolder();
            m_currentViewHolder.text = (TextView) convertView
                    .findViewById(R.id.tv_user_list_item_name);
            m_currentViewHolder.gravatar = (ImageView) convertView
                    .findViewById(R.id.iv_user_list_gravatar);
            convertView.setTag(m_currentViewHolder);
        } else {
            m_currentViewHolder = (ViewHolder) convertView.getTag();
        }
        m_currentViewHolder.text.setTextColor(R.color.textColor);
        try {
            m_currentViewHolder.text.setText(m_data.getJSONObject(m_currentIndex).getString(
                    "username"));
            m_currentViewHolder.gravatar.setImageBitmap(m_gravatars.get(m_data.getJSONObject(
                    m_currentIndex).getString("username")));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public void loadGravatars() {
        final int length = m_data.length();
        for (int i = 0; i < length; i++) {
            try {
                final String username = m_data.getJSONObject(i).getString("username");
                m_gravatars.put(username, GravatarCache.getDipGravatar(GravatarCache
                        .getGravatarID(username), 30.0f, m_context.getResources()
                        .getDisplayMetrics().density));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }
}


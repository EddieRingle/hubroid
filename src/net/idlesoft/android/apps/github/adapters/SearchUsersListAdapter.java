/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchUsersListAdapter extends GravatarListAdapter {

    public SearchUsersListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.user_list_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.tv_user_list_item_name);
            holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_user_list_gravatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            final String username = ((JSONObject) getData().get(index)).getString("username");
            holder.text.setText(username);
            holder.gravatar.setImageBitmap(mGravatars.get(username));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public void loadGravatars() {
        final int length = mJson.length();
        for (int i = 0; i < length; i++) {
            try {
                final String username = ((JSONObject) mJson.get(i)).getString("username");
                Log.d("hubroid", "Username found: " + username);
                mGravatars.put(username, GravatarCache.getDipGravatar(GravatarCache
                        .getGravatarID(username), 30.0f, mActivity.getResources()
                        .getDisplayMetrics().density));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

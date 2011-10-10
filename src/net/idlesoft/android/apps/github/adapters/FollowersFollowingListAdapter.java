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

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class FollowersFollowingListAdapter extends GravatarJsonListAdapter {

    public FollowersFollowingListAdapter(final Activity pActivity, final AbsListView pListView) {
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
        final String username = (String) getData().get(index);
        holder.text.setText(username);
        holder.gravatar.setImageBitmap(mGravatars.get(username));
        return convertView;
    }

    @Override
    public void loadGravatars() {
        final int length = mData.size();
        for (int i = 0; i < length; i++) {
            try {
                final String username = (String) mData.get(i).getLogin();
                mGravatars.put(username, GravatarCache.getDipGravatar(username, 30.0f, mActivity
                        .getResources().getDisplayMetrics().density));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}

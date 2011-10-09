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
import net.idlesoft.android.apps.github.utils.StringUtils;

import org.eclipse.egit.github.core.RepositoryCommit;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class CommitListAdapter extends GravatarArrayListAdapter<RepositoryCommit> {
    public static class ViewHolder {
        public TextView commit_date;

        public TextView shortdesc;

        public ImageView gravatar;
    }

    public CommitListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.commit_list_item, null);
            holder = new ViewHolder();
            holder.commit_date = (TextView) convertView
                    .findViewById(R.id.tv_commit_list_item_commit_date);
            holder.shortdesc = (TextView) convertView
                    .findViewById(R.id.tv_commit_list_item_shortdesc);
            holder.gravatar = (ImageView) convertView
                    .findViewById(R.id.iv_commit_list_item_gravatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.commit_date.setText(StringUtils.getTimeSince(mData.get(index).getCommit()
                .getCommitter().getDate())
                + " ago");
        holder.gravatar.setImageBitmap(mGravatars.get(mData.get(index).getAuthor().getLogin()));
        holder.shortdesc.setText(mData.get(index).getCommit().getMessage().split("\n")[0]);
        return convertView;
    }

    /**
     * Get the Gravatars of all users in the commit log
     */
    public void loadGravatars() {
        final int length = mData.size();
        for (int i = 0; i < length; i++) {
            final String login = mData.get(i).getAuthor().getLogin();
            if (!mGravatars.containsKey(login)) {
                mGravatars.put(login, GravatarCache.getDipGravatar(GravatarCache
                        .getGravatarID(login), 30.0f,
                        mActivity.getResources().getDisplayMetrics().density));
            }
        }
    }
}

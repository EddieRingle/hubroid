/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.BaseActivity;
import net.idlesoft.android.apps.github.utils.GravatarCache;
import net.idlesoft.android.apps.github.utils.NewsFeedHelpers;

import org.eclipse.egit.github.core.event.Event;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public class ActivityFeedAdapter extends GravatarArrayListAdapter<Event> {
    public static class ViewHolder {
        public TextView date;

        public ImageView gravatar;

        public ImageView icon;

        public TextView title;
    }

    private HashMap<String, Bitmap> mGravatars;

    private final boolean mIsSingleUser;

    public ActivityFeedAdapter(final Activity pActivity, final AbsListView pListView,
            final boolean single) {
        super(pActivity, pListView);

        mIsSingleUser = single;
    }

    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_item, null);
            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.tv_activity_item_date);
            holder.title = (TextView) convertView.findViewById(R.id.tv_activity_item_title);
            holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_activity_item_gravatar);
            holder.icon = (ImageView) convertView.findViewById(R.id.iv_activity_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Event entry = (Event) getData().get(index);

        NewsFeedHelpers.buildEventEntry((BaseActivity)mActivity, holder, entry, mGravatars.get(entry.getActor().getLogin()));

        return convertView;
    }

    /**
     * Get the Gravatars of all users in the commit log This method is different
     * from the gravatar loaders in other adapters, in that we only need to get
     * the first one if we are displaying a public activity feed for a single
     * user
     */
    public void loadGravatars() {
        if (mGravatars == null) {
            mGravatars = new HashMap<String, Bitmap>();
        }
        if (mIsSingleUser) {
            // Load only the first gravatar
            final String actor = mData.get(0).getActor().getLogin();
            mGravatars.put(actor, GravatarCache.getDipGravatar(actor, 30.0f, mActivity
                    .getResources().getDisplayMetrics().density));
        } else {
            // Load all of 'em
            final int length = mData.size();
            for (int i = 0; i < length; i++) {
                final String actor = mData.get(i).getActor().getLogin();
                if (!mGravatars.containsKey(actor)) {
                    mGravatars.put(actor, GravatarCache.getDipGravatar(actor, 30.0f, mActivity
                            .getResources().getDisplayMetrics().density));
                }
            }
        }
    }

    @Override
    public void pushData() {
        super.pushData();
    }
}

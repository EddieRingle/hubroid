/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Hubroid;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.json.JSONException;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IssueCommentsAdapter extends GravatarJsonListAdapter {
    public static class ViewHolder {
        public TextView body;

        public ImageView gravatar;

        public TextView meta;
    }

    public IssueCommentsAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.issue_comment, null);
            holder = new ViewHolder();
            holder.meta = (TextView) convertView.findViewById(R.id.tv_issue_comment_meta);
            holder.body = (TextView) convertView.findViewById(R.id.tv_issue_comment_body);
            holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_issue_comment_gravatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date item_time = dateFormat.parse(mJson.getJSONObject(index).getString(
                    "created_at"));
            final Date current_time = new Date();
            final long ms = current_time.getTime() - item_time.getTime();
            final long sec = ms / 1000;
            final long min = sec / 60;
            final long hour = min / 60;
            final long day = hour / 24;
            final long year = day / 365;
            if (year > 0) {
                if (year == 1) {
                    end = " year ago";
                } else {
                    end = " years ago";
                }
                holder.meta.setText("Posted " + year + end + " by "
                        + mJson.getJSONObject(index).getString("user"));
            }
            if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                holder.meta.setText("Posted " + day + end + " by "
                        + mJson.getJSONObject(index).getString("user"));
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                holder.meta.setText("Posted " + hour + end + " by "
                        + mJson.getJSONObject(index).getString("user"));
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                holder.meta.setText("Posted " + min + end + " by "
                        + mJson.getJSONObject(index).getString("user"));
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                holder.meta.setText("Posted " + sec + end + " by "
                        + mJson.getJSONObject(index).getString("user"));
            }
            holder.gravatar.setImageBitmap(mGravatars.get(mJson.getJSONObject(index).getString(
                    "user")));
            holder.body.setText(mJson.getJSONObject(index).getString("body")
                    .replaceAll("\r\n", "\n").replaceAll("\r", "\n"));
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    /**
     * Get the Gravatars of all users in the commit log This method is different
     * from the gravatar loaders in other adapters, in that we only need to get
     * the first one if we are displaying a public activity feed for a single
     * user
     */
    @Override
    public void loadGravatars() {
        try {
            final int length = mJson.length();
            for (int i = 0; i < length; i++) {
                final String actor = mJson.getJSONObject(i).getString("user");
                if (!mGravatars.containsKey(actor)) {
                    mGravatars.put(actor, GravatarCache.getDipGravatar(GravatarCache
                            .getGravatarID(actor), 30.0f, mActivity.getResources()
                            .getDisplayMetrics().density));
                }
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }
}

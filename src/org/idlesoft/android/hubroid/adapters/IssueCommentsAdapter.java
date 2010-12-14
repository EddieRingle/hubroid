/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.adapters;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.Hubroid;
import org.idlesoft.android.hubroid.utils.GravatarCache;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class IssueCommentsAdapter extends BaseAdapter {
    public static class ViewHolder {
        public TextView body;

        public ImageView gravatar;

        public TextView meta;
    }

    private final Context m_context;

    private JSONArray m_data = new JSONArray();

    private final HashMap<String, Bitmap> m_gravatars;

    private final LayoutInflater m_inflater;

    /**
     * Create a new IssueCommentsAdapter
     * 
     * @param context
     * @param jsonarray
     */
    public IssueCommentsAdapter(final Context context, final JSONArray json) {
        m_context = context;
        m_inflater = LayoutInflater.from(m_context);
        m_data = json;
        m_gravatars = new HashMap<String, Bitmap>(m_data.length());

        loadGravatars();
    }

    public int getCount() {
        return m_data.length();
    }

    public Object getItem(final int i) {
        try {
            return m_data.get(i);
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(final int i) {
        return i;
    }

    public View getView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = m_inflater.inflate(R.layout.issue_comment, null);
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
            final Date item_time = dateFormat.parse(m_data.getJSONObject(index).getString(
                    "created_at"));
            final Date current_time = dateFormat.parse(dateFormat.format(new Date()));
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
                        + m_data.getJSONObject(index).getString("user"));
            }
            if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                holder.meta.setText("Posted " + day + end + " by "
                        + m_data.getJSONObject(index).getString("user"));
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                holder.meta.setText("Posted " + hour + end + " by "
                        + m_data.getJSONObject(index).getString("user"));
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                holder.meta.setText("Posted " + min + end + " by "
                        + m_data.getJSONObject(index).getString("user"));
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                holder.meta.setText("Posted " + sec + end + " by "
                        + m_data.getJSONObject(index).getString("user"));
            }
            holder.gravatar.setImageBitmap(m_gravatars.get(m_data.getJSONObject(index).getString(
                    "user")));
            holder.body.setText(m_data.getJSONObject(index).getString("body").replaceAll("\r\n",
                    "\n").replaceAll("\r", "\n"));
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
    public void loadGravatars() {
        try {
            final int length = m_data.length();
            for (int i = 0; i < length; i++) {
                final String actor = m_data.getJSONObject(i).getString("user");
                if (!m_gravatars.containsKey(actor)) {
                    m_gravatars.put(actor, GravatarCache.getDipGravatar(GravatarCache
                            .getGravatarID(actor), 30.0f, m_context.getResources()
                            .getDisplayMetrics().density));
                }
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }
}

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

public class CommitListAdapter extends BaseAdapter {
    public static class ViewHolder {
        public TextView commit_date;

        public TextView commit_shortdesc;

        public ImageView gravatar;
    }

    private final Context mContext;

    private final HashMap<String, Bitmap> mGravatars;

    private final LayoutInflater mInflater;

    private JSONArray mJson = new JSONArray();

    public CommitListAdapter(final Context context, final JSONArray jsonarray) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mJson = jsonarray;
        mGravatars = new HashMap<String, Bitmap>(mJson.length());

        loadGravatars();
    }

    public int getCount() {
        return mJson.length();
    }

    public Object getItem(final int i) {
        try {
            return mJson.get(i);
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
            convertView = mInflater.inflate(R.layout.commit_list_item, null);
            holder = new ViewHolder();
            holder.commit_date = (TextView) convertView
                    .findViewById(R.id.tv_commit_list_item_commit_date);
            holder.commit_shortdesc = (TextView) convertView
                    .findViewById(R.id.tv_commit_list_item_shortdesc);
            holder.gravatar = (ImageView) convertView
                    .findViewById(R.id.iv_commit_list_item_gravatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
            final Date commit_time = dateFormat.parse(mJson.getJSONObject(index).getString(
                    "committed_date"));
            final Date current_time = dateFormat.parse(dateFormat.format(new Date()));
            final long ms = current_time.getTime() - commit_time.getTime();
            final long sec = ms / 1000;
            final long min = sec / 60;
            final long hour = min / 60;
            final long day = hour / 24;
            if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                holder.commit_date.setText(day + end);
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                holder.commit_date.setText(hour + end);
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                holder.commit_date.setText(min + end);
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                holder.commit_date.setText(sec + end);
            }
            holder.gravatar.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.gravatar_border));
            holder.gravatar.setImageBitmap(mGravatars.get(mJson.getJSONObject(index).getJSONObject(
                    "author").getString("login")));
            holder.commit_shortdesc.setText(mJson.getJSONObject(index).getString("message").split(
                    "\n")[0]);
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    /**
     * Get the Gravatars of all users in the commit log
     */
    public void loadGravatars() {
        final int length = mJson.length();
        for (int i = 0; i < length; i++) {
            try {
                final String login = mJson.getJSONObject(i).getJSONObject("author").getString(
                        "login");
                if (!mGravatars.containsKey(login)) {
                    mGravatars.put(login, GravatarCache.getDipGravatar(GravatarCache
                            .getGravatarID(login), 30.0f, mContext.getResources()
                            .getDisplayMetrics().density));
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.Hubroid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityFeedAdapter extends BaseAdapter {
    private JSONArray m_data = new JSONArray();
    private Context m_context;
    private LayoutInflater m_inflater;
    private HashMap<String, Bitmap> m_gravatars;
    private boolean m_single;

    public static class ViewHolder {
        public TextView title;
        public ImageView gravatar;
        public ImageView icon;
        public TextView date;
    }

    /**
     * Get the Gravatars of all users in the commit log
     * 
     * This method is different from the gravatar loaders in other adapters, in that we only need to
     * get the first one if we are displaying a public activity feed for a single user
     */
    public void loadGravatars() {
        try {
            if (m_single) {
                // Load only the first gravatar
                String actor = m_data.getJSONObject(0).getString("actor");
                m_gravatars.put(actor, Hubroid.getGravatar(Hubroid.getGravatarID(actor), 30));
            } else {
                // Load all of 'em
                int length = m_data.length();
                for (int i = 0; i < length; i++) {
                    String actor = m_data.getJSONObject(i).getString("actor");
                    if (!m_gravatars.containsKey(actor)) {
                        m_gravatars.put(actor,
                                Hubroid.getGravatar(Hubroid.getGravatarID(actor), 30));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new ActivityFeedAdapter
     * 
     * @param context
     * @param jsonarray
     * @param single
     *            - whether this is a public activity feed or not
     */
    public ActivityFeedAdapter(final Context context, JSONArray json, boolean single) {
        m_context = context;
        m_inflater = LayoutInflater.from(m_context);
        m_data = json;
        m_single = single;
        m_gravatars = new HashMap<String, Bitmap>(m_data.length());

        this.loadGravatars();
    }

    public int getCount() {
        return m_data.length();
    }

    public Object getItem(int i) {
        try {
            return m_data.get(i);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = m_inflater.inflate(R.layout.activity_item, null);
            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.tv_activity_item_date);
            holder.title = (TextView) convertView.findViewById(R.id.tv_activity_item_title);
            holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_activity_item_gravatar);
            holder.icon = (ImageView) convertView.findViewById(R.id.iv_activity_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            JSONObject entry = m_data.getJSONObject(index);
            JSONObject payload = entry.getJSONObject("payload");
            String end;
            SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            Date item_time = dateFormat.parse(entry.getString("created_at"));
            Date current_time = dateFormat.parse(dateFormat.format(new Date()));
            long ms = current_time.getTime() - item_time.getTime();
            long sec = ms / 1000;
            long min = sec / 60;
            long hour = min / 60;
            long day = hour / 24;
            if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                holder.date.setText(day + end);
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                holder.date.setText(hour + end);
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                holder.date.setText(min + end);
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                holder.date.setText(sec + end);
            }

            String actor = entry.getString("actor");
            String eventType = entry.getString("type");
            String title = actor + " did something...";
            holder.gravatar.setImageBitmap(m_gravatars.get(actor));

            if (eventType.contains("PushEvent")) {
                holder.icon.setImageResource(R.drawable.push);
                title = actor + " pushed to " + payload.getString("ref").split("/")[2] + " at "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("WatchEvent")) {
                String action = payload.getString("action");
                if (action.equalsIgnoreCase("started")) {
                    holder.icon.setImageResource(R.drawable.watch_started);
                } else {
                    holder.icon.setImageResource(R.drawable.watch_stopped);
                }
                title = actor + " " + action + " watching "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("GistEvent")) {
                String action = payload.getString("action");
                holder.icon.setImageResource(R.drawable.gist);
                title = actor + " " + action + "d " + payload.getString("name");
            } else if (eventType.contains("ForkEvent")) {
                holder.icon.setImageResource(R.drawable.fork);
                title = actor + " forked " + entry.getJSONObject("repository").getString("name")
                        + "/" + entry.getJSONObject("repository").getString("owner");
            } else if (eventType.contains("CommitCommentEvent")) {
                holder.icon.setImageResource(R.drawable.comment);
                title = actor + " commented on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("ForkApplyEvent")) {
                holder.icon.setImageResource(R.drawable.merge);
                title = actor + " applied fork commits to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("FollowEvent")) {
                holder.icon.setImageResource(R.drawable.follow);
                title = actor + " started following " + payload.getString("target");
            } else if (eventType.contains("CreateEvent")) {
                holder.icon.setImageResource(R.drawable.create);
                if (payload.getString("object").contains("repository")) {
                    title = actor + " created repository " + payload.getString("name");
                } else if (payload.getString("object").contains("branch")) {
                    title = actor + " created branch " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("object").contains("tag")) {
                    title = actor + " created tag " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("IssuesEvent")) {
                if (payload.getString("action").equalsIgnoreCase("opened")) {
                    holder.icon.setImageResource(R.drawable.issues_open);
                } else {
                    holder.icon.setImageResource(R.drawable.issues_closed);
                }
                title = actor + " " + payload.getString("action") + " issue "
                        + payload.getInt("number") + " on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("DeleteEvent")) {
                holder.icon.setImageResource(R.drawable.delete);
                if (payload.getString("object").contains("repository")) {
                    title = actor + " deleted repository " + payload.getString("name");
                } else if (payload.getString("object").contains("branch")) {
                    title = actor + " deleted branch " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("object").contains("tag")) {
                    title = actor + " deleted tag " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("WikiEvent")) {
                holder.icon.setImageResource(R.drawable.wiki);
                title = actor + " " + payload.getString("action") + " a page in the "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name") + " wiki";
            } else if (eventType.contains("DownloadEvent")) {
                holder.icon.setImageResource(R.drawable.download);
                title = actor + " uploaded a file to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("PublicEvent")) {
                holder.icon.setImageResource(R.drawable.opensource);
                title = actor + " open sourced "
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("PullRequestEvent")) {
                if (payload.getString("action").equalsIgnoreCase("opened")) {
                    holder.icon.setImageResource(R.drawable.issues_open);
                    title = actor + " opened pull request " + payload.getInt("number") + " on "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("action").equalsIgnoreCase("closed")) {
                    holder.icon.setImageResource(R.drawable.issues_closed);
                    title = actor + " closed pull request " + payload.getInt("number") + " on "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("MemberEvent")) {
                holder.icon.setImageResource(R.drawable.follow);
                title = actor + " added " + payload.getString("member") + " to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            }
            holder.title.setText(title);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}
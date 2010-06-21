/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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
	 * This method is different from the gravatar loaders in other adapters,
	 * in that we only need to get the first one if we are displaying
	 * a public activity feed for a single user
	 */
	public void loadGravatars()
	{
		try {
			if (m_single) {
				// Load only the first gravatar
				String actor = m_data.getJSONObject(0).getJSONObject("author").getString("name");
				m_gravatars.put(actor, Hubroid.getGravatar(Hubroid.getGravatarID(actor), 30));
			} else {
				// Load all of 'em
				int length = m_data.length();
				for (int i = 0; i < length; i++) {
					String actor = m_data.getJSONObject(i).getJSONObject("author").getString("name");
					if (!m_gravatars.containsKey(actor)) {
						m_gravatars.put(actor, Hubroid.getGravatar(Hubroid.getGravatarID(actor), 30));
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
	 * @param single - whether this is a public activity feed or not
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
			String end;
			SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
			Date item_time = dateFormat.parse(m_data.getJSONObject(index).getString("published"));
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
			holder.gravatar.setImageBitmap(m_gravatars.get(m_data.getJSONObject(index).getJSONObject("author").getString("name")));

			String eventType = m_data.getJSONObject(index).getString("id");
			String title = m_data.getJSONObject(index).getString("title");
			if (eventType.contains("PushEvent")) {
				holder.icon.setImageResource(R.drawable.push);
			} else if (eventType.contains("WatchEvent")) {
				if (title.contains(" started ")) {
					holder.icon.setImageResource(R.drawable.watch_started);
				} else {
					holder.icon.setImageResource(R.drawable.watch_stopped);
				}
			} else if (eventType.contains("GistEvent")) {
				holder.icon.setImageResource(R.drawable.gist);
			} else if (eventType.contains("ForkEvent")) {
				holder.icon.setImageResource(R.drawable.fork);
			} else if (eventType.contains("CommitCommentEvent")) {
				holder.icon.setImageResource(R.drawable.comment);
			} else if (eventType.contains("ForkApplyEvent")) {
				holder.icon.setImageResource(R.drawable.merge);
			} else if (eventType.contains("FollowEvent")) {
				holder.icon.setImageResource(R.drawable.follow);
			} else if (eventType.contains("CreateEvent")) {
				holder.icon.setImageResource(R.drawable.create);
			} else if (eventType.contains("IssuesEvent")) {
				if (title.contains(" opened ")) {
					holder.icon.setImageResource(R.drawable.issues_open);
				} else if (title.contains(" closed ")) {
					holder.icon.setImageResource(R.drawable.issues_closed);
				}
			} else if (eventType.contains("DeleteEvent")) {
				holder.icon.setImageResource(R.drawable.delete);
			} else if (eventType.contains("WikiEvent")) {
				holder.icon.setImageResource(R.drawable.wiki);
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
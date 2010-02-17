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
	private Bitmap[] m_gravatars;
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
				m_gravatars[0] = Hubroid.getGravatar(Hubroid.getGravatarID(m_data.getJSONObject(0).getString("actor")), 30);
			} else {
				// Load all of 'em
				for (int i = 0; i < m_data.length(); i++) {
					String actor = m_data.getJSONObject(i).getString("actor");
					if (!actor.equals("")) {
						String id = Hubroid.getGravatarID(actor);
						m_gravatars[i] = Hubroid.getGravatar(id, 30);
					} else {
						m_gravatars[i] = null;
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
	public ActivityFeedAdapter(final Context context, JSONArray jsonarray, boolean single) {
		m_context = context;
		m_inflater = LayoutInflater.from(m_context);
		m_data = jsonarray;
		m_single = single;
		m_gravatars = (!single) ? new Bitmap[m_data.length()] : new Bitmap[1];

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
			JSONObject payload = m_data.getJSONObject(index).getJSONObject("payload");
			String end;
			SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_ISSUES_TIME_FORMAT);
			Date item_time = dateFormat.parse(m_data.getJSONObject(index).getString("created_at"));
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
			if (m_single) {
				holder.gravatar.setImageBitmap(m_gravatars[0]);
			} else {
				holder.gravatar.setImageBitmap(m_gravatars[index]);
			}

			String eventType = m_data.getJSONObject(index).getString("type");
			String actor = m_data.getJSONObject(index).getString("actor");
			String title = "";
			if (eventType.equals("PushEvent")) {
				String branch = payload.getString("ref").split("/")[2];
				String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
				String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
				title = actor + " pushed to " + branch + " at " + owner + "/" + repository;
				holder.icon.setImageResource(R.drawable.push);
			} else if (eventType.equals("WatchEvent")) {
				if (payload.getString("action").equals("started")) {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " started watching " + owner + "/" + repository;
					holder.icon.setImageResource(R.drawable.watch_started);
				} else {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " stopped watching " + owner + "/" + repository;
					holder.icon.setImageResource(R.drawable.watch_stopped);
				}
			} else if (eventType.equals("GistEvent")) {
				if (payload.getString("action").equals("create")) {
					title = actor + " created " + payload.getString("name");
				} else {
					title = actor + " updated " + payload.getString("name");
				}
				holder.icon.setImageResource(R.drawable.gist);
			} else if (eventType.equals("ForkEvent")) {
				String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
				String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
				title = actor + " forked " + owner + "/" + repository;
				holder.icon.setImageResource(R.drawable.fork);
			} else if (eventType.equals("CommitCommentEvent")) {
				String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
				String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
				title = actor + " commented on " + owner + "/" + repository;
				holder.icon.setImageResource(R.drawable.comment);
			} else if (eventType.equals("ForkApplyEvent")) {
				String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
				String branch = payload.getString("head");
				title = actor + " applied fork commits to " + repository + "/" + branch;
				holder.icon.setImageResource(R.drawable.merge);
			} else if (eventType.equals("FollowEvent")) {
				title = actor + " started following " + payload.getString("target");
				holder.icon.setImageResource(R.drawable.follow);
			} else if (eventType.equals("CreateEvent")) {
				if (payload.getString("object").equals("branch")) {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " created branch " + payload.getString("object_name") + " at " + owner + "/" + repository;
				} else if (payload.getString("object").equals("repository")) {
					title = actor + " created repository " + payload.getString("name");
				} else if (payload.getString("object").equals("tag")) {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " created tag " + payload.getString("object_name") + " at " + owner + "/" + repository;
				}
				holder.icon.setImageResource(R.drawable.create);
			} else if (eventType.equals("IssuesEvent")) {
				if (payload.getString("action").equals("opened")) {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " opened an issue at " + owner + "/" + repository;
					holder.icon.setImageResource(R.drawable.issues_open);
				} else if (payload.getString("action").equals("closed")) {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " closed an issue at " + owner + "/" + repository;
					holder.icon.setImageResource(R.drawable.issues_closed);
				}
			} else if (eventType.equals("DeleteEvent")) {
				if (payload.getString("object").equals("branch")) {
					String repository = payload.getString("name");
					title = actor + " deleted branch " + payload.getString("object_name") + " at " + actor + "/" + repository;
				} else if (payload.getString("object").equals("repository")) {
					title = actor + " deleted repository " + payload.getString("name");
				} else if (payload.getString("object").equals("tag")) {
					String repository = m_data.getJSONObject(index).getJSONObject("repository").getString("name");
					String owner = m_data.getJSONObject(index).getJSONObject("repository").getString("owner");
					title = actor + " deleted tag " + payload.getString("object_name") + " at " + owner + "/" + repository;
				}
				holder.icon.setImageResource(R.drawable.delete);
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
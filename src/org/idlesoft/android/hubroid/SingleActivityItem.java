/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class SingleActivityItem extends Activity {
	public ProgressDialog m_progressDialog;
	public Intent m_intent;
	private JSONObject m_JSON = new JSONObject();
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private String m_username;
	private String m_token;

	public static final String CSS =
		"<style type=\"text/css\">" +
		"div, ul, li, blockquote {" +
		"font-size: 14px;" +
		"}" +
		"blockquote {" +
		"color: #999;" +
		"margin: 0;" +
		"}" +
		"</style>";

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.hasVisibleItems()) menu.clear();
		menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, 1, 0, "Clear Preferences");
		menu.add(0, 2, 0, "Clear Cache");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent i1 = new Intent(this, Hubroid.class);
			startActivity(i1);
			return true;
		case 1:
			m_editor.clear().commit();
			Intent intent = new Intent(this, Hubroid.class);
			startActivity(intent);
        	return true;
		case 2:
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite()) {
				File hubroid = new File(root, "hubroid");
				if (!hubroid.exists() && !hubroid.isDirectory()) {
					return true;
				} else {
					hubroid.delete();
					return true;
				}
			}
		}
		return false;
	}

	private void loadActivityItemBox() {
		TextView date = (TextView)findViewById(R.id.tv_activity_item_date);
		ImageView gravatar = (ImageView)findViewById(R.id.iv_activity_item_gravatar);
		ImageView icon	= (ImageView)findViewById(R.id.iv_activity_item_icon);
		TextView title_tv = (TextView)findViewById(R.id.tv_activity_item_title);

		TextView topbar = (TextView)findViewById(R.id.tv_top_bar_title);

		try {
			String end;
			SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
			Date item_time = dateFormat.parse(m_JSON.getString("published"));
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
				date.setText(day + end);
			} else if (hour > 0) {
				if (hour == 1) {
					end = " hour ago";
				} else {
					end = " hours ago";
				}
				date.setText(hour + end);
			} else if (min > 0) {
				if (min == 1) {
					end = " minute ago";
				} else {
					end = " minutes ago";
				}
				date.setText(min + end);
			} else {
				if (sec == 1) {
					end = " second ago";
				} else {
					end = " seconds ago";
				}
				date.setText(sec + end);
			}
			gravatar.setImageBitmap(Hubroid.getGravatar(Hubroid.getGravatarID(m_JSON.getJSONObject("author").getString("name")), 30));
	
			String eventType = m_JSON.getString("id");
			String title = m_JSON.getString("title");
			if (eventType.contains("PushEvent")) {
				topbar.setText("Push");
				icon.setImageResource(R.drawable.push);
			} else if (eventType.contains("WatchEvent")) {
				topbar.setText("Watch");
				if (title.contains(" started ")) {
					icon.setImageResource(R.drawable.watch_started);
				} else {
					icon.setImageResource(R.drawable.watch_stopped);
				}
			} else if (eventType.contains("GistEvent")) {
				topbar.setText("Gist");
				icon.setImageResource(R.drawable.gist);
			} else if (eventType.contains("ForkEvent")) {
				topbar.setText("Fork");
				icon.setImageResource(R.drawable.fork);
			} else if (eventType.contains("CommitCommentEvent")) {
				topbar.setText("Comment");
				icon.setImageResource(R.drawable.comment);
			} else if (eventType.contains("ForkApplyEvent")) {
				topbar.setText("Merge");
				icon.setImageResource(R.drawable.merge);
			} else if (eventType.contains("FollowEvent")) {
				topbar.setText("Follow");
				icon.setImageResource(R.drawable.follow);
			} else if (eventType.contains("CreateEvent")) {
				topbar.setText("Create");
				icon.setImageResource(R.drawable.create);
			} else if (eventType.contains("IssuesEvent")) {
				topbar.setText("Issues");
				if (title.contains(" opened ")) {
					icon.setImageResource(R.drawable.issues_open);
				} else if (title.contains(" closed ")) {
					icon.setImageResource(R.drawable.issues_closed);
				}
			} else if (eventType.contains("DeleteEvent")) {
				topbar.setText("Delete");
				icon.setImageResource(R.drawable.delete);
			} else if (eventType.contains("WikiEvent")) {
				topbar.setText("Wiki");
				icon.setImageResource(R.drawable.wiki);
			}
			title_tv.setText(title);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.single_activity_item);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	try {
				m_JSON = new JSONObject(extras.getString("item_json"));
				loadActivityItemBox();
				WebView content = (WebView)findViewById(R.id.wv_single_activity_item_content);
				String html = m_JSON.getJSONObject("content").getString("content");
				html = html.replace('\n', ' ');
				String out = CSS + html;
				content.loadData(out, "text/" + m_JSON.getJSONObject("content").getString("type"), "UTF-8");
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }
}

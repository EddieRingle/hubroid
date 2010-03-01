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

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIBase.Response;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SingleIssue extends Activity {
	public ProgressDialog m_progressDialog;
	public Intent m_intent;
	private IssueCommentsAdapter m_adapter;
	private JSONObject m_JSON = new JSONObject();
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private String m_repoOwner;
	private String m_repoName;
	private String m_username;
	private String m_token;
	private static final GitHubAPI gh = new GitHubAPI();
	private View m_header;
	private View m_issueBox;

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

	private void loadIssueItemBox() {
		TextView date = (TextView)m_header.findViewById(R.id.tv_issue_list_item_updated_date);
		ImageView icon	= (ImageView)m_header.findViewById(R.id.iv_issue_list_item_icon);
		TextView title = (TextView)m_header.findViewById(R.id.tv_issue_list_item_title);
		TextView number = (TextView)m_header.findViewById(R.id.tv_issue_list_item_number);

		TextView topbar = (TextView)findViewById(R.id.tv_top_bar_title);

		try {
			String end;
			SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_ISSUES_TIME_FORMAT);
			Date item_time = dateFormat.parse(m_JSON.getString("updated_at"));
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
			if (m_JSON.getString("state").equalsIgnoreCase("open")) {
				icon.setImageResource(R.drawable.issues_open);
			} else {
				icon.setImageResource(R.drawable.issues_closed);
			}
			number.setText("#" + m_JSON.getString("number"));
			title.setText(m_JSON.getString("title"));
			topbar.setText(number.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.single_issue);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	try {
        		m_repoOwner = extras.getString("repoOwner");
        		m_repoName = extras.getString("repoName");
				m_JSON = new JSONObject(extras.getString("item_json"));
				m_issueBox = getLayoutInflater().inflate(R.layout.issue_list_item, null);
				
				m_header = getLayoutInflater().inflate(R.layout.issue_header, null);
				loadIssueItemBox();
				((ListView)findViewById(R.id.lv_single_issue_comments)).addHeaderView(m_header);
				((TextView)m_header.findViewById(R.id.tv_single_issue_body)).setText(m_JSON.getString("body"));

				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							Response response = gh.Issues.list_comments(m_repoOwner, m_repoName, m_JSON.getInt("number"), m_username, m_token);
							m_adapter = new IssueCommentsAdapter(getApplicationContext(), new JSONObject(response.resp).getJSONArray("comments"));
							runOnUiThread(new Runnable() {
								public void run() {
									((ListView)findViewById(R.id.lv_single_issue_comments)).setAdapter(m_adapter);
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				thread.start();
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }
}

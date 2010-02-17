/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.File;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIBase.Response;
import org.json.JSONArray;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityFeeds extends Activity {
	private GitHubAPI gh;
	private ActivityFeedAdapter m_publicActivityAdapter;
	private ActivityFeedAdapter m_privateActivityAdapter;
	private ProgressDialog m_progressDialog;
	private String m_targetUser;
	private SharedPreferences m_prefs;
	private String m_username;
	private String m_token;
	private boolean m_privateDisabled;
	private String m_type;

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
			menu.add(0, 1, 0, "Clear Preferences");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent i1 = new Intent(this, Hubroid.class);
			startActivity(i1);
			return true;
		case 1:
			getSharedPreferences(Hubroid.PREFS_NAME, 0).edit().clear().commit();
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

	private void toggleList(String type)
	{
		ListView publicList = (ListView) findViewById(R.id.lv_activity_feeds_public_list);
		ListView privateList = (ListView) findViewById(R.id.lv_activity_feeds_private_list);
		TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

		if (type.equals("") || type == null) {
			type = (m_type.equals("public")) ? "private" : "public";
		}
		m_type = type;

		if (m_type.equals("public")) {
			publicList.setVisibility(View.VISIBLE);
			privateList.setVisibility(View.GONE);
			title.setText(m_targetUser + "'s Activity");
		} else if (m_type.equals("private")) {
			privateList.setVisibility(View.VISIBLE);
			publicList.setVisibility(View.GONE);
			title.setText("News Feed");
		}
	}

	private OnClickListener onButtonToggleClickListener = new OnClickListener() {
		public void onClick(View v) {
			if(v.getId() == R.id.btn_activity_feeds_public) {
				toggleList("public");
				m_type = "public";
			} else if(v.getId() == R.id.btn_activity_feeds_private) {
				toggleList("private");
				m_type = "private";
			}
		}
	};

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_feeds);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        gh = new GitHubAPI();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_targetUser = extras.getString("username");
        	m_privateDisabled = (m_targetUser.equalsIgnoreCase(m_username)) ? false : true;

        	if (m_privateDisabled) {
        		m_type = "public";
        		((ListView)findViewById(R.id.lv_activity_feeds_private_list)).setVisibility(View.GONE);
        		TextView title = (TextView)findViewById(R.id.tv_top_bar_title);
        		title.setText(m_targetUser + "'s Activity");
        		((LinearLayout)findViewById(R.id.ll_activity_feeds_button_holder)).setVisibility(View.GONE);
        	} else {
        		m_type = "private";
        		TextView title = (TextView)findViewById(R.id.tv_top_bar_title);
        		title.setText("News Feed");
        		((Button)findViewById(R.id.btn_activity_feeds_private)).setOnClickListener(onButtonToggleClickListener);
        		((Button)findViewById(R.id.btn_activity_feeds_public)).setOnClickListener(onButtonToggleClickListener);
        	}
        	m_progressDialog = ProgressDialog.show(this, "Please Wait", "Loading activity feeds... (this may take awhile)");
            Thread thread = new Thread(new Runnable() {
				public void run()
				{
					try {
						Response publicActivityFeedResp = gh.User.activity(m_targetUser);
						if (publicActivityFeedResp.statusCode == 200) {
							JSONArray feedJSON = new JSONObject(publicActivityFeedResp.resp).getJSONObject("query").getJSONObject("results").getJSONArray("entry");
							m_publicActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), feedJSON, true);
						}
						if (!m_privateDisabled) {
							Response privateActivityFeedResp = gh.User.activity(m_targetUser, m_token);
							if (privateActivityFeedResp.statusCode == 200) {
								JSONArray feedJSON = new JSONObject(privateActivityFeedResp.resp).getJSONObject("query").getJSONObject("results").getJSONArray("entry");
								m_privateActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), feedJSON, false);
							}
						}
						runOnUiThread(new Runnable() {
							public void run() {
								toggleList(m_type);
								((ListView)findViewById(R.id.lv_activity_feeds_public_list)).setAdapter(m_publicActivityAdapter);
								((ListView)findViewById(R.id.lv_activity_feeds_private_list)).setAdapter(m_privateActivityAdapter);
								m_progressDialog.dismiss();
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
            thread.start();
        }
    }
}
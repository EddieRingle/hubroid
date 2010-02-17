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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class PublicActivity extends Activity {
	private GitHubAPI gh;
	private ActivityFeedAdapter m_adapter;
	private ProgressDialog m_progressDialog;
	private String m_targetUser;

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

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.public_activity);

        gh = new GitHubAPI();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_targetUser = extras.getString("username");

        	TextView title = (TextView)findViewById(R.id.tv_top_bar_title);
            title.setText(m_targetUser + "'s Activity");

            Thread thread = new Thread(new Runnable() {
				public void run()
				{
					try {
						Response activityFeedResp = gh.User.activity(m_targetUser);
						if (activityFeedResp.statusCode == 200) {
							JSONArray feedJSON = new JSONArray(activityFeedResp.resp);
							m_adapter = new ActivityFeedAdapter(getApplicationContext(), feedJSON, true);

							runOnUiThread(new Runnable() {
								public void run() {
									((ListView)findViewById(R.id.lv_public_activity_list)).setAdapter(m_adapter);
								}
							});
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
            thread.start();
        }
    }
}
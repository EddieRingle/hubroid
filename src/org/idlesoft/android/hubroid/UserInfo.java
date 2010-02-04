/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserInfo extends Activity {
	public JSONObject m_jsonData;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public Intent m_intent;
	protected String m_username;

	private OnClickListener onButtonClick = new OnClickListener() {
		public void onClick(View v) {
			Intent intent;
			// Figure out what button was clicked
			int id = v.getId();
			switch (id) {
			case R.id.btn_user_info_repositories:
				// Go to the user's list of repositories
				intent = new Intent(UserInfo.this, RepositoriesList.class);
				intent.putExtra("username", m_username);
				startActivity(intent);
				break;
			case R.id.btn_user_info_followers_following:
				// Go to the Followers/Following screen
				intent = new Intent(UserInfo.this, FollowersFollowing.class);
				intent.putExtra("username", m_username);
				startActivity(intent);
				break;
			case R.id.btn_user_info_watched_repositories:
				// Go to the Watched Repositories screen
				intent = new Intent(UserInfo.this, WatchedRepositories.class);
				intent.putExtra("username", m_username);
				startActivity(intent);
				break;
			default:
				// oh well...
				break;
			}
		}
	};

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

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.user_info);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	try {
				URL user_query = new URL("http://github.com/api/v2/json/user/show/"
										+ URLEncoder.encode(extras.getString("username")));
				JSONObject json = Hubroid.make_api_request(user_query);
				if (json == null) {
					setResult(RESULT_CANCELED);
					finishActivity(-2);
				} else {
					m_jsonData = json.getJSONObject("user");

					m_username = m_jsonData.getString("login");

					String company, location, full_name, email, blog;

					if (m_jsonData.has("company")) {
						company = m_jsonData.getString("company");
					} else {
						company = "N/A";
					}
					if (m_jsonData.has("location")) {
						location = m_jsonData.getString("location");
					} else {
						location = "N/A";
					}
					if (m_jsonData.has("name")) {
						full_name = m_jsonData.getString("name");
					} else {
						full_name = "N/A";
					}
					if (m_jsonData.has("email")) {
						email = m_jsonData.getString("email");
					} else {
						email = "N/A";
					}
					if (m_jsonData.has("blog")) {
						blog = m_jsonData.getString("blog");
					} else {
						blog = "N/A";
					}

					// Set all the values in the layout
					((TextView)findViewById(R.id.tv_top_bar_title)).setText(m_username);
					((ImageView)findViewById(R.id.iv_user_info_gravatar)).setImageBitmap(Hubroid.getGravatar(Hubroid.getGravatarID(m_username), 50));
					((TextView)findViewById(R.id.tv_user_info_full_name)).setText(full_name);
					((TextView)findViewById(R.id.tv_user_info_company)).setText(company);
					((TextView)findViewById(R.id.tv_user_info_email)).setText(email);
					((TextView)findViewById(R.id.tv_user_info_location)).setText(location);
					((TextView)findViewById(R.id.tv_user_info_blog)).setText(blog);

					// Make the buttons work
					Button repositoriesBtn = (Button) findViewById(R.id.btn_user_info_repositories);
					Button followersFollowingBtn = (Button) findViewById(R.id.btn_user_info_followers_following);
					Button watchedRepositoriesBtn = (Button) findViewById(R.id.btn_user_info_watched_repositories);

					repositoriesBtn.setOnClickListener(onButtonClick);
					followersFollowingBtn.setOnClickListener(onButtonClick);
					watchedRepositoriesBtn.setOnClickListener(onButtonClick);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }
}

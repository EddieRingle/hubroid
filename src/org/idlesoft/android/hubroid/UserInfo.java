/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.File;

import org.idlesoft.libraries.ghapi.User;
import org.idlesoft.libraries.ghapi.APIBase.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.flurry.android.FlurryAgent;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UserInfo extends Activity {
	public JSONObject m_jsonData;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public Intent m_intent;
	private String m_username;
	private String m_token;
	private String m_targetUser;
	private boolean m_isFollowing;
	private Thread m_thread;
	private ProgressDialog m_progressDialog;

	private OnClickListener onButtonClick = new OnClickListener() {
		public void onClick(View v) {
			Intent intent;
			// Figure out what button was clicked
			int id = v.getId();
			switch (id) {
			case R.id.btn_user_info_public_activity:
				// View the user's public activity feed
				intent = new Intent(UserInfo.this, ActivityFeeds.class);
				intent.putExtra("username", m_targetUser);
				startActivity(intent);
				break;
			case R.id.btn_user_info_repositories:
				// Go to the user's list of repositories
				intent = new Intent(UserInfo.this, RepositoriesList.class);
				intent.putExtra("username", m_targetUser);
				startActivity(intent);
				break;
			case R.id.btn_user_info_followers_following:
				// Go to the Followers/Following screen
				intent = new Intent(UserInfo.this, FollowersFollowing.class);
				intent.putExtra("username", m_targetUser);
				startActivity(intent);
				break;
			case R.id.btn_user_info_watched_repositories:
				// Go to the Watched Repositories screen
				intent = new Intent(UserInfo.this, WatchedRepositories.class);
				intent.putExtra("username", m_targetUser);
				startActivity(intent);
				break;
			default:
				// oh well...
				break;
			}
		}
	};

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.hasVisibleItems()) menu.clear();
		if (m_isFollowing) {
			menu.add(0, 3, 0, "Unfollow");
		} else {
			menu.add(0, 3, 0, "Follow");
		}
		menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, 1, 0, "Clear Preferences");
		menu.add(0, 2, 0, "Clear Cache");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 3:
			Response postResp;

			if (m_isFollowing) {
				postResp = User.unfollow(m_targetUser, m_username, m_token);
				if (postResp.statusCode == 200) {
					Toast.makeText(this, "You are no longer following " + m_targetUser + ".", Toast.LENGTH_SHORT).show();
				}
				m_isFollowing = !m_isFollowing;
			} else {
				postResp = User.follow(m_targetUser, m_username, m_token);
				if (postResp.statusCode == 200) {
					Toast.makeText(this, "You are now following " + m_targetUser + ".", Toast.LENGTH_SHORT).show();
				}
				m_isFollowing = !m_isFollowing;
			}
			break;
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

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");
        m_isFollowing = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	try {
        		m_targetUser = extras.getString("username");

        		Response userInfoResp;
        		if (m_targetUser.equalsIgnoreCase(m_username)) {
        			userInfoResp = User.info(m_username, m_token);
        		} else {
        			userInfoResp = User.info(m_targetUser);
        		}
        		JSONObject json = null;
        		if (userInfoResp.statusCode == 200)
        			json = new JSONObject(userInfoResp.resp);
				if (json == null) {
					// User doesn't really exist, return to the previous activity
					this.setResult(5005);
					this.finish();
				} else {
					m_jsonData = json.getJSONObject("user");

		        	JSONArray following_list = new JSONObject(User.following(m_username).resp).getJSONArray("users");
		        	int length = following_list.length() - 1;
		        	for (int i = 0; i <= length; i++) {
		        		if (following_list.getString(i).equalsIgnoreCase(m_targetUser)) {
		        			m_isFollowing = true;
		        		}
		        	}

					String company, location, full_name, email, blog;

					// Replace empty values with "N/A"
					if (m_jsonData.has("company") && !m_jsonData.getString("company").equals("null") && !m_jsonData.getString("company").equals("")) {
						company = m_jsonData.getString("company");
					} else {
						company = "N/A";
					}
					if (m_jsonData.has("location") && !m_jsonData.getString("location").equals("null") && !m_jsonData.getString("location").equals("")) {
						location = m_jsonData.getString("location");
					} else {
						location = "N/A";
					}
					if (m_jsonData.has("name") && !m_jsonData.getString("name").equals("null")) {
						full_name = m_jsonData.getString("name");
					} else {
						full_name = "N/A";
					}
					if (m_jsonData.has("email") && !m_jsonData.getString("email").equals("null") && !m_jsonData.getString("email").equals("")) {
						email = m_jsonData.getString("email");
					} else {
						email = "N/A";
					}
					if (m_jsonData.has("blog") && !m_jsonData.getString("blog").equals("null") && !m_jsonData.getString("blog").equals("")) {
						blog = m_jsonData.getString("blog");
					} else {
						blog = "N/A";
					}

					// Set all the values in the layout
					((TextView)findViewById(R.id.tv_top_bar_title)).setText(m_targetUser);
					((ImageView)findViewById(R.id.iv_user_info_gravatar)).setImageBitmap(Hubroid.getGravatar(Hubroid.getGravatarID(m_targetUser), 50));
					((TextView)findViewById(R.id.tv_user_info_full_name)).setText(full_name);
					((TextView)findViewById(R.id.tv_user_info_company)).setText(company);
					((TextView)findViewById(R.id.tv_user_info_email)).setText(email);
					((TextView)findViewById(R.id.tv_user_info_location)).setText(location);
					((TextView)findViewById(R.id.tv_user_info_blog)).setText(blog);

					// Make the buttons work
					Button activityBtn = (Button) findViewById(R.id.btn_user_info_public_activity);
					Button repositoriesBtn = (Button) findViewById(R.id.btn_user_info_repositories);
					Button followersFollowingBtn = (Button) findViewById(R.id.btn_user_info_followers_following);
					Button watchedRepositoriesBtn = (Button) findViewById(R.id.btn_user_info_watched_repositories);

					activityBtn.setOnClickListener(onButtonClick);
					repositoriesBtn.setOnClickListener(onButtonClick);
					followersFollowingBtn.setOnClickListener(onButtonClick);
					watchedRepositoriesBtn.setOnClickListener(onButtonClick);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }

	@Override
    public void onPause()
    {
    	if (m_thread != null && m_thread.isAlive())
    		m_thread.stop();
    	if (m_progressDialog != null && m_progressDialog.isShowing())
    		m_progressDialog.dismiss();
    	super.onPause();
    }

	@Override
    public void onStart()
    {
       super.onStart();
       FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
}

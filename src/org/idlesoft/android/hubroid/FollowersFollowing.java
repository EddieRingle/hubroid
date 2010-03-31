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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FollowersFollowing extends Activity {
	private FollowersFollowingListAdapter m_followers_adapter;
	private FollowersFollowingListAdapter m_following_adapter;
	public ProgressDialog m_progressDialog;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public String m_username;
	public String m_type;
	public JSONObject m_followersData;
	public JSONObject m_followingData;
	public Intent m_intent;
	public int m_position;
	private Thread m_thread;

	public FollowersFollowingListAdapter initializeList(String username) {
		FollowersFollowingListAdapter adapter = null;
		JSONObject json = null;
		try {
			if (m_type.equals("followers")) {
				json = new JSONObject(User.followers(username).resp);
				m_followersData = json;
			} else if (m_type.equals("following")) {
				json = new JSONObject(User.following(username).resp);
				m_followingData = json;
			}

			if (json == null) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FollowersFollowing.this, "Error gathering user data, please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				adapter = new FollowersFollowingListAdapter(getApplicationContext(), json.getJSONArray("users"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return adapter;
	}

	private Runnable threadProc_initializeList = new Runnable() {
		public void run() {
			m_type = "following";
			m_following_adapter = initializeList(m_username);
			m_type = "followers";
			m_followers_adapter = initializeList(m_username);

			runOnUiThread(new Runnable() {
				public void run() {
					if(m_followers_adapter != null && m_following_adapter != null) {
						ListView followers = (ListView) findViewById(R.id.lv_followers_following_followers_list);
						ListView following = (ListView) findViewById(R.id.lv_followers_following_following_list);
						followers.setAdapter(m_followers_adapter);
						following.setAdapter(m_following_adapter);
						if (m_type.equals("followers")) {
							toggleList("followers");
						}
						if (m_type.equals("following")) {
							toggleList("following");
						}
					}
					m_progressDialog.dismiss();
				}
			});
		}
	};

	public void toggleList(String type)
	{
		ListView followers = (ListView) findViewById(R.id.lv_followers_following_followers_list);
		ListView following = (ListView) findViewById(R.id.lv_followers_following_following_list);
		TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

		if (type.equals("") || type == null) {
			type = (m_type.equals("followers")) ? "following" : "followers";
		}
		m_type = type;

		if (m_type.equals("followers")) {
			followers.setVisibility(View.VISIBLE);
			following.setVisibility(View.GONE);
			title.setText("Followers");
		} else if (m_type.equals("following")) {
			following.setVisibility(View.VISIBLE);
			followers.setVisibility(View.GONE);
			title.setText("Following");
		}
	}

	private OnClickListener onButtonToggleClickListener = new OnClickListener() {
		public void onClick(View v) {
			if(v.getId() == R.id.btn_followers_following_followers) {
				toggleList("followers");
				m_type = "followers";
			} else if(v.getId() == R.id.btn_followers_following_following) {
				toggleList("following");
				m_type = "following";
			}
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        m_position = position;
	        try {
	        	m_intent = new Intent(FollowersFollowing.this, UserInfo.class);
	        	if (m_type.equals("followers")) {
	        		m_intent.putExtra("username", m_followersData.getJSONArray("users").getString(m_position));
	        	} else if (m_type.equals("following")) {
	        		m_intent.putExtra("username", m_followingData.getJSONArray("users").getString(m_position));
	        	}
	        	FollowersFollowing.this.startActivity(m_intent);
			} catch (JSONException e) {
				e.printStackTrace();
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
        setContentView(R.layout.followers_following);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();
        m_type = "followers";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
	        if(extras.containsKey("username")) {
	        	m_username = extras.getString("username");
	        } else {
	        	m_username = m_prefs.getString("login", "");
	        }
        } else {
        	m_username = m_prefs.getString("login", "");
        }

        m_progressDialog = ProgressDialog.show(FollowersFollowing.this, "Please wait...", "Fetching User Information...", true);
		m_thread = new Thread(null, threadProc_initializeList);
		m_thread.start();
    }

    @Override
    public void onStart() {
    	super.onStart();

    	((Button)findViewById(R.id.btn_followers_following_followers)).setOnClickListener(onButtonToggleClickListener);
        ((Button)findViewById(R.id.btn_followers_following_following)).setOnClickListener(onButtonToggleClickListener);

        ((ListView)findViewById(R.id.lv_followers_following_followers_list)).setOnItemClickListener(m_MessageClickedHandler);
        ((ListView)findViewById(R.id.lv_followers_following_following_list)).setOnItemClickListener(m_MessageClickedHandler);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putString("type", m_type);
    	if (m_followersData != null) {
    		savedInstanceState.putString("followers_json", m_followersData.toString());
    	}
    	if (m_followingData != null) {
    		savedInstanceState.putString("following_json", m_followingData.toString());
    	}
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	boolean keepGoing = true;
    	m_type = savedInstanceState.getString("type");
    	try {
    		if (savedInstanceState.containsKey("followers_json")) {
    			m_followersData = new JSONObject(savedInstanceState.getString("followers_json"));
    		} else {
    			keepGoing = false;
    		}
    		if (savedInstanceState.containsKey("following_json")) {
    			m_followingData = new JSONObject(savedInstanceState.getString("following_json"));
    		} else {
    			keepGoing = false;
    		}
		} catch (JSONException e) {
			keepGoing = false;
		}
		if (keepGoing == true) {
			try {
				m_followers_adapter = new FollowersFollowingListAdapter(getApplicationContext(), m_followersData.getJSONArray("users"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				m_following_adapter = new FollowersFollowingListAdapter(getApplicationContext(), m_followingData.getJSONArray("users"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			m_followers_adapter = null;
			m_following_adapter = null;
		}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	ListView followers = (ListView) findViewById(R.id.lv_followers_following_followers_list);
    	ListView following = (ListView) findViewById(R.id.lv_followers_following_following_list);

    	followers.setAdapter(m_followers_adapter);
    	following.setAdapter(m_following_adapter);
    	toggleList(m_type);
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
}
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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;

public class ActivityFeeds extends Activity {
	private ActivityFeedAdapter m_timelineActivityAdapter;
	private ActivityFeedAdapter m_publicActivityAdapter;
	private ActivityFeedAdapter m_privateActivityAdapter;
	private ProgressDialog m_progressDialog;
	private String m_targetUser;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private String m_username;
	private String m_token;
	private boolean m_isLoggedIn;
	private String m_type;
	public JSONArray m_publicJSON;
	public JSONArray m_displayedPublicJSON;
	public JSONArray m_privateJSON;
	public JSONArray m_displayedPrivateJSON;
	public JSONArray m_timelineJSON;
	public JSONArray m_displayedTimelineJSON;
	private loadTimelineTask m_loadTimelineTask;
	private loadPublicTask m_loadPublicTask;
	private loadPrivateTask m_loadPrivateTask;
	private Dialog m_loginDialog;
	private Thread m_thread;
	public View loadingItem;

	public Dialog onCreateDialog(int id)
	{
		m_loginDialog = new Dialog(ActivityFeeds.this);
		m_loginDialog.setCancelable(true);
		m_loginDialog.setTitle("Login");
		m_loginDialog.setContentView(R.layout.login_dialog);
		Button loginBtn = (Button) m_loginDialog.findViewById(R.id.btn_loginDialog_login);
		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				m_progressDialog = ProgressDialog.show(ActivityFeeds.this, null, "Logging in...");
				m_thread = new Thread(new Runnable() {
					public void run() {
						String username = ((EditText)m_loginDialog.findViewById(R.id.et_loginDialog_userField)).getText().toString();
						String token = ((EditText)m_loginDialog.findViewById(R.id.et_loginDialog_tokenField)).getText().toString();

						if (username.equals("") || token.equals("")) {
							runOnUiThread(new Runnable() {
								public void run() {
									m_progressDialog.dismiss();
									Toast.makeText(ActivityFeeds.this, "Login details cannot be blank", Toast.LENGTH_LONG).show();
								}
							});
						} else {
							Response authResp = User.info(username, token);
	
							if (authResp.statusCode == 401) {
								runOnUiThread(new Runnable() {
									public void run() {
										m_progressDialog.dismiss();
										Toast.makeText(ActivityFeeds.this, "Error authenticating with server", Toast.LENGTH_LONG).show();
									}
								});
							} else if (authResp.statusCode == 200) {
								m_editor.putString("login", username);
								m_editor.putString("token", token);
								m_editor.putBoolean("isLoggedIn", true);
								m_editor.commit();
								runOnUiThread(new Runnable() {
									public void run() {
										m_progressDialog.dismiss();
										dismissDialog(0);
										Intent intent = new Intent(ActivityFeeds.this, Hubroid.class);
										startActivity(intent);
										finish();
									}
								});
							}
						}
					}
				});
				m_thread.start();
			}
		});
		return m_loginDialog;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			if (!m_isLoggedIn)
				menu.add(0, 0, 0, "Login");
			else if (m_isLoggedIn)
				menu.add(0, 1, 0, "Logout");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			showDialog(0);
			return true;
		case 1:
			m_editor.clear().commit();
			Intent intent = new Intent(getApplicationContext(), Hubroid.class);
			startActivity(intent);
			finish();
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
		ListView timelineList = (ListView) findViewById(R.id.lv_activity_feeds_timeline_list);
		ListView publicList = (ListView) findViewById(R.id.lv_activity_feeds_public_list);
		ListView privateList = (ListView) findViewById(R.id.lv_activity_feeds_private_list);
		TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

		m_type = type;

		if (m_type.equals("public")) {
			publicList.setVisibility(View.VISIBLE);
			privateList.setVisibility(View.GONE);
			timelineList.setVisibility(View.GONE);
			if (m_isLoggedIn && m_targetUser == null)
				title.setText(m_username + "'s Activity");
			else
				title.setText(m_targetUser + "'s Activity");
		} else if (m_type.equals("private")) {
			privateList.setVisibility(View.VISIBLE);
			publicList.setVisibility(View.GONE);
			timelineList.setVisibility(View.GONE);
			title.setText("News Feed");
		} else if (m_type.equals("timeline")) {
			timelineList.setVisibility(View.VISIBLE);
			publicList.setVisibility(View.GONE);
			privateList.setVisibility(View.GONE);
			title.setText("GitHub Timeline");
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
			} else if(v.getId() == R.id.btn_activity_feeds_timeline) {
				toggleList("timeline");
				m_type = "timeline";
			}
		}
	};

	private OnItemClickListener onPublicActivityItemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			try {
				Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
				intent.putExtra("item_json", m_displayedPublicJSON.getJSONObject(arg2).toString());
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	private OnItemClickListener onPrivateActivityItemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			try {
				Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
				intent.putExtra("item_json", m_displayedPrivateJSON.getJSONObject(arg2).toString());
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	private OnItemClickListener onTimelineActivityItemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			try {
				Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
				intent.putExtra("item_json", m_displayedTimelineJSON.getJSONObject(arg2).toString());
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	public void navBarOnClickSetup()
	{
		((LinearLayout)findViewById(R.id.ll_activity_feeds_navbar)).setVisibility(View.VISIBLE);
		((Button)findViewById(R.id.btn_navbar_activity)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ActivityFeeds.this, ActivityFeeds.class));
				finish();
			}
		});
		((Button)findViewById(R.id.btn_navbar_repositories)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ActivityFeeds.this, RepositoriesList.class));
				finish();
			}
		});
		((Button)findViewById(R.id.btn_navbar_profile)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ActivityFeeds.this, UserInfo.class));
				finish();
			}
		});
		((Button)findViewById(R.id.btn_navbar_search)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ActivityFeeds.this, Search.class));
				finish();
			}
		});

		((Button)findViewById(R.id.btn_navbar_activity)).setEnabled(false);
		if (!m_isLoggedIn) { 
			((Button)findViewById(R.id.btn_navbar_profile)).setVisibility(View.GONE);
			((Button)findViewById(R.id.btn_navbar_repositories)).setVisibility(View.GONE);
		}
	}

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_feeds);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();
        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_targetUser = extras.getString("username");
        } else {
        	navBarOnClickSetup();
        }
    	TextView title = (TextView)findViewById(R.id.tv_top_bar_title);
        if (m_isLoggedIn) {
        	if (m_targetUser != null && !m_targetUser.equals(m_username)) {
        		((LinearLayout)findViewById(R.id.ll_activity_feeds_navbar)).setVisibility(View.GONE);
        		m_type = "public";
        		((ListView)findViewById(R.id.lv_activity_feeds_private_list)).setVisibility(View.GONE);
        		((ListView)findViewById(R.id.lv_activity_feeds_timeline_list)).setVisibility(View.GONE);
        		title.setText(m_targetUser + "'s Activity");
        		((LinearLayout)findViewById(R.id.ll_activity_feeds_button_holder)).setVisibility(View.GONE);
        	} else if (m_targetUser == null) {
        		m_type = "private";
        		title.setText("News Feed");
        		((Button)findViewById(R.id.btn_activity_feeds_private)).setOnClickListener(onButtonToggleClickListener);
        		((Button)findViewById(R.id.btn_activity_feeds_public)).setOnClickListener(onButtonToggleClickListener);
        		((Button)findViewById(R.id.btn_activity_feeds_timeline)).setOnClickListener(onButtonToggleClickListener);
        	}
    	} else {
    		((LinearLayout)findViewById(R.id.ll_activity_feeds_button_holder)).setVisibility(View.GONE);
    		if (m_targetUser != null) {
    			((LinearLayout)findViewById(R.id.ll_activity_feeds_navbar)).setVisibility(View.GONE);
        		m_type = "public";
        		((ListView)findViewById(R.id.lv_activity_feeds_private_list)).setVisibility(View.GONE);
        		((ListView)findViewById(R.id.lv_activity_feeds_timeline_list)).setVisibility(View.GONE);
        		title.setText(m_targetUser + "'s Activity");
        	} else if (m_targetUser == null) {
        		m_type = "timeline";
        		title.setText("GitHub Timeline");
        		((ListView)findViewById(R.id.lv_activity_feeds_private_list)).setVisibility(View.GONE);
        		((ListView)findViewById(R.id.lv_activity_feeds_public_list)).setVisibility(View.GONE);
        	}
    	}
        loadingItem = getLayoutInflater().inflate(R.layout.loading_feed_item, null);

        toggleList(m_type);

		if (m_targetUser != null) {
			m_loadPublicTask = new loadPublicTask();
			m_loadPublicTask.execute(this);
		} else {
			if (m_isLoggedIn) {
				m_targetUser = m_username;
				m_loadPublicTask = new loadPublicTask();
				m_loadPublicTask.execute(this);
				m_loadPrivateTask = new loadPrivateTask();
				m_loadPrivateTask.execute(this);
			}
			m_loadTimelineTask = new loadTimelineTask();
			m_loadTimelineTask.execute(this);
		}
    }

	private static class loadTimelineTask extends AsyncTask<ActivityFeeds, Integer, Boolean> {
		public ActivityFeeds parent;

		protected void setLoadingView()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_timeline_list)).addHeaderView(parent.loadingItem);
	        ((ListView)parent.findViewById(R.id.lv_activity_feeds_timeline_list)).setAdapter(null);
		}

		protected void removeLoadingView()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_timeline_list)).removeHeaderView(parent.loadingItem);
		}

		protected void setAdapter()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_timeline_list)).setAdapter(parent.m_timelineActivityAdapter);
			((ListView)parent.findViewById(R.id.lv_activity_feeds_timeline_list)).setOnItemClickListener(parent.onTimelineActivityItemClick);
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			if (progress[0] == 0) {
				setLoadingView();
			} else if (progress[0] == 100) {
				setAdapter();
				removeLoadingView();
			}
		}

		@Override
		protected Boolean doInBackground(ActivityFeeds... params)
		{
			parent = params[0];
			publishProgress(0);
			if (parent.m_timelineJSON == null) {
				try {
					Response resp = User.timeline();
					if (resp.statusCode != 200)
						return false;
					parent.m_timelineJSON = new JSONArray(resp.resp);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			if (parent.m_displayedTimelineJSON == null)
				parent.m_displayedTimelineJSON = new JSONArray();
			int length = parent.m_displayedTimelineJSON.length();
			for (int i = length; i < length + 10; i++) {
				if (parent.m_timelineJSON.isNull(i))
					break;
				try {
					parent.m_displayedTimelineJSON.put(parent.m_timelineJSON.get(i));
				} catch (JSONException e) {
					e.printStackTrace();
					break;
				}
			}
			parent.m_timelineActivityAdapter = new ActivityFeedAdapter(parent.getApplicationContext(), parent.m_displayedTimelineJSON, false);
			publishProgress(100);
			return true;
		}
	}

	private static class loadPublicTask extends AsyncTask<ActivityFeeds, Integer, Boolean> {
		public ActivityFeeds parent;

		protected void setLoadingView()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_public_list)).addHeaderView(parent.loadingItem);
	        ((ListView)parent.findViewById(R.id.lv_activity_feeds_public_list)).setAdapter(null);
		}

		protected void removeLoadingView()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_public_list)).removeHeaderView(parent.loadingItem);
		}

		protected void setAdapter()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_public_list)).setAdapter(parent.m_publicActivityAdapter);
			((ListView)parent.findViewById(R.id.lv_activity_feeds_public_list)).setOnItemClickListener(parent.onPublicActivityItemClick);
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			if (progress[0] == 0) {
				setLoadingView();
			} else if (progress[0] == 100) {
				setAdapter();
				removeLoadingView();
			}
		}

		@Override
		protected Boolean doInBackground(ActivityFeeds... params)
		{
			parent = params[0];
			publishProgress(0);
			if (parent.m_publicJSON == null) {
				try {
					Response resp = User.activity(parent.m_targetUser);
					if (resp.statusCode != 200)
						return false;
					parent.m_publicJSON = new JSONArray(resp.resp);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			if (parent.m_displayedPublicJSON == null)
				parent.m_displayedPublicJSON = new JSONArray();
			int length = parent.m_displayedPublicJSON.length();
			for (int i = length; i < length + 10; i++) {
				if (parent.m_publicJSON.isNull(i))
					break;
				try {
					parent.m_displayedPublicJSON.put(parent.m_publicJSON.get(i));
				} catch (JSONException e) {
					e.printStackTrace();
					break;
				}
			}
			parent.m_publicActivityAdapter = new ActivityFeedAdapter(parent.getApplicationContext(), parent.m_displayedPublicJSON, false);
			publishProgress(100);
			return true;
		}
	}

	private static class loadPrivateTask extends AsyncTask<ActivityFeeds, Integer, Boolean> {
		public ActivityFeeds parent;

		protected void setLoadingView()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_private_list)).addHeaderView(parent.loadingItem);
	        ((ListView)parent.findViewById(R.id.lv_activity_feeds_private_list)).setAdapter(null);
		}

		protected void removeLoadingView()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_private_list)).removeHeaderView(parent.loadingItem);
		}

		protected void setAdapter()
		{
			((ListView)parent.findViewById(R.id.lv_activity_feeds_private_list)).setAdapter(parent.m_privateActivityAdapter);
			((ListView)parent.findViewById(R.id.lv_activity_feeds_private_list)).setOnItemClickListener(parent.onPrivateActivityItemClick);
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			if (progress[0] == 0) {
				setLoadingView();
			} else if (progress[0] == 100) {
				setAdapter();
				removeLoadingView();
			}
		}

		@Override
		protected Boolean doInBackground(ActivityFeeds... params)
		{
			parent = params[0];
			publishProgress(0);
			if (parent.m_privateJSON == null) {
				try {
					Response resp = User.activity(parent.m_username, parent.m_token);
					if (resp.statusCode != 200)
						return false;
					parent.m_privateJSON = new JSONArray(resp.resp);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			if (parent.m_displayedPrivateJSON == null)
				parent.m_displayedPrivateJSON = new JSONArray();
			int length = parent.m_displayedPrivateJSON.length();
			for (int i = length; i < length + 10; i++) {
				if (parent.m_privateJSON.isNull(i))
					break;
				try {
					parent.m_displayedPrivateJSON.put(parent.m_privateJSON.get(i));
				} catch (JSONException e) {
					e.printStackTrace();
					break;
				}
			}
			parent.m_privateActivityAdapter = new ActivityFeedAdapter(parent.getApplicationContext(), parent.m_displayedPrivateJSON, false);
			publishProgress(100);
			return true;
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
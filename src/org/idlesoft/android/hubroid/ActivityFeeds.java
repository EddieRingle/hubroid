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

import android.app.Activity;
import android.app.Dialog;
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
	private JSONArray m_publicJSON;
	private JSONArray m_privateJSON;
	private JSONArray m_timelineJSON;
	private Thread m_thread;
	private Dialog m_loginDialog;

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
				intent.putExtra("item_json", m_publicJSON.getJSONObject(arg2).toString());
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
				intent.putExtra("item_json", m_privateJSON.getJSONObject(arg2).toString());
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
				intent.putExtra("item_json", m_timelineJSON.getJSONObject(arg2).toString());
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	public void navBarOnClickSetup()
	{
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
    	m_progressDialog = ProgressDialog.show(this, "Please Wait", "Loading activity feeds... (this may take awhile)");
        m_thread = new Thread(new Runnable() {
			public void run()
			{
				try {
					if (m_targetUser != null) {
						Response publicActivityFeedResp = User.activity(m_targetUser);
						if (publicActivityFeedResp.statusCode == 200) {
							m_publicJSON = new JSONObject(publicActivityFeedResp.resp).getJSONObject("query").getJSONObject("results").getJSONArray("entry");
							m_publicActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), m_publicJSON, true);
						}
					} else {
						if (m_isLoggedIn) {
							Response publicActivityFeedResp = User.activity(m_username);
							if (publicActivityFeedResp.statusCode == 200) {
								m_publicJSON = new JSONObject(publicActivityFeedResp.resp).getJSONObject("query").getJSONObject("results").getJSONArray("entry");
								m_publicActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), m_publicJSON, true);
							}
							Response privateActivityFeedResp = User.activity(m_username, m_token);
							if (privateActivityFeedResp.statusCode == 200) {
								m_privateJSON = new JSONObject(privateActivityFeedResp.resp).getJSONObject("query").getJSONObject("results").getJSONArray("entry");
								m_privateActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), m_privateJSON, false);
							}
						}
						Response timelineActivityFeedResp = User.timeline();
						if (timelineActivityFeedResp.statusCode == 200) {
							m_timelineJSON = new JSONObject(timelineActivityFeedResp.resp).getJSONObject("query").getJSONObject("results").getJSONArray("entry");
							m_timelineActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), m_timelineJSON, false);
						}
					}
					runOnUiThread(new Runnable() {
						public void run() {
							toggleList(m_type);
							ListView publicList = (ListView)findViewById(R.id.lv_activity_feeds_public_list);
							ListView privateList = (ListView)findViewById(R.id.lv_activity_feeds_private_list);
							ListView timelineList = (ListView)findViewById(R.id.lv_activity_feeds_timeline_list);
							publicList.setAdapter(m_publicActivityAdapter);
							privateList.setAdapter(m_privateActivityAdapter);
							timelineList.setAdapter(m_timelineActivityAdapter);
							publicList.setOnItemClickListener(onPublicActivityItemClick);
							privateList.setOnItemClickListener(onPrivateActivityItemClick);
							timelineList.setOnItemClickListener(onTimelineActivityItemClick);
							m_progressDialog.dismiss();
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
        m_thread.start();

        navBarOnClickSetup();
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
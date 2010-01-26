package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
	public String m_username;
	public String m_type;
	public JSONObject m_followersData;
	public JSONObject m_followingData;
	public Intent m_intent;
	public int m_position;

	public FollowersFollowingListAdapter initializeList(String username) {
		FollowersFollowingListAdapter adapter = null;
		JSONObject json = null;
		try {
			URL query = new URL("http://github.com/api/v2/json/user/show/"
								+ URLEncoder.encode(username)
								+ "/"
								+ m_type);
			
			json = Hubroid.make_api_request(query);

			if (m_type == "followers") {
				m_followersData = json;
			} else if (m_type == "following") {
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

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return adapter;
	}

	private Runnable threadProc_initializeList = new Runnable() {
		public void run() {

			m_type = "followers";
			m_followers_adapter = initializeList(m_username);
			m_type = "following";
			m_following_adapter = initializeList(m_username);
			m_type = "followers";

			runOnUiThread(new Runnable() {
				public void run() {
					if(m_followers_adapter != null && m_following_adapter != null) {
						if (m_type == "followers") {
							((ListView)findViewById(R.id.lv_followers_following_followers_list)).setAdapter(m_followers_adapter);
						}
						if (m_type == "following") {
							((ListView)findViewById(R.id.lv_followers_following_following_list)).setAdapter(m_following_adapter);
						}
					}
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private Runnable threadProc_itemClick = new Runnable() {
		public void run() {
			try {
	        	m_intent = new Intent(FollowersFollowing.this, UserInfo.class);
	        	if (m_type == "followers") {
	        		m_intent.putExtra("username", m_followersData.getJSONArray("users").getString(m_position));
	        	} else if (m_type == "following") {
	        		m_intent.putExtra("username", m_followingData.getJSONArray("users").getString(m_position));
	        	}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					FollowersFollowing.this.startActivity(m_intent);
				}
			});
		}
	};

	private OnClickListener onButtonToggleClickListener = new OnClickListener() {
		public void onClick(View v) {
			ListView followers = (ListView)findViewById(R.id.lv_followers_following_followers_list);
			ListView following = (ListView)findViewById(R.id.lv_followers_following_following_list);
			TextView title = (TextView)findViewById(R.id.tv_followers_following_title);

			if(v.getId() == R.id.btn_followers_following_followers && m_type != "followers") {
				followers.setAdapter(m_followers_adapter);
				following.setAdapter(null);
				m_type = "followers";
				title.setText(m_username + "'s stalkers:");
			} else if(v.getId() == R.id.btn_followers_following_following && m_type != "following") {
				following.setAdapter(m_following_adapter);
				followers.setAdapter(null);
				m_type = "following";
				title.setText("Who " + m_username + " stalks:");
			}
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        m_position = position;
	        Thread thread = new Thread(null, threadProc_itemClick);
	        thread.start();
		}
	};

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.followers_following);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_type = "followers";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
	        if(extras.containsKey("username")) {
	        	m_username = icicle.getString("username");
	        } else {
	        	m_username = m_prefs.getString("login", "");
	        }
        } else {
        	m_username = m_prefs.getString("login", "");
        }

        TextView title = (TextView)findViewById(R.id.tv_followers_following_title);
        title.setText(m_username + "'s stalkers");

        m_progressDialog = ProgressDialog.show(FollowersFollowing.this, "Please wait...", "Fetching User Information...", true);
		Thread thread = new Thread(null, threadProc_initializeList);
		thread.start();
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

    	if (m_followers_adapter != null && m_type == "followers") {
    		followers.setAdapter(m_followers_adapter);
    		following.setAdapter(null);
    		following.setVisibility(View.GONE);
    	}
    	if (m_following_adapter != null && m_type == "following") {
    		following.setAdapter(m_following_adapter);
    		followers.setAdapter(null);
    		followers.setVisibility(View.GONE);
    	}
    }
}
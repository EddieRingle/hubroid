/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.File;

import org.idlesoft.libraries.ghapi.Issues;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;

public class IssuesList extends Activity {
	private IssuesListAdapter m_openIssues_adapter;
	private IssuesListAdapter m_closedIssues_adapter;
	private ProgressDialog m_progressDialog;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private String m_targetUser;
	private String m_targetRepo;
	private String m_username;
	private String m_token;
	private String m_type;
	private JSONArray m_openIssuesData;
	private JSONArray m_closedIssuesData;
	private Intent m_intent;
	private int m_position;
	private Thread m_thread;

	public void initializeList() {
		JSONObject json = null;
		m_openIssuesData = new JSONArray();
		m_closedIssuesData = new JSONArray();
		try {
			json = new JSONObject(Issues.list(m_targetUser, m_targetRepo, "open", m_username, m_token).resp);
			m_openIssuesData = new JSONArray();
			for (int i = 0; !json.getJSONArray("issues").isNull(i); i++) {
				m_openIssuesData.put(json.getJSONArray("issues").getJSONObject(i));
			}
			m_openIssues_adapter = new IssuesListAdapter(IssuesList.this, m_openIssuesData);

			json = new JSONObject(Issues.list(m_targetUser, m_targetRepo, "closed", m_username, m_token).resp);		
			m_closedIssuesData = new JSONArray();
			for (int i = 0; !json.getJSONArray("issues").isNull(i); i++) {
				m_closedIssuesData.put(json.getJSONArray("issues").getJSONObject(i));
			}
			m_closedIssues_adapter = new IssuesListAdapter(IssuesList.this, m_closedIssuesData);
		} catch (JSONException e) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(IssuesList.this, "Error gathering issue data, please try again.", Toast.LENGTH_SHORT).show();
				}
			});
			e.printStackTrace();
		}
	}

	private Runnable threadProc_initializeList = new Runnable() {
		public void run() {
			initializeList();

			runOnUiThread(new Runnable() {
				public void run() {
					if(m_openIssues_adapter != null && m_closedIssues_adapter != null) {
						ListView openIssues = (ListView) findViewById(R.id.lv_issues_list_open_list);
						ListView closedIssues = (ListView) findViewById(R.id.lv_issues_list_closed_list);
						openIssues.setAdapter(m_openIssues_adapter);
						closedIssues.setAdapter(m_closedIssues_adapter);
						if (m_type.equals("open")) {
							toggleList("open");
						}
						if (m_type.equals("closed")) {
							toggleList("closed");
						}
					}
					m_progressDialog.dismiss();
				}
			});
		}
	};

	public void toggleList(String type)
	{
		ListView openList = (ListView) findViewById(R.id.lv_issues_list_open_list);
		ListView closedList = (ListView) findViewById(R.id.lv_issues_list_closed_list);
		TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

		if (type.equals("") || type == null) {
			type = (m_type.equals("open")) ? "closed" : "public";
		}
		m_type = type;

		if (m_type.equals("open")) {
			openList.setVisibility(View.VISIBLE);
			closedList.setVisibility(View.GONE);
			title.setText("Open Issues");
		} else if (m_type.equals("closed")) {
			closedList.setVisibility(View.VISIBLE);
			openList.setVisibility(View.GONE);
			title.setText("Closed Issues");
		}
	}

	private OnClickListener onButtonToggleClickListener = new OnClickListener() {
		public void onClick(View v) {
			if(v.getId() == R.id.btn_issues_list_open) {
				toggleList("open");
				m_type = "open";
			} else if(v.getId() == R.id.btn_issues_list_closed) {
				toggleList("closed");
				m_type = "closed";
			}
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			JSONObject json = null;
			try {
		        if (parent.getId() == R.id.lv_issues_list_open_list) {
		        	json = m_openIssuesData.getJSONObject(position);
		        } else {
		        	json = m_closedIssuesData.getJSONObject(position);
		        }
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        Intent intent = new Intent(getApplicationContext(), SingleIssue.class);
	        intent.putExtra("repoOwner", m_targetUser);
	        intent.putExtra("repoName", m_targetRepo);
	        intent.putExtra("item_json", json.toString());
	        startActivity(intent);
		}
	};

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		int id = v.getId();
		if (id == R.id.lv_issues_list_open_list) {
			menu.add(0, 0, 0, "View");
			if (m_username.equals(m_targetUser))
				menu.add(0, 1, 0,  "Close");
		} else if (id == R.id.lv_issues_list_closed_list) {
			menu.add(0, 0, 0, "View");
			if (m_username.equals(m_targetUser))
				menu.add(0, 2, 0, "Reopen");
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		JSONObject json = null;
		try {
	        if (m_type.equals("open")) {
	        	json = m_openIssuesData.getJSONObject(info.position);
	        } else {
	        	json = m_closedIssuesData.getJSONObject(info.position);
	        }
			switch (item.getItemId()) {
			case 0:
		        Intent intent = new Intent(getApplicationContext(), SingleIssue.class);
		        intent.putExtra("repoOwner", m_targetUser);
		        intent.putExtra("repoName", m_targetRepo);
		        intent.putExtra("item_json", json.toString());
		        startActivity(intent);
				return true;
			case 1:
				if (Issues.close(m_targetUser, m_targetRepo, json.getInt("number"), m_username, m_token).statusCode == 200) {
					m_progressDialog = ProgressDialog.show(IssuesList.this, "Please wait...", "Refreshing Issue List...", true);
					m_thread = new Thread(null, threadProc_initializeList);
					m_thread.start();
				} else {
					Toast.makeText(getApplicationContext(), "Error closing issue.", Toast.LENGTH_SHORT).show();
				}
				return true;
			case 2:
				if (Issues.reopen(m_targetUser, m_targetRepo, json.getInt("number"), m_username, m_token).statusCode == 200) {
					m_progressDialog = ProgressDialog.show(IssuesList.this, "Please wait...", "Refreshing Issue List...", true);
					m_thread = new Thread(null, threadProc_initializeList);
					m_thread.start();
				} else {
					Toast.makeText(getApplicationContext(), "Error reopening issue.", Toast.LENGTH_SHORT).show();
				}
				return true;
			default:
				return super.onContextItemSelected(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			menu.add(0, -1, 0, "Create Issue").setIcon(android.R.drawable.ic_menu_add);
			menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
			menu.add(0, 1, 0, "Clear Preferences");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case -1:
			intent = new Intent(this, CreateIssue.class);
			intent.putExtra("owner", m_targetUser);
			intent.putExtra("repository", m_targetRepo);
			startActivity(intent);
			return true;
		case 0:
			intent = new Intent(this, Hubroid.class);
			startActivity(intent);
			return true;
		case 1:
			m_editor.clear().commit();
			intent = new Intent(this, Hubroid.class);
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
        setContentView(R.layout.issues_list);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();
        m_type = "open";

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
	        if (extras.containsKey("owner")) {
	        	m_targetUser = extras.getString("owner");
	        } else {
	        	m_targetUser = m_username;
	        }
	        if (extras.containsKey("repository")) {
	        	m_targetRepo = extras.getString("repository");
	        }
        } else {
        	m_targetUser = m_username;
        }

        m_progressDialog = ProgressDialog.show(IssuesList.this, "Please wait...", "Loading Issues...", true);
		m_thread = new Thread(null, threadProc_initializeList);
		m_thread.start();
    }

    @Override
    public void onStart() {
    	super.onStart();

    	FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

    	((Button)findViewById(R.id.btn_issues_list_open)).setOnClickListener(onButtonToggleClickListener);
        ((Button)findViewById(R.id.btn_issues_list_closed)).setOnClickListener(onButtonToggleClickListener);

        ((ListView)findViewById(R.id.lv_issues_list_open_list)).setOnItemClickListener(m_MessageClickedHandler);
        ((ListView)findViewById(R.id.lv_issues_list_closed_list)).setOnItemClickListener(m_MessageClickedHandler);
        registerForContextMenu((ListView)findViewById(R.id.lv_issues_list_open_list));
        registerForContextMenu((ListView)findViewById(R.id.lv_issues_list_closed_list));
    }

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putString("type", m_type);
    	if (m_openIssuesData != null) {
    		savedInstanceState.putString("openIssues_json", m_openIssuesData.toString());
    	}
    	if (m_closedIssuesData != null) {
    		savedInstanceState.putString("closedIssues_json", m_closedIssuesData.toString());
    	}
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	boolean keepGoing = true;
    	m_type = savedInstanceState.getString("type");
    	try {
    		if (savedInstanceState.containsKey("openIssues_json")) {
    			m_openIssuesData = new JSONArray(savedInstanceState.getString("openIssues_json"));
    		} else {
    			keepGoing = false;
    		}
    		if (savedInstanceState.containsKey("closedIssues_json")) {
    			m_closedIssuesData = new JSONArray(savedInstanceState.getString("closedIssues_json"));
    		} else {
    			keepGoing = false;
    		}
		} catch (JSONException e) {
			keepGoing = false;
		}
		if (keepGoing == true) {
			m_openIssues_adapter = new IssuesListAdapter(getApplicationContext(), m_openIssuesData);
			m_closedIssues_adapter = new IssuesListAdapter(getApplicationContext(), m_closedIssuesData);
		} else {
			m_openIssues_adapter = null;
			m_closedIssues_adapter = null;
		}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	ListView openList = (ListView) findViewById(R.id.lv_issues_list_open_list);
    	ListView closedList = (ListView) findViewById(R.id.lv_issues_list_closed_list);

    	openList.setAdapter(m_openIssues_adapter);
    	closedList.setAdapter(m_closedIssues_adapter);
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
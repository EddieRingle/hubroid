/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.File;

import org.idlesoft.libraries.ghapi.Repository;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RepositoriesList extends Activity {
	private RepositoriesListAdapter m_publicRepositories_adapter;
	private RepositoriesListAdapter m_privateRepositories_adapter;
	public ProgressDialog m_progressDialog;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public String m_targetUser;
	public String m_username;
	private String m_token;
	public String m_type;
	public JSONArray m_publicRepoData;
	public JSONArray m_privateRepoData;
	public Intent m_intent;
	public int m_position;

	public void initializeList() {
		JSONObject json = null;
		try {
			json = new JSONObject(Repository.list(m_targetUser, m_username, m_token).resp);
			m_publicRepoData = new JSONArray();
			m_privateRepoData = new JSONArray();
			for (int i = 0; !json.getJSONArray("repositories").isNull(i); i++) {
				if (json.getJSONArray("repositories").getJSONObject(i).getBoolean("private")) {
					m_privateRepoData.put(json.getJSONArray("repositories").getJSONObject(i));
				} else {
					m_publicRepoData.put(json.getJSONArray("repositories").getJSONObject(i));
				}
			}
			m_publicRepositories_adapter = new RepositoriesListAdapter(RepositoriesList.this, m_publicRepoData);
			m_privateRepositories_adapter = new RepositoriesListAdapter(RepositoriesList.this, m_privateRepoData);
		} catch (JSONException e) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(RepositoriesList.this, "Error gathering repository data, please try again.", Toast.LENGTH_SHORT).show();
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
					if(m_publicRepositories_adapter != null && m_privateRepositories_adapter != null) {
						ListView publicRepos = (ListView) findViewById(R.id.lv_repositories_list_public_list);
						ListView privateRepos = (ListView) findViewById(R.id.lv_repositories_list_private_list);
						publicRepos.setAdapter(m_publicRepositories_adapter);
						privateRepos.setAdapter(m_privateRepositories_adapter);
						if (m_type.equals("public")) {
							toggleList("public");
						}
						if (m_type.equals("private")) {
							toggleList("private");
						}
					}
					m_progressDialog.dismiss();
				}
			});
		}
	};

	public void toggleList(String type)
	{
		ListView publicList = (ListView) findViewById(R.id.lv_repositories_list_public_list);
		ListView privateList = (ListView) findViewById(R.id.lv_repositories_list_private_list);
		TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

		if (type.equals("") || type == null) {
			type = (m_type.equals("public")) ? "private" : "public";
		}
		m_type = type;

		if (m_type.equals("public")) {
			publicList.setVisibility(View.VISIBLE);
			privateList.setVisibility(View.GONE);
			title.setText("Public Repos");
		} else if (m_type.equals("private")) {
			privateList.setVisibility(View.VISIBLE);
			publicList.setVisibility(View.GONE);
			title.setText("Private Repos");
		}
	}

	private OnClickListener onButtonToggleClickListener = new OnClickListener() {
		public void onClick(View v) {
			if(v.getId() == R.id.btn_repositories_list_public) {
				toggleList("public");
				m_type = "public";
			} else if(v.getId() == R.id.btn_repositories_list_private) {
				toggleList("private");
				m_type = "private";
			}
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        m_position = position;
	        try {
	        	m_intent = new Intent(RepositoriesList.this, RepositoryInfo.class);
	        	if (m_type.equals("public")) {
	        		m_intent.putExtra("repo_name", m_publicRepoData.getJSONObject(m_position).getString("name"));
		        	m_intent.putExtra("username", m_publicRepoData.getJSONObject(m_position).getString("owner"));
	        	} else if (m_type.equals("private")) {
	        		m_intent.putExtra("repo_name", m_privateRepoData.getJSONObject(m_position).getString("name"));
		        	m_intent.putExtra("username", m_privateRepoData.getJSONObject(m_position).getString("owner"));
	        	}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			RepositoriesList.this.startActivity(m_intent);
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
        setContentView(R.layout.repositories_list);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();
        m_type = "public";

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
	        if(extras.containsKey("username")) {
	        	m_targetUser = extras.getString("username");
	        } else {
	        	m_targetUser = m_username;
	        }
        } else {
        	m_targetUser = m_username;
        }

        m_progressDialog = ProgressDialog.show(RepositoriesList.this, "Please wait...", "Loading Repositories...", true);
		Thread thread = new Thread(null, threadProc_initializeList);
		thread.start();
    }

    @Override
    public void onStart() {
    	super.onStart();

    	((Button)findViewById(R.id.btn_repositories_list_public)).setOnClickListener(onButtonToggleClickListener);
        ((Button)findViewById(R.id.btn_repositories_list_private)).setOnClickListener(onButtonToggleClickListener);

        ((ListView)findViewById(R.id.lv_repositories_list_public_list)).setOnItemClickListener(m_MessageClickedHandler);
        ((ListView)findViewById(R.id.lv_repositories_list_private_list)).setOnItemClickListener(m_MessageClickedHandler);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putString("type", m_type);
    	if (m_publicRepoData != null) {
    		savedInstanceState.putString("publicRepos_json", m_publicRepoData.toString());
    	}
    	if (m_privateRepoData != null) {
    		savedInstanceState.putString("privateRepos_json", m_privateRepoData.toString());
    	}
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	boolean keepGoing = true;
    	m_type = savedInstanceState.getString("type");
    	try {
    		if (savedInstanceState.containsKey("publicRepos_json")) {
    			m_publicRepoData = new JSONArray(savedInstanceState.getString("publicRepos_json"));
    		} else {
    			keepGoing = false;
    		}
    		if (savedInstanceState.containsKey("privateRepos_json")) {
    			m_privateRepoData = new JSONArray(savedInstanceState.getString("privateRepos_json"));
    		} else {
    			keepGoing = false;
    		}
		} catch (JSONException e) {
			keepGoing = false;
		}
		if (keepGoing == true) {
			m_publicRepositories_adapter = new RepositoriesListAdapter(getApplicationContext(), m_publicRepoData);
			m_privateRepositories_adapter = new RepositoriesListAdapter(getApplicationContext(), m_privateRepoData);
		} else {
			m_publicRepositories_adapter = null;
			m_privateRepositories_adapter = null;
		}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	ListView publicList = (ListView) findViewById(R.id.lv_repositories_list_public_list);
    	ListView privateList = (ListView) findViewById(R.id.lv_repositories_list_private_list);

    	publicList.setAdapter(m_publicRepositories_adapter);
    	privateList.setAdapter(m_privateRepositories_adapter);
    	toggleList(m_type);
    }
}
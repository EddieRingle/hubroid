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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WatchedRepositories extends ListActivity {
	private RepositoriesListAdapter m_adapter;
	public ProgressDialog m_progressDialog;
	public JSONObject m_jsonData;
	public int m_position;
	public String m_username;
	public SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public Intent m_intent;

	public RepositoriesListAdapter initializeList() {
		RepositoriesListAdapter adapter = null;
		try {
			m_jsonData = new JSONObject(User.watching(m_username).resp);
			if (m_jsonData == null) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(WatchedRepositories.this, "Error gathering repository data, please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				adapter = new RepositoriesListAdapter(getApplicationContext(), m_jsonData.getJSONArray("repositories"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return adapter;
	}

	private Runnable threadProc_initializeList = new Runnable() {
		public void run() {
			m_adapter = initializeList();

			runOnUiThread(new Runnable() {
				public void run() {
					if (m_adapter != null) {
						setListAdapter(m_adapter);
					}
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private Runnable threadProc_itemClick = new Runnable() {
		public void run() {
			try {
	        	m_intent = new Intent(WatchedRepositories.this, RepositoryInfo.class);
	        	m_intent.putExtra("repo_name", m_jsonData.getJSONArray("repositories").getJSONObject(m_position).getString("name"));
	        	m_intent.putExtra("username", m_jsonData.getJSONArray("repositories").getJSONObject(m_position).getString("owner"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					WatchedRepositories.this.startActivity(m_intent);
				}
			});
		}
	};
	
	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			m_position = position;
			Thread thread = new Thread(null, threadProc_itemClick);
			thread.start();
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
        setContentView(R.layout.watched_repositories);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

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

        TextView title = (TextView)findViewById(R.id.tv_watched_repositories_title);
        title.setText("Watched Repositories");

        m_progressDialog = ProgressDialog.show(WatchedRepositories.this, "Please wait...", "Loading Repositories...", true);
		Thread thread = new Thread(null, threadProc_initializeList);
		thread.start();

        ListView list = (ListView)findViewById(android.R.id.list);
        list.setOnItemClickListener(m_MessageClickedHandler);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	if (m_jsonData != null) {
    		savedInstanceState.putString("json", m_jsonData.toString());
    	}
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	boolean keepGoing = true;
    	try {
    		if (savedInstanceState.containsKey("json")) {
    			m_jsonData = new JSONObject(savedInstanceState.getString("json"));
    		} else {
    			keepGoing = false;
    		}
		} catch (JSONException e) {
			keepGoing = false;
		}
		if (keepGoing == true) {
			try {
				m_adapter = new RepositoriesListAdapter(getApplicationContext(), m_jsonData.getJSONArray("repositories"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			m_adapter = null;
		}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	if (m_adapter != null) {
    		setListAdapter(m_adapter);
    	}
    }
}
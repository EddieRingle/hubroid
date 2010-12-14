/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import com.flurry.android.FlurryAgent;

import org.idlesoft.libraries.ghapi.Commits;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.Repository;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class CommitsList extends Activity {
	public CommitListAdapter m_commitListAdapter;
	public ArrayAdapter<String> m_branchesAdapter;
	public ArrayList<String> m_branches;
	public ProgressDialog m_progressDialog;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public JSONArray m_commitsJSON;
	public String m_repo_owner;
	public String m_repo_name;
	private String m_username;
	private String m_password;
	public Intent m_intent;
	public int m_position;
	private Thread m_thread;
	private GitHubAPI mGapi = new GitHubAPI();

	private Runnable threadProc_gatherCommits = new Runnable() {
		public void run() {
			try {
				m_commitsJSON = new JSONObject(mGapi.commits.list(m_repo_owner, m_repo_name, m_branches.get(m_position)).resp).getJSONArray("commits");
				Log.d("debug1",m_commitsJSON.toString());
				m_commitListAdapter = new CommitListAdapter(CommitsList.this, m_commitsJSON);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					ListView commitList = (ListView)findViewById(R.id.lv_commits_list_list);
					commitList.setAdapter(m_commitListAdapter);
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private OnItemSelectedListener m_onBranchSelect = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			m_position = position;
			m_progressDialog = ProgressDialog.show(CommitsList.this, "Please wait...", "Loading Repository's Commits...", true);
			m_thread = new Thread(null, threadProc_gatherCommits);
			m_thread.start();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
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
        setContentView(R.layout.commits_list);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_password = m_prefs.getString("password", "");
        mGapi.authenticate(m_username, m_password);

        TextView title = (TextView) findViewById(R.id.tv_top_bar_title);
        title.setText("Recent Commits");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_repo_name = extras.getString("repo_name");
        	m_repo_owner = extras.getString("username");

			try {
				Response branchesResponse = mGapi.repo.branches(m_repo_owner, m_repo_name);
				JSONObject branchesJson = new JSONObject(branchesResponse.resp).getJSONObject("branches");
				m_branches = new ArrayList<String>(branchesJson.length());
				Iterator<String> keys = branchesJson.keys();
				while (keys.hasNext()) {
					String next_branch = keys.next();
					m_branches.add(next_branch);
				}

				// Find the position of the master branch
				int masterPos = m_branches.indexOf("master");
				
				m_branchesAdapter = new ArrayAdapter<String>(CommitsList.this, android.R.layout.simple_spinner_item, m_branches);
				m_branchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				Spinner branchesSpinner = (Spinner)findViewById(R.id.spn_commits_list_branch_select);
				branchesSpinner.setAdapter(m_branchesAdapter);
				branchesSpinner.setOnItemSelectedListener(m_onBranchSelect);
				// Set the spinner to the master branch, if it exists, by default
				if (masterPos > -1)
					branchesSpinner.setSelection(masterPos);
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }
	
	@Override
	protected void onStart() {
		super.onStart();

		FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

		((ListView)findViewById(R.id.lv_commits_list_list)).setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				try {
			        m_position = position;
			        Intent i = new Intent(CommitsList.this, CommitChangeViewer.class);
					i.putExtra("id", m_commitsJSON.getJSONObject(m_position).getString("id"));
					i.putExtra("repo_name", m_repo_name);
					i.putExtra("username", m_repo_owner);
					CommitsList.this.startActivity(i);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
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
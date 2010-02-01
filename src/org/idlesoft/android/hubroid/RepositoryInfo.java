package org.idlesoft.android.hubroid;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.app.TabActivity;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class RepositoryInfo extends TabActivity {
	public ProgressDialog m_progressDialog;
	public JSONObject m_jsonData;
	public Intent m_intent;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public ForkListAdapter m_forkListAdapter;
	public CommitListAdapter m_commitListAdapter;
	public JSONArray m_jsonForkData;
	public int m_position;
	public ArrayList<String> m_branches;
	public String m_repo_owner;
	public String m_repo_name;

	private Runnable threadProc_userInfo = new Runnable() {
		public void run() {
			TextView tv = (TextView)findViewById(R.id.repository_owner);
			m_intent = new Intent(RepositoryInfo.this, UserInfo.class);
			m_intent.putExtra("username", tv.getText());
			RepositoryInfo.this.startActivity(m_intent);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private Runnable threadProc_forkedRepoInfo = new Runnable() {
		public void run() {
			TextView tv = (TextView)findViewById(R.id.repository_fork_of);
			m_intent = new Intent(RepositoryInfo.this, RepositoryInfo.class);
			m_intent.putExtra("username", tv.getText().toString().split("/")[0]);
			m_intent.putExtra("repo_name", tv.getText().toString().split("/")[1]);
			RepositoryInfo.this.startActivity(m_intent);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					m_progressDialog.dismiss();
				}
			});
		}
	};

	View.OnClickListener username_onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading User Information...");
			Thread thread = new Thread(null, threadProc_userInfo);
			thread.start();
		}
	};

	View.OnClickListener forkedRepo_onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading Repository Information...");
			Thread thread = new Thread(null, threadProc_forkedRepoInfo);
			thread.start();
		}
	};

	private Runnable threadProc_itemClick = new Runnable() {
		public void run() {
			try {
	        	m_intent = new Intent(RepositoryInfo.this, RepositoryInfo.class);
	        	m_intent.putExtra("repo_name", m_jsonForkData.getJSONObject(m_position).getString("name"));
	        	m_intent.putExtra("username", m_jsonForkData.getJSONObject(m_position).getString("owner"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			RepositoryInfo.this.startActivity(m_intent);

			runOnUiThread(new Runnable() {
				public void run() {
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private OnItemClickListener m_onForkListItemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			m_position = position;
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading Repository's Network...", true);
			Thread thread = new Thread(null, threadProc_itemClick);
			thread.start();
		}
	};

	private Runnable threadProc_gatherCommits = new Runnable() {
		public void run() {
			try {
				JSONArray commitsJSON = Hubroid.make_api_request(new URL("http://github.com/api/v2/json/commits/list/"
																		+ URLEncoder.encode(m_repo_owner)
																		+ "/"
																		+ URLEncoder.encode(m_repo_name)
																		+ "/"
																		+ URLEncoder.encode(m_branches.get(m_position)))).getJSONArray("commits");
				Log.d("debug1",commitsJSON.toString());
				m_commitListAdapter = new CommitListAdapter(RepositoryInfo.this, commitsJSON);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					ListView commitList = (ListView)findViewById(R.id.repo_commit_log_list);
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
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading Repository's Commits...", true);
			Thread thread = new Thread(null, threadProc_gatherCommits);
			thread.start();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			menu.add(0, 1, 0, "Clear Preferences");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			m_editor.clear().commit();
			Intent intent = new Intent(RepositoryInfo.this, Hubroid.class);
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
        setContentView(R.layout.repository_info);

        TabHost tabhost = getTabHost();

        tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator("Repository Info").setContent(R.id.repo_info_tab));
        tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator("Commit Log").setContent(R.id.repo_commits_tab));
        tabhost.addTab(tabhost.newTabSpec("tab3").setIndicator("Network").setContent(R.id.repo_forks_tab));

        tabhost.setCurrentTab(0);        

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_repo_name = extras.getString("repo_name");
        	m_repo_owner = extras.getString("username");
			try {
				URL repo_query = new URL("http://github.com/api/v2/json/repos/show/"
						+ URLEncoder.encode(m_repo_owner)
						+ "/"
						+ URLEncoder.encode(m_repo_name));
				m_jsonData = Hubroid.make_api_request(repo_query).getJSONObject("repository");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				TextView repo_name = (TextView)findViewById(R.id.repository_name);
				repo_name.setText(m_jsonData.getString("name"));
				TextView repo_desc = (TextView)findViewById(R.id.repository_description);
				repo_desc.setText(m_jsonData.getString("description"));
				TextView repo_owner = (TextView)findViewById(R.id.repository_owner);
				repo_owner.setText(m_jsonData.getString("owner"));
				TextView repo_fork_count = (TextView)findViewById(R.id.repository_fork_count);
				repo_fork_count.setText(m_jsonData.getString("forks"));
				TextView repo_watcher_count = (TextView)findViewById(R.id.repository_watcher_count);
				repo_watcher_count.setText(m_jsonData.getString("watchers"));

				m_jsonForkData = Hubroid.make_api_request(new URL("http://github.com/api/v2/json/repos/show/"
						+ URLEncoder.encode(m_repo_owner)
						+ "/"
						+ URLEncoder.encode(m_repo_name)
						+ "/network")).getJSONArray("network");

				if (m_jsonData.getBoolean("fork") == true) {
					// Find out what this is a fork of...
					String forked_user = m_jsonForkData.getJSONObject(0).getString("owner");
					String forked_repo = m_jsonForkData.getJSONObject(0).getString("name");
					// Show "Fork of:" label, it's value, and the button
					TextView repo_fork_of_label = (TextView)findViewById(R.id.repository_fork_of_label);
					repo_fork_of_label.setVisibility(0);
					TextView repo_fork_of = (TextView)findViewById(R.id.repository_fork_of);
					repo_fork_of.setText(forked_user + "/" + forked_repo);
					repo_fork_of.setVisibility(0);
					Button goto_forked_repository_btn = (Button)findViewById(R.id.goto_forked_repository_btn);
					goto_forked_repository_btn.setVisibility(0);
					// Set the onClick listener for the button
					goto_forked_repository_btn.setOnClickListener(forkedRepo_onClickListener);
				}

				m_forkListAdapter = new ForkListAdapter(RepositoryInfo.this, m_jsonForkData);
				ListView repo_list = (ListView)findViewById(R.id.repo_forks_list);
				repo_list.setAdapter(m_forkListAdapter);
				repo_list.setOnItemClickListener(m_onForkListItemClick);

				JSONObject branchesJson = Hubroid.make_api_request(new URL("http://github.com/api/v2/json/repos/show/"
																+ URLEncoder.encode(extras.getString("username"))
																+ "/"
																+ URLEncoder.encode(extras.getString("repo_name"))
																+ "/branches")).getJSONObject("branches");
				m_branches = new ArrayList<String>(branchesJson.length());
				Iterator<String> keys = branchesJson.keys();
				while (keys.hasNext()) {
					String next_branch = keys.next();
					m_branches.add(next_branch);
				}

				Spinner branchesSpinner = (Spinner)findViewById(R.id.branch_select);
				ArrayAdapter<String> branchesAdapter = new ArrayAdapter<String>(RepositoryInfo.this, android.R.layout.simple_spinner_item, m_branches);
				branchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				branchesSpinner.setAdapter(branchesAdapter);
				branchesSpinner.setOnItemSelectedListener(m_onBranchSelect);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			Button user_info_btn = (Button)findViewById(R.id.goto_repo_owner_info_btn);
			user_info_btn.setOnClickListener(username_onClickListener);
        }
    }
}

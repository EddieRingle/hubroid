package org.idlesoft.android.hubroid;

import java.io.File;
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
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class RepositoryInfo extends Activity {
	public ProgressDialog m_progressDialog;
	public JSONObject m_jsonData;
	public Intent m_intent;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public int m_position;
	public String m_repo_owner;
	public String m_repo_name;

	/* bleh.
	private Runnable threadProc_userInfo = new Runnable() {
		public void run() {
			TextView tv = (TextView)findViewById(R.id.tv_repository_info_owner);
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
	}; */

	/* Holding off on this...
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
	}; */

	/* This too...
	View.OnClickListener username_onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading User Information...");
			Thread thread = new Thread(null, threadProc_userInfo);
			thread.start();
		}
	}; */

	/* grr... too many methods I want to hold off from releasing >.<
	View.OnClickListener forkedRepo_onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading Repository Information...");
			Thread thread = new Thread(null, threadProc_forkedRepoInfo);
			thread.start();
		}
	}; */

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
        setContentView(R.layout.repository_info);

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
				TextView title = (TextView)findViewById(R.id.tv_top_bar_title);
				title.setText(m_jsonData.getString("name"));
				TextView repo_name = (TextView)findViewById(R.id.tv_repository_info_name);
				repo_name.setText(m_jsonData.getString("name"));
				TextView repo_desc = (TextView)findViewById(R.id.tv_repository_info_description);
				repo_desc.setText(m_jsonData.getString("description"));
				TextView repo_owner = (TextView)findViewById(R.id.tv_repository_info_owner);
				repo_owner.setText(m_jsonData.getString("owner"));
				TextView repo_watcher_count = (TextView)findViewById(R.id.tv_repository_info_watchers);
				repo_watcher_count.setText(m_jsonData.getString("watchers") + " watchers");
				TextView repo_fork_count = (TextView)findViewById(R.id.tv_repository_info_forks);
				repo_fork_count.setText(m_jsonData.getString("forks") + " forks");
				TextView repo_website = (TextView)findViewById(R.id.tv_repository_info_website);
				if (m_jsonData.getString("homepage") != "") {
					repo_website.setText(m_jsonData.getString("homepage"));
				} else {
					repo_website.setText("N/A");
				}

				/* Let's hold off on putting this in the new version for now...
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
				*/

			} catch (JSONException e) {
				e.printStackTrace();
			}

			((Button)findViewById(R.id.btn_repository_info_commits)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(RepositoryInfo.this, CommitsList.class);
					intent.putExtra("repo_name", m_repo_name);
					intent.putExtra("username", m_repo_owner);
					startActivity(intent);
				}
			});
			((Button)findViewById(R.id.btn_repository_info_network)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(RepositoryInfo.this, NetworkList.class);
					intent.putExtra("repo_name", m_repo_name);
					intent.putExtra("username", m_repo_owner);
					startActivity(intent);
				}
			});
			/* Hold off on this as well...
			Button user_info_btn = (Button)findViewById(R.id.goto_repo_owner_info_btn);
			user_info_btn.setOnClickListener(username_onClickListener);
			*/
        }
    }
}

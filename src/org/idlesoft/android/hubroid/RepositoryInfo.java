package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class RepositoryInfo extends TabActivity {
	public ProgressDialog m_progressDialog;
	public JSONObject m_jsonData;
	public Intent m_intent;

	private Runnable threadProc_userInfo = new Runnable() {
		public void run() {
			TextView tv = (TextView)findViewById(R.id.repository_owner);
			m_intent = new Intent(RepositoryInfo.this, UserInfo.class);
			m_intent.putExtra("username", tv.getText());
			RepositoryInfo.this.startActivity(m_intent);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repository_info);

        TabHost tabhost = getTabHost();

        tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator("Repository Info").setContent(R.id.repo_info_tab));
        tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator("Commit Log").setContent(R.id.repo_commits_tab));
        tabhost.addTab(tabhost.newTabSpec("tab3").setIndicator("Forks").setContent(R.id.repo_forks_tab));

        tabhost.setCurrentTab(0);        

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
			try {
				URL repo_query = new URL("http://github.com/api/v2/json/repos/show/"
						+ URLEncoder.encode(extras.getString("username"))
						+ "/"
						+ URLEncoder.encode(extras.getString("repo_name")));
				m_jsonData = Hubroid.make_api_request(repo_query).getJSONObject("repository");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
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

				if (m_jsonData.getBoolean("fork") == true) {
					// Find out what this is a fork of...
					JSONArray network = Hubroid.make_api_request(new URL("http://github.com/api/v2/json/repos/show/"
																		+ URLEncoder.encode(extras.getString("username"))
																		+ "/"
																		+ URLEncoder.encode(extras.getString("repo_name"))
																		+ "/network")).getJSONArray("network");
					String forked_user = network.getJSONObject(0).getString("owner");
					String forked_repo = network.getJSONObject(0).getString("name");
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

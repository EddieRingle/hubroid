package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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

	View.OnClickListener username_onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			m_progressDialog = ProgressDialog.show(RepositoryInfo.this, "Please wait...", "Loading User Information...");
			Thread thread = new Thread(null, threadProc_userInfo);
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
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Button user_info_btn = (Button)findViewById(R.id.goto_repo_owner_info_btn);
			user_info_btn.setOnClickListener(username_onClickListener);
        }
    }
}

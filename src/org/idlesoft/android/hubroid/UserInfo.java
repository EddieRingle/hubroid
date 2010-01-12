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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UserInfo extends TabActivity {
	public ProgressDialog m_progressDialog;
	public JSONObject m_jsonData;
	public JSONArray m_userRepoData;
	public RepositoriesListAdapter m_adapter;
	public Intent m_intent;
	public int m_position;

	private Runnable threadProc_itemClick = new Runnable() {
		public void run() {
			try {
	        	m_intent = new Intent(UserInfo.this, RepositoryInfo.class);
	        	m_intent.putExtra("repo_name", m_userRepoData.getJSONObject(m_position).getString("name"));
	        	m_intent.putExtra("username", m_userRepoData.getJSONObject(m_position).getString("owner"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			UserInfo.this.startActivity(m_intent);

			runOnUiThread(new Runnable() {
				public void run() {
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			m_position = position;
			m_progressDialog = ProgressDialog.show(UserInfo.this, "Please wait...", "Loading Repository...", true);
			Thread thread = new Thread(null, threadProc_itemClick);
			thread.start();
		}
	};

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.user_info);

        TabHost tabhost = getTabHost();

        tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator("User Info").setContent(R.id.user_info_tab));
        tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator("User's Repositories").setContent(R.id.user_repositories_tab));

        tabhost.setCurrentTab(0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	try {
				URL user_query = new URL("http://github.com/api/v2/json/user/show/"
										+ URLEncoder.encode(extras.getString("username")));
				m_jsonData = Hubroid.make_api_request(user_query).getJSONObject("user");
				TextView user_name = (TextView)findViewById(R.id.user_name);
				user_name.setText(m_jsonData.getString("login"));
				TextView user_fullname = (TextView)findViewById(R.id.user_fullname);
				user_fullname.setText(m_jsonData.getString("name"));
				TextView user_email = (TextView)findViewById(R.id.user_email);
				user_email.setText(m_jsonData.getString("email"));
				TextView user_location = (TextView)findViewById(R.id.user_location);
				user_location.setText(m_jsonData.getString("location"));
				TextView user_repository_count = (TextView)findViewById(R.id.user_repository_count);
				user_repository_count.setText(m_jsonData.getString("public_repo_count"));

				m_userRepoData = Hubroid.make_api_request(new URL("http://github.com/api/v2/json/repos/show/"
															+ URLEncoder.encode(extras.getString("username")))).getJSONArray("repositories");
				m_adapter = new RepositoriesListAdapter(UserInfo.this, m_userRepoData);
				ListView repo_list = (ListView)findViewById(R.id.repo_list);
				repo_list.setAdapter(m_adapter);
				repo_list.setOnItemClickListener(m_MessageClickedHandler);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}

package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RepositoriesList extends ListActivity {
	private RepositoriesListAdapter m_adapter;
	public ProgressDialog m_progressDialog;
	private EditText m_searchBox;
	public JSONObject m_jsonData;
	public int m_position;
	public Intent m_intent;

	public RepositoriesListAdapter initializeList() {
		RepositoriesListAdapter adapter = null;
		try {
			URL query = new URL("http://github.com/api/v2/json/repos/search/" + URLEncoder.encode(m_searchBox.getText().toString()));
			m_jsonData = Hubroid.make_api_request(query);
			if (m_jsonData == null) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(RepositoriesList.this, "Error gathering repository data, please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				adapter = new RepositoriesListAdapter(getApplicationContext(), m_jsonData.getJSONArray("repositories"));
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
	        	m_intent = new Intent(RepositoriesList.this, RepositoryInfo.class);
	        	m_intent.putExtra("repo_name", m_jsonData.getJSONArray("repositories").getJSONObject(m_position).getString("name"));
	        	m_intent.putExtra("username", m_jsonData.getJSONArray("repositories").getJSONObject(m_position).getString("username"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					RepositoriesList.this.startActivity(m_intent);
				}
			});
		}
	};

	private OnClickListener m_btnSearchListener = new OnClickListener() {
		public void onClick(View v) {
			m_searchBox = (EditText)findViewById(R.id.search_repositories_box);
			if (m_searchBox.getText().toString() != "") {
				m_progressDialog = ProgressDialog.show(RepositoriesList.this, "Please wait...", "Searching Repositories...", true);
				Thread thread = new Thread(null, threadProc_initializeList);
				thread.start();
			}
		}
	};
	
	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			m_position = position;
			Thread thread = new Thread(null, threadProc_itemClick);
			thread.start();
		}
	};

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repositories_list);
    }

    @Override
    public void onStart() {
    	super.onStart();

    	Button btnSearch = (Button)findViewById(R.id.search_repositories_btn);
        btnSearch.setOnClickListener(m_btnSearchListener);

        ListView list = (ListView)findViewById(android.R.id.list);
        list.setOnItemClickListener(m_MessageClickedHandler);
    }
}
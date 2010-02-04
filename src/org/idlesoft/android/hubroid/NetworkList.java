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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class NetworkList extends Activity {
	public ProgressDialog m_progressDialog;
	public Intent m_intent;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public int m_position;
	public String m_repo_owner;
	public String m_repo_name;
	public ForkListAdapter m_forkListAdapter;
	public JSONArray m_jsonForkData;

	private Runnable threadProc_itemClick = new Runnable() {
		public void run() {
			try {
	        	m_intent = new Intent(NetworkList.this, RepositoryInfo.class);
	        	m_intent.putExtra("repo_name", m_jsonForkData.getJSONObject(m_position).getString("name"));
	        	m_intent.putExtra("username", m_jsonForkData.getJSONObject(m_position).getString("owner"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			NetworkList.this.startActivity(m_intent);

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
			m_progressDialog = ProgressDialog.show(NetworkList.this, "Please wait...", "Loading Repository's Network...", true);
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
        setContentView(R.layout.network);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_repo_name = extras.getString("repo_name");
        	m_repo_owner = extras.getString("username");

			try {
				TextView title = (TextView)findViewById(R.id.tv_top_bar_title);
				title.setText("Network");

				URL forkrequest = new URL("http://github.com/api/v2/json/repos/show/"
						+ URLEncoder.encode(m_repo_owner)
						+ "/"
						+ URLEncoder.encode(m_repo_name)
						+ "/network");
				JSONObject forkjson = Hubroid.make_api_request(forkrequest);
				m_jsonForkData = forkjson.getJSONArray("network");

				m_forkListAdapter = new ForkListAdapter(NetworkList.this, m_jsonForkData);

				ListView repo_list = (ListView)findViewById(R.id.lv_network_list);
				repo_list.setAdapter(m_forkListAdapter);
				repo_list.setOnItemClickListener(m_onForkListItemClick);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
        }
    }
}

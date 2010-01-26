package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
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

public class UsersList extends ListActivity {
	private UsersListAdapter m_adapter;
	public ProgressDialog m_progressDialog;
	private EditText m_searchBox;
	public JSONObject m_jsonData;
	public Intent m_intent;
	public int m_position;

	public UsersListAdapter initializeList() {
		UsersListAdapter adapter = null;
		try {
			URL query = new URL("http://github.com/api/v2/json/user/search/" + URLEncoder.encode(m_searchBox.getText().toString()));
			m_jsonData = Hubroid.make_api_request(query);

			if (m_jsonData == null) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(UsersList.this, "Error gathering user data, please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				JSONArray array = m_jsonData.getJSONArray("users");
				adapter = new UsersListAdapter(getApplicationContext(), array);
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
	        	m_intent = new Intent(UsersList.this, UserInfo.class);
	        	m_intent.putExtra("username", m_jsonData.getJSONArray("users").getJSONObject(m_position).getString("username"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {
				public void run() {
					UsersList.this.startActivity(m_intent);
				}
			});
		}
	};

	private OnClickListener m_btnSearchListener = new OnClickListener() {
		public void onClick(View v) {
			m_searchBox = (EditText)findViewById(R.id.search_users_box);
			if (m_searchBox.getText().toString() != "") {
				m_progressDialog = ProgressDialog.show(UsersList.this, "Please wait...", "Searching Users...", true);
				Thread thread = new Thread(null, threadProc_initializeList);
				thread.start();
			}
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        m_position = position;
	        Thread thread = new Thread(null, threadProc_itemClick);
	        thread.start();
		}
	};

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.users_list);
    }

    @Override
    public void onStart() {
    	super.onStart();

    	Button btnSearch = (Button)findViewById(R.id.search_users_btn);
        btnSearch.setOnClickListener(m_btnSearchListener);

        ListView list = (ListView) findViewById(android.R.id.list);
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
				m_adapter = new UsersListAdapter(getApplicationContext(), m_jsonData.getJSONArray("users"));
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
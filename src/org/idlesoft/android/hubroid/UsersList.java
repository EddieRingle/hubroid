package org.idlesoft.android.hubroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
	public JSONArray m_jsonData;
	public Intent m_intent;
	public int m_position;

	public JSONArray request(URL url) throws ClientProtocolException, IOException, URISyntaxException, JSONException {
		HttpClient c = new DefaultHttpClient();
		HttpGet getReq = new HttpGet(url.toURI());
		HttpResponse resp = c.execute(getReq);
		JSONArray json = null;
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			resp.getEntity().writeTo(os);
			json = new JSONObject(os.toString()).getJSONArray("users");
		}
		return json;
	}

	public void initializeList() {
		try {
			URL query = new URL("http://github.com/api/v2/json/user/search/" + URLEncoder.encode(m_searchBox.getText().toString()));
			m_jsonData = request(query);
			m_adapter = new UsersListAdapter(UsersList.this, m_jsonData);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Runnable threadProc_initializeList = new Runnable() {
		public void run() {
			initializeList();

			runOnUiThread(new Runnable() {
				public void run() {
					setListAdapter(m_adapter);
					m_progressDialog.dismiss();
				}
			});
		}
	};

	private Runnable threadProc_itemClick = new Runnable() {
		public void run() {
			try {
	        	m_intent = new Intent(UsersList.this, UserInfo.class);
	        	m_intent.putExtra("username", m_jsonData.getJSONObject(m_position).getString("username"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
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
		public void onItemClick(AdapterView parent, View v, int position, long id) {
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
}
package org.idlesoft.android.hubroid;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Hubroid extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public ProgressDialog m_progressDialog;
	public boolean m_isLoggedIn;

	public static JSONObject make_api_request(URL url) {
		JSONObject json = null;
		HttpClient c = new DefaultHttpClient();
		HttpGet getReq;
		try {
			getReq = new HttpGet(url.toURI());
			HttpResponse resp = c.execute(getReq);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				resp.getEntity().writeTo(os);
				json = new JSONObject(os.toString());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static Bitmap getGravatar(String id, int size) { 
		Bitmap bm = null; 
    	try { 
    		URL aURL = new URL("http://www.gravatar.com/avatar.php?gravatar_id="
    							+ URLEncoder.encode(id)
    							+ "&size="
    							+ size);
        	URLConnection conn = aURL.openConnection(); 
        	conn.connect(); 
        	InputStream is = conn.getInputStream(); 
        	BufferedInputStream bis = new BufferedInputStream(is); 
        	bm = BitmapFactory.decodeStream(bis); 
        	bis.close(); 
        	is.close(); 
    	} catch (IOException e) { 
    		Log.e("debug", "Error getting bitmap", e); 
    	} 
    	return bm; 
    }

	private Runnable threadProc_login = new Runnable() {
		public void run() {
			EditText loginBox = (EditText)findViewById(R.id.et_splash_login_user);
			EditText tokenBox = (EditText)findViewById(R.id.et_splash_login_token);
			String login = loginBox.getText().toString();
			String token = tokenBox.getText().toString();
			URL query = null;
			try {
				query = new URL("http://github.com/api/v2/json/user/emails?login="
									+ URLEncoder.encode(login)
									+ "&token="
									+ URLEncoder.encode(token));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			runOnUiThread(new Runnable() {
				public void run() {
					m_progressDialog.setMessage("Authenticating...");
					m_progressDialog.show();
				}
			});
			JSONObject result = make_api_request(query);
			if (result == null || result.has("error")) {
				runOnUiThread(new Runnable() {
					public void run() {
						m_progressDialog.dismiss();
						Toast.makeText(Hubroid.this, "Error authenticating with server", Toast.LENGTH_LONG).show();
					}
				});
			} else if(result.has("emails")) {
				m_editor.putString("login", login);
				m_editor.putString("token", token);
				m_editor.putBoolean("isLoggedIn", true);
				m_editor.commit();
				runOnUiThread(new Runnable() {
					public void run() {
						m_progressDialog.dismiss();
						Intent intent = new Intent(Hubroid.this, MainScreen.class);
			        	startActivity(intent);
					}
				});
			}
		}
	};

	private OnClickListener m_loginButtonClick = new OnClickListener() {
		public void onClick(View v) {
			Thread thread = new Thread(threadProc_login);
			m_progressDialog = ProgressDialog.show(Hubroid.this, "Logging in...", "Initializing...");
			thread.start();
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        TabHost m_TabHost = getTabHost();

        m_TabHost.addTab(m_TabHost.newTabSpec("tab1")
        					.setIndicator(getString(R.string.repositories_tab_label), getResources().getDrawable(R.drawable.repository))
        					.setContent(new Intent(this, RepositoriesList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        m_TabHost.addTab(m_TabHost.newTabSpec("tab2")
        					.setIndicator(getString(R.string.users_tab_label), getResources().getDrawable(R.drawable.users))
        					.setContent(new Intent(this, UsersList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        */
        m_prefs = getSharedPreferences(PREFS_NAME, 0);
    	m_editor = m_prefs.edit();
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);
        if (!m_isLoggedIn) {
        	setContentView(R.layout.splash);
        	Button loginBtn = (Button)findViewById(R.id.btn_splash_login);
        	loginBtn.setOnClickListener(m_loginButtonClick);
        } else {
        	Intent intent = new Intent(Hubroid.this, MainScreen.class);
        	startActivity(intent);
        }
    }
}
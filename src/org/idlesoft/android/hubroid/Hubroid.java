package org.idlesoft.android.hubroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Hubroid extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	public ProgressDialog m_progressDialog;

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
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("login", login);
				editor.putString("token", token);
				editor.putBoolean("isLoggedIn", true);
				editor.commit();
				runOnUiThread(new Runnable() {
					public void run() {
						m_progressDialog.dismiss();
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
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
        	setContentView(R.layout.splash);
        	Button loginBtn = (Button)findViewById(R.id.btn_splash_login);
        	loginBtn.setOnClickListener(m_loginButtonClick);
        } else {
        	setContentView(R.layout.splash);
        	Toast.makeText(Hubroid.this, "Yay!", Toast.LENGTH_SHORT).show();
        }
    }
}
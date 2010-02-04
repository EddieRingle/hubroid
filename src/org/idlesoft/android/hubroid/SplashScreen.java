/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SplashScreen extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public ListView m_menuList;
	public JSONObject m_userData;
	public ProgressDialog m_progressDialog;
	public boolean m_isLoggedIn;

	private Runnable threadProc_login = new Runnable() {
		public void run() {
			EditText loginBox = (EditText) findViewById(R.id.et_splash_login_user);
			EditText tokenBox = (EditText) findViewById(R.id.et_splash_login_token);
			String login = loginBox.getText().toString();
			String token = tokenBox.getText().toString();
			URL query = null;
			try {
				query = new URL(
						"http://github.com/api/v2/json/user/emails?login="
								+ URLEncoder.encode(login) + "&token="
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
			JSONObject result = Hubroid.make_api_request(query);
			if (result == null || result.has("error")) {
				runOnUiThread(new Runnable() {
					public void run() {
						m_progressDialog.dismiss();
						Toast.makeText(SplashScreen.this,
								"Error authenticating with server",
								Toast.LENGTH_LONG).show();
					}
				});
			} else if (result.has("emails")) {
				m_editor.putString("login", login);
				m_editor.putString("token", token);
				m_editor.putBoolean("isLoggedIn", true);
				m_editor.commit();
				runOnUiThread(new Runnable() {
					public void run() {
						m_progressDialog.dismiss();
						Intent intent = new Intent(SplashScreen.this, Hubroid.class);
						startActivity(intent);
						SplashScreen.this.finish();
					}
				});
			}
		}
	};

	private OnClickListener m_loginButtonClick = new OnClickListener() {
		public void onClick(View v) {
			Thread thread = new Thread(threadProc_login);
			m_progressDialog = ProgressDialog.show(SplashScreen.this,
					"Logging in...", "Initializing...");
			thread.start();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_prefs = getSharedPreferences(PREFS_NAME, 0);
		m_editor = m_prefs.edit();
		m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);
		
		setContentView(R.layout.splash);
		Button loginBtn = (Button) findViewById(R.id.btn_splash_login);
		loginBtn.setOnClickListener(m_loginButtonClick);
	}
}
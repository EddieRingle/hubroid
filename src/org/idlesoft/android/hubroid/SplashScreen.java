/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import com.flurry.android.FlurryAgent;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.User;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
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
	private Thread m_thread;
	private GitHubAPI mGapi = new GitHubAPI();

	private Runnable threadProc_login = new Runnable() {
		public void run() {
			EditText loginBox = (EditText) findViewById(R.id.et_splash_login_user);
			EditText passwordBox = (EditText) findViewById(R.id.et_splash_login_password);
			String login = loginBox.getText().toString();
			String password = passwordBox.getText().toString();

			runOnUiThread(new Runnable() {
				public void run() {
					m_progressDialog.setMessage("Authenticating...");
					m_progressDialog.show();
				}
			});
			mGapi.authenticate(login, password);
			Response authResp = mGapi.user.info(login);

			if (authResp.statusCode == 401) {
				runOnUiThread(new Runnable() {
					public void run() {
						m_progressDialog.dismiss();
						Toast.makeText(SplashScreen.this,
								"Error authenticating with server",
								Toast.LENGTH_LONG).show();
					}
				});
			} else if (authResp.statusCode == 200) {
				m_editor.putString("login", login);
				m_editor.putString("password", password);
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
			m_thread = new Thread(threadProc_login);
			m_progressDialog = ProgressDialog.show(SplashScreen.this,
					"Logging in...", "Initializing...");
			m_thread.start();
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

	@Override
    public void onPause()
    {
    	if (m_thread != null && m_thread.isAlive())
    		m_thread.stop();
    	if (m_progressDialog != null && m_progressDialog.isShowing())
    		m_progressDialog.dismiss();
    	super.onPause();
    }

	@Override
    public void onStart()
    {
       super.onStart();
       FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
}
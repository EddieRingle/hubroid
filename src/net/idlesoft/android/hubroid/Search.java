/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.hubroid;

import java.io.File;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class Search extends Activity {
	private static final String REPO_TYPE = "repositories";
	private static final String USER_TYPE = "users";

	private RepositoriesListAdapter m_repositories_adapter;
	private SearchUsersListAdapter m_users_adapter;
	public ProgressDialog m_progressDialog;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private boolean m_isLoggedIn;
	private Dialog m_loginDialog;
	private String m_username;
	private String m_token;
	public String m_type;
	public JSONArray m_repositoriesData;
	public JSONArray m_usersData;
	public Intent m_intent;
	public int m_position;
	private Thread m_thread;
	public InputMethodManager m_imm;
	private GitHubAPI _gapi;

	public void initializeList() {
		String query = ((EditText) findViewById(R.id.et_search_search_box)).getText().toString();
		if (m_type.equals(REPO_TYPE)) {
			try {
				JSONObject response;
				if (m_isLoggedIn)
					response = new JSONObject(_gapi.repo.search(query).resp);
				else
					response = new JSONObject(_gapi.repo.search(query).resp); 
				m_repositoriesData = response.getJSONArray(REPO_TYPE);
				m_repositories_adapter = new RepositoriesListAdapter(getApplicationContext(), m_repositoriesData);
			} catch (JSONException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(Search.this, "Error gathering repository data, please try again.", Toast.LENGTH_SHORT).show();
					}
				});
				e.printStackTrace();
			}
		} else if (m_type.equals(USER_TYPE)) {
			try {
				JSONObject response = new JSONObject(_gapi.user.search(query).resp);
				m_usersData = response.getJSONArray(USER_TYPE);
				m_users_adapter = new SearchUsersListAdapter(getApplicationContext(), m_usersData);
			} catch (JSONException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(Search.this, "Error gathering user data, please try again.", Toast.LENGTH_SHORT).show();
					}
				});
				e.printStackTrace();
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == 5005) {
			Toast.makeText(Search.this, "That user has recently been deleted.", Toast.LENGTH_SHORT).show();
		}
	}

	public void toggleList(String type) {
		ListView repositoriesList = (ListView) findViewById(R.id.lv_search_repositories_list);
		ListView usersList = (ListView) findViewById(R.id.lv_search_users_list);
		TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

		if (type.equals("") || type == null) {
			type = (m_type.equals(REPO_TYPE)) ? USER_TYPE : REPO_TYPE;
		}
		m_type = type;

		if (m_type.equals(REPO_TYPE)) {
			repositoriesList.setVisibility(View.VISIBLE);
			usersList.setVisibility(View.GONE);
			title.setText("Search Repositories");
		} else if (m_type.equals(USER_TYPE)) {
			usersList.setVisibility(View.VISIBLE);
			repositoriesList.setVisibility(View.GONE);
			title.setText("Search Users");
		}
	}

	public void search()
	{
		EditText search_box = (EditText) findViewById(R.id.et_search_search_box);
		if (!search_box.getText().toString().equals("")) {
			if (m_type.equals(REPO_TYPE)) {
				m_progressDialog = ProgressDialog.show(Search.this, "Please wait...", "Searching Repositories...", true);
				m_thread = new Thread(new Runnable() {
					public void run() {
						initializeList();
						runOnUiThread(new Runnable() {
							public void run() {
								((ListView)findViewById(R.id.lv_search_repositories_list)).setAdapter(m_repositories_adapter);
								m_progressDialog.dismiss();
							}
						});
					}
				});
				m_thread.start();
			} else if (m_type.equals(USER_TYPE)) {
				m_progressDialog = ProgressDialog.show(Search.this, "Please wait...", "Searching Users...", true);
				m_thread = new Thread(new Runnable() {
					public void run() {
						initializeList();
						runOnUiThread(new Runnable() {
							public void run() {
								((ListView)findViewById(R.id.lv_search_users_list)).setAdapter(m_users_adapter);
								m_progressDialog.dismiss();
							}
						});
					}
				});
				m_thread.start();
			}
		}
	}

	private OnClickListener onButtonToggleClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == R.id.btn_search_repositories) {
				toggleList(REPO_TYPE);
				m_type = REPO_TYPE;
			} else if (v.getId() == R.id.btn_search_users) {
				toggleList(USER_TYPE);
				m_type = USER_TYPE;
			}
		}
	};

	private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			m_position = position;
			try {
				if (m_type.equals(REPO_TYPE)) {
					m_intent = new Intent(Search.this, RepositoryInfo.class);
					m_intent.putExtra("repo_name", m_repositoriesData
							.getJSONObject(m_position).getString("name"));
					m_intent.putExtra("username", m_repositoriesData
							.getJSONObject(m_position).getString("username"));
				} else if (m_type.equals(USER_TYPE)) {
					m_intent = new Intent(Search.this, UserInfo.class);
					m_intent.putExtra("username", m_usersData.getJSONObject(
							m_position).getString("username"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Search.this.startActivityForResult(m_intent, 5005);
		}
	};

	public Dialog onCreateDialog(int id)
	{
		m_loginDialog = new Dialog(Search.this);
		m_loginDialog.setCancelable(true);
		m_loginDialog.setTitle("Login");
		m_loginDialog.setContentView(R.layout.login_dialog);
		Button loginBtn = (Button) m_loginDialog.findViewById(R.id.btn_loginDialog_login);
		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				m_progressDialog = ProgressDialog.show(Search.this, null, "Logging in...");
				m_thread = new Thread(new Runnable() {
					public void run() {
						String username = ((EditText)m_loginDialog.findViewById(R.id.et_loginDialog_userField)).getText().toString();
						String token = ((EditText)m_loginDialog.findViewById(R.id.et_loginDialog_tokenField)).getText().toString();

						if (username.equals("") || token.equals("")) {
							runOnUiThread(new Runnable() {
								public void run() {
									m_progressDialog.dismiss();
									Toast.makeText(Search.this, "Login details cannot be blank", Toast.LENGTH_LONG).show();
								}
							});
						} else {
							Response authResp = _gapi.user.info(username);
	
							if (authResp.statusCode == 401) {
								runOnUiThread(new Runnable() {
									public void run() {
										m_progressDialog.dismiss();
										Toast.makeText(Search.this, "Error authenticating with server", Toast.LENGTH_LONG).show();
									}
								});
							} else if (authResp.statusCode == 200) {
								m_editor.putString("login", username);
								m_editor.putString("token", token);
								m_editor.putBoolean("isLoggedIn", true);
								m_editor.commit();
								runOnUiThread(new Runnable() {
									public void run() {
										m_progressDialog.dismiss();
										dismissDialog(0);
										Intent intent = new Intent(Search.this, Hubroid.class);
										startActivity(intent);
										finish();
									}
								});
							}
						}
					}
				});
				m_thread.start();
			}
		});
		return m_loginDialog;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			if (!m_isLoggedIn)
				menu.add(0, 0, 0, "Login");
			else if (m_isLoggedIn)
				menu.add(0, 1, 0, "Logout");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			showDialog(0);
			return true;
		case 1:
			m_editor.clear().commit();
			Intent intent = new Intent(getApplicationContext(), Hubroid.class);
			startActivity(intent);
			finish();
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

	public void navBarOnClickSetup()
	{
		((Button)findViewById(R.id.btn_navbar_repositories)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Search.this, RepositoriesList.class));
				finish();
			}
		});
		((Button)findViewById(R.id.btn_navbar_profile)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Search.this, UserInfo.class));
				finish();
			}
		});
		((Button)findViewById(R.id.btn_navbar_search)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Search.this, Search.class));
				finish();
			}
		});

		((Button)findViewById(R.id.btn_navbar_search)).setEnabled(false);
		if (!m_isLoggedIn) { 
			((Button)findViewById(R.id.btn_navbar_profile)).setVisibility(View.GONE);
			((Button)findViewById(R.id.btn_navbar_repositories)).setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.search);

		m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
		m_editor = m_prefs.edit();
		m_type = REPO_TYPE;
		m_username = m_prefs.getString("login", "");
		m_token = m_prefs.getString("token", "");
		m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			navBarOnClickSetup();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

		m_imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

		((Button) findViewById(R.id.btn_search_repositories))
				.setOnClickListener(onButtonToggleClickListener);
		((Button) findViewById(R.id.btn_search_users))
				.setOnClickListener(onButtonToggleClickListener);
		((ImageButton) findViewById(R.id.btn_search_go)).setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				m_imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				search();
			}
		});
		((EditText) findViewById(R.id.et_search_search_box)).setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionCode, KeyEvent arg2)
			{
				if (actionCode == EditorInfo.IME_ACTION_SEARCH)
					search();
				return false;
			}
		});

		((ListView) findViewById(R.id.lv_search_repositories_list))
				.setOnItemClickListener(m_MessageClickedHandler);
		((ListView) findViewById(R.id.lv_search_users_list))
				.setOnItemClickListener(m_MessageClickedHandler);
	}

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("type", m_type);
		if (m_repositoriesData != null) {
			savedInstanceState.putString("repositories_json",
					m_repositoriesData.toString());
		}
		if (m_usersData != null) {
			savedInstanceState.putString("users_json", m_usersData.toString());
		}
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		m_type = savedInstanceState.getString("type");
		try {
			if (savedInstanceState.containsKey("repositories_json")) {
				m_repositoriesData = new JSONArray(savedInstanceState.getString("repositories_json"));
			} else {
				m_repositoriesData = new JSONArray();
			}
		} catch (JSONException e) {
			m_repositoriesData = new JSONArray();
		}
		try {
			if (savedInstanceState.containsKey("users_json")) {
				m_usersData = new JSONArray(savedInstanceState.getString("users_json"));
			} else {
				m_usersData = new JSONArray();
			}
		} catch (JSONException e) {
			m_usersData = new JSONArray();
		}
 		if (m_repositoriesData.length() > 0) {
			m_repositories_adapter = new RepositoriesListAdapter(Search.this, m_repositoriesData);
		} else {
			m_repositories_adapter = null;
		}
 		if (m_usersData.length() > 0) {
 			m_users_adapter = new SearchUsersListAdapter(Search.this, m_usersData);
 		} else {
 			m_users_adapter = null;
 		}
	}

	@Override
	public void onResume() {
		super.onResume();
		ListView repositories = (ListView) findViewById(R.id.lv_search_repositories_list);
		ListView users = (ListView) findViewById(R.id.lv_search_users_list);

		repositories.setAdapter(m_repositories_adapter);
		users.setAdapter(m_users_adapter);
		toggleList(m_type);
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
}
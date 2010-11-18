/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import java.io.File;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.adapters.RepositoriesListAdapter;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class RepositoriesList extends Activity {
    private RepositoriesListAdapter m_usersRepositories_adapter;
    private RepositoriesListAdapter m_watchedRepositories_adapter;
    public ProgressDialog m_progressDialog;
    private SharedPreferences m_prefs;
    private SharedPreferences.Editor m_editor;
    public String m_targetUser;
    public String m_username;
    public boolean m_isLoggedIn;
    public String m_token;
    public String m_type;
    public JSONArray m_usersRepoData;
    public JSONArray m_watchedRepoData;
    public Intent m_intent;
    public int m_position;
    private Thread m_thread;
    private Dialog m_loginDialog;
    private GitHubAPI _gapi;

    public void initializeList() {
        JSONObject json = null;
        try {
            json = new JSONObject(_gapi.repo.list(m_targetUser).resp);
            m_usersRepoData = json.getJSONArray("repositories");
            json = new JSONObject(_gapi.user.watching(m_targetUser).resp);
            m_watchedRepoData = json.getJSONArray("repositories");
            m_usersRepositories_adapter = new RepositoriesListAdapter(RepositoriesList.this,
                    m_usersRepoData);
            m_watchedRepositories_adapter = new RepositoriesListAdapter(RepositoriesList.this,
                    m_watchedRepoData);
        } catch (JSONException e) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(RepositoriesList.this,
                            "Error gathering repository data, please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }

    private Runnable threadProc_initializeList = new Runnable() {
        public void run() {
            initializeList();

            runOnUiThread(new Runnable() {
                public void run() {
                    if (m_type.equals("users")) {
                        toggleList("users");
                    } else if (m_type.equals("watched")) {
                        toggleList("watched");
                    }
                    m_progressDialog.dismiss();
                }
            });
        }
    };

    public void toggleList(String type) {
        TextView title = (TextView) findViewById(R.id.tv_top_bar_title);

        if (type.equals("") || type == null) {
            type = (m_type.equals("users")) ? "watched" : "users";
        }
        m_type = type;

        if (m_type.equals("users")) {
            ((ListView) findViewById(R.id.lv_repositories_list))
                    .setAdapter(m_usersRepositories_adapter);
            if (m_targetUser.equalsIgnoreCase(m_username))
                title.setText("Your Repos");
            else
                title.setText(m_targetUser + "'s Repos");
        } else if (m_type.equals("watched")) {
            ((ListView) findViewById(R.id.lv_repositories_list))
                    .setAdapter(m_watchedRepositories_adapter);
            if (m_targetUser.equalsIgnoreCase(m_username))
                title.setText("Your Watched Repos");
            else
                title.setText(m_targetUser + "'s Watched Repos");
        }
    }

    private OnClickListener onButtonToggleClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == R.id.btn_repositories_list_usersRepos) {
                toggleList("users");
                m_type = "users";
            } else if (v.getId() == R.id.btn_repositories_list_watchedRepos) {
                toggleList("watched");
                m_type = "watched";
            }
        }
    };

    private OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            m_position = position;
            try {
                m_intent = new Intent(RepositoriesList.this, RepositoryInfo.class);
                if (m_type.equals("users")) {
                    m_intent.putExtra("repo_name", m_usersRepoData.getJSONObject(m_position)
                            .getString("name"));
                    m_intent.putExtra("username", m_usersRepoData.getJSONObject(m_position)
                            .getString("owner"));
                } else if (m_type.equals("watched")) {
                    m_intent.putExtra("repo_name", m_watchedRepoData.getJSONObject(m_position)
                            .getString("name"));
                    m_intent.putExtra("username", m_watchedRepoData.getJSONObject(m_position)
                            .getString("owner"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RepositoriesList.this.startActivity(m_intent);
        }
    };

    public Dialog onCreateDialog(int id) {
        m_loginDialog = new Dialog(RepositoriesList.this);
        m_loginDialog.setCancelable(true);
        m_loginDialog.setTitle("Login");
        m_loginDialog.setContentView(R.layout.login_dialog);
        Button loginBtn = (Button) m_loginDialog.findViewById(R.id.btn_loginDialog_login);
        loginBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                m_progressDialog = ProgressDialog
                        .show(RepositoriesList.this, null, "Logging in...");
                m_thread = new Thread(new Runnable() {
                    public void run() {
                        String username = ((EditText) m_loginDialog
                                .findViewById(R.id.et_loginDialog_userField)).getText().toString();
                        String token = ((EditText) m_loginDialog
                                .findViewById(R.id.et_loginDialog_tokenField)).getText().toString();

                        if (username.equals("") || token.equals("")) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    m_progressDialog.dismiss();
                                    Toast.makeText(RepositoriesList.this,
                                            "Login details cannot be blank", Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        } else {
                            Response authResp = _gapi.user.info(username);

                            if (authResp.statusCode == 401) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        m_progressDialog.dismiss();
                                        Toast.makeText(RepositoriesList.this,
                                                "Error authenticating with server",
                                                Toast.LENGTH_LONG).show();
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
                                        Intent intent = new Intent(RepositoriesList.this,
                                                Hubroid.class);
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

    public void navBarOnClickSetup() {
        ((Button) findViewById(R.id.btn_navbar_repositories))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(RepositoriesList.this, RepositoriesList.class));
                        finish();
                    }
                });
        ((Button) findViewById(R.id.btn_navbar_profile)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(RepositoriesList.this, UserInfo.class));
                finish();
            }
        });
        ((Button) findViewById(R.id.btn_navbar_search)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(RepositoriesList.this, Search.class));
                finish();
            }
        });

        ((Button) findViewById(R.id.btn_navbar_repositories)).setEnabled(false);
        if (!m_isLoggedIn) {
            ((Button) findViewById(R.id.btn_navbar_profile)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.btn_navbar_repositories)).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repositories_list);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();
        m_type = "users";

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("username")) {
                m_targetUser = extras.getString("username");
            } else {
                m_targetUser = m_username;
            }
        } else {
            m_targetUser = m_username;
            navBarOnClickSetup();
        }

        m_progressDialog = ProgressDialog.show(RepositoriesList.this, "Please wait...",
                "Loading Repositories...", true);
        m_thread = new Thread(null, threadProc_initializeList);
        m_thread.start();
    }

    @Override
    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

        ((Button) findViewById(R.id.btn_repositories_list_usersRepos))
                .setOnClickListener(onButtonToggleClickListener);
        ((Button) findViewById(R.id.btn_repositories_list_watchedRepos))
                .setOnClickListener(onButtonToggleClickListener);

        ((ListView) findViewById(R.id.lv_repositories_list))
                .setOnItemClickListener(m_MessageClickedHandler);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("type", m_type);
        if (m_usersRepoData != null) {
            savedInstanceState.putString("usersRepos_json", m_usersRepoData.toString());
        }
        if (m_watchedRepoData != null) {
            savedInstanceState.putString("watchedRepos_json", m_watchedRepoData.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean keepGoing = true;
        m_type = savedInstanceState.getString("type");
        try {
            if (savedInstanceState.containsKey("usersRepos_json")) {
                m_usersRepoData = new JSONArray(savedInstanceState.getString("usersRepos_json"));
            } else {
                keepGoing = false;
            }
            if (savedInstanceState.containsKey("watchedRepos_json")) {
                m_watchedRepoData = new JSONArray(savedInstanceState.getString("watchedRepos_json"));
            } else {
                keepGoing = false;
            }
        } catch (JSONException e) {
            keepGoing = false;
        }
        if (keepGoing == true) {
            m_usersRepositories_adapter = new RepositoriesListAdapter(getApplicationContext(),
                    m_usersRepoData);
            m_watchedRepositories_adapter = new RepositoriesListAdapter(getApplicationContext(),
                    m_watchedRepoData);
        } else {
            m_usersRepositories_adapter = null;
            m_watchedRepositories_adapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleList(m_type);
    }

    @Override
    public void onPause() {
        if (m_thread != null && m_thread.isAlive())
            m_thread.stop();
        if (m_progressDialog != null && m_progressDialog.isShowing())
            m_progressDialog.dismiss();
        super.onPause();
    }
}
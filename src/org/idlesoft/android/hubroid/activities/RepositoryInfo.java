/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import com.flurry.android.FlurryAgent;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class RepositoryInfo extends Activity {
    private GitHubAPI _gapi;

    private SharedPreferences.Editor m_editor;

    public Intent m_intent;

    private boolean m_isWatching;

    public JSONObject m_jsonData;

    private String m_password;

    private SharedPreferences m_prefs;

    public ProgressDialog m_progressDialog;

    private String m_repo_name;

    private String m_repo_owner;

    private Thread m_thread;

    private String m_username;

    /*
     * bleh. private Runnable threadProc_userInfo = new Runnable() { public void
     * run() { TextView tv =
     * (TextView)findViewById(R.id.tv_repository_info_owner); m_intent = new
     * Intent(RepositoryInfo.this, UserInfo.class);
     * m_intent.putExtra("username", tv.getText());
     * RepositoryInfo.this.startActivity(m_intent); try { Thread.sleep(1000); }
     * catch (InterruptedException e) { e.printStackTrace(); } runOnUiThread(new
     * Runnable() { public void run() { m_progressDialog.dismiss(); } }); } };
     */

    /*
     * Holding off on this... private Runnable threadProc_forkedRepoInfo = new
     * Runnable() { public void run() { TextView tv =
     * (TextView)findViewById(R.id.repository_fork_of); m_intent = new
     * Intent(RepositoryInfo.this, RepositoryInfo.class);
     * m_intent.putExtra("username", tv.getText().toString().split("/")[0]);
     * m_intent.putExtra("repo_name", tv.getText().toString().split("/")[1]);
     * RepositoryInfo.this.startActivity(m_intent); try { Thread.sleep(1000); }
     * catch (InterruptedException e) { e.printStackTrace(); } runOnUiThread(new
     * Runnable() { public void run() { m_progressDialog.dismiss(); } }); } };
     */

    /*
     * This too... View.OnClickListener username_onClickListener = new
     * View.OnClickListener() { public void onClick(View v) { m_progressDialog =
     * ProgressDialog.show(RepositoryInfo.this, "Please wait...",
     * "Loading User Information..."); Thread thread = new Thread(null,
     * threadProc_userInfo); thread.start(); } };
     */

    /*
     * grr... too many methods I want to hold off from releasing >.<
     * View.OnClickListener forkedRepo_onClickListener = new
     * View.OnClickListener() { public void onClick(View v) { m_progressDialog =
     * ProgressDialog.show(RepositoryInfo.this, "Please wait...",
     * "Loading Repository Information..."); Thread thread = new Thread(null,
     * threadProc_forkedRepoInfo); thread.start(); } };
     */

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repository_info);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("username", "");
        m_password = m_prefs.getString("password", "");
        m_isWatching = false;

        _gapi = new GitHubAPI();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            m_repo_name = extras.getString("repo_name");
            m_repo_owner = extras.getString("username");

            try {
                m_jsonData = new JSONObject(_gapi.repo.info(m_repo_owner, m_repo_name).resp)
                        .getJSONObject("repository");

                final JSONArray watched_list = new JSONObject(_gapi.user.watching(m_username).resp)
                        .getJSONArray("repositories");
                final int length = watched_list.length() - 1;
                for (int i = 0; i <= length; i++) {
                    if (watched_list.getJSONObject(i).getString("name").equalsIgnoreCase(
                            m_repo_name)) {
                        m_isWatching = true;
                    }
                }

                // TextView title =
                // (TextView)findViewById(R.id.tv_top_bar_title);
                // title.setText(m_jsonData.getString("name"));
                final TextView repo_name = (TextView) findViewById(R.id.tv_repository_info_name);
                repo_name.setText(m_jsonData.getString("name"));
                final TextView repo_desc = (TextView) findViewById(R.id.tv_repository_info_description);
                repo_desc.setText(m_jsonData.getString("description"));
                final TextView repo_owner = (TextView) findViewById(R.id.tv_repository_info_owner);
                repo_owner.setText(m_jsonData.getString("owner"));
                final TextView repo_watcher_count = (TextView) findViewById(R.id.tv_repository_info_watchers);
                if (m_jsonData.getInt("watchers") == 1) {
                    repo_watcher_count.setText(m_jsonData.getInt("watchers") + " watcher");
                } else {
                    repo_watcher_count.setText(m_jsonData.getInt("watchers") + " watchers");
                }
                final TextView repo_fork_count = (TextView) findViewById(R.id.tv_repository_info_forks);
                if (m_jsonData.getInt("forks") == 1) {
                    repo_fork_count.setText(m_jsonData.getInt("forks") + " fork");
                } else {
                    repo_fork_count.setText(m_jsonData.getInt("forks") + " forks");
                }
                final TextView repo_website = (TextView) findViewById(R.id.tv_repository_info_website);
                if (m_jsonData.getString("homepage") != "") {
                    repo_website.setText(m_jsonData.getString("homepage"));
                } else {
                    repo_website.setText("N/A");
                }

                /*
                 * Let's hold off on putting this in the new version for now...
                 * if (m_jsonData.getBoolean("fork") == true) { // Find out what
                 * this is a fork of... String forked_user =
                 * m_jsonForkData.getJSONObject(0).getString("owner"); String
                 * forked_repo =
                 * m_jsonForkData.getJSONObject(0).getString("name"); // Show
                 * "Fork of:" label, it's value, and the button TextView
                 * repo_fork_of_label =
                 * (TextView)findViewById(R.id.repository_fork_of_label);
                 * repo_fork_of_label.setVisibility(0); TextView repo_fork_of =
                 * (TextView)findViewById(R.id.repository_fork_of);
                 * repo_fork_of.setText(forked_user + "/" + forked_repo);
                 * repo_fork_of.setVisibility(0); Button
                 * goto_forked_repository_btn =
                 * (Button)findViewById(R.id.goto_forked_repository_btn);
                 * goto_forked_repository_btn.setVisibility(0); // Set the
                 * onClick listener for the button
                 * goto_forked_repository_btn.setOnClickListener
                 * (forkedRepo_onClickListener); }
                 */

            } catch (final JSONException e) {
                e.printStackTrace();
            }

            ((Button) findViewById(R.id.btn_repository_info_commits))
                    .setOnClickListener(new OnClickListener() {
                        public void onClick(final View v) {
                            final Intent intent = new Intent(RepositoryInfo.this, CommitsList.class);
                            intent.putExtra("repo_name", m_repo_name);
                            intent.putExtra("username", m_repo_owner);
                            startActivity(intent);
                        }
                    });
            ((Button) findViewById(R.id.btn_repository_info_issues))
                    .setOnClickListener(new OnClickListener() {
                        public void onClick(final View v) {
                            final Intent intent = new Intent(RepositoryInfo.this, IssuesList.class);
                            intent.putExtra("repository", m_repo_name);
                            intent.putExtra("owner", m_repo_owner);
                            startActivity(intent);
                        }
                    });
            ((Button) findViewById(R.id.btn_repository_info_network))
                    .setOnClickListener(new OnClickListener() {
                        public void onClick(final View v) {
                            final Intent intent = new Intent(RepositoryInfo.this, NetworkList.class);
                            intent.putExtra("repo_name", m_repo_name);
                            intent.putExtra("username", m_repo_owner);
                            startActivity(intent);
                        }
                    });
            /*
             * Hold off on this as well... Button user_info_btn =
             * (Button)findViewById(R.id.goto_repo_owner_info_btn);
             * user_info_btn.setOnClickListener(username_onClickListener);
             */
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 3:
                try {
                    JSONObject newRepoInfo = null;
                    if (m_isWatching) {
                        final Response unwatchResp = _gapi.repo.unwatch(m_repo_owner, m_repo_name);
                        if (unwatchResp.statusCode == 200) {
                            newRepoInfo = new JSONObject(unwatchResp.resp)
                                    .getJSONObject("repository");
                            m_isWatching = false;
                        }
                    } else {
                        final Response watchResp = _gapi.repo.watch(m_repo_owner, m_repo_name);
                        if (watchResp.statusCode == 200) {
                            newRepoInfo = new JSONObject(watchResp.resp)
                                    .getJSONObject("repository");
                            m_isWatching = true;
                        }
                    }
                    if (newRepoInfo != null) {
                        final String newWatcherCount = newRepoInfo.getString("watchers");
                        if (newWatcherCount != m_jsonData.getString("watchers")) {
                            if (newWatcherCount == "1") {
                                ((TextView) findViewById(R.id.tv_repository_info_watchers))
                                        .setText(newWatcherCount + " watcher");
                            } else {
                                ((TextView) findViewById(R.id.tv_repository_info_watchers))
                                        .setText(newWatcherCount + " watchers");
                            }
                        }
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                m_editor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 2:
                final File root = Environment.getExternalStorageDirectory();
                if (root.canWrite()) {
                    final File hubroid = new File(root, "hubroid");
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
    public void onPause() {
        if ((m_thread != null) && m_thread.isAlive()) {
            m_thread.stop();
        }
        if ((m_progressDialog != null) && m_progressDialog.isShowing()) {
            m_progressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        if (m_isWatching) {
            menu.add(0, 3, 0, "Unwatch");
        } else {
            menu.add(0, 3, 0, "Watch");
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Clear Preferences");
        menu.add(0, 2, 0, "Clear Cache");
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}

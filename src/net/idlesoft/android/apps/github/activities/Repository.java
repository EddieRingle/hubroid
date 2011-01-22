/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.File;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
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

import com.flurry.android.FlurryAgent;

public class Repository extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    private SharedPreferences.Editor mEditor;

    public Intent mIntent;

    private boolean mIsWatching;

    public JSONObject mJson;

    private String mPassword;

    private SharedPreferences mPrefs;

    public ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repository);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");
        mIsWatching = false;

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");

            try {
                Response resp = mGapi.repo.info(mRepositoryOwner, mRepositoryName);
                if (resp.statusCode == 200) {
                    mJson = new JSONObject(resp.resp).getJSONObject("repository");
    
                    final JSONArray watched_list = new JSONObject(mGapi.user.watching(mUsername).resp)
                            .getJSONArray("repositories");
                    final int length = watched_list.length() - 1;
                    for (int i = 0; i <= length; i++) {
                        if (watched_list.getJSONObject(i).getString("name").equalsIgnoreCase(
                                mRepositoryName)) {
                            mIsWatching = true;
                        }
                    }
    
                    // TextView title =
                    // (TextView)findViewById(R.id.tv_top_bar_title);
                    // title.setText(m_jsonData.getString("name"));
                    final TextView repo_name = (TextView) findViewById(R.id.tv_repository_info_name);
                    repo_name.setText(mJson.getString("name"));
                    final TextView repo_desc = (TextView) findViewById(R.id.tv_repository_info_description);
                    repo_desc.setText(mJson.getString("description"));
                    final TextView repo_owner = (TextView) findViewById(R.id.tv_repository_info_owner);
                    repo_owner.setText(mJson.getString("owner"));
                    final TextView repo_watcher_count = (TextView) findViewById(R.id.tv_repository_info_watchers);
                    if (mJson.getInt("watchers") == 1) {
                        repo_watcher_count.setText(mJson.getInt("watchers") + " watcher");
                    } else {
                        repo_watcher_count.setText(mJson.getInt("watchers") + " watchers");
                    }
                    final TextView repo_fork_count = (TextView) findViewById(R.id.tv_repository_info_forks);
                    if (mJson.getInt("forks") == 1) {
                        repo_fork_count.setText(mJson.getInt("forks") + " fork");
                    } else {
                        repo_fork_count.setText(mJson.getInt("forks") + " forks");
                    }
                    final TextView repo_website = (TextView) findViewById(R.id.tv_repository_info_website);
                    if (mJson.getString("homepage") != "") {
                        repo_website.setText(mJson.getString("homepage"));
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
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }

            ((Button) findViewById(R.id.btn_repository_info_branches))
                    .setOnClickListener(new OnClickListener() {
                        public void onClick(final View v) {
                            final Intent intent = new Intent(Repository.this, BranchesList.class);
                            intent.putExtra("repo_name", mRepositoryName);
                            intent.putExtra("repo_owner", mRepositoryOwner);
                            startActivity(intent);
                        }
                    });
            ((Button) findViewById(R.id.btn_repository_info_issues))
                    .setOnClickListener(new OnClickListener() {
                        public void onClick(final View v) {
                            final Intent intent = new Intent(Repository.this, IssuesList.class);
                            intent.putExtra("repository", mRepositoryName);
                            intent.putExtra("owner", mRepositoryOwner);
                            startActivity(intent);
                        }
                    });
            ((Button) findViewById(R.id.btn_repository_info_network))
                    .setOnClickListener(new OnClickListener() {
                        public void onClick(final View v) {
                            final Intent intent = new Intent(Repository.this, NetworkList.class);
                            intent.putExtra("repo_name", mRepositoryName);
                            intent.putExtra("username", mRepositoryOwner);
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
                    if (mIsWatching) {
                        final Response unwatchResp = mGapi.repo.unwatch(mRepositoryOwner, mRepositoryName);
                        if (unwatchResp.statusCode == 200) {
                            newRepoInfo = new JSONObject(unwatchResp.resp)
                                    .getJSONObject("repository");
                            mIsWatching = false;
                        }
                    } else {
                        final Response watchResp = mGapi.repo.watch(mRepositoryOwner, mRepositoryName);
                        if (watchResp.statusCode == 200) {
                            newRepoInfo = new JSONObject(watchResp.resp)
                                    .getJSONObject("repository");
                            mIsWatching = true;
                        }
                    }
                    if (newRepoInfo != null) {
                        final String newWatcherCount = newRepoInfo.getString("watchers");
                        if (newWatcherCount != mJson.getString("watchers")) {
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
                mEditor.clear().commit();
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        if (mIsWatching) {
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

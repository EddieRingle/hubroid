/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Boolean.parseBoolean;
import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Repository extends BaseActivity {
    private static final String GIT_CLONE_INTENT = "org.openintents.git.clone.PREPARE";

    private static class LoadRepositoryTask extends AsyncTask<Void, Void, Void> {
        public Repository activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                final Response resp = activity.mGApi.repo.info(activity.mRepositoryOwner,
                        activity.mRepositoryName);
                if (resp.statusCode == 200) {
                    activity.mJson = new JSONObject(resp.resp).getJSONObject("repository");

                    final JSONArray watched_list = new JSONObject(
                            activity.mGApi.user.watching(activity.mUsername).resp)
                            .getJSONArray("repositories");
                    final int length = watched_list.length() - 1;
                    for (int i = 0; i <= length; i++) {
                        if (watched_list.getJSONObject(i).getString("name")
                                .equalsIgnoreCase(activity.mRepositoryName)) {
                            activity.mIsWatching = true;
                        }
                    }
                } else if (resp.statusCode == 404) {
                    activity.mJson = null;
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            if (activity.mJson != null) {
                activity.loadRepoInfo();
                activity.findViewById(R.id.sv_repository_scrollView).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.rl_repository_progress).setVisibility(View.GONE);
            } else {
                Toast.makeText(activity, "Repository no longer exists.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }

        @Override
        protected void onPreExecute() {
            activity.findViewById(R.id.rl_repository_progress).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.sv_repository_scrollView).setVisibility(View.GONE);
        }

    }

    public Intent mIntent;

    private boolean mIsWatching;

    public JSONObject mJson;

    private LoadRepositoryTask mLoadRepositoryTask;

    public ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private Uri mRepositoryUrl;

    private boolean mRepositoryPrivate;
    
    public void loadRepoInfo() {
        try {
            // TextView title =
            // (TextView)findViewById(R.id.tv_top_bar_title);
            // title.setText(m_jsonData.getString("name"));
            final TextView repo_name = (TextView) findViewById(R.id.tv_repository_info_name);
            repo_name.setText(mJson.getString("name"));
            repo_name.requestFocus();
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
            
            mRepositoryUrl = Uri.parse(mJson.getString("url"));
            mRepositoryPrivate = parseBoolean(mJson.getString("private"));

            /* Make the repository owner text link to his/her profile */
            repo_owner.setMovementMethod(LinkMovementMethod.getInstance());
            final Spannable spans = (Spannable) repo_owner.getText();
            final ClickableSpan clickSpan = new ClickableSpan() {
                @Override
                public void onClick(final View widget) {
                    final Intent i = new Intent(Repository.this, Profile.class);
                    try {
                        i.putExtra("username", mJson.getString("owner"));
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(i);
                }
            };
            spans.setSpan(clickSpan, 0, spans.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                        final Intent intent = new Intent(Repository.this, Issues.class);
                        intent.putExtra("repo_name", mRepositoryName);
                        intent.putExtra("repo_owner", mRepositoryOwner);
                        startActivity(intent);
                    }
                });
        
        Button cloneButton = (Button) findViewById(R.id.btn_repository_info_clone);
        if (isIntentAvailable(this, GIT_CLONE_INTENT)) {
        	cloneButton.setVisibility(VISIBLE);
			cloneButton.setOnClickListener(new OnClickListener() {
	                    public void onClick(final View v) {
	                        String host = mRepositoryUrl.getHost(),path=mRepositoryUrl.getPath().substring(1);
	                        //SSH READ+WRITE : 'git@' domain ':' path '.git'
	                        //GIT READ ONLY : 'git://' domain '/' path '.git'
	
	                        String cloneUrl=mRepositoryPrivate?("git@"+host+":"+path +".git"):("git://"+host+"/"+path +".git");
					
	                        final Intent intent = new Intent(GIT_CLONE_INTENT);
	                        intent.putExtra("source-uri", cloneUrl);
	                        startActivity(intent);
	            }
	        });
        } else {
        	cloneButton.setVisibility(GONE);
        }
        
        ((Button) findViewById(R.id.btn_repository_info_network))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        final Intent intent = new Intent(Repository.this, NetworkList.class);
                        intent.putExtra("repo_name", mRepositoryName);
                        intent.putExtra("repo_owner", mRepositoryOwner);
                        startActivity(intent);
                    }
                });
    }
    
    /**
     * Indicates whether the specified action can be used as an intent.
     * 
     * Adapted from http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     */
    public static boolean isIntentAvailable(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(action);
        return !packageManager.queryIntentActivities(intent, MATCH_DEFAULT_ONLY).isEmpty();
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.repository);

        mIsWatching = false;

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");

            mLoadRepositoryTask = (LoadRepositoryTask) getLastNonConfigurationInstance();
            if (mLoadRepositoryTask == null) {
                mLoadRepositoryTask = new LoadRepositoryTask();
            }
            mLoadRepositoryTask.activity = Repository.this;
            if (mLoadRepositoryTask.getStatus() == AsyncTask.Status.PENDING) {
                mLoadRepositoryTask.execute();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                mPrefsEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 0:
                startActivity(new Intent(this, HubroidPreferences.class));
                return true;
            case 3:
                try {
                    JSONObject newRepoInfo = null;
                    if (mIsWatching) {
                        final Response unwatchResp = mGApi.repo.unwatch(mRepositoryOwner,
                                mRepositoryName);
                        if (unwatchResp.statusCode == 200) {
                            newRepoInfo = new JSONObject(unwatchResp.resp)
                                    .getJSONObject("repository");
                            mIsWatching = false;
                        }
                    } else {
                        final Response watchResp = mGApi.repo.watch(mRepositoryOwner,
                                mRepositoryName);
                        if (watchResp.statusCode == 200) {
                            newRepoInfo = new JSONObject(watchResp.resp)
                                    .getJSONObject("repository");
                            mIsWatching = true;
                        }
                    }
                    if (newRepoInfo != null) {
                        final int newWatcherCount = newRepoInfo.getInt("watchers");
                        mJson.put("watchers", newWatcherCount);
                        if (newWatcherCount == 1) {
                            ((TextView) findViewById(R.id.tv_repository_info_watchers))
                                    .setText(newWatcherCount + " watcher");
                        } else {
                            ((TextView) findViewById(R.id.tv_repository_info_watchers))
                                    .setText(newWatcherCount + " watchers");
                        }
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                break;
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
        menu.add(0, 0, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, 1, 0, "Logout").setIcon(android.R.drawable.ic_lock_power_off);
        return true;
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONObject(savedInstanceState.getString("json"));
            }
            if (mJson != null) {
                loadRepoInfo();
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        if (mJson != null) {
            outState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(outState);
    }
}

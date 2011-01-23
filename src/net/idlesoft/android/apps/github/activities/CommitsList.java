/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.CommitListAdapter;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class CommitsList extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public CommitListAdapter mCommitListAdapter;

    public JSONArray mCommitsJSON;

    public ListView mCommitListView;

    private static class GatherCommitsTask extends AsyncTask<Void, Void, Void> {
        public CommitsList activity;

        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please wait...", "Loading repository's commits...", true);
        }

        protected Void doInBackground(Void... params) {
            try {
                activity.mCommitsJSON = new JSONObject(activity.mGapi.commits.list(activity.mRepoOwner, activity.mRepoName,
                        activity.mBranchName).resp).getJSONArray("commits");
                activity.mCommitListAdapter = new CommitListAdapter(activity, activity.mCommitsJSON);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.mCommitListView.setAdapter(activity.mCommitListAdapter);
            activity.mProgressDialog.dismiss();
            super.onPostExecute(result);
        }
    };

    private SharedPreferences mPrefs;

    public ProgressDialog mProgressDialog;

    public String mRepoName;

    public String mRepoOwner;

    public String mBranchName;

    private String mPassword;

    private String mUsername;

    private GatherCommitsTask mGatherCommitsTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commits_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Recent Commits");

        mCommitListView = (ListView) findViewById(R.id.lv_commits_list_list);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
        }
    }

    @Override
    protected void onResume() {
        if (mCommitListAdapter != null) {
            mCommitListView.setAdapter(mCommitListAdapter);
        }
        mGatherCommitsTask = (GatherCommitsTask) getLastNonConfigurationInstance();
        if (mGatherCommitsTask == null) {
            mGatherCommitsTask = new GatherCommitsTask();
        }
        mGatherCommitsTask.activity = this;
        if (mGatherCommitsTask.getStatus() == AsyncTask.Status.PENDING && mCommitListAdapter == null) {
            mGatherCommitsTask.execute();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

        ((ListView) findViewById(R.id.lv_commits_list_list))
                .setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(final AdapterView<?> parent, final View v,
                            final int position, final long id) {
                        try {
                            final Intent i = new Intent(CommitsList.this, Commit.class);
                            i.putExtra("committer", mCommitsJSON.getJSONObject(position)
                                    .getJSONObject("committer").getString("login"));
                            i.putExtra("author", mCommitsJSON.getJSONObject(position)
                                    .getJSONObject("author").getString("login"));
                            i.putExtra("commit_sha", mCommitsJSON.getJSONObject(position)
                                    .getString("id"));
                            i.putExtra("repo_name", mRepoName);
                            i.putExtra("repo_owner", mRepoOwner);
                            CommitsList.this.startActivity(i);
                        } catch (final JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    public Object onRetainNonConfigurationInstance() {
        return mGatherCommitsTask;
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("commitsJson")) {
                mCommitsJSON = new JSONArray(savedInstanceState.getString("commitsJson"));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mCommitsJSON != null) {
            mCommitListAdapter = new CommitListAdapter(this, mCommitsJSON);
        } else {
            mCommitListAdapter = null;
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mCommitsJSON != null) {
            savedInstanceState.putString("commitsJson", mCommitsJSON.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}

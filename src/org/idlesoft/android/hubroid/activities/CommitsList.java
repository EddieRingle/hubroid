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
import org.idlesoft.android.hubroid.adapters.CommitListAdapter;
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
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class CommitsList extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();;

    public ArrayList<String> mBranches;

    public ArrayAdapter<String> mBranchesAdapter;

    public CommitListAdapter mCommitListAdapter;

    public JSONArray mCommitsJSON;

    private SharedPreferences.Editor mEditor;

    public Intent mIntent;

    public ListView mCommitListView;

    private static class GatherCommitsTask extends AsyncTask<Void, Void, Void> {
        public CommitsList mActivity;

        protected void onPreExecute() {
            mActivity.mProgressDialog = ProgressDialog.show(mActivity, "Please wait...", "Loading repository's commits...", true);
        }

        protected Void doInBackground(Void... params) {
            try {
                mActivity.mCommitsJSON = new JSONObject(mActivity.mGapi.commits.list(mActivity.mRepoOwner, mActivity.mRepoName,
                        mActivity.mBranches.get(mActivity.mBranchPosition)).resp).getJSONArray("commits");
                mActivity.mCommitListAdapter = new CommitListAdapter(mActivity, mActivity.mCommitsJSON);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            mActivity.mCommitListView.setAdapter(mActivity.mCommitListAdapter);
            mActivity.mProgressDialog.dismiss();

            super.onPostExecute(result);
        }
    };

    private static class DiscoverBranchesTask extends AsyncTask<Void, Void, Void> {
        public CommitsList mActivity;

        protected void onPreExecute() {
            mActivity.mProgressDialog = ProgressDialog.show(mActivity, "Please wait...", "Loading repository's branches...", true);
        }

        protected Void doInBackground(Void... params) {
            try {
                final JSONObject branchesJson = new JSONObject(mActivity.mGapi.repo.branches(mActivity.mRepoOwner, mActivity.mRepoName).resp).getJSONObject("branches");
                mActivity.mBranches = new ArrayList<String>(branchesJson.length());
                final Iterator<?> keys = branchesJson.keys();
                while (keys.hasNext()) {
                    final String next_branch = (String) keys.next();
                    mActivity.mBranches.add(next_branch);
                }

                mActivity.mBranchesAdapter = new ArrayAdapter<String>(mActivity,
                        android.R.layout.simple_spinner_item, mActivity.mBranches);
                mActivity.mBranchesAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            mActivity.mBranchesSpinner.setAdapter(mActivity.mBranchesAdapter);
            /* Find the position of the master branch */
            final int masterPos = mActivity.mBranches.indexOf("master");
            /* Set the spinner to the master branch, if it exists, by
             * default */
            mActivity.mBranchesSpinner.setSelection(masterPos);
            mActivity.mProgressDialog.dismiss();

            super.onPostExecute(result);
        }
    };

    private final OnItemSelectedListener mOnBranchSelect = new OnItemSelectedListener() {
        public void onItemSelected(final AdapterView<?> parent, final View view,
                final int position, final long id) {
            if (position != mBranchPosition) {
                mBranchPosition = position;
                if (mGatherCommitsTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mGatherCommitsTask = new GatherCommitsTask();
                    mGatherCommitsTask.mActivity = CommitsList.this;
                }
                if (mGatherCommitsTask.getStatus() == AsyncTask.Status.PENDING) {
                    mGatherCommitsTask.execute();
                }
            }
        }

        public void onNothingSelected(final AdapterView<?> arg0) {
        }
    };

    public int mBranchPosition;

    public int mPosition;

    private SharedPreferences mPrefs;

    public ProgressDialog mProgressDialog;

    public String mRepoName;

    public String mRepoOwner;

    private String mPassword;

    private String mUsername;

    private GatherCommitsTask mGatherCommitsTask;

    private DiscoverBranchesTask mDiscoverBranchesTask;

    public Spinner mBranchesSpinner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commits_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("login", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final TextView title = (TextView) findViewById(R.id.tv_top_bar_title);
        title.setText("Recent Commits");

        mBranchesSpinner = (Spinner) findViewById(R.id.spn_commits_list_branch_select);
        mBranchesSpinner.setOnItemSelectedListener(mOnBranchSelect);

        mCommitListView = (ListView) findViewById(R.id.lv_commits_list_list);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
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
    protected void onResume() {
        if (mBranchesAdapter != null) {
            mBranchesSpinner.setAdapter(mBranchesAdapter);
            mBranchesSpinner.setSelection(mBranchPosition);
        }
        if (mCommitListAdapter != null) {
            mCommitListView.setAdapter(mCommitListAdapter);
        }
        final Object tasks = getLastNonConfigurationInstance();
        if (tasks == null) {
            mDiscoverBranchesTask = new DiscoverBranchesTask();
            mGatherCommitsTask = new GatherCommitsTask();
        } else {
            mDiscoverBranchesTask = (DiscoverBranchesTask) ((AsyncTask[])tasks)[0];
            mGatherCommitsTask = (GatherCommitsTask) ((AsyncTask[])tasks)[1];
        }
        mDiscoverBranchesTask.mActivity = this;
        mGatherCommitsTask.mActivity = this;
        if (mDiscoverBranchesTask.getStatus() == AsyncTask.Status.PENDING && mBranchesAdapter == null) {
            mDiscoverBranchesTask.execute();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mBranchPosition = mBranchesSpinner.getSelectedItemPosition();
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!menu.hasVisibleItems()) {
            menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
            menu.add(0, 1, 0, "Clear Preferences");
            menu.add(0, 2, 0, "Clear Cache");
        }
        return true;
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
                            mPosition = position;
                            final Intent i = new Intent(CommitsList.this, CommitChangeViewer.class);
                            i.putExtra("commit_sha", mCommitsJSON.getJSONObject(mPosition)
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
        final AsyncTask[] tasks = new AsyncTask[]{ mDiscoverBranchesTask, mGatherCommitsTask };
        return tasks;
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("branches")) {
                mBranches = savedInstanceState.getStringArrayList("branches");
            }
            if (savedInstanceState.containsKey("commitsJson")) {
                mCommitsJSON = new JSONArray(savedInstanceState.getString("commitsJson"));
            }
            mBranchPosition = savedInstanceState.getInt("position");
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mBranches != null) {
            mBranchesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mBranches);
            mBranchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        if (mCommitsJSON != null) {
            mCommitListAdapter = new CommitListAdapter(this, mCommitsJSON);
        } else {
            mCommitListAdapter = null;
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mBranches != null) {
            savedInstanceState.putStringArrayList("branches", mBranches);
        }
        if (mCommitsJSON != null) {
            savedInstanceState.putString("commitsJson", mCommitsJSON.toString());
        }
        savedInstanceState.putInt("position", mBranchesSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(savedInstanceState);
    }
}

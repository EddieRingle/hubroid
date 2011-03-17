/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.HubroidApplication;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.BranchListAdapter;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class BranchesList extends Activity {
    private static class GetBranchesTask extends AsyncTask<Void, Void, Void> {
        BranchesList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGapi.repo.branches(activity.mRepoOwner,
                        activity.mRepoName).resp).getJSONObject("branches");
                activity.mBranchListAdapter = new BranchListAdapter(activity, activity.mJson);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mBranchList.removeHeaderView(activity.mLoadView);
            activity.mBranchList.setAdapter(activity.mBranchListAdapter);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            activity.mBranchList.addHeaderView(activity.mLoadView);
            activity.mBranchList.setAdapter(null);
            super.onPreExecute();
        }
    }

    public ListView mBranchList;

    public BranchListAdapter mBranchListAdapter;

    private SharedPreferences.Editor mEditor;

    private final GitHubAPI mGapi = new GitHubAPI();

    public GetBranchesTask mGetBranchesTask;

    public Intent mIntent;

    public JSONObject mJson;

    public View mLoadView;

    private final OnItemClickListener mOnBranchListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            try {
                mIntent = new Intent(BranchesList.this, Branch.class);
                mIntent.putExtra("repo_name", mRepoName);
                mIntent.putExtra("repo_owner", mRepoOwner);
                mIntent.putExtra("branch_name", mJson.names().getString(position));
                mIntent.putExtra("branch_sha", mJson.getString(mJson.names().getString(position)));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            BranchesList.this.startActivity(mIntent);
        }
    };

    private String mPassword;

    private SharedPreferences mPrefs;

    public String mRepoName;

    public String mRepoOwner;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.branch_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        mLoadView = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        mBranchList = (ListView) findViewById(R.id.lv_branchList_list);
        mBranchList.setOnItemClickListener(mOnBranchListItemClick);

        HubroidApplication.setupActionBar(BranchesList.this);

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Branches");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");

            mGetBranchesTask = (GetBranchesTask) getLastNonConfigurationInstance();

            if (mGetBranchesTask == null) {
                mGetBranchesTask = new GetBranchesTask();
            }

            mGetBranchesTask.activity = this;

            if ((mGetBranchesTask.getStatus() == AsyncTask.Status.PENDING)
                    && (mBranchListAdapter == null)) {
                mGetBranchesTask.execute();
            }
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
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetBranchesTask;
    }
}

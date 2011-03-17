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
import net.idlesoft.android.apps.github.adapters.BranchTreeListAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class BranchTree extends Activity {
    private static class LoadTreeTask extends AsyncTask<Void, Void, Void> {
        public BranchTree activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final Response r = activity.mGapi.object.tree(activity.mRepositoryOwner,
                    activity.mRepositoryName, activity.mBranchSha);
            if (r.statusCode == 200) {
                try {
                    activity.mJson = (new JSONObject(r.resp)).getJSONArray("tree");
                    activity.mAdapter.loadData(activity.mJson);
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mAdapter.pushData();
            activity.mAdapter.setIsLoadingData(false);
        }

        @Override
        protected void onPreExecute() {
            activity.mAdapter.setIsLoadingData(true);
        }
    }

    private BranchTreeListAdapter mAdapter;

    private String mBranchName;

    private String mBranchSha;

    private final GitHubAPI mGapi = new GitHubAPI();

    public Intent mIntent;

    public JSONArray mJson;

    private ListView mListView;

    private LoadTreeTask mLoadTreeTask;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            try {
                final JSONObject treeItem = mJson.getJSONObject(position);
                if (treeItem.getString("type").equals("tree")) {
                    final Intent i = new Intent(BranchTree.this, BranchTree.class);
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    i.putExtra("branch_name", mBranchName);
                    i.putExtra("branch_sha", treeItem.getString("sha"));
                    startActivity(i);
                } else if (treeItem.getString("type").equals("blob")) {
                    final Intent i = new Intent(BranchTree.this, FileViewer.class);
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    i.putExtra("tree_sha", mBranchSha);
                    i.putExtra("blob_path", treeItem.getString("name"));
                    i.putExtra("blob_sha", treeItem.getString("sha"));
                    startActivity(i);
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    private Editor mEditor;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.branch_tree_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        HubroidApplication.setupActionBar(BranchTree.this);

        final TextView pageTitle = (TextView) findViewById(R.id.tv_page_title);
        pageTitle.setText("Tree Browser");

        mListView = (ListView) findViewById(R.id.lv_branchTreeList_list);
        mListView.setOnItemClickListener(mOnListItemClick);

        mAdapter = new BranchTreeListAdapter(BranchTree.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
            mBranchSha = extras.getString("branch_sha");
        }
    }

    @Override
    protected void onResume() {
        mLoadTreeTask = (LoadTreeTask) getLastNonConfigurationInstance();
        if (mLoadTreeTask == null) {
            mLoadTreeTask = new LoadTreeTask();
        }
        mLoadTreeTask.activity = BranchTree.this;
        if ((mLoadTreeTask.getStatus() == AsyncTask.Status.PENDING) && !mAdapter.hasItems()) {
            mLoadTreeTask.execute();
        }
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadTreeTask;
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
}

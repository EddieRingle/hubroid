/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class BranchTree extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public Intent mIntent;

    public JSONArray mJson;

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    private String mBranchName;

    private String mBranchSha;

    private BranchTreeListAdapter mAdapter;

    private ListView mListView;

    private LoadTreeTask mLoadTreeTask;

    private static class LoadTreeTask extends AsyncTask<Void, Void, Void> {
        public BranchTree activity;

        protected void onPreExecute() {
            activity.mAdapter.setIsLoadingData(true);
        }

        protected Void doInBackground(Void... params) {
            Response r = activity.mGapi.object.tree(activity.mRepositoryOwner, activity.mRepositoryName, activity.mBranchSha);
            if (r.statusCode == 200) {
                try {
                    activity.mJson = (new JSONObject(r.resp)).getJSONArray("tree");
                    activity.mAdapter.loadData(activity.mJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.mAdapter.pushData();
            activity.mAdapter.setIsLoadingData(false);
        }
    }

    private OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            try {
                JSONObject treeItem = mJson.getJSONObject(position);
                if (treeItem.getString("type").equals("tree")) {
                    Intent i = new Intent(BranchTree.this, BranchTree.class);
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    i.putExtra("branch_name", mBranchName);
                    i.putExtra("branch_sha", treeItem.getString("sha"));
                    startActivity(i);
                } else if (treeItem.getString("type").equals("blob")) {
                    Intent i = new Intent(BranchTree.this, FileViewer.class);
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    i.putExtra("tree_sha", mBranchSha);
                    i.putExtra("blob_path", treeItem.getString("name"));
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.branch_tree_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(BranchTree.this, Search.class));
            }
        });

        final TextView pageTitle = (TextView) findViewById(R.id.tv_page_title);
        pageTitle.setText("Tree Browser");

        mListView = (ListView) findViewById(R.id.lv_branchTreeList_list);
        mListView.setOnItemClickListener(mOnListItemClick);

        mAdapter = new BranchTreeListAdapter(BranchTree.this, mListView);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
            mBranchSha = extras.getString("branch_sha");
        }
    }

    protected void onResume() {
        mListView.setAdapter(mAdapter);

        mLoadTreeTask = (LoadTreeTask) getLastNonConfigurationInstance();
        if (mLoadTreeTask == null) {
            mLoadTreeTask = new LoadTreeTask();
        }
        mLoadTreeTask.activity = BranchTree.this;
        if (mLoadTreeTask.getStatus() == AsyncTask.Status.PENDING && !mAdapter.hasItems()) {
            mLoadTreeTask.execute();
        }
        super.onResume();
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

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadTreeTask;
    }
}

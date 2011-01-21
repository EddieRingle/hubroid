package org.idlesoft.android.hubroid.activities;

import com.flurry.android.FlurryAgent;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.adapters.BranchTreeListAdapter;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class BranchTree extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public Intent mIntent;

    public JSONObject mJson;

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    private String mBranchName;

    private String mBranchSha;

    private BranchTreeListAdapter mBranchTreeListAdapter;

    private View mLoadingView;

    private ListView mListView;

    private LoadTreeTask mLoadTreeTask;

    private static class LoadTreeTask extends AsyncTask<Void, Void, Void> {
        public BranchTree activity;

        protected void onPreExecute() {
            activity.mListView.addHeaderView(activity.mLoadingView);
            activity.mListView.setAdapter(null);
        }

        protected Void doInBackground(Void... params) {
            Response r = activity.mGapi.object.tree(activity.mRepositoryOwner, activity.mRepositoryName, activity.mBranchSha);
            if (r.statusCode == 200) {
                try {
                    activity.mJson = new JSONObject(r.resp);
                    activity.mBranchTreeListAdapter = new BranchTreeListAdapter(activity, activity.mJson.getJSONArray("tree"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.mListView.removeHeaderView(activity.mLoadingView);
            activity.mListView.setAdapter(activity.mBranchTreeListAdapter);
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.branch_tree_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final TextView pageTitle = (TextView) findViewById(R.id.tv_page_title);
        pageTitle.setText("Tree Browser");

        mLoadingView = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        mListView = (ListView) findViewById(R.id.lv_branchTreeList_list);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
            mBranchSha = extras.getString("branch_sha");
        }
    }

    protected void onResume() {
        if (mBranchTreeListAdapter != null) {
            mListView.setAdapter(mBranchTreeListAdapter);
        }
        mLoadTreeTask = (LoadTreeTask) getLastNonConfigurationInstance();
        if (mLoadTreeTask == null) {
            mLoadTreeTask = new LoadTreeTask();
        }
        mLoadTreeTask.activity = BranchTree.this;
        if (mLoadTreeTask.getStatus() == AsyncTask.Status.PENDING && mBranchTreeListAdapter == null) {
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

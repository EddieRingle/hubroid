package org.idlesoft.android.hubroid.activities.tabs;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.Hubroid;
import org.idlesoft.android.hubroid.adapters.RepositoriesListAdapter;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MyRepos extends Activity {
    private ListView mListView;
    private RepositoriesListAdapter mAdapter;
    private JSONArray mJson;
    private GitHubAPI mGapi = new GitHubAPI();
    private String mTarget;
    private MyReposTask mTask;
    public View mLoadingItem;

    private static class MyReposTask extends AsyncTask<Void, Void, Void> {
        public MyRepos mActivity;

        public MyReposTask(MyRepos activity) {
            mActivity = activity;
        }

        protected void onPreExecute() {
            mActivity.mListView.addHeaderView(mActivity.mLoadingItem);
            mActivity.mListView.setAdapter(null);
        }

        protected void onPostExecute(Void result) {
            mActivity.mListView.setAdapter(mActivity.mAdapter);
            mActivity.mListView.setOnItemClickListener(mActivity.onListItemClick);
            mActivity.mListView.removeHeaderView(mActivity.mLoadingItem);
        }

        protected Void doInBackground(Void... params) {
            if (mActivity.mJson == null) {
                try {
                    Response resp = mActivity.mGapi.repo.list(mActivity.mTarget);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    mActivity.mJson = (new JSONObject(resp.resp)).getJSONArray("repositories");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mActivity.mAdapter = new RepositoriesListAdapter(mActivity.getApplicationContext(), mActivity.mJson);
            return null;
        }
    }

    private OnItemClickListener onListItemClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            return;
        }
    };

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mGapi.authenticate(prefs.getString("username", ""), prefs.getString("password", ""));

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        setContentView(mListView);

        mLoadingItem = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        ((TextView) mLoadingItem.findViewById(R.id.tv_loadingListItem_loadingText))
                .setText("Loading...");

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mTarget = extras.getString("target");
        if (mTarget == null || mTarget.equals(""))
            mTarget = prefs.getString("username", "");

        mTask = (MyReposTask) getLastNonConfigurationInstance();
        if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED)
            mTask = new MyReposTask(MyRepos.this);
        mTask.mActivity = this;
        if (mTask.getStatus() == AsyncTask.Status.PENDING)
            mTask.execute();
    }

    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (mJson != null) {
            mAdapter = new RepositoriesListAdapter(getApplicationContext(), mJson);
        } else {
            mAdapter = null;
        }
    }

    public void onResume() {
        super.onResume();
        mListView.setAdapter(mAdapter);
    }
}
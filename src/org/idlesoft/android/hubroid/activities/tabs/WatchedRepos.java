
package org.idlesoft.android.hubroid.activities.tabs;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.Hubroid;
import org.idlesoft.android.hubroid.activities.Repository;
import org.idlesoft.android.hubroid.adapters.RepositoriesListAdapter;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WatchedRepos extends Activity {
    private static class WatchedReposTask extends AsyncTask<Void, Void, Void> {
        public WatchedRepos mActivity;

        public WatchedReposTask(final WatchedRepos activity) {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (mActivity.mJson == null) {
                try {
                    final Response resp = mActivity.mGapi.user.watching(mActivity.mTarget);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    mActivity.mJson = (new JSONObject(resp.resp)).getJSONArray("repositories");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            mActivity.mAdapter = new RepositoriesListAdapter(mActivity.getApplicationContext(),
                    mActivity.mJson);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            mActivity.mListView.setAdapter(mActivity.mAdapter);
            mActivity.mListView.setOnItemClickListener(mActivity.onListItemClick);
            mActivity.mListView.removeHeaderView(mActivity.mLoadingItem);
        }

        @Override
        protected void onPreExecute() {
            mActivity.mListView.addHeaderView(mActivity.mLoadingItem);
            mActivity.mListView.setAdapter(null);
        }
    }

    private RepositoriesListAdapter mAdapter;

    private final GitHubAPI mGapi = new GitHubAPI();

    private JSONArray mJson;

    private ListView mListView;

    public View mLoadingItem;

    private String mTarget;

    private WatchedReposTask mTask;

    private String mUsername;

    private String mPassword;

    private final OnItemClickListener onListItemClick = new OnItemClickListener() {

        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            Intent i = new Intent(getApplicationContext(), Repository.class);
            try {
                i.putExtra("repo_owner", mJson.getJSONObject(position).getString("owner"));
                i.putExtra("repo_name", mJson.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        final SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mUsername = prefs.getString("username", "");
        mPassword = prefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        setContentView(mListView);

        mLoadingItem = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        ((TextView) mLoadingItem.findViewById(R.id.tv_loadingListItem_loadingText))
                .setText("Loading...");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mTask = (WatchedReposTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new WatchedReposTask(WatchedRepos.this);
        }
        mTask.mActivity = this;
        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                return;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mJson != null) {
            mAdapter = new RepositoriesListAdapter(getApplicationContext(), mJson);
        } else {
            mAdapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListView.setAdapter(mAdapter);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}

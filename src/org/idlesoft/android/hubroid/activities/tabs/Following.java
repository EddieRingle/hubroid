
package org.idlesoft.android.hubroid.activities.tabs;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.Hubroid;
import org.idlesoft.android.hubroid.activities.Profile;
import org.idlesoft.android.hubroid.adapters.FollowersFollowingListAdapter;
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

public class Following extends Activity {
    private static class FollowingTask extends AsyncTask<Void, Void, Void> {
        public Following mActivity;

        public FollowingTask(final Following activity) {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (mActivity.mJson == null) {
                try {
                    final Response resp = mActivity.mGapi.user.following(mActivity.mTarget);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    mActivity.mJson = (new JSONObject(resp.resp)).getJSONArray("users");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            mActivity.mAdapter = new FollowersFollowingListAdapter(mActivity
                    .getApplicationContext(), mActivity.mJson);
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

    private FollowersFollowingListAdapter mAdapter;

    private final GitHubAPI mGapi = new GitHubAPI();

    private JSONArray mJson;

    private ListView mListView;

    public View mLoadingItem;

    private String mTarget;

    private FollowingTask mTask;

    private final OnItemClickListener onListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), Profile.class);
            try {
                i.putExtra("username", mJson.getString(position));
            } catch (final JSONException e) {
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
        mGapi.authenticate(prefs.getString("username", ""), prefs.getString("password", ""));

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
            mTarget = prefs.getString("username", "");
        }

        mTask = (FollowingTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new FollowingTask(Following.this);
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
            mAdapter = new FollowersFollowingListAdapter(getApplicationContext(), mJson);
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

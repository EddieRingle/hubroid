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
import org.idlesoft.android.hubroid.adapters.ActivityFeedAdapter;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;

public class NewsFeed extends Activity {
    private static class LoadPrivateFeedTask extends AsyncTask<Void, Void, Void> {
        public NewsFeed activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mPrivateJSON == null) {
                try {
                    final Response resp = activity.mGapi.user.private_activity();
                    if (resp.statusCode != 200) {
                        /* Let the user know something went wrong */
                        return null;
                    }
                    activity.mPrivateJSON = new JSONArray(resp.resp);
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            if (activity.mDisplayedPrivateJSON == null) {
                activity.mDisplayedPrivateJSON = new JSONArray();
            }
            final int length = activity.mDisplayedPrivateJSON.length();
            for (int i = length; i < length + 10; i++) {
                if (activity.mPrivateJSON.isNull(i)) {
                    break;
                }
                try {
                    activity.mDisplayedPrivateJSON.put(activity.mPrivateJSON.get(i));
                } catch (final JSONException e) {
                    e.printStackTrace();
                    break;
                }
            }
            activity.mPrivateActivityAdapter = new ActivityFeedAdapter(activity
                    .getApplicationContext(), activity.mDisplayedPrivateJSON, false);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            setAdapter();
            removeLoadingView();
        }

        @Override
        protected void onPreExecute() {
            setLoadingView();
        }

        protected void removeLoadingView() {
            ((ListView) activity.findViewById(R.id.lv_news_feed))
                    .removeHeaderView(activity.mLoadingItem);
        }

        protected void setAdapter() {
            ((ListView) activity.findViewById(R.id.lv_news_feed))
                    .setAdapter(activity.mPrivateActivityAdapter);
            ((ListView) activity.findViewById(R.id.lv_news_feed))
                    .setOnItemClickListener(activity.onPrivateActivityItemClick);
        }

        protected void setLoadingView() {
            ((ListView) activity.findViewById(R.id.lv_news_feed))
                    .addHeaderView(activity.mLoadingItem);
            ((ListView) activity.findViewById(R.id.lv_news_feed)).setAdapter(null);
        }
    }

    public JSONArray mDisplayedPrivateJSON;

    private SharedPreferences.Editor mEditor;

    public GitHubAPI mGapi = new GitHubAPI();

    public View mLoadingItem;

    private LoadPrivateFeedTask mLoadPrivateTask;

    private String mPassword;

    private SharedPreferences mPrefs;

    private ActivityFeedAdapter mPrivateActivityAdapter;

    public JSONArray mPrivateJSON;

    private String mUsername;

    private final OnItemClickListener onPrivateActivityItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                final long arg3) {
            try {
                final Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
                intent.putExtra("item_json", mDisplayedPrivateJSON.getJSONObject(arg2).toString());
                startActivity(intent);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.news_feed);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();
        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((TextView) findViewById(R.id.tv_page_title)).setText("News Feed");

        mLoadingItem = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        ((TextView) mLoadingItem.findViewById(R.id.tv_loadingListItem_loadingText))
                .setText("Loading Feed, Please Wait...");

        mLoadPrivateTask = (LoadPrivateFeedTask) getLastNonConfigurationInstance();
        if (mLoadPrivateTask == null) {
            mLoadPrivateTask = new LoadPrivateFeedTask();
        }
        mLoadPrivateTask.activity = this;
        if (mLoadPrivateTask.getStatus() == AsyncTask.Status.PENDING) {
            mLoadPrivateTask.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                mEditor.clear().commit();
                final Intent intent = new Intent(NewsFeed.this, Hubroid.class);
                startActivity(intent);
                finish();
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!menu.hasVisibleItems()) {
            if (!mUsername.equals(null) && !mPassword.equals(null)) {
                menu.add(0, 1, 0, "Sign out");
            }
            menu.add(0, 2, 0, "Clear Cache");
        }
        return true;
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean keepGoing = true;
        try {
            if (savedInstanceState.containsKey("json")) {
                mPrivateJSON = new JSONArray(savedInstanceState.getString("json"));
            } else {
                keepGoing = false;
            }
            if (savedInstanceState.containsKey("displayed_json")) {
                mDisplayedPrivateJSON = new JSONArray(savedInstanceState
                        .getString("displayed_json"));
            } else {
                mDisplayedPrivateJSON = new JSONArray();
                final int length = mDisplayedPrivateJSON.length();
                for (int i = length; i < length + 10; i++) {
                    if (mPrivateJSON.isNull(i)) {
                        break;
                    }
                    try {
                        mDisplayedPrivateJSON.put(mPrivateJSON.get(i));
                    } catch (final JSONException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        } catch (final JSONException e) {
            keepGoing = false;
        }
        if (keepGoing == true) {
            mPrivateActivityAdapter = new ActivityFeedAdapter(NewsFeed.this, mDisplayedPrivateJSON,
                    false);
        } else {
            mPrivateActivityAdapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ListView) findViewById(R.id.lv_news_feed)).setAdapter(mPrivateActivityAdapter);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadPrivateTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mDisplayedPrivateJSON != null) {
            savedInstanceState.putString("displayed_json", mDisplayedPrivateJSON.toString());
        }
        if (mPrivateJSON != null) {
            savedInstanceState.putString("json", mPrivateJSON.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
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
}

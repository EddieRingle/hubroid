/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.File;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class NewsFeed extends Activity {
    private static class LoadActivityFeedTask extends AsyncTask<Void, Void, Void> {
        public NewsFeed activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mJson == null) {
                try {
                    final Response resp;
                    if (mPrivate) {
                        resp = activity.mGapi.user.private_activity();
                    } else {
                        resp = activity.mGapi.user.activity(mTargetUser);
                    }
                    if (resp.statusCode != 200) {
                        /* Let the user know something went wrong */
                        return null;
                    }
                    activity.mJson = new JSONArray(resp.resp);
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            if (activity.mJson != null) {
                activity.mActivityAdapter = new ActivityFeedAdapter(activity, activity.mJson, mPrivate == false);
            }
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
            activity.mListView.removeHeaderView(activity.mLoadingItem);
        }

        protected void setAdapter() {
            activity.mListView.setAdapter(activity.mActivityAdapter);
        }

        protected void setLoadingView() {
            if (activity.mJson == null) {
                activity.mListView.addHeaderView(activity.mLoadingItem);
                activity.mListView.setAdapter(null);
            }
        }
    }

    private SharedPreferences.Editor mEditor;

    public GitHubAPI mGapi = new GitHubAPI();

    public View mLoadingItem;

    private LoadActivityFeedTask mLoadActivityTask;

    private String mPassword;

    private SharedPreferences mPrefs;

    private ActivityFeedAdapter mActivityAdapter;

    public JSONArray mJson;

    private String mUsername;

    private final OnItemClickListener onActivityItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                final long arg3) {
            try {
                final Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
                intent.putExtra("item_json", mJson.getJSONObject(arg2).toString());
                startActivity(intent);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private ListView mListView;

    private static String mTargetUser;

    private static boolean mPrivate;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.news_feed);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();
        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTargetUser = bundle.getString("username");
            mPrivate = false;
        } else {
            mTargetUser = mUsername;
            mPrivate = true;
        }

        mListView = (ListView) findViewById(R.id.lv_news_feed);
        mListView.setOnItemClickListener(onActivityItemClick);

        mLoadingItem = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        ((TextView) mLoadingItem.findViewById(R.id.tv_loadingListItem_loadingText))
                .setText("Loading Feed, Please Wait...");

        if (mPrivate) {
            ((TextView) findViewById(R.id.tv_page_title)).setText("News Feed");
        } else {
            ((TextView) findViewById(R.id.tv_page_title)).setText(mTargetUser + "'s Activity");
        }

        mLoadActivityTask = (LoadActivityFeedTask) getLastNonConfigurationInstance();
        if (mLoadActivityTask == null) {
            mLoadActivityTask = new LoadActivityFeedTask();
        }
        mLoadActivityTask.activity = this;
        if (mLoadActivityTask.getStatus() == AsyncTask.Status.PENDING) {
            mLoadActivityTask.execute();
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
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                keepGoing = false;
            }
        } catch (final JSONException e) {
            keepGoing = false;
        }
        if (keepGoing == true) {
            mActivityAdapter = new ActivityFeedAdapter(NewsFeed.this, mJson,
                    false);
        } else {
            mActivityAdapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ListView) findViewById(R.id.lv_news_feed)).setAdapter(mActivityAdapter);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadActivityTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
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

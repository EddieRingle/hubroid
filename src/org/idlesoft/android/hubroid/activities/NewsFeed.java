/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import java.io.File;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.adapters.ActivityFeedAdapter;
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
    private ActivityFeedAdapter mPrivateActivityAdapter;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private String mUsername;
    private String mPassword;
    public JSONArray mPrivateJSON;
    public JSONArray mDisplayedPrivateJSON;
    private LoadPrivateFeedTask mLoadPrivateTask;
    public View mLoadingItem;
    public GitHubAPI mGapi = new GitHubAPI();

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!menu.hasVisibleItems()) {
            if (!mUsername.equals(null) && !mPassword.equals(null))
                menu.add(0, 1, 0, "Sign out");
            menu.add(0, 2, 0, "Clear Cache");
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 1:
            mEditor.clear().commit();
            Intent intent = new Intent(NewsFeed.this, Hubroid.class);
            startActivity(intent);
            finish();
            return true;
        case 2:
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()) {
                File hubroid = new File(root, "hubroid");
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

    private OnItemClickListener onPrivateActivityItemClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            try {
                Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
                intent.putExtra("item_json", mDisplayedPrivateJSON.getJSONObject(arg2).toString());
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
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
        if (mLoadPrivateTask == null)
            mLoadPrivateTask = new LoadPrivateFeedTask();
        mLoadPrivateTask.activity = this;
        if (mLoadPrivateTask.getStatus() == AsyncTask.Status.PENDING)
            mLoadPrivateTask.execute();
    }

    private static class LoadPrivateFeedTask extends AsyncTask<Void, Void, Void> {
        public NewsFeed activity;

        protected void setLoadingView() {
            ((ListView) activity.findViewById(R.id.lv_news_feed))
                    .addHeaderView(activity.mLoadingItem);
            ((ListView) activity.findViewById(R.id.lv_news_feed)).setAdapter(null);
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

        @Override
        protected void onPreExecute() {
            setLoadingView();
        }

        @Override
        protected void onPostExecute(Void result) {
            setAdapter();
            removeLoadingView();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (activity.mPrivateJSON == null) {
                try {
                    Response resp = activity.mGapi.user.private_activity();
                    if (resp.statusCode != 200) {
                        /* Let the user know something went wrong */
                        return null;
                    }
                    activity.mPrivateJSON = new JSONArray(resp.resp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (activity.mDisplayedPrivateJSON == null)
                activity.mDisplayedPrivateJSON = new JSONArray();
            int length = activity.mDisplayedPrivateJSON.length();
            for (int i = length; i < length + 10; i++) {
                if (activity.mPrivateJSON.isNull(i))
                    break;
                try {
                    activity.mDisplayedPrivateJSON.put(activity.mPrivateJSON.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }
            }
            activity.mPrivateActivityAdapter = new ActivityFeedAdapter(
                    activity.getApplicationContext(), activity.mDisplayedPrivateJSON, false);
            return null;
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadPrivateTask;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mDisplayedPrivateJSON != null) {
            savedInstanceState.putString("displayed_json", mDisplayedPrivateJSON.toString());
        }
        if (mPrivateJSON != null) {
            savedInstanceState.putString("json", mPrivateJSON.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean keepGoing = true;
        try {
            if (savedInstanceState.containsKey("json")) {
                mPrivateJSON = new JSONArray(savedInstanceState.getString("json"));
            } else {
                keepGoing = false;
            }
            if (savedInstanceState.containsKey("displayed_json")) {
                mDisplayedPrivateJSON = new JSONArray(
                        savedInstanceState.getString("displayed_json"));
            } else {
                mDisplayedPrivateJSON = new JSONArray();
                int length = mDisplayedPrivateJSON.length();
                for (int i = length; i < length + 10; i++) {
                    if (mPrivateJSON.isNull(i))
                        break;
                    try {
                        mDisplayedPrivateJSON.put(mPrivateJSON.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        } catch (JSONException e) {
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
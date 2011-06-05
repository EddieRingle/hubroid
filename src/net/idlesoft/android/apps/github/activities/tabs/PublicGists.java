/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.BaseActivity;
import net.idlesoft.android.apps.github.activities.Repository;
import net.idlesoft.android.apps.github.adapters.GistListAdapter;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.GistService;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class PublicGists extends BaseActivity {
    private ArrayList<Gist> mGists = null;

    private static class StarredGistsTask extends AsyncTask<Void, Void, Void> {
        public PublicGists activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mGists == null) {
                try {
                    Log.d("HUBROID", "Requesting public gists");
                    final GistService gs = new GistService(activity.getGitHubClient());
                    activity.mGists = new ArrayList<Gist>(gs.getPublicGists());
                    Log.d("HUBROID", "Done getting public gists, loading adapter now");
                } catch (final RequestException e) {
                    e.getStatus();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            activity.mAdapter.loadData(activity.mGists);
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

    private GistListAdapter mAdapter;

    private ListView mListView;

    /*
    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), Repository.class);
            i.putExtra("repo_owner", mTarget);
            try {
                i.putExtra("repo_name", mJson.getJSONObject(position).getString("name"));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };
    */

    private String mTarget;

    private StarredGistsTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        //mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new GistListAdapter(PublicGists.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mTask = (StarredGistsTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new StarredGistsTask();
        }
        mTask.activity = this;
        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }
}

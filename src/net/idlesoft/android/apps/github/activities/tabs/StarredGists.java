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
import net.idlesoft.android.apps.github.activities.SingleGist;
import net.idlesoft.android.apps.github.adapters.GistListAdapter;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.GistService;
import org.json.JSONArray;

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

public class StarredGists extends BaseActivity {
    private ArrayList<Gist> mGists;

    private static class StarredGistsTask extends AsyncTask<Void, Void, Void> {
        public StarredGists activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mJson == null) {
                try {
                    final GistService gs = new GistService(activity.getGitHubClient());
                    activity.mGists = new ArrayList<Gist>(gs.getStarredGists());
                } catch (final RequestException e) {
                    Log.d("HUBROID", "Status: " + e.getStatus() + "; Msg: " + e.getMessage());
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

    private JSONArray mJson;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleGist.class);
            i.putExtra("gistId", mGists.get(position).getId());
            startActivity(i);
            return;
        }
    };

    private String mTarget;

    private StarredGistsTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new GistListAdapter(StarredGists.this, mListView);
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

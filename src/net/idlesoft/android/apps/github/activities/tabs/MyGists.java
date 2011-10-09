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
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

public class MyGists extends BaseActivity {
    private PageIterator<Gist> mGistPages;

    private ArrayList<Gist> mGists;

    private static class MyGistsTask extends AsyncTask<Void, Void, Void> {
        public MyGists activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final GistService gs = new GistService(activity.getGitHubClient());
            if (activity.mGistPages == null) {
                activity.mGistPages = gs.pageGists(activity.mTarget, 10);
            }
            if (activity.mGistPages.hasNext()) {
                activity.mGists = new ArrayList<Gist>(activity.mGistPages.next());
            }
            if (activity.mGists == null) {
                activity.mGists = new ArrayList<Gist>();
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

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleGist.class);
            final Gist g = mGists.get(position);
            i.putExtra("gistId", g.getId());
            i.putExtra("gistDescription", g.getDescription());
            i.putExtra("gistOwner", g.getUser().getLogin());
            i.putExtra("gistURL", g.getHtmlUrl());
            i.putExtra("gistUpdatedDate", g.getUpdatedAt().toString());
            i.putExtra("gistCreatedDate", g.getCreatedAt().toString());
            i.putExtra("gistFileCount", g.getFiles().size());
            startActivity(i);
            return;
        }
    };

    private String mTarget;

    private MyGistsTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new GistListAdapter(MyGists.this, mListView);
        mListView.setAdapter(mAdapter);

        mTask = (MyGistsTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new MyGistsTask();
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

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
import net.idlesoft.android.apps.github.activities.Profile;
import net.idlesoft.android.apps.github.adapters.FollowersFollowingListAdapter;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.UserService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

public class Following extends BaseActivity {
    private static class FollowingTask extends AsyncTask<Void, Void, Void> {
        public Following activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final UserService us = new UserService(activity.getGitHubClient());
            final PageIterator<User> itr = us.pageFollowing(activity.mTarget, 30);
            activity.mFollowing = new ArrayList<User>(itr.next());
            if (activity.mFollowing == null) {
                activity.mFollowing = new ArrayList<User>();
            }
            activity.mAdapter.loadData(activity.mFollowing);
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

    private FollowersFollowingListAdapter mAdapter;

    private ArrayList<User> mFollowing;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), Profile.class);
            i.putExtra("username", mFollowing.get(position).getLogin());
            startActivity(i);
            return;
        }
    };

    private String mTarget;

    private FollowingTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new FollowersFollowingListAdapter(Following.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mTask = (FollowingTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new FollowingTask();
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

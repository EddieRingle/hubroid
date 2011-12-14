/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter;

import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

public class NewsFeed extends BaseActivity {
    private ArrayList<Event> mEvents;

    private static class LoadActivityFeedTask extends AsyncTask<Void, Void, Void> {
        public NewsFeed activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final EventService es = new EventService(activity.getGitHubClient());
            final PageIterator<Event> itr;
            if (mPrivate) {
                itr = es.pageUserReceivedEvents(mTargetUser, false, 30);
            } else {
                itr = es.pageUserEvents(mTargetUser, true, 30);
            }
            if (itr.hasNext()) {
                activity.mEvents = new ArrayList<Event>(itr.next());
            } else {
                activity.mEvents = new ArrayList<Event>();
            }
            activity.mActivityAdapter.loadData(activity.mEvents);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mActivityAdapter.pushData();
            activity.mActivityAdapter.setIsLoadingData(false);
        }

        @Override
        protected void onPreExecute() {
            activity.mActivityAdapter.setIsLoadingData(true);
        }
    }

    private static boolean mPrivate;

    private static String mTargetUser;

    private ActivityFeedAdapter mActivityAdapter;

    private ListView mListView;

    private LoadActivityFeedTask mLoadActivityTask;

    private final OnItemClickListener onActivityItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                final long arg3) {
            final Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
            intent.putExtra("item_json", GsonUtils.toJson(mEvents.get(arg2)));
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.news_feed);

        setupActionBar();

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTargetUser = bundle.getString("username");
            mPrivate = false;
        } else {
            mTargetUser = mUsername;
            mPrivate = true;
        }

        mListView = (ListView) findViewById(R.id.lv_news_feed);
        mListView.setOnItemClickListener(onActivityItemClick);

        mActivityAdapter = new ActivityFeedAdapter(NewsFeed.this, mListView, mPrivate == false);
        mListView.setAdapter(mActivityAdapter);

        if (mPrivate) {
            getActionBar().setTitle("News Feed");
        } else {
            getActionBar().setTitle("Public Activity");
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
    public Object onRetainNonConfigurationInstance() {
        return mLoadActivityTask;
    }
}

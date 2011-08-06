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
import net.idlesoft.android.apps.github.activities.SingleGist;
import net.idlesoft.android.apps.github.adapters.GistListAdapter;
import net.idlesoft.android.apps.github.adapters.OrgsListAdapter;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.OrganizationService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.test.MoreAsserts;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class MyOrgs extends BaseActivity {
    private ArrayList<User> mOrganizations;

    private static class MyOrgsTask extends AsyncTask<Void, Void, Void> {
        public MyOrgs activity;

        @Override
        protected Void doInBackground(final Void... params) {
        	final OrganizationService os = new OrganizationService(activity.getGitHubClient());
        	try {
	        	if (activity.mTarget.equalsIgnoreCase(activity.mUsername)) {
	        		activity.mOrganizations = new ArrayList<User>(os.getOrganizations());
	        	} else {
	        		activity.mOrganizations = new ArrayList<User>(os.getOrganizations(activity.mTarget));
	        	}
        	} catch (IOException e) {
        		e.printStackTrace();
        		activity.mOrganizations = null;
        	}
            if (activity.mOrganizations == null) {
                activity.mOrganizations = new ArrayList<User>();
            }
            activity.mAdapter.loadData(activity.mOrganizations);
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

    private OrgsListAdapter mAdapter;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), Profile.class);
            final User org = mOrganizations.get(position);
            i.putExtra("username", org.getLogin());
            startActivity(i);
            return;
        }
    };

    private String mTarget;

    private MyOrgsTask mTask;

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

        mAdapter = new OrgsListAdapter(MyOrgs.this, mListView);
        mListView.setAdapter(mAdapter);

        mTask = (MyOrgsTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new MyOrgsTask();
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

/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import java.io.IOException;
import java.util.ArrayList;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.BaseActivity;
import net.idlesoft.android.apps.github.adapters.TeamsListAdapter;

import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.service.TeamService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Teams extends BaseActivity {
	private ArrayList<Team> mTeams;
    private static class TeamsTask extends AsyncTask<Void, Void, Void> {
        public Teams activity;

        @Override
        protected Void doInBackground(final Void... params) {
        	final TeamService ts = new TeamService(activity.getGitHubClient());
        	try {
        		activity.mTeams = new ArrayList<Team>(ts.getTeams(activity.mTarget));
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	if (activity.mTeams == null) {
        		activity.mTeams = new ArrayList<Team>();
        	}
        	activity.mAdapter.loadData(activity.mTeams);
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

    private TeamsListAdapter mAdapter;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {/*
            final Intent i = new Intent(getApplicationContext(), SingleTeam.class);
            i.putExtra("id", mTeams.get(position).getId());
            startActivity(i);*/
            return;
        }
    };

    private String mTarget;

    private TeamsTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new TeamsListAdapter(Teams.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mTask = (TeamsTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new TeamsTask();
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

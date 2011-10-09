/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.CommitListAdapter;

import org.eclipse.egit.github.core.PullRequestCommit;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

public class CommitsList extends BaseActivity {
    private ArrayList<PullRequestCommit> mCommits;

    private static class GatherCommitsTask extends AsyncTask<Void, Void, Void> {
        public CommitsList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final RepositoryService rs = new RepositoryService(activity.getGitHubClient());
            PageIterator<PullRequestCommit> itr = rs.pageCommits(activity.mRepoOwner,
                    activity.mRepoName, activity.mBranchName, null, 30);
            if (itr.hasNext()) {
                activity.mCommits = new ArrayList<PullRequestCommit>(itr.next());
            }
            if (activity.mCommits == null) {
                activity.mCommits = new ArrayList<PullRequestCommit>();
            }
            activity.mCommitListAdapter.loadData(activity.mCommits);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mCommitListAdapter.pushData();
            activity.mCommitListAdapter.setIsLoadingData(false);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            activity.mCommitListAdapter.setIsLoadingData(true);
        }
    }

    public String mBranchName;

    public CommitListAdapter mCommitListAdapter;

    public ListView mCommitListView;

    private GatherCommitsTask mGatherCommitsTask;

    public String mRepoName;

    public String mRepoOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.commits_list);

        setupActionBar();

        getActionBar().setTitle("Recent Commits");

        mCommitListView = (ListView) findViewById(R.id.lv_commits_list_list);
        mCommitListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Intent i = new Intent(CommitsList.this, SingleCommit.class);
                i.putExtra("committer", mCommits.get(position).getCommitter().getLogin());
                i.putExtra("author", mCommits.get(position).getAuthor().getLogin());
                i.putExtra("commit_sha", mCommits.get(position).getSha());
                i.putExtra("repo_name", mRepoName);
                i.putExtra("repo_owner", mRepoOwner);
                CommitsList.this.startActivity(i);
            }
        });

        mCommitListAdapter = new CommitListAdapter(CommitsList.this, mCommitListView);
        mCommitListView.setAdapter(mCommitListAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
        }
    }

    @Override
    protected void onResume() {
        mGatherCommitsTask = (GatherCommitsTask) getLastNonConfigurationInstance();
        if (mGatherCommitsTask == null) {
            mGatherCommitsTask = new GatherCommitsTask();
        }
        mGatherCommitsTask.activity = this;
        if (mGatherCommitsTask.getStatus() == AsyncTask.Status.PENDING) {
            mGatherCommitsTask.execute();
        }
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGatherCommitsTask;
    }
}

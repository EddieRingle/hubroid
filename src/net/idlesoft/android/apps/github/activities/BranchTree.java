/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.BranchTreeListAdapter;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.service.DataService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class BranchTree extends BaseActivity {
    private Tree mTree;

    private static class LoadTreeTask extends AsyncTask<Void, Void, Void> {
        public BranchTree activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final DataService ds = new DataService(activity.getGitHubClient());
            try {
                activity.mTree = ds.getTree(
                        RepositoryId.create(activity.mRepositoryOwner, activity.mRepositoryName),
                        activity.mBranchSha);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (activity.mTree == null) {
                activity.mTree = new Tree();
            }
            if (activity.mTree.getTree() == null) {
                activity.mTree.setTree(new ArrayList<TreeEntry>());
            }
            activity.mAdapter.loadData(new ArrayList<TreeEntry>(activity.mTree.getTree()));
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

    private BranchTreeListAdapter mAdapter;

    private String mBranchName;

    private String mBranchSha;

    private ListView mListView;

    private LoadTreeTask mLoadTreeTask;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            final TreeEntry treeItem = mTree.getTree().get(position);
            if (treeItem.getType().equals("tree")) {
                final Intent i = new Intent(BranchTree.this, BranchTree.class);
                i.putExtra("repo_owner", mRepositoryOwner);
                i.putExtra("repo_name", mRepositoryName);
                i.putExtra("branch_name", mBranchName);
                i.putExtra("branch_sha", treeItem.getSha());
                startActivity(i);
            } else if (treeItem.getType().equals("blob")) {
                final Intent i = new Intent(BranchTree.this, FileViewer.class);
                i.putExtra("repo_owner", mRepositoryOwner);
                i.putExtra("repo_name", mRepositoryName);
                i.putExtra("blob_name", treeItem.getPath());
                i.putExtra("blob_sha", treeItem.getSha());
                startActivity(i);
            }
        }
    };

    private String mRepositoryName;

    private String mRepositoryOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.branch_tree_list);

        setupActionBar();

        getActionBar().setTitle("Tree Browser");

        mListView = (ListView) findViewById(R.id.lv_branchTreeList_list);
        mListView.setOnItemClickListener(mOnListItemClick);

        mAdapter = new BranchTreeListAdapter(BranchTree.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
            mBranchSha = extras.getString("branch_sha");
        }
    }

    @Override
    protected void onResume() {
        mLoadTreeTask = (LoadTreeTask) getLastNonConfigurationInstance();
        if (mLoadTreeTask == null) {
            mLoadTreeTask = new LoadTreeTask();
        }
        mLoadTreeTask.activity = BranchTree.this;
        if ((mLoadTreeTask.getStatus() == AsyncTask.Status.PENDING) && !mAdapter.hasItems()) {
            mLoadTreeTask.execute();
        }
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadTreeTask;
    }
}

/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.IOException;
import java.util.ArrayList;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.BranchListAdapter;

import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.DataService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BranchesList extends BaseActivity {
	private ArrayList<Reference> mBranches;

    private static class GetBranchesTask extends AsyncTask<Void, Void, Void> {
        BranchesList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
            	final DataService ds = new DataService(activity.getGitHubClient());
            	ArrayList<Reference> refs = new ArrayList<Reference>(ds.getReferences(
            			RepositoryId.create(activity.mRepoOwner, activity.mRepoName)));
            	int size = refs.size();
            	activity.mBranches = new ArrayList<Reference>();
            	// Loop through the list of retrieved references to sort out the branches (heads)
            	for (int i = 0; i < size; i++) {
            		if (refs.get(i).getRef().startsWith("refs/heads/")) {
            			// It's a branch, let's add it to the branches ArrayList
            			activity.mBranches.add(refs.get(i));
            		}
            	}
            	activity.mBranchListAdapter.loadData(activity.mBranches);
            } catch (IOException e) {
            	// TODO: Do better exception handling
				e.printStackTrace();
			}
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
        	activity.mBranchListAdapter.pushData();
        	activity.mBranchListAdapter.setIsLoadingData(false);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
        	activity.mBranchListAdapter.setIsLoadingData(true);
            super.onPreExecute();
        }
    }

    public ListView mBranchList;

    public BranchListAdapter mBranchListAdapter;

    public GetBranchesTask mGetBranchesTask;

    public Intent mIntent;

    private final OnItemClickListener mOnBranchListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
		    mIntent = new Intent(BranchesList.this, Branch.class);
		    mIntent.putExtra("repo_name", mRepoName);
		    mIntent.putExtra("repo_owner", mRepoOwner);
		    mIntent.putExtra("branch_name", mBranches.get(position).getRef().replace("refs/heads/", ""));
		    mIntent.putExtra("branch_sha", mBranches.get(position).getObject().getSha());
            BranchesList.this.startActivity(mIntent);
        }
    };

    public String mRepoName;

    public String mRepoOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.branch_list);

        setupActionBar();

        getActionBar().setTitle("Branches");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");

            mBranchList = (ListView) findViewById(R.id.lv_branchList_list);
            mBranchList.setOnItemClickListener(mOnBranchListItemClick);

            mBranchListAdapter = new BranchListAdapter(this, mBranchList);
            mBranchList.setAdapter(mBranchListAdapter);

            mGetBranchesTask = (GetBranchesTask) getLastNonConfigurationInstance();

            if (mGetBranchesTask == null) {
                mGetBranchesTask = new GetBranchesTask();
            }

            mGetBranchesTask.activity = this;

            if (mGetBranchesTask.getStatus() == AsyncTask.Status.PENDING) {
                mGetBranchesTask.execute();
            }
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetBranchesTask;
    }
}

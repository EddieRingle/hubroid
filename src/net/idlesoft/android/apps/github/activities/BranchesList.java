/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.BranchListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class BranchesList extends BaseActivity {
    private static class GetBranchesTask extends AsyncTask<Void, Void, Void> {
        BranchesList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGApi.repo.branches(activity.mRepoOwner,
                        activity.mRepoName).resp).getJSONObject("branches");
                activity.mBranchListAdapter = new BranchListAdapter(activity, activity.mJson);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mBranchList.removeHeaderView(activity.mLoadView);
            activity.mBranchList.setAdapter(activity.mBranchListAdapter);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            activity.mBranchList.addHeaderView(activity.mLoadView);
            activity.mBranchList.setAdapter(null);
            super.onPreExecute();
        }
    }

    public ListView mBranchList;

    public BranchListAdapter mBranchListAdapter;

    public GetBranchesTask mGetBranchesTask;

    public Intent mIntent;

    public JSONObject mJson;

    public View mLoadView;

    private final OnItemClickListener mOnBranchListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            try {
                if (mJson != null) {
                    mIntent = new Intent(BranchesList.this, Branch.class);
                    mIntent.putExtra("repo_name", mRepoName);
                    mIntent.putExtra("repo_owner", mRepoOwner);
                    mIntent.putExtra("branch_name", mJson.names().getString(position));
                    mIntent.putExtra("branch_sha", mJson.getString(mJson.names().getString(position)));
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            BranchesList.this.startActivity(mIntent);
        }
    };

    public String mRepoName;

    public String mRepoOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.branch_list);

        mLoadView = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        mBranchList = (ListView) findViewById(R.id.lv_branchList_list);
        mBranchList.setOnItemClickListener(mOnBranchListItemClick);

        setupActionBar();

        getActionBar().setTitle("Branches");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");

            mGetBranchesTask = (GetBranchesTask) getLastNonConfigurationInstance();

            if (mGetBranchesTask == null) {
                mGetBranchesTask = new GetBranchesTask();
            }

            mGetBranchesTask.activity = this;

            if ((mGetBranchesTask.getStatus() == AsyncTask.Status.PENDING)
                    && (mBranchListAdapter == null)) {
                mGetBranchesTask.execute();
            }
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetBranchesTask;
    }
}

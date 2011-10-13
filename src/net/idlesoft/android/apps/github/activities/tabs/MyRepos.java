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
import net.idlesoft.android.apps.github.activities.BranchesList;
import net.idlesoft.android.apps.github.activities.Issues;
import net.idlesoft.android.apps.github.activities.NetworkList;
import net.idlesoft.android.apps.github.activities.Profile;
import net.idlesoft.android.apps.github.activities.Repositories;
import net.idlesoft.android.apps.github.activities.SingleRepository;
import net.idlesoft.android.apps.github.adapters.RepositoriesListAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MyRepos extends BaseActivity {
    private static class MyReposTask extends AsyncTask<Void, Void, Void> {
        public MyRepos activity;

        @Override
        protected Void doInBackground(final Void... params) {
            // TODO: Convert to use egit-github
            if (activity.mJson == null) {
                try {
                    final Response resp = activity.mGApi.repo.list(activity.mTarget);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    activity.mJson = (new JSONObject(resp.resp)).getJSONArray("repositories");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            activity.mAdapter.loadData(activity.mJson);
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

    private RepositoriesListAdapter mAdapter;

    private JSONArray mJson;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleRepository.class);
            i.putExtra("repo_owner", mTarget);
            try {
                i.putExtra("repo_name", mJson.getJSONObject(position).getString("name"));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };

    private String mTarget;

    private MyReposTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);
        registerForContextMenu(mListView);

        setContentView(mListView);

        mAdapter = new RepositoriesListAdapter(MyRepos.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mTask = (MyReposTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new MyReposTask();
        }
        mTask.activity = this;
        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                return;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mJson != null) {
            mAdapter.loadData(mJson);
            mAdapter.pushData();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (!menu.hasVisibleItems()) {
            menu.add(0, Repositories.CONTEXT_MENU_DETAILS, 0, "Details");
            menu.add(0, Repositories.CONTEXT_MENU_BRANCHES, 1, "Branches");
            menu.add(0, Repositories.CONTEXT_MENU_ISSUES, 2, "Issues");
            menu.add(0, Repositories.CONTEXT_MENU_FORKS, 3, "Forks");
            menu.add(0, Repositories.CONTEXT_MENU_OWNER, 4, "Owner's Profile");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = ((AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo());

        final Intent intent = new Intent();
        try {
            intent.putExtra("repo_owner", mJson.getJSONObject(info.position).getString("owner"));
            intent.putExtra("repo_name", mJson.getJSONObject(info.position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (item.getItemId()) {
            case Repositories.CONTEXT_MENU_DETAILS:
                mListView.performItemClick(info.targetView, info.position, info.id);
                break;
            case Repositories.CONTEXT_MENU_BRANCHES:
                intent.setClass(MyRepos.this, BranchesList.class);
                startActivity(intent);
                break;
            case Repositories.CONTEXT_MENU_ISSUES:
                intent.setClass(MyRepos.this, Issues.class);
                startActivity(intent);
                break;
            case Repositories.CONTEXT_MENU_FORKS:
                intent.setClass(MyRepos.this, NetworkList.class);
                startActivity(intent);
                break;
            case Repositories.CONTEXT_MENU_OWNER:
                intent.setClass(MyRepos.this, Profile.class);
                intent.putExtra("username", intent.getStringExtra("repo_owner"));
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
}

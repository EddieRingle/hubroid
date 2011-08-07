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
import net.idlesoft.android.apps.github.activities.SingleRepository;
import net.idlesoft.android.apps.github.adapters.RepositoriesListAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchRepos extends BaseActivity {
    private static class SearchReposTask extends AsyncTask<Void, Void, Void> {
        public SearchRepos activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                final Response resp = activity.mGApi.repo.search(activity.mSearchTerm);
                if (resp.statusCode != 200) {
                    /* Oh noez, something went wrong */
                    return null;
                }
                activity.mJson = (new JSONObject(resp.resp)).getJSONArray("repositories");
            } catch (final JSONException e) {
                e.printStackTrace();
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
            activity.mAdapter.clear();
            activity.mAdapter.setIsLoadingData(true);
        }
    }

    private RepositoriesListAdapter mAdapter;

    private JSONArray mJson;

    private ListView mListView;

    private final OnEditorActionListener mOnEditorAction = new OnEditorActionListener() {
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            mSearchBox.clearFocus();
            final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            mSearchTerm = mSearchBox.getText().toString();
            if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                mTask = new SearchReposTask();
                mTask.activity = SearchRepos.this;
            }
            if (mTask.getStatus() == AsyncTask.Status.PENDING) {
                mTask.execute();
            }
            return false;
        }
    };

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleRepository.class);
            try {
                i.putExtra("repo_owner", mJson.getJSONObject(position).getString("owner"));
                i.putExtra("repo_name", mJson.getJSONObject(position).getString("name"));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };

    private final OnClickListener mOnSearchButtonClick = new OnClickListener() {
        public void onClick(final View v) {
            mSearchBox.clearFocus();
            final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            mSearchTerm = mSearchBox.getText().toString();
            if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                mTask = new SearchReposTask();
                mTask.activity = SearchRepos.this;
            }
            if (mTask.getStatus() == AsyncTask.Status.PENDING) {
                mTask.execute();
            }
        }
    };

    private EditText mSearchBox;

    private ImageButton mSearchButton;

    private String mSearchTerm;

    private SearchReposTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.search_tab);

        mListView = (ListView) findViewById(R.id.lv_searchTab_list);
        mListView.setOnItemClickListener(mOnListItemClick);

        mSearchBox = (EditText) findViewById(R.id.et_searchTab_search_box);
        mSearchBox.setOnEditorActionListener(mOnEditorAction);

        mSearchButton = (ImageButton) findViewById(R.id.btn_searchTab_go);
        mSearchButton.setOnClickListener(mOnSearchButtonClick);

        mAdapter = new RepositoriesListAdapter(SearchRepos.this, mListView);
        mListView.setAdapter(mAdapter);

        mTask = (SearchReposTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new SearchReposTask();
        }
        mTask.activity = this;
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
}

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Hubroid;
import net.idlesoft.android.apps.github.activities.Profile;
import net.idlesoft.android.apps.github.adapters.SearchUsersListAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class SearchUsers extends Activity {
    private static class SearchUsersTask extends AsyncTask<Void, Void, Void> {
        public SearchUsers activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                final Response resp = activity.mGapi.user.search(activity.mSearchTerm);
                if (resp.statusCode != 200) {
                    /* Oh noez, something went wrong */
                    return null;
                }
                activity.mJson = (new JSONObject(resp.resp)).getJSONArray("users");
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
            activity.mAdapter.setIsLoadingData(true);
        }
    }

    private SearchUsersListAdapter mAdapter;

    private final GitHubAPI mGapi = new GitHubAPI();

    private JSONArray mJson;

    private ListView mListView;

    private EditText mSearchBox;

    private ImageButton mSearchButton;

    public View mLoadingItem;

    private String mSearchTerm;

    private SearchUsersTask mTask;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            Intent i = new Intent(getApplicationContext(), Profile.class);
            try {
                i.putExtra("username", mJson.getJSONObject(position).getString("login"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };

    private final OnClickListener mOnSearchButtonClick = new OnClickListener() {
        public void onClick(View v) {
            mSearchBox.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            mSearchTerm = mSearchBox.getText().toString();
            if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                mTask = new SearchUsersTask();
                mTask.activity = SearchUsers.this;
            }
            if (mTask.getStatus() == AsyncTask.Status.PENDING) {
                mTask.execute();
            }
        }
    };

    private final OnEditorActionListener mOnEditorAction = new OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            mSearchBox.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            mSearchTerm = mSearchBox.getText().toString();
            if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                mTask = new SearchUsersTask();
                mTask.activity = SearchUsers.this;
            }
            if (mTask.getStatus() == AsyncTask.Status.PENDING) {
                mTask.execute();
            }
            return false;
        }
    };

    private String mUsername;

    private String mPassword;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        final SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mUsername = prefs.getString("username", "");
        mPassword = prefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        setContentView(R.layout.search_tab);

        mListView = (ListView) findViewById(R.id.lv_searchTab_list);
        mListView.setOnItemClickListener(mOnListItemClick);

        mSearchBox = (EditText) findViewById(R.id.et_searchTab_search_box);
        mSearchBox.setOnEditorActionListener(mOnEditorAction);

        mSearchButton = (ImageButton) findViewById(R.id.btn_searchTab_go);
        mSearchButton.setOnClickListener(mOnSearchButtonClick);

        mAdapter = new SearchUsersListAdapter(SearchUsers.this, mListView);

        mTask = (SearchUsersTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new SearchUsersTask();
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
    public void onResume() {
        super.onResume();
        mListView.setAdapter(mAdapter);
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

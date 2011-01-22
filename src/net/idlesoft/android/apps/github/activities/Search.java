/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.RepositoriesListAdapter;
import net.idlesoft.android.apps.github.adapters.SearchUsersListAdapter;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class Search extends Activity {
    private static class SearchTask extends AsyncTask<String, Void, Void> {
        public Search mActivity;

        public SearchTask(final Search activity) {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(final String... params) {
            if (mActivity.m_type.equals(REPO_TYPE)) {
                try {
                    final JSONObject response = new JSONObject(mActivity._gapi.repo
                            .search(params[0]).resp);
                    mActivity.m_repositoriesData = response.getJSONArray(REPO_TYPE);
                    mActivity.m_repositories_adapter = new RepositoriesListAdapter(mActivity,
                            mActivity.m_repositoriesData);
                } catch (final JSONException e) {
                    publishProgress((Void) null);
                    e.printStackTrace();
                }
            } else if (mActivity.m_type.equals(USER_TYPE)) {
                try {
                    final JSONObject response = new JSONObject(mActivity._gapi.user
                            .search(params[0]).resp);
                    mActivity.m_usersData = response.getJSONArray(USER_TYPE);
                    mActivity.m_users_adapter = new SearchUsersListAdapter(mActivity,
                            mActivity.m_usersData);
                } catch (final JSONException e) {
                    publishProgress((Void) null);
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            mActivity.mListView.removeHeaderView(mActivity.mLoadingItem);
            if (mActivity.m_type.equals(REPO_TYPE)) {
                mActivity.mListView.setAdapter(mActivity.m_repositories_adapter);
            } else if (mActivity.m_type.equals(USER_TYPE)) {
                mActivity.mListView.setAdapter(mActivity.m_users_adapter);
            }
        }

        @Override
        protected void onPreExecute() {
            if (mActivity.m_type.equals(REPO_TYPE)) {
                ((TextView) mActivity.mLoadingItem
                        .findViewById(R.id.tv_loadingListItem_loadingText))
                        .setText("Searching for Repositories...");
                mActivity.mListView.addHeaderView(mActivity.mLoadingItem);
            } else if (mActivity.m_type.equals(USER_TYPE)) {
                ((TextView) mActivity.mLoadingItem
                        .findViewById(R.id.tv_loadingListItem_loadingText))
                        .setText("Searching for Users...");
                mActivity.mListView.addHeaderView(mActivity.mLoadingItem);
            }
            mActivity.mListView.setAdapter(null);
        }

        @Override
        protected void onProgressUpdate(final Void... progress) {
            if (mActivity.m_type.equals(REPO_TYPE)) {
                Toast.makeText(mActivity, "Error gathering repository data, please try again.",
                        Toast.LENGTH_SHORT).show();
            } else if (mActivity.m_type.equals(USER_TYPE)) {
                Toast.makeText(mActivity, "Error gathering user data, please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static final String REPO_TYPE = "repositories";

    private static final String USER_TYPE = "users";

    private GitHubAPI _gapi;

    public InputMethodManager m_imm;

    public Intent m_intent;

    private final OnItemClickListener m_MessageClickedHandler = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            m_position = position;
            try {
                if (m_type.equals(REPO_TYPE)) {
                    m_intent = new Intent(Search.this, Repository.class);
                    m_intent.putExtra("repo_name", m_repositoriesData.getJSONObject(m_position)
                            .getString("name"));
                    m_intent.putExtra("username", m_repositoriesData.getJSONObject(m_position)
                            .getString("username"));
                } else if (m_type.equals(USER_TYPE)) {
                    m_intent = new Intent(Search.this, Profile.class);
                    m_intent.putExtra("username", m_usersData.getJSONObject(m_position).getString(
                            "username"));
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            Search.this.startActivityForResult(m_intent, 5005);
        }
    };

    public int m_position;

    private RepositoriesListAdapter m_repositories_adapter;

    public JSONArray m_repositoriesData;

    public String m_type;

    private SearchUsersListAdapter m_users_adapter;

    public JSONArray m_usersData;

    private ListView mListView;

    private View mLoadingItem;

    private Button mReposBtn;

    private SearchTask mSearchTask;

    private Button mUsersBtn;

    private final OnClickListener onButtonToggleClickListener = new OnClickListener() {
        public void onClick(final View v) {
            if (v.getId() == R.id.btn_search_repositories) {
                mReposBtn.setEnabled(false);
                mUsersBtn.setEnabled(true);
                toggleList(REPO_TYPE);
                m_type = REPO_TYPE;
            } else if (v.getId() == R.id.btn_search_users) {
                mUsersBtn.setEnabled(false);
                mReposBtn.setEnabled(true);
                toggleList(USER_TYPE);
                m_type = USER_TYPE;
            }
        }
    };

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == 5005) {
            Toast.makeText(Search.this, "That user has recently been deleted.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.search);

        m_type = REPO_TYPE;
        _gapi = new GitHubAPI();
        mListView = (ListView) findViewById(R.id.lv_search);
        mReposBtn = (Button) findViewById(R.id.btn_search_repositories);
        mUsersBtn = (Button) findViewById(R.id.btn_search_users);
        mLoadingItem = getLayoutInflater().inflate(R.layout.loading_listitem, null);

        ((TextView) mLoadingItem.findViewById(R.id.tv_loadingListItem_loadingText))
                .setText("Searching Repositories, Please Wait...");
        mReposBtn.setEnabled(false);

        mSearchTask = (SearchTask) getLastNonConfigurationInstance();
        if (mSearchTask != null) {
            mSearchTask.mActivity = Search.this;
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        m_type = savedInstanceState.getString("type");
        try {
            if (savedInstanceState.containsKey("repositories_json")) {
                m_repositoriesData = new JSONArray(savedInstanceState
                        .getString("repositories_json"));
            } else {
                m_repositoriesData = new JSONArray();
            }
        } catch (final JSONException e) {
            m_repositoriesData = new JSONArray();
        }
        try {
            if (savedInstanceState.containsKey("users_json")) {
                m_usersData = new JSONArray(savedInstanceState.getString("users_json"));
            } else {
                m_usersData = new JSONArray();
            }
        } catch (final JSONException e) {
            m_usersData = new JSONArray();
        }
        if (m_repositoriesData.length() > 0) {
            m_repositories_adapter = new RepositoriesListAdapter(Search.this, m_repositoriesData);
        } else {
            m_repositories_adapter = null;
        }
        if (m_usersData.length() > 0) {
            m_users_adapter = new SearchUsersListAdapter(Search.this, m_usersData);
        } else {
            m_users_adapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleList(m_type);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mSearchTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("type", m_type);
        if (m_repositoriesData != null) {
            savedInstanceState.putString("repositories_json", m_repositoriesData.toString());
        }
        if (m_usersData != null) {
            savedInstanceState.putString("users_json", m_usersData.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

        m_imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        ((Button) findViewById(R.id.btn_search_repositories))
                .setOnClickListener(onButtonToggleClickListener);
        ((Button) findViewById(R.id.btn_search_users))
                .setOnClickListener(onButtonToggleClickListener);
        ((ImageButton) findViewById(R.id.btn_search_go)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                m_imm.hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                search();
            }
        });
        ((EditText) findViewById(R.id.et_search_search_box))
                .setOnEditorActionListener(new OnEditorActionListener() {
                    public boolean onEditorAction(final TextView v, final int actionCode,
                            final KeyEvent arg2) {
                        if (actionCode == EditorInfo.IME_ACTION_SEARCH) {
                            search();
                        }
                        return false;
                    }
                });

        mListView.setOnItemClickListener(m_MessageClickedHandler);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    public void search() {
        if ((mSearchTask == null) || (mSearchTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mSearchTask = new SearchTask(Search.this);
        }
        if (mSearchTask.getStatus() == AsyncTask.Status.PENDING) {
            mSearchTask.execute(((EditText) findViewById(R.id.et_search_search_box)).getText()
                    .toString());
        }
    }

    public void toggleList(String type) {
        if (type.equals("") || (type == null)) {
            type = (m_type.equals(REPO_TYPE)) ? USER_TYPE : REPO_TYPE;
        }
        m_type = type;

        if (m_type.equals(REPO_TYPE)) {
            mListView.setAdapter(m_repositories_adapter);
        } else if (m_type.equals(USER_TYPE)) {
            mListView.setAdapter(m_users_adapter);
        }
    }
}

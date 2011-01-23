/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ForkListAdapter;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class NetworkList extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public ForkListAdapter mAdapter;

    public Intent mIntent;

    public JSONArray mJson;

    private final OnItemClickListener mOnForkListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            try {
                mIntent = new Intent(NetworkList.this, Repository.class);
                mIntent.putExtra("repo_name", mJson.getJSONObject(position).getString(
                        "name"));
                mIntent.putExtra("username", mJson.getJSONObject(position).getString(
                        "owner"));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            NetworkList.this.startActivity(mIntent);
        }
    };

    private SharedPreferences mPrefs;

    public ProgressDialog mProgressDialog;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private Thread mThread;

    private String mPassword;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.network);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("username");

            try {
                final TextView title = (TextView) findViewById(R.id.tv_page_title);
                title.setText("Network");

                final JSONObject forkjson = new JSONObject(mGapi.repo.network(mRepositoryOwner,
                        mRepositoryName).resp);

                mJson = forkjson.getJSONArray("network");

                mAdapter = new ForkListAdapter(NetworkList.this, mJson);

                final ListView repo_list = (ListView) findViewById(R.id.lv_network_list);
                repo_list.setAdapter(mAdapter);
                repo_list.setOnItemClickListener(mOnForkListItemClick);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        if ((mThread != null) && mThread.isAlive()) {
            mThread.stop();
        }
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}

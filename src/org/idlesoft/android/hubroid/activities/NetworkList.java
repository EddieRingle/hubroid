/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import com.flurry.android.FlurryAgent;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.adapters.ForkListAdapter;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;

public class NetworkList extends Activity {
    private GitHubAPI _gapi;

    private SharedPreferences.Editor m_editor;

    public ForkListAdapter m_forkListAdapter;

    public Intent m_intent;

    public JSONArray m_jsonForkData;

    private final OnItemClickListener m_onForkListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            m_position = position;
            try {
                m_intent = new Intent(NetworkList.this, Repository.class);
                m_intent.putExtra("repo_name", m_jsonForkData.getJSONObject(m_position).getString(
                        "name"));
                m_intent.putExtra("username", m_jsonForkData.getJSONObject(m_position).getString(
                        "owner"));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            NetworkList.this.startActivity(m_intent);
        }
    };

    public int m_position;

    private SharedPreferences m_prefs;

    public ProgressDialog m_progressDialog;

    public String m_repo_name;

    public String m_repo_owner;

    private Thread m_thread;

    private String m_token;

    private String m_username;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.network);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            m_repo_name = extras.getString("repo_name");
            m_repo_owner = extras.getString("username");

            try {
                final TextView title = (TextView) findViewById(R.id.tv_top_bar_title);
                title.setText("Network");

                final JSONObject forkjson = new JSONObject(_gapi.repo.network(m_repo_owner,
                        m_repo_name).resp);

                m_jsonForkData = forkjson.getJSONArray("network");

                m_forkListAdapter = new ForkListAdapter(NetworkList.this, m_jsonForkData);

                final ListView repo_list = (ListView) findViewById(R.id.lv_network_list);
                repo_list.setAdapter(m_forkListAdapter);
                repo_list.setOnItemClickListener(m_onForkListItemClick);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                m_editor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 2:
                final File root = Environment.getExternalStorageDirectory();
                if (root.canWrite()) {
                    final File hubroid = new File(root, "hubroid");
                    if (!hubroid.exists() && !hubroid.isDirectory()) {
                        return true;
                    } else {
                        hubroid.delete();
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    public void onPause() {
        if ((m_thread != null) && m_thread.isAlive()) {
            m_thread.stop();
        }
        if ((m_progressDialog != null) && m_progressDialog.isShowing()) {
            m_progressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!menu.hasVisibleItems()) {
            menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
            menu.add(0, 1, 0, "Clear Preferences");
            menu.add(0, 2, 0, "Clear Cache");
        }
        return true;
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

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.flurry.android.FlurryAgent;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DiffFilesList extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public ArrayAdapter<String> mAdapter;

    public JSONObject mJson;

    public String mType;

    private final OnItemClickListener mOnFileListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            if (!mType.equals("modified")) {
                return;
            } else {
                Intent i = new Intent(DiffFilesList.this, CommitChangeViewer.class);
                i.putExtra("repo_owner", mRepositoryOwner);
                i.putExtra("repo_name", mRepositoryName);
                try {
                    i.putExtra("json", mJson.getJSONArray("modified").getJSONObject(position).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        }
    };

    private SharedPreferences mPrefs;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private String mPassword;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.diff_file_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mType = extras.getString("type");
            try {
                mJson = new JSONObject(extras.getString("json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                final TextView title = (TextView) findViewById(R.id.tv_page_title);
                if (mType.equals("added")) {
                    title.setText("Added Files");
                } else if (mType.equals("removed")) {
                    title.setText("Removed Files");
                } else if (mType.equals("modified")) {
                    title.setText("Changed Files");
                } else {
                    title.setText("Files");
                }

                String[] filenames = new String[mJson.getJSONArray(mType).length()];
                for (int i = 0; i < filenames.length; i++) {
                    if (mType.equals("modified")) {
                        filenames[i] = mJson.getJSONArray("modified").getJSONObject(i).getString("filename");
                    } else {
                        filenames[i] = mJson.getJSONArray(mType).getString(i);
                    }
                    if (filenames[i].lastIndexOf("/") > -1)
                        filenames[i] = filenames[i].substring(filenames[i].lastIndexOf("/") + 1);
                }

                mAdapter = new ArrayAdapter<String>(DiffFilesList.this, android.R.layout.simple_list_item_1, android.R.id.text1, filenames);

                final ListView file_list = (ListView) findViewById(R.id.lv_diffFileList_list);
                file_list.setAdapter(mAdapter);
                file_list.setOnItemClickListener(mOnFileListItemClick);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
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

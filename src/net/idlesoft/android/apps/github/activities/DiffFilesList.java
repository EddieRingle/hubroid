/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DiffFilesList extends BaseActivity {
    public ArrayAdapter<String> mAdapter;

    public JSONObject mJson;

    private final OnItemClickListener mOnFileListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            /*
             * Since added/removed files don't have a diff, only clicks on
             * modified files should link off to CommitChangeViewer
             */
            if (!mType.equals("modified")) {
                return;
            } else {
                final Intent i = new Intent(DiffFilesList.this, CommitChangeViewer.class);
                i.putExtra("repo_owner", mRepositoryOwner);
                i.putExtra("repo_name", mRepositoryName);
                try {
                    i.putExtra("json", mJson.getJSONArray("modified").getJSONObject(position)
                            .toString());
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        }
    };

    public String mRepositoryName;

    public String mRepositoryOwner;

    public String mType;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.diff_file_list);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mType = extras.getString("type");
            try {
                mJson = new JSONObject(extras.getString("json"));
            } catch (final JSONException e) {
                e.printStackTrace();
            }

            try {
                if (mType.equals("added")) {
                    getActionBar().setTitle("Added Files");
                } else if (mType.equals("removed")) {
                    getActionBar().setTitle("Removed Files");
                } else if (mType.equals("modified")) {
                    getActionBar().setTitle("Changed Files");
                } else {
                    getActionBar().setTitle("Files");
                }

                /*
                 * Split JSONArray into a String array so we can populate the
                 * list of files
                 */
                final String[] filenames = new String[mJson.getJSONArray(mType).length()];
                for (int i = 0; i < filenames.length; i++) {
                    if (mType.equals("modified")) {
                        filenames[i] = mJson.getJSONArray("modified").getJSONObject(i)
                                .getString("filename");
                    } else {
                        filenames[i] = mJson.getJSONArray(mType).getString(i);
                    }
                    if (filenames[i].lastIndexOf("/") > -1) {
                        filenames[i] = filenames[i].substring(filenames[i].lastIndexOf("/") + 1);
                    }
                }

                mAdapter = new ArrayAdapter<String>(DiffFilesList.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, filenames);

                final ListView file_list = (ListView) findViewById(R.id.lv_diffFileList_list);
                file_list.setAdapter(mAdapter);
                file_list.setOnItemClickListener(mOnFileListItemClick);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

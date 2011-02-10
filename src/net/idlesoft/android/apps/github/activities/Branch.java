/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.flurry.android.FlurryAgent;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Branch extends Activity {
    private String mBranchName;

    private String mBranchSha;

    private final GitHubAPI mGapi = new GitHubAPI();

    public Intent mIntent;

    public JSONObject mJson;

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.branch);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(Branch.this, Search.class));
            }
        });

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
            mBranchSha = extras.getString("branch_sha");

            final TextView branchName = (TextView) findViewById(R.id.tv_branch_name);
            final TextView branchSha = (TextView) findViewById(R.id.tv_branch_sha);
            final ListView infoList = (ListView) findViewById(R.id.lv_branch_infoList);

            branchName.setText(mBranchName);
            branchSha.setText(mBranchSha);

            infoList.setAdapter(new ArrayAdapter<String>(Branch.this, R.layout.branch_info_item,
                    R.id.tv_branchInfoItem_text1, new String[] {
                            "Commit Log", "View Branch's Tree"
                    }));
            infoList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View v,
                        final int position, final long id) {
                    if (position == 0) {
                        mIntent = new Intent(Branch.this, CommitsList.class);
                        mIntent.putExtra("repo_owner", mRepositoryOwner);
                        mIntent.putExtra("repo_name", mRepositoryName);
                        mIntent.putExtra("branch_name", mBranchName);
                        startActivity(mIntent);
                    } else if (position == 1) {
                        mIntent = new Intent(Branch.this, BranchTree.class);
                        mIntent.putExtra("repo_owner", mRepositoryOwner);
                        mIntent.putExtra("repo_name", mRepositoryName);
                        mIntent.putExtra("branch_name", mBranchName);
                        mIntent.putExtra("branch_sha", mBranchSha);
                        startActivity(mIntent);
                    }
                }
            });
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

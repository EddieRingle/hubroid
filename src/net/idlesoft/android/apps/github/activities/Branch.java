/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Branch extends BaseActivity {
    private String mBranchName;

    private String mBranchSha;

    public Intent mIntent;

    public JSONObject mJson;

    private String mRepositoryName;

    private String mRepositoryOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.branch);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mBranchName = extras.getString("branch_name");
            mBranchSha = extras.getString("branch_sha");

            // Break mBranchSha into two pieces - the first seven chars and the
            // rest.
            final String branch_sha_head = mBranchSha.substring(0, 7);
            final String branch_sha_tail = mBranchSha.substring(7, 40);

            final TextView branchName = (TextView) findViewById(R.id.tv_branch_name);
            final TextView branchShaHead = (TextView) findViewById(R.id.tv_branch_sha_head);
            final TextView branchShaTail = (TextView) findViewById(R.id.tv_branch_sha_tail);
            final ListView infoList = (ListView) findViewById(R.id.lv_branch_infoList);

            branchName.setText(mBranchName);
            branchShaHead.setText(branch_sha_head);
            branchShaTail.setText(branch_sha_tail);

            infoList.setAdapter(new ArrayAdapter<String>(Branch.this, R.layout.branch_info_item,
                    R.id.tv_branchInfoItem_text1, new String[] {
                            "Commit Log", "View Branch's Tree"
                    }));
            infoList.setOnItemClickListener(new OnItemClickListener() {
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                mPrefsEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }
}

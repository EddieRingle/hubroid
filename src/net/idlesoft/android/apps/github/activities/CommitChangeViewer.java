/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.CommitChangeViewerDiffAdapter;
import net.idlesoft.android.apps.github.adapters.CommitListAdapter;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class CommitChangeViewer extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public ArrayList<String> mBranches;

    public ArrayAdapter<String> mBranchesAdapter;

    public JSONArray mCommitData;

    public CommitListAdapter mCommitListAdapter;

    private SharedPreferences.Editor mEditor;

    private String mCommitSha;

    public Intent mIntent;

    private SharedPreferences mPrefs;

    public String mRepoName;

    public String mRepoOwner;

    private String mPassword;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit_view);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Commit Viewer");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("repo_owner");
            mCommitSha = extras.getString("commit_sha");

            // Get the commit data for that commit ID so that we can get the
            // tree ID and filename.
            try {
                final Response commitInfo = mGapi.commits.commit(mRepoOwner, mRepoName, mCommitSha);
                final JSONObject commitJSON = new JSONObject(commitInfo.resp)
                        .getJSONObject("commit");

                // Display the committer and author
                final JSONObject authorInfo = commitJSON.getJSONObject("author");
                final String authorName = authorInfo.getString("login");

                final Bitmap authorGravatar = Commit.loadGravatarByLoginName(CommitChangeViewer.this, authorName);

                final JSONObject committerInfo = commitJSON.getJSONObject("committer");
                final String committerName = committerInfo.getString("login");

                // If the committer is the author then just show them as the
                // author, otherwise show
                // both people
                ((TextView) findViewById(R.id.commit_view_author_name)).setText(authorName);

                if (authorGravatar != null) {
                    ((ImageView) findViewById(R.id.commit_view_author_gravatar))
                            .setImageBitmap(authorGravatar);
                }

                // Set the commit message
                ((TextView) findViewById(R.id.commit_view_message)).setText(commitJSON
                        .getString("message"));

                final SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
                Date commit_time;
                Date current_time;
                String authorDate = "";

                try {
                    commit_time = dateFormat.parse(commitJSON.getString("authored_date"));
                    current_time = dateFormat.parse(dateFormat.format(new Date()));
                    ((TextView) findViewById(R.id.commit_view_author_time))
                            .setText(Commit.getHumanDate(current_time, commit_time));

                    commit_time = dateFormat.parse(commitJSON.getString("committed_date"));
                    authorDate = Commit.getHumanDate(current_time, commit_time);

                } catch (final ParseException e) {
                    e.printStackTrace();
                }

                if (!authorName.equals(committerName)) {
                    // They are not the same person, make the author visible and
                    // fill in the details
                    ((LinearLayout) findViewById(R.id.commit_view_author_layout))
                            .setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.commit_view_committer_name))
                            .setText(committerName);
                    ((TextView) findViewById(R.id.commit_view_committer_time))
                            .setText(authorDate);
                    final Bitmap committerGravatar = Commit.loadGravatarByLoginName(CommitChangeViewer.this, committerName);
                    if (committerGravatar != null) {
                        ((ImageView) findViewById(R.id.commit_view_committer_gravatar))
                                .setImageBitmap(committerGravatar);
                    }
                }

                // Populate the ListView with the files that have changed
                final JSONArray changesJSON = commitJSON.getJSONArray("modified");
                final CommitChangeViewerDiffAdapter diffs = new CommitChangeViewerDiffAdapter(
                        getApplicationContext(), changesJSON);
                final ListView diffList = (ListView) findViewById(R.id.commit_view_diffs_list);
                diffList.setAdapter(diffs);
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
                mEditor.clear().commit();
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

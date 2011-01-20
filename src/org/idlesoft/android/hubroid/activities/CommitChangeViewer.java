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
import org.idlesoft.android.hubroid.adapters.CommitChangeViewerDiffAdapter;
import org.idlesoft.android.hubroid.adapters.CommitListAdapter;
import org.idlesoft.android.hubroid.utils.GravatarCache;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CommitChangeViewer extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public ArrayList<String> mBranches;

    public ArrayAdapter<String> mBranchesAdapter;

    public JSONArray mCommitData;

    public CommitListAdapter mCommitListAdapter;

    private SharedPreferences.Editor mEditor;

    private String mId;

    public Intent mIntent;

    public int mPosition;

    private SharedPreferences mPrefs;

    public String mRepoName;

    public String mRepoOwner;

    private String mPassword;

    private String mUsername;

    public String getHumanDate(final Date current_time, final Date commit_time) {
        String end;
        final long ms = current_time.getTime() - commit_time.getTime();
        final long sec = ms / 1000;
        final long min = sec / 60;
        final long hour = min / 60;
        final long day = hour / 24;
        if (day > 0) {
            if (day == 1) {
                end = " day ago";
            } else {
                end = " days ago";
            }
            return day + end;
        } else if (hour > 0) {
            if (hour == 1) {
                end = " hour ago";
            } else {
                end = " hours ago";
            }
            return hour + end;
        } else if (min > 0) {
            if (min == 1) {
                end = " minute ago";
            } else {
                end = " minutes ago";
            }
            return min + end;
        } else {
            if (sec == 1) {
                end = " second ago";
            } else {
                end = " seconds ago";
            }
            return sec + end;
        }
    }

    /**
     * Get the Gravatars of all users in the commit log
     */
    public Bitmap loadGravatarByLoginName(final String login) {
        if (!login.equals("")) {
            return GravatarCache.getDipGravatar(GravatarCache.getGravatarID(login), 30.0f,
                    getResources().getDisplayMetrics().density);
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit_view);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("login", "");
        mPassword = mPrefs.getString("password", "");
        final View header = getLayoutInflater().inflate(R.layout.commit_view_header, null);

        mGapi.authenticate(mUsername, mPassword);

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Commit Viewer");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepoName = extras.getString("repo_name");
            mRepoOwner = extras.getString("username");
            mId = extras.getString("id");

            // Get the commit data for that commit ID so that we can get the
            // tree ID and filename.
            try {
                final Response commitInfo = mGapi.commits.commit(mRepoOwner, mRepoName, mId);
                final JSONObject commitJSON = new JSONObject(commitInfo.resp)
                        .getJSONObject("commit");

                // Display the committer and author
                final JSONObject authorInfo = commitJSON.getJSONObject("author");
                final String authorName = authorInfo.getString("login");

                final Bitmap authorGravatar = loadGravatarByLoginName(authorName);

                final JSONObject committerInfo = commitJSON.getJSONObject("committer");
                final String committerName = committerInfo.getString("login");

                // If the committer is the author then just show them as the
                // author, otherwise show
                // both people
                ((TextView) header.findViewById(R.id.commit_view_author_name)).setText(authorName);

                if (authorGravatar != null) {
                    ((ImageView) header.findViewById(R.id.commit_view_author_gravatar))
                            .setImageBitmap(authorGravatar);
                }

                // Set the commit message
                ((TextView) header.findViewById(R.id.commit_view_message)).setText(commitJSON
                        .getString("message"));

                final SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
                Date commit_time;
                Date current_time;
                String authorDate = "";

                try {
                    commit_time = dateFormat.parse(commitJSON.getString("authored_date"));
                    current_time = dateFormat.parse(dateFormat.format(new Date()));
                    ((TextView) header.findViewById(R.id.commit_view_author_time))
                            .setText(getHumanDate(current_time, commit_time));

                    commit_time = dateFormat.parse(commitJSON.getString("committed_date"));
                    authorDate = getHumanDate(current_time, commit_time);

                } catch (final ParseException e) {
                    e.printStackTrace();
                }

                if (!authorName.equals(committerName)) {
                    // They are not the same person, make the author visible and
                    // fill in the details
                    ((LinearLayout) header.findViewById(R.id.commit_view_author_layout))
                            .setVisibility(View.VISIBLE);
                    ((TextView) header.findViewById(R.id.commit_view_committer_name))
                            .setText(committerName);
                    ((TextView) header.findViewById(R.id.commit_view_committer_time))
                            .setText(authorDate);
                    final Bitmap committerGravatar = loadGravatarByLoginName(committerName);
                    if (committerGravatar != null) {
                        ((ImageView) header.findViewById(R.id.commit_view_committer_gravatar))
                                .setImageBitmap(committerGravatar);
                    }
                }

                // Populate the ListView with the files that have changed
                final JSONArray changesJSON = commitJSON.getJSONArray("modified");
                final CommitChangeViewerDiffAdapter diffs = new CommitChangeViewerDiffAdapter(
                        getApplicationContext(), changesJSON);
                final ListView diffList = (ListView) findViewById(R.id.commit_view_diffs_list);
                diffList.addHeaderView(header);
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

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.CommitChangeViewerDiffAdapter;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class Commit extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public Intent mIntent;

    private JSONObject mJson;

    private Bitmap mAuthorGravatar;

    private Bitmap mCommitterGravatar;

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    private String mCommitSha;

    private ProgressDialog mProgressDialog;

    private ScrollView mScrollView;

    private String mAuthor;

    private String mCommitter;

    private static class GetCommitTask extends AsyncTask<Void, Void, Void> {
        public Commit activity;

        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please wait...", "Loading commit...", true);
        }

        protected Void doInBackground(Void... params) {
            try {
                final Response commitResponse = activity.mGapi.commits.commit(activity.mRepositoryOwner, activity.mRepositoryName, activity.mCommitSha);
                if (commitResponse.statusCode != 200) {
                    /* Oh noez, something went wrong...
                     * TODO: Do some failure handling here
                     */
                    return null;
                }
                activity.mJson = new JSONObject(commitResponse.resp);
                activity.mAuthorGravatar = Commit.loadGravatarByLoginName(activity, activity.mAuthor);
                activity.mCommitterGravatar = Commit.loadGravatarByLoginName(activity, activity.mCommitter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            buildUi();
            activity.mScrollView.setVisibility(View.VISIBLE);
            activity.mProgressDialog.dismiss();
        }

        protected void buildUi() {
            // Get the commit data for that commit ID so that we can get the
            // tree ID and filename.
            try {
                // Display the committer and author
                final JSONObject authorInfo = activity.mJson.getJSONObject("author");
                final String authorName = authorInfo.getString("name");

                final JSONObject committerInfo = activity.mJson.getJSONObject("committer");
                final String committerName = committerInfo.getString("name");

                // If the committer is the author then just show them as the
                // author, otherwise show
                // both people
                ((TextView) activity.findViewById(R.id.commit_view_author_name)).setText(authorName);

                if (authorGravatar != null) {
                    ((ImageView) findViewById(R.id.commit_view_author_gravatar))
                            .setImageBitmap(authorGravatar);
                }

                // Set the commit message
                ((TextView) findViewById(R.id.commit_view_message)).setText(activity.mJson
                        .getString("message"));

                final SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
                Date commit_time;
                Date current_time;
                String authorDate = "";

                try {
                    commit_time = dateFormat.parse(activity.mJson.getString("authored_date"));
                    current_time = dateFormat.parse(dateFormat.format(new Date()));
                    ((TextView) findViewById(R.id.commit_view_author_time))
                            .setText(Commit.getHumanDate(current_time, commit_time));

                    commit_time = dateFormat.parse(activity.mJson.getString("committed_date"));
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
                    final Bitmap committerGravatar = Commit.loadGravatarByLoginName(Commit.this, committerName);
                    if (committerGravatar != null) {
                        ((ImageView) findViewById(R.id.commit_view_committer_gravatar))
                                .setImageBitmap(committerGravatar);
                    }
                }
                diffList.setAdapter(diffs);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getHumanDate(final Date current_time, final Date commit_time) {
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
    public static Bitmap loadGravatarByLoginName(final Activity pActivity, final String pLogin) {
        if (!pLogin.equals("")) {
            return GravatarCache.getDipGravatar(GravatarCache.getGravatarID(pLogin), 30.0f,
                    pActivity.getResources().getDisplayMetrics().density);
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit);
        mScrollView = (ScrollView) findViewById(R.id.sv_commit_scrollView);
        mScrollView.setVisibility(View.INVISIBLE);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mCommitSha = extras.getString("commit_sha");
            mCommitter = extras.getString("committer");
            mAuthor = extras.getString("author");

            

            infoList.setAdapter(new ArrayAdapter<String>(Commit.this, R.layout.branch_info_item, R.id.tv_branchInfoItem_text1, new String[]{"Commit Log", "View Branch's Tree"}));
            infoList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    if (position == 0) {
                        mIntent = new Intent(Commit.this, CommitsList.class);
                        mIntent.putExtra("repo_owner", mRepositoryOwner);
                        mIntent.putExtra("repo_name", mRepositoryName);
                        mIntent.putExtra("branch_name", mBranchName);
                        startActivity(mIntent);
                    } else if (position == 1) {
                        mIntent = new Intent(Commit.this, BranchTree.class);
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

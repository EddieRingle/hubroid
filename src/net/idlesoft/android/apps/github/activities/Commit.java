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
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private String mAuthor;

    private String mCommitter;

    private String mAuthorName;

    private String mCommitterName;

    private GetCommitTask mGetCommitTask;

    private ScrollView mCommitLayout;

    private RelativeLayout mProgressLayout;

    private static class GetCommitTask extends AsyncTask<Void, Void, Void> {
        public Commit activity;

        protected void onPreExecute() {
            activity.mCommitLayout.setVisibility(View.GONE);
            activity.mProgressLayout.setVisibility(View.VISIBLE);
            super.onPreExecute();
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
                activity.mJson = (new JSONObject(commitResponse.resp)).getJSONObject("commit");

                // This Activity has two entry points:
                // * From CommitsList which provides an author and committer in the bundle.
                // * From SingleActivityItem which does not.
                // If the author or committer are null, populate them from the JSON data.
                if(activity.mAuthor == null) {
                    activity.mAuthor = activity.mJson.getJSONObject("author").getString("login");
                }

                if(activity.mCommitter == null) {
                    activity.mCommitter = activity.mJson.getJSONObject("committer").getString("login");
                }

                activity.mAuthorName = activity.mJson.getJSONObject("author").getString("name");
                activity.mCommitterName = activity.mJson.getJSONObject("committer").getString("name");

                activity.mAuthorGravatar = Commit.loadGravatarByLoginName(activity, activity.mAuthor);
                activity.mCommitterGravatar = Commit.loadGravatarByLoginName(activity, activity.mCommitter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.buildUi();
            activity.mProgressLayout.setVisibility(View.GONE);
            activity.mCommitLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void buildUi() {
        // Get the commit data for that commit ID so that we can get the
        // tree ID and filename.
        try {
            final ImageView authorImage = (ImageView) findViewById(R.id.commit_view_author_gravatar);
            final ImageView committerImage = (ImageView) findViewById(R.id.commit_view_committer_gravatar);

            // If the committer is the author then just show them as the
            // author, otherwise show
            // both people
            ((TextView) findViewById(R.id.commit_view_author_name)).setText(mAuthorName);
            if (mAuthorGravatar != null) {
                authorImage.setImageBitmap(mAuthorGravatar);
            } else {
                authorImage.setImageBitmap(Commit.loadGravatarByLoginName(Commit.this, mAuthor));
            }

            // Set the commit message
            ((TextView) findViewById(R.id.commit_view_message)).setText(mJson
                    .getString("message"));

            final SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
            Date commit_time;
            Date current_time;
            String authorDate = "";

            try {
                commit_time = dateFormat.parse(mJson.getString("authored_date"));
                current_time = dateFormat.parse(dateFormat.format(new Date()));
                ((TextView) findViewById(R.id.commit_view_author_time))
                        .setText(Commit.getHumanDate(current_time, commit_time));

                commit_time = dateFormat.parse(mJson.getString("committed_date"));
                authorDate = Commit.getHumanDate(current_time, commit_time);

            } catch (final ParseException e) {
                e.printStackTrace();
            }

            if (!mAuthor.equals(mCommitter)) {
                // They are not the same person, make the author visible and
                // fill in the details
                ((LinearLayout) findViewById(R.id.commit_view_author_layout))
                        .setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.commit_view_committer_name))
                        .setText(mCommitterName);
                ((TextView) findViewById(R.id.commit_view_committer_time))
                        .setText(authorDate);
                if (mCommitterGravatar != null) {
                    committerImage.setImageBitmap(mCommitterGravatar);
                } else {
                    committerImage.setImageBitmap(Commit.loadGravatarByLoginName(Commit.this, mCommitter));
                }
            }

            OnClickListener onGravatarClick = new OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(Commit.this, Profile.class);
                    if (v.getId() == authorImage.getId()) {
                        i.putExtra("username", mAuthor);
                    } else if (v.getId() == committerImage.getId()) {
                        i.putExtra("username", mCommitter);
                    } else {
                        return;
                    }
                    startActivity(i);
                }
            };

            if (mAuthor != null && !mAuthor.equals("")) {
                authorImage.setOnClickListener(onGravatarClick);
            }
            if (mCommitter != null && !mCommitter.equals("")) {
                committerImage.setOnClickListener(onGravatarClick);
            }

            int filesAdded, filesRemoved, filesChanged;

            try {
                filesAdded = mJson.getJSONArray("added").length();
            } catch (JSONException e) {
                filesAdded = 0;
            }
            try {
                filesRemoved = mJson.getJSONArray("removed").length();
            } catch (JSONException e) {
                filesRemoved = 0;
            }
            try {
                filesChanged = mJson.getJSONArray("modified").length();
            } catch (JSONException e) {
                filesChanged = 0;
            }

            final Button filesAddedButton = (Button) findViewById(R.id.btn_commit_addedFiles);
            final Button filesRemovedButton = (Button) findViewById(R.id.btn_commit_removedFiles);
            final Button filesChangedButton = (Button) findViewById(R.id.btn_commit_changedFiles);

            Log.d("debug", filesAdded + " " + filesRemoved + " " + filesChanged);
            if (filesAdded > 0) {
                filesAddedButton.setText(filesAdded + " files added");
            } else {
                filesAddedButton.setVisibility(View.GONE);
            }
            if (filesRemoved > 0) {
                filesRemovedButton.setText(filesRemoved + " files removed");
            } else {
                filesRemovedButton.setVisibility(View.GONE);
            }
            if (filesChanged > 0) {
                filesChangedButton.setText(filesChanged + " files changed");
            } else {
                filesChangedButton.setVisibility(View.GONE);
            }

            filesAddedButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(Commit.this, DiffFilesList.class);
                    i.putExtra("type", "added");
                    i.putExtra("json", mJson.toString());
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    startActivity(i);
                }
            });
            filesRemovedButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(Commit.this, DiffFilesList.class);
                    i.putExtra("type", "removed");
                    i.putExtra("json", mJson.toString());
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    startActivity(i);
                }
            });
            filesChangedButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(Commit.this, DiffFilesList.class);
                    i.putExtra("type", "modified");
                    i.putExtra("json", mJson.toString());
                    i.putExtra("repo_owner", mRepositoryOwner);
                    i.putExtra("repo_name", mRepositoryName);
                    startActivity(i);
                }
            });
        } catch (final JSONException e) {
            e.printStackTrace();
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

    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(Commit.this, Search.class));
            }
        });

        mCommitLayout = (ScrollView) findViewById(R.id.sv_commit_content);
        mProgressLayout = (RelativeLayout) findViewById(R.id.rl_commit_progressLayout);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mCommitSha = extras.getString("commit_sha");
            mCommitter = extras.getString("committer");
            mAuthor = extras.getString("author");
        }
    }

    protected void onResume() {
        mGetCommitTask = (GetCommitTask) getLastNonConfigurationInstance();
        if (mGetCommitTask == null) {
            mGetCommitTask = new GetCommitTask();
        }
        mGetCommitTask.activity = Commit.this;
        if (mGetCommitTask.getStatus() == AsyncTask.Status.PENDING && mJson == null) {
            mGetCommitTask.execute();
        } else {
            mCommitLayout.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    public Object onRetainNonConfigurationInstance() {
        return mGetCommitTask;
    }

    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            mJson = new JSONObject(savedInstanceState.getString("json"));
            if (mJson != null) {
                buildUi();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("json", mJson.toString());
        super.onSaveInstanceState(outState);
    }

}

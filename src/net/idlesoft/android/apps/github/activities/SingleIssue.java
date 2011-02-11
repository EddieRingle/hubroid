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
import net.idlesoft.android.apps.github.adapters.IssueCommentsAdapter;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SingleIssue extends Activity {
    private static class AddCommentTask extends AsyncTask<Void, Void, Integer> {
        public SingleIssue activity;

        @Override
        protected Integer doInBackground(final Void... params) {
            return activity.mGapi.issues
                    .add_comment(activity.mRepositoryOwner, activity.mRepositoryName,
                            activity.mIssueNumber, ((EditText) activity.mCommentArea
                                    .findViewById(R.id.et_issue_comment_area_body)).getText()
                                    .toString()).statusCode;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            ((ProgressBar) activity.mCommentArea.findViewById(R.id.pb_issue_comment_area_progress))
                    .setVisibility(View.GONE);
            ((EditText) activity.mCommentArea.findViewById(R.id.et_issue_comment_area_body))
                    .setText("");
            activity.mCommentsJson = null;
            activity.mGetCommentsTask = new GetCommentsTask();
            activity.mGetCommentsTask.activity = activity;
            activity.mGetCommentsTask.execute();
        }

        @Override
        protected void onPreExecute() {
            ((ProgressBar) activity.mCommentArea.findViewById(R.id.pb_issue_comment_area_progress))
                    .setVisibility(View.VISIBLE);
        }
    }

    private static class CloseIssueTask extends AsyncTask<Void, Void, Integer> {
        public SingleIssue activity;

        @Override
        protected Integer doInBackground(final Void... params) {
            final int statusCode = activity.mGapi.issues.close(activity.mRepositoryOwner,
                    activity.mRepositoryName, activity.mIssueNumber).statusCode;
            return statusCode;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            activity.mProgressDialog.dismiss();
            if (result.intValue() == 200) {
                activity.setResult(1);
                activity.finish();
            } else {
                Toast.makeText(activity, "Error closing issue.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                    "Closing issue...", true);
        }
    }

    private static class GetCommentsTask extends AsyncTask<Void, Void, Void> {
        public SingleIssue activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mCommentsJson == null) {
                try {
                    final Response resp = activity.mGapi.issues.list_comments(
                            activity.mRepositoryOwner, activity.mRepositoryName,
                            activity.mIssueNumber);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    activity.mCommentsJson = (new JSONObject(resp.resp)).getJSONArray("comments");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                activity.mAdapter.loadData(activity.mCommentsJson);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mAdapter.pushData();
            activity.mProgressDialog.dismiss();
            if ((activity.mClickedBtn != null)
                    && (activity.mClickedBtn.getId() == R.id.btn_issue_comment_area_submit_and_close)) {
                activity.mCloseIssueTask = new CloseIssueTask();
                activity.mCloseIssueTask.activity = activity;
                activity.mCloseIssueTask.execute();
                activity.mClickedBtn = null;
            }
        }

        @Override
        protected void onPreExecute() {
            activity.mAdapter.clear();
            activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                    "Gathering comments...", true);
        }
    }

    public static String getTimeSince(final String pTime) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date item_time = dateFormat.parse(pTime);
            final Date current_time = dateFormat.parse(dateFormat.format(new Date()));
            final long ms = current_time.getTime() - item_time.getTime();
            final long sec = ms / 1000;
            final long min = sec / 60;
            final long hour = min / 60;
            final long day = hour / 24;
            final long year = day / 365;
            if (year > 0) {
                if (year == 1) {
                    return "Updated " + year + " year ago";
                } else {
                    return "Updated " + year + " years ago";
                }
            } else if (day > 0) {
                if (day == 1) {
                    return "Updated " + day + " day ago";
                } else {
                    return "Updated " + day + " days ago";
                }
            } else if (hour > 0) {
                if (hour == 1) {
                    return "Updated " + hour + " hour ago";
                } else {
                    return "Updated " + hour + " hours ago";
                }
            } else if (min > 0) {
                if (min == 1) {
                    return "Updated " + min + " minute ago";
                } else {
                    return "Updated " + min + " minutes ago";
                }
            } else {
                if (sec == 1) {
                    return "Updated " + sec + " second ago";
                } else {
                    return "Updated " + sec + " seconds ago";
                }
            }
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IssueCommentsAdapter mAdapter;

    private AddCommentTask mAddCommentTask;

    private View mClickedBtn;

    private CloseIssueTask mCloseIssueTask;

    private View mCommentArea;

    private JSONArray mCommentsJson;

    private final GitHubAPI mGapi = new GitHubAPI();

    private GetCommentsTask mGetCommentsTask;

    private View mHeader;

    public Intent mIntent;

    private int mIssueNumber;

    private JSONObject mJson;

    private ListView mListView;

    private final OnClickListener mOnSubmitClickListener = new OnClickListener() {
        public void onClick(final View v) {
            final String comment_body = ((TextView) mCommentArea
                    .findViewById(R.id.et_issue_comment_area_body)).getText().toString();
            if (!comment_body.equals("")) {
                mClickedBtn = v;
                if ((mAddCommentTask == null)
                        || (mAddCommentTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    mAddCommentTask = new AddCommentTask();
                }
                mAddCommentTask.activity = SingleIssue.this;
                mAddCommentTask.execute();
            }
        }
    };

    private String mPassword;

    private SharedPreferences mPrefs;

    private ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private String mUsername;

    private void loadIssueItemBox() {
        final TextView date = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_updated_date);
        final ImageView icon = (ImageView) mHeader.findViewById(R.id.iv_issue_list_item_icon);
        final TextView title = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_title);
        final TextView number = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_number);

        final TextView topbar = (TextView) findViewById(R.id.tv_page_title);

        try {
            date.setText(getTimeSince(mJson.getString("updated_at")));
            if (mJson.getString("state").equalsIgnoreCase("open")) {
                icon.setImageResource(R.drawable.issues_open);
            } else {
                icon.setImageResource(R.drawable.issues_closed);
            }
            number.setText("#" + mJson.getString("number"));
            title.setText(mJson.getString("title"));
            topbar.setText("Issue " + number.getText().toString());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.single_issue);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mListView = (ListView) findViewById(R.id.lv_single_issue_comments);

        mAdapter = new IssueCommentsAdapter(SingleIssue.this, mListView);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(SingleIssue.this, Search.class));
            }
        });

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                mRepositoryOwner = extras.getString("repo_owner");
                mRepositoryName = extras.getString("repo_name");
                mJson = new JSONObject(extras.getString("json"));

                mIssueNumber = mJson.getInt("number");

                mHeader = getLayoutInflater().inflate(R.layout.issue_header, null);
                loadIssueItemBox();
                ((ListView) findViewById(R.id.lv_single_issue_comments)).addHeaderView(mHeader);
                ((ImageView) mHeader.findViewById(R.id.iv_single_issue_gravatar))
                        .setImageBitmap(GravatarCache.getDipGravatar(GravatarCache
                                .getGravatarID(mJson.getString("user")), 30.0f, getResources()
                                .getDisplayMetrics().density));
                ((TextView) mHeader.findViewById(R.id.tv_single_issue_body)).setText(mJson
                        .getString("body").replaceAll("\r\n", "\n").replaceAll("\r", "\n"));

                ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                        + getTimeSince(mJson.getString("created_at") + " by "
                                + mJson.getString("user")));

                mCommentArea = getLayoutInflater().inflate(R.layout.issue_comment_area, null);
                ((ListView) findViewById(R.id.lv_single_issue_comments))
                        .addFooterView(mCommentArea);
                ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit))
                        .setOnClickListener(mOnSubmitClickListener);
                ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit_and_close))
                        .setOnClickListener(mOnSubmitClickListener);

                if (mJson.getString("state").equals("closed")) {
                    ((Button) mCommentArea
                            .findViewById(R.id.btn_issue_comment_area_submit_and_close))
                            .setVisibility(View.GONE);
                }

                mGetCommentsTask = (GetCommentsTask) getLastNonConfigurationInstance();
                if (mGetCommentsTask == null) {
                    mGetCommentsTask = new GetCommentsTask();
                }
                mGetCommentsTask.activity = SingleIssue.this;

                if (mGetCommentsTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mProgressDialog = ProgressDialog.show(SingleIssue.this, "Please Wait...",
                            "Gathering comments...", true);
                }

                if ((mGetCommentsTask.getStatus() == AsyncTask.Status.PENDING)
                        && (mCommentsJson == null)) {
                    mGetCommentsTask.execute();
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("commentText")) {
            ((EditText) mCommentArea.findViewById(R.id.et_issue_comment_area_body))
                    .setText(savedInstanceState.getString("commentText"));
        }
        try {
            if (savedInstanceState.containsKey("commentsJson")) {
                mCommentsJson = new JSONArray(savedInstanceState.getString("commentsJson"));
            } else {
                return;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mCommentsJson != null) {
            mAdapter.loadData(mCommentsJson);
            mAdapter.pushData();
        }
    }

    @Override
    protected void onResume() {
        mListView.setAdapter(mAdapter);
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetCommentsTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("commentText", ((EditText) mCommentArea
                .findViewById(R.id.et_issue_comment_area_body)).getText().toString());
        if (mCommentsJson != null) {
            savedInstanceState.putString("commentsJson", mCommentsJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
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

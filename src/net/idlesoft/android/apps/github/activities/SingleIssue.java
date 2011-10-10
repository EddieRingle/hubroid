/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.IssueCommentsAdapter;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class SingleIssue extends BaseActivity {
    private Issue mIssue;

    private ArrayList<Comment> mComments;

    private static class AddCommentTask extends AsyncTask<Void, Void, Integer> {
        public SingleIssue activity;

        @Override
        protected Integer doInBackground(final Void... params) {
            String commentBody = ((EditText) activity.mCommentArea
                    .findViewById(R.id.et_issue_comment_area_body)).getText().toString();
            if (activity.mPrefs.getBoolean(
                    activity.getString(R.string.preferences_key_issue_comment_signature), true)) {
                commentBody += "\n\n_Sent via Hubroid_";
            }
            final IssueService is = new IssueService(activity.getGitHubClient());
            try {
                is.createComment(activity.mRepositoryOwner, activity.mRepositoryName,
                        Integer.toString(activity.mIssue.getNumber()), commentBody);
                return 200;
            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof RequestException) {
                    return ((RequestException) e).getStatus();
                } else {
                    return -1;
                }
            }
        }

        @Override
        protected void onPostExecute(final Integer result) {
            ((ProgressBar) activity.mCommentArea.findViewById(R.id.pb_issue_comment_area_progress))
                    .setVisibility(View.GONE);
            ((EditText) activity.mCommentArea.findViewById(R.id.et_issue_comment_area_body))
                    .setText("");
            activity.mLoadIssueTask = new LoadIssueTask();
            activity.mLoadIssueTask.activity = activity;
            activity.mLoadIssueTask.execute();
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
            final IssueService is = new IssueService(activity.getGitHubClient());
            try {
                is.editIssue(activity.mRepositoryOwner, activity.mRepositoryName,
                        activity.mIssue.setState(STATE_CLOSED));
                return 200;
            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof RequestException) {
                    return ((RequestException) e).getStatus();
                } else {
                    return -1;
                }
            }
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

    private static class LoadIssueTask extends AsyncTask<Void, Void, Void> {
        public SingleIssue activity;

        @Override
        protected Void doInBackground(final Void... params) {
            final IssueService is = new IssueService(activity.getGitHubClient());
            try {
                activity.mIssue = is.getIssue(activity.mRepositoryOwner, activity.mRepositoryName,
                        Integer.toString(activity.mIssue.getNumber()));
                if (activity.mIssue != null) {
                    activity.mComments = new ArrayList<Comment>(is.getComments(
                            activity.mRepositoryOwner, activity.mRepositoryName,
                            Integer.toString(activity.mIssue.getNumber())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (activity.mComments == null) {
                activity.mComments = new ArrayList<Comment>();
            }
            activity.mAdapter.loadData(activity.mComments);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.fillViewInfo();
            activity.mAdapter.pushData();
            ((LinearLayout) activity.findViewById(R.id.ll_single_issue_progress))
                    .setVisibility(View.GONE);
            ((LinearLayout) activity.findViewById(R.id.ll_single_issue_content))
                    .setVisibility(View.VISIBLE);
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
            ((LinearLayout) activity.findViewById(R.id.ll_single_issue_progress))
                    .setVisibility(View.VISIBLE);
            ((LinearLayout) activity.findViewById(R.id.ll_single_issue_content))
                    .setVisibility(View.GONE);
            activity.mAdapter.clear();
        }
    }

    public static String getTimeSince(final Date pTime) {
        final Date item_time = pTime;
        final Date current_time = new Date();
        final long ms = current_time.getTime() - item_time.getTime();
        final long sec = ms / 1000;
        final long min = sec / 60;
        final long hour = min / 60;
        final long day = hour / 24;
        final long year = day / 365;
        if (year > 0) {
            if (year == 1) {
                return year + " year";
            } else {
                return year + " years";
            }
        } else if (day > 0) {
            if (day == 1) {
                return day + " day";
            } else {
                return day + " days";
            }
        } else if (hour > 0) {
            if (hour == 1) {
                return hour + " hour";
            } else {
                return hour + " hours";
            }
        } else if (min > 0) {
            if (min == 1) {
                return min + " minute";
            } else {
                return min + " minutes";
            }
        } else {
            if (sec == 1) {
                return sec + " second";
            } else {
                return sec + " seconds";
            }
        }
    }

    private IssueCommentsAdapter mAdapter;

    private AddCommentTask mAddCommentTask;

    private View mClickedBtn;

    private CloseIssueTask mCloseIssueTask;

    private View mCommentArea;

    private LoadIssueTask mLoadIssueTask;

    private View mHeader;

    public Intent mIntent;

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

    private ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private void loadIssueItemBox() {
        final TextView date = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_updated_date);
        final ImageView icon = (ImageView) mHeader.findViewById(R.id.iv_issue_list_item_icon);
        final TextView title = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_title);
        final TextView number = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_number);

        try {
            date.setText("Updated " + getTimeSince(mIssue.getUpdatedAt()) + " ago");
            if (mIssue.getState().equalsIgnoreCase(STATE_OPEN)) {
                icon.setImageResource(R.drawable.issues_open);
            } else {
                icon.setImageResource(R.drawable.issues_closed);
            }
            number.setText("#" + mIssue.getNumber());
            title.setText(mIssue.getTitle());
            getActionBar().setTitle("Issue " + number.getText().toString());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void loadView() {
        mHeader = getLayoutInflater().inflate(R.layout.issue_header, null);
        ((ListView) findViewById(R.id.lv_single_issue_comments)).addHeaderView(mHeader);

        mCommentArea = getLayoutInflater().inflate(R.layout.issue_comment_area, null);
        ((ListView) findViewById(R.id.lv_single_issue_comments)).addFooterView(mCommentArea);
        ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit))
                .setOnClickListener(mOnSubmitClickListener);
        ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit_and_close))
                .setOnClickListener(mOnSubmitClickListener);
    }

    public void fillViewInfo() {
        loadIssueItemBox();

        ((ImageView) mHeader.findViewById(R.id.iv_single_issue_gravatar))
                .setImageBitmap(GravatarCache.getDipGravatar(mIssue.getUser().getLogin(), 30.0f,
                        getResources().getDisplayMetrics().density));
        ((TextView) mHeader.findViewById(R.id.tv_single_issue_body)).setText(mIssue.getBody()
                .replaceAll("\r\n", "\n").replaceAll("\r", "\n"));

        ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                + getTimeSince(mIssue.getCreatedAt()) + " by " + mIssue.getUser().getLogin());

        if (mIssue.getState().equalsIgnoreCase(STATE_CLOSED)) {
            ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit_and_close))
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.single_issue);

        mListView = (ListView) findViewById(R.id.lv_single_issue_comments);

        mAdapter = new IssueCommentsAdapter(SingleIssue.this, mListView);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryOwner = extras.getString("repo_owner");
            mRepositoryName = extras.getString("repo_name");
            if (extras.containsKey("json")) {
                mIssue = GsonUtils.fromJson(extras.getString("json"), Issue.class);
            } else if (extras.containsKey("number")) {
                mIssue = new Issue();
                mIssue.setNumber(extras.getInt("number"));
            }

            loadView();

            mLoadIssueTask = (LoadIssueTask) getLastNonConfigurationInstance();
            if (mLoadIssueTask == null) {
                mLoadIssueTask = new LoadIssueTask();
            }
            mLoadIssueTask.activity = SingleIssue.this;

            if ((mLoadIssueTask.getStatus() == AsyncTask.Status.PENDING)) {
                mLoadIssueTask.execute();
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
    }

    @Override
    protected void onResume() {
        mListView.setAdapter(mAdapter);
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadIssueTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("commentText", ((EditText) mCommentArea
                .findViewById(R.id.et_issue_comment_area_body)).getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }
}

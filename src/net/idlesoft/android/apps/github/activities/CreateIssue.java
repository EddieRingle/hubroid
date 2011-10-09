/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CreateIssue extends BaseActivity {
    private static class CreateIssueTask extends AsyncTask<Void, Void, Integer> {
        public CreateIssue activity;

        @Override
        protected Integer doInBackground(final Void... params) {
            final String title = ((TextView) activity.findViewById(R.id.et_create_issue_title))
                    .getText().toString();
            String body = ((TextView) activity.findViewById(R.id.et_create_issue_body)).getText()
                    .toString();
            if (!title.equals("") && !body.equals("")) {
                if (activity.mPrefs.getBoolean(
                        activity.getString(R.string.preferences_key_issue_signature), true)) {
                    body += "\n\n_Sent via Hubroid_";
                }
                final IssueService is = new IssueService(activity.getGitHubClient());
                final Issue newIssue = new Issue();
                newIssue.setTitle(title);
                newIssue.setBody(body);
                try {
                    Issue result = is.createIssue(activity.mRepositoryOwner,
                            activity.mRepositoryName, newIssue);
                    activity.mIssueJson = GsonUtils.toJson(result);
                    return 201;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof RequestException) {
                        return ((RequestException) e).getStatus();
                    } else {
                        return -2;
                    }
                }
            } else {
                return -1;
            }
        }

        @Override
        protected void onPostExecute(final Integer result) {
            activity.mProgressDialog.dismiss();

            if (result.intValue() == 201 && activity.mIssueJson != null) {
                final Intent i = new Intent(activity, SingleIssue.class);
                i.putExtra("repo_owner", activity.mRepositoryOwner);
                i.putExtra("repo_name", activity.mRepositoryName);
                i.putExtra("json", activity.mIssueJson);

                activity.startActivity(i);
                activity.setResult(0);
                activity.finish();
            } else if (result.intValue() == -1) {
                Toast.makeText(activity, "Title and body fields cannot be blank.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Error creating issue. Error " + result.intValue(),
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                    "Creating issue...");
        }
    }

    private CreateIssueTask mCreateIssueTask;

    private String mIssueJson;

    private ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.create_issue);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("repo_owner")) {
                mRepositoryOwner = extras.getString("repo_owner");
            } else {
                mRepositoryOwner = mUsername;
            }
            if (extras.containsKey("repo_name")) {
                mRepositoryName = extras.getString("repo_name");
            }
        } else {
            mRepositoryOwner = mUsername;
        }

        mCreateIssueTask = (CreateIssueTask) getLastNonConfigurationInstance();
        if (mCreateIssueTask == null) {
            mCreateIssueTask = new CreateIssueTask();
        }
        mCreateIssueTask.activity = this;

        if (mCreateIssueTask.getStatus() == AsyncTask.Status.RUNNING) {
            mProgressDialog = ProgressDialog.show(CreateIssue.this, "Please Wait...",
                    "Creating issue...", true);
        }

        getActionBar().setTitle("New Issue");

        ((Button) findViewById(R.id.btn_create_issue_submit))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        if (mCreateIssueTask.getStatus() == AsyncTask.Status.FINISHED) {
                            mCreateIssueTask = new CreateIssueTask();
                            mCreateIssueTask.activity = CreateIssue.this;
                        }
                        if (mCreateIssueTask.getStatus() == AsyncTask.Status.PENDING) {
                            mCreateIssueTask.execute();
                        }
                    }
                });
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
        if (savedInstanceState.containsKey("titleText")) {
            ((EditText) findViewById(R.id.et_create_issue_title)).setText(savedInstanceState
                    .getString("titleText"));
        }
        if (savedInstanceState.containsKey("bodyText")) {
            ((EditText) findViewById(R.id.et_create_issue_body)).setText(savedInstanceState
                    .getString("bodyText"));
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mCreateIssueTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("titleText",
                ((EditText) findViewById(R.id.et_create_issue_title)).getText().toString());
        savedInstanceState.putString("bodyText",
                ((EditText) findViewById(R.id.et_create_issue_body)).getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }
}

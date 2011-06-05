/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.BaseActivity;
import net.idlesoft.android.apps.github.activities.Issues;
import net.idlesoft.android.apps.github.activities.SingleIssue;
import net.idlesoft.android.apps.github.adapters.IssuesListAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ClosedIssues extends BaseActivity {
    private static final int TASK_LOAD_ISSUES = 1;

    private static final int TASK_REOPEN_ISSUE = 2;

    private static class ClosedIssuesTask extends AsyncTask<Void, Void, Integer> {
        public int taskId = 0;

        public ClosedIssues activity;

        @Override
        protected Integer doInBackground(final Void... params) {
            switch (taskId) {
                case TASK_LOAD_ISSUES:
                    if (activity.mJson == null) {
                        try {
                            final Response resp = activity.mGApi.issues.list(
                                    activity.mRepositoryOwner, activity.mRepositoryName, "closed");
                            if (resp.statusCode != 200) {
                                /* Oh noez, something went wrong */
                                return resp.statusCode;
                            }
                            activity.mJson = (new JSONObject(resp.resp)).getJSONArray("issues");
                            activity.mAdapter.loadData(activity.mJson);
                            return resp.statusCode;
                        } catch (final JSONException e) {
                            e.printStackTrace();
                            return -1;
                        }
                    }
                    break;
                case TASK_REOPEN_ISSUE:
                    final int statusCode = activity.mGApi.issues.reopen(activity.mRepositoryOwner,
                            activity.mRepositoryName, activity.mIssueNumber).statusCode;
                    return statusCode;
                default:
                    return 0;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            switch (taskId) {
                case TASK_LOAD_ISSUES:
                    activity.mAdapter.pushData();
                    activity.mAdapter.setIsLoadingData(false);
                    break;
                case TASK_REOPEN_ISSUE:
                    activity.mProgressDialog.dismiss();
                    if (result.intValue() == 200) {
                        final Intent reloadIssuesIntent = new Intent(activity, Issues.class);
                        reloadIssuesIntent.putExtra("repo_owner", activity.mRepositoryOwner);
                        reloadIssuesIntent.putExtra("repo_name", activity.mRepositoryName);
                        activity.startActivity(reloadIssuesIntent);
                        activity.finish();
                    } else {
                        Toast.makeText(
                                activity,
                                "Error reopening issue " + activity.mIssueNumber + ": "
                                        + result.intValue(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPreExecute() {
            switch (taskId) {
                case TASK_LOAD_ISSUES:
                    activity.mAdapter.setIsLoadingData(true);
                    break;
                case TASK_REOPEN_ISSUE:
                    activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                            "Reopening issue...", true);
                    break;
                default:
                    break;
            }
        }
    }

    private int mIssueNumber;

    private ProgressDialog mProgressDialog;

    private IssuesListAdapter mAdapter;

    private JSONArray mJson;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleIssue.class);
            i.putExtra("repo_owner", mRepositoryOwner);
            i.putExtra("repo_name", mRepositoryName);
            try {
                i.putExtra("json", mJson.getJSONObject(position).toString());
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            startActivityForResult(i, 1);
            return;
        }
    };

    private String mRepositoryName;

    private String mRepositoryOwner;

    private ClosedIssuesTask mTask;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == 1) {
            mAdapter.clear();
            mJson = null;
            mTask = new ClosedIssuesTask();
            mTask.activity = ClosedIssues.this;
            mTask.taskId = TASK_LOAD_ISSUES;
            mTask.execute();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (!menu.hasVisibleItems()) {
            menu.add(0, Issues.CONTEXT_MENU_DETAILS, 0, "Details");
            menu.add(0, Issues.CONTEXT_MENU_REOPEN, 1, "Reopen");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = ((AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo());

        try {
            mIssueNumber = mJson.getJSONObject(info.position).getInt("number");
        } catch (JSONException e) {
            mIssueNumber = -1;
            e.printStackTrace();
        }
        switch (item.getItemId()) {
            case Issues.CONTEXT_MENU_DETAILS:
                mListView.performItemClick(info.targetView, info.position, info.id);
                break;
            case Issues.CONTEXT_MENU_REOPEN:
                mTask = new ClosedIssuesTask();
                mTask.activity = ClosedIssues.this;
                mTask.taskId = TASK_REOPEN_ISSUE;
                mTask.execute();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);
        registerForContextMenu(mListView);

        setContentView(mListView);

        mAdapter = new IssuesListAdapter(ClosedIssues.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryOwner = extras.getString("repo_owner");
            mRepositoryName = extras.getString("repo_name");
        }

        mTask = (ClosedIssuesTask) getLastNonConfigurationInstance();
        if (mTask == null) {
            mTask = new ClosedIssuesTask();
        }
        mTask.activity = ClosedIssues.this;
        mTask.taskId = TASK_LOAD_ISSUES;
        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        } else if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
            if (mTask.taskId == TASK_REOPEN_ISSUE) {
                mProgressDialog = ProgressDialog.show(ClosedIssues.this, "Please wait...",
                        "Reopening issue...");
            }
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                return;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mJson != null) {
            mAdapter.loadData(mJson);
            mAdapter.pushData();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}

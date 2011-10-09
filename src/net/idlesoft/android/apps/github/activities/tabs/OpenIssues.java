/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import static org.eclipse.egit.github.core.service.IssueService.FILTER_STATE;
import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.BaseActivity;
import net.idlesoft.android.apps.github.activities.Issues;
import net.idlesoft.android.apps.github.activities.SingleIssue;
import net.idlesoft.android.apps.github.adapters.IssuesListAdapter;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;

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

public class OpenIssues extends BaseActivity {
    private static final int TASK_LOAD_ISSUES = 1;

    private static final int TASK_CLOSE_ISSUE = 2;

    private ArrayList<Issue> mIssues;

    private static class OpenIssuesTask extends AsyncTask<Issue, Void, Integer> {
        public int taskId = 0;

        public OpenIssues activity;

        @Override
        protected Integer doInBackground(final Issue... params) {
        	final IssueService is = new IssueService(activity.getGitHubClient());
            switch (taskId) {
                case TASK_LOAD_ISSUES:
                	try {
	                	activity.mIssues = new ArrayList<Issue>(is.getIssues(
	                			activity.mRepositoryOwner, activity.mRepositoryName,
	                			Collections.singletonMap(FILTER_STATE, STATE_OPEN)));
                	} catch (IOException e) {
                		e.printStackTrace();
                		return ((RequestException) e).getStatus();
                	}
                	if (activity.mIssues == null) {
                		activity.mIssues = new ArrayList<Issue>();
                	}
                	activity.mAdapter.loadData(activity.mIssues);
                	return 200;
                case TASK_CLOSE_ISSUE:
                	try {
                		is.editIssue(activity.mRepositoryOwner, activity.mRepositoryName,
                				params[0].setState(STATE_CLOSED));
                		return 200;
                	} catch (IOException e) {
                		e.printStackTrace();
                		if (e instanceof RequestException) {
                			return ((RequestException) e).getStatus();
                		} else {
                			return -1;
                		}
                	}
                default:
                    return 0;
            }
        }

        @Override
        protected void onPostExecute(final Integer result) {
            switch (taskId) {
                case TASK_LOAD_ISSUES:
                    activity.mAdapter.pushData();
                    activity.mAdapter.setIsLoadingData(false);
                    break;
                case TASK_CLOSE_ISSUE:
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
                                "Error closing issue: "
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
                case TASK_CLOSE_ISSUE:
                    activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                            "Closing issue...", true);
                    break;
                default:
                    break;
            }
        }
    }

    private ProgressDialog mProgressDialog;

    private IssuesListAdapter mAdapter;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleIssue.class);
            i.putExtra("repo_owner", mRepositoryOwner);
            i.putExtra("repo_name", mRepositoryName);
            i.putExtra("json", GsonUtils.toJson(mIssues.get(position)));
            startActivityForResult(i, 1);
            return;
        }
    };

    private String mRepositoryName;

    private String mRepositoryOwner;

    private OpenIssuesTask mTask;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == 1) {
            mAdapter.clear();
            mTask = new OpenIssuesTask();
            mTask.activity = OpenIssues.this;
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
            menu.add(0, Issues.CONTEXT_MENU_CLOSE, 1, "Close");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = ((AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo());

        switch (item.getItemId()) {
            case Issues.CONTEXT_MENU_DETAILS:
                mListView.performItemClick(info.targetView, info.position, info.id);
                break;
            case Issues.CONTEXT_MENU_CLOSE:
                mTask = new OpenIssuesTask();
                mTask.activity = OpenIssues.this;
                mTask.taskId = TASK_CLOSE_ISSUE;
                mTask.execute(mIssues.get(info.position));
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

        mAdapter = new IssuesListAdapter(OpenIssues.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryOwner = extras.getString("repo_owner");
            mRepositoryName = extras.getString("repo_name");
        }

        mTask = (OpenIssuesTask) getLastNonConfigurationInstance();
        if (mTask == null) {
            mTask = new OpenIssuesTask();
        }
        mTask.activity = OpenIssues.this;
        mTask.taskId = TASK_LOAD_ISSUES;
        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        } else if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
            if (mTask.taskId == TASK_CLOSE_ISSUE) {
                mProgressDialog = ProgressDialog.show(OpenIssues.this, "Please wait...",
                        "Closing issue...");
            }
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }
}

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.IssueCommentsAdapter;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class SingleIssue extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    private IssueCommentsAdapter mAdapter;

    private View mClickedBtn;

    private View mCommentArea;

    private SharedPreferences.Editor mEditor;

    private View mHeader;

    public Intent mIntent;

    private JSONObject mJson = new JSONObject();

    private final OnClickListener mOnSubmitClickListener = new OnClickListener() {
        public void onClick(final View v) {
            mClickedBtn = v;
            final String comment_body = ((TextView) mCommentArea
                    .findViewById(R.id.et_issue_comment_area_body)).getText().toString();
            if (!comment_body.equals("")) {
                ((ProgressBar) mCommentArea.findViewById(R.id.pb_issue_comment_area_progress))
                        .setVisibility(View.VISIBLE);
                mThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            if (mGapi.issues.add_comment(mRepositoryOwner, mRepositoryName, mJson
                                    .getInt("number"), ((TextView) mCommentArea
                                    .findViewById(R.id.et_issue_comment_area_body)).getText()
                                    .toString()).statusCode == 200) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ((ProgressBar) mCommentArea
                                                .findViewById(R.id.pb_issue_comment_area_progress))
                                                .setVisibility(View.GONE);
                                        mProgressDialog = ProgressDialog.show(SingleIssue.this,
                                                "Please wait...", "Refreshing Comments...", true);
                                    }
                                });
                                if (mClickedBtn.getId() == R.id.btn_issue_comment_area_submit_and_close) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            mProgressDialog.setMessage("Closing issue...");
                                        }
                                    });
                                    final int statusCode = mGapi.issues.close(mRepositoryOwner,
                                            mRepositoryName, mJson.getInt("number")).statusCode;
                                    if (statusCode == 200) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                mProgressDialog.setMessage("Refreshing Issue...");
                                            }
                                        });
                                        mJson = new JSONObject(mGapi.issues.issue(mRepositoryOwner,
                                                mRepositoryName, mJson.getInt("number")).resp)
                                                .getJSONObject("issue");
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                loadIssueItemBox();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(SingleIssue.this,
                                                        "Error closing issue.", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });
                                    }
                                }
                                final Response response = mGapi.issues.list_comments(mRepositoryOwner,
                                        mRepositoryName, mJson.getInt("number"));
                                mAdapter = new IssueCommentsAdapter(getApplicationContext(),
                                        new JSONObject(response.resp).getJSONArray("comments"));
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ((TextView) mCommentArea
                                                .findViewById(R.id.et_issue_comment_area_body))
                                                .setText("");
                                        ((ListView) findViewById(R.id.lv_single_issue_comments))
                                                .setAdapter(mAdapter);
                                        mProgressDialog.dismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Error posting comment.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (final JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mThread.start();
            }
        }
    };

    private SharedPreferences mPrefs;

    public ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private Thread mThread;

    private String mPassword;

    private String mUsername;

    private void loadIssueItemBox() {
        final TextView date = (TextView) mHeader
                .findViewById(R.id.tv_issue_list_item_updated_date);
        final ImageView icon = (ImageView) mHeader.findViewById(R.id.iv_issue_list_item_icon);
        final TextView title = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_title);
        final TextView number = (TextView) mHeader.findViewById(R.id.tv_issue_list_item_number);

        final TextView topbar = (TextView) findViewById(R.id.tv_page_title);

        try {
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date item_time = dateFormat.parse(mJson.getString("updated_at"));
            final Date current_time = dateFormat.parse(dateFormat.format(new Date()));
            final long ms = current_time.getTime() - item_time.getTime();
            final long sec = ms / 1000;
            final long min = sec / 60;
            final long hour = min / 60;
            final long day = hour / 24;
            final long year = day / 365;
            if (year > 0) {
                if (year == 1) {
                    end = " year ago";
                } else {
                    end = " years ago";
                }
                date.setText("Updated " + year + end);
            } else if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                date.setText("Updated " + day + end);
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                date.setText("Updated " + hour + end);
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                date.setText("Updated " + min + end);
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                date.setText("Updated " + sec + end);
            }
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
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                mRepositoryOwner = extras.getString("repoOwner");
                mRepositoryName = extras.getString("repoName");
                mJson = new JSONObject(extras.getString("item_json"));

                mHeader = getLayoutInflater().inflate(R.layout.issue_header, null);
                loadIssueItemBox();
                ((ListView) findViewById(R.id.lv_single_issue_comments)).addHeaderView(mHeader);
                ((ImageView) mHeader.findViewById(R.id.iv_single_issue_gravatar))
                        .setImageBitmap(GravatarCache.getDipGravatar(GravatarCache
                                .getGravatarID(mJson.getString("user")), 30.0f, getResources()
                                .getDisplayMetrics().density));
                ((TextView) mHeader.findViewById(R.id.tv_single_issue_body)).setText(mJson
                        .getString("body").replaceAll("\r\n", "\n").replaceAll("\r", "\n"));

                String end;
                final SimpleDateFormat dateFormat = new SimpleDateFormat(
                        Hubroid.GITHUB_ISSUES_TIME_FORMAT);
                final Date item_time = dateFormat.parse(mJson.getString("created_at"));
                final Date current_time = dateFormat.parse(dateFormat.format(new Date()));
                final long ms = current_time.getTime() - item_time.getTime();
                final long sec = ms / 1000;
                final long min = sec / 60;
                final long hour = min / 60;
                final long day = hour / 24;
                final long year = day / 365;
                if (year > 0) {
                    if (year == 1) {
                        end = " year ago";
                    } else {
                        end = " years ago";
                    }
                    ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + year + end + " by " + mJson.getString("user"));
                } else if (day > 0) {
                    if (day == 1) {
                        end = " day ago";
                    } else {
                        end = " days ago";
                    }
                    ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + day + end + " by " + mJson.getString("user"));
                } else if (hour > 0) {
                    if (hour == 1) {
                        end = " hour ago";
                    } else {
                        end = " hours ago";
                    }
                    ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + hour + end + " by " + mJson.getString("user"));
                } else if (min > 0) {
                    if (min == 1) {
                        end = " minute ago";
                    } else {
                        end = " minutes ago";
                    }
                    ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + min + end + " by " + mJson.getString("user"));
                } else {
                    if (sec == 1) {
                        end = " second ago";
                    } else {
                        end = " seconds ago";
                    }
                    ((TextView) mHeader.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + sec + end + " by " + mJson.getString("user"));
                }

                mCommentArea = getLayoutInflater().inflate(R.layout.issue_comment_area, null);
                ((ListView) findViewById(R.id.lv_single_issue_comments))
                        .addFooterView(mCommentArea);
                ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit))
                        .setOnClickListener(mOnSubmitClickListener);
                ((Button) mCommentArea.findViewById(R.id.btn_issue_comment_area_submit_and_close))
                        .setOnClickListener(mOnSubmitClickListener);

                mThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            final Response response = mGapi.issues.list_comments(mRepositoryOwner,
                                    mRepositoryName, mJson.getInt("number"));
                            mAdapter = new IssueCommentsAdapter(getApplicationContext(),
                                    new JSONObject(response.resp).getJSONArray("comments"));
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ((ListView) findViewById(R.id.lv_single_issue_comments))
                                            .setAdapter(mAdapter);
                                }
                            });
                        } catch (final JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mThread.start();
            } catch (final JSONException e) {
                e.printStackTrace();
            } catch (final java.text.ParseException e) {
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
        if ((mThread != null) && mThread.isAlive()) {
            mThread.stop();
        }
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Clear Preferences");
        menu.add(0, 2, 0, "Clear Cache");
        return true;
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
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("commentText", ((EditText) mCommentArea
                .findViewById(R.id.et_issue_comment_area_body)).getText().toString());
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

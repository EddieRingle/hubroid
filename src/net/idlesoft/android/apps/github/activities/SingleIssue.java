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

    private IssueCommentsAdapter m_adapter;

    private View m_clickedBtn;

    private View m_commentArea;

    private SharedPreferences.Editor m_editor;

    private View m_header;

    public Intent m_intent;

    private View m_issueBox;

    private JSONObject m_JSON = new JSONObject();

    private final OnClickListener m_onSubmitClickListener = new OnClickListener() {
        public void onClick(final View v) {
            m_clickedBtn = v;
            final String comment_body = ((TextView) m_commentArea
                    .findViewById(R.id.et_issue_comment_area_body)).getText().toString();
            if (!comment_body.equals("")) {
                ((ProgressBar) m_commentArea.findViewById(R.id.pb_issue_comment_area_progress))
                        .setVisibility(View.VISIBLE);
                m_thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            if (mGapi.issues.add_comment(m_repoOwner, m_repoName, m_JSON
                                    .getInt("number"), ((TextView) m_commentArea
                                    .findViewById(R.id.et_issue_comment_area_body)).getText()
                                    .toString()).statusCode == 200) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ((ProgressBar) m_commentArea
                                                .findViewById(R.id.pb_issue_comment_area_progress))
                                                .setVisibility(View.GONE);
                                        m_progressDialog = ProgressDialog.show(SingleIssue.this,
                                                "Please wait...", "Refreshing Comments...", true);
                                    }
                                });
                                if (m_clickedBtn.getId() == R.id.btn_issue_comment_area_submit_and_close) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            m_progressDialog.setMessage("Closing issue...");
                                        }
                                    });
                                    final int statusCode = mGapi.issues.close(m_repoOwner,
                                            m_repoName, m_JSON.getInt("number")).statusCode;
                                    if (statusCode == 200) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                m_progressDialog.setMessage("Refreshing Issue...");
                                            }
                                        });
                                        m_JSON = new JSONObject(mGapi.issues.issue(m_repoOwner,
                                                m_repoName, m_JSON.getInt("number")).resp)
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
                                final Response response = mGapi.issues.list_comments(m_repoOwner,
                                        m_repoName, m_JSON.getInt("number"));
                                m_adapter = new IssueCommentsAdapter(getApplicationContext(),
                                        new JSONObject(response.resp).getJSONArray("comments"));
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ((TextView) m_commentArea
                                                .findViewById(R.id.et_issue_comment_area_body))
                                                .setText("");
                                        ((ListView) findViewById(R.id.lv_single_issue_comments))
                                                .setAdapter(m_adapter);
                                        m_progressDialog.dismiss();
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
                m_thread.start();
            }
        }
    };

    private SharedPreferences m_prefs;

    public ProgressDialog m_progressDialog;

    private String m_repoName;

    private String m_repoOwner;

    private Thread m_thread;

    private String mPassword;

    private String mUsername;

    private void loadIssueItemBox() {
        final TextView date = (TextView) m_header
                .findViewById(R.id.tv_issue_list_item_updated_date);
        final ImageView icon = (ImageView) m_header.findViewById(R.id.iv_issue_list_item_icon);
        final TextView title = (TextView) m_header.findViewById(R.id.tv_issue_list_item_title);
        final TextView number = (TextView) m_header.findViewById(R.id.tv_issue_list_item_number);

        final TextView topbar = (TextView) findViewById(R.id.tv_page_title);

        try {
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date item_time = dateFormat.parse(m_JSON.getString("updated_at"));
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
            if (m_JSON.getString("state").equalsIgnoreCase("open")) {
                icon.setImageResource(R.drawable.issues_open);
            } else {
                icon.setImageResource(R.drawable.issues_closed);
            }
            number.setText("#" + m_JSON.getString("number"));
            title.setText(m_JSON.getString("title"));
            topbar.setText("Issue " + number.getText().toString());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.single_issue);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        mUsername = m_prefs.getString("login", "");
        mPassword = m_prefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                m_repoOwner = extras.getString("repoOwner");
                m_repoName = extras.getString("repoName");
                m_JSON = new JSONObject(extras.getString("item_json"));
                m_issueBox = getLayoutInflater().inflate(R.layout.issue_list_item, null);

                m_header = getLayoutInflater().inflate(R.layout.issue_header, null);
                loadIssueItemBox();
                ((ListView) findViewById(R.id.lv_single_issue_comments)).addHeaderView(m_header);
                ((ImageView) m_header.findViewById(R.id.iv_single_issue_gravatar))
                        .setImageBitmap(GravatarCache.getDipGravatar(GravatarCache
                                .getGravatarID(m_JSON.getString("user")), 30.0f, getResources()
                                .getDisplayMetrics().density));
                ((TextView) m_header.findViewById(R.id.tv_single_issue_body)).setText(m_JSON
                        .getString("body").replaceAll("\r\n", "\n").replaceAll("\r", "\n"));

                String end;
                final SimpleDateFormat dateFormat = new SimpleDateFormat(
                        Hubroid.GITHUB_ISSUES_TIME_FORMAT);
                final Date item_time = dateFormat.parse(m_JSON.getString("created_at"));
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
                    ((TextView) m_header.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + year + end + " by " + m_JSON.getString("user"));
                } else if (day > 0) {
                    if (day == 1) {
                        end = " day ago";
                    } else {
                        end = " days ago";
                    }
                    ((TextView) m_header.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + day + end + " by " + m_JSON.getString("user"));
                } else if (hour > 0) {
                    if (hour == 1) {
                        end = " hour ago";
                    } else {
                        end = " hours ago";
                    }
                    ((TextView) m_header.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + hour + end + " by " + m_JSON.getString("user"));
                } else if (min > 0) {
                    if (min == 1) {
                        end = " minute ago";
                    } else {
                        end = " minutes ago";
                    }
                    ((TextView) m_header.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + min + end + " by " + m_JSON.getString("user"));
                } else {
                    if (sec == 1) {
                        end = " second ago";
                    } else {
                        end = " seconds ago";
                    }
                    ((TextView) m_header.findViewById(R.id.tv_single_issue_meta)).setText("Posted "
                            + sec + end + " by " + m_JSON.getString("user"));
                }

                m_commentArea = getLayoutInflater().inflate(R.layout.issue_comment_area, null);
                ((ListView) findViewById(R.id.lv_single_issue_comments))
                        .addFooterView(m_commentArea);
                ((Button) m_commentArea.findViewById(R.id.btn_issue_comment_area_submit))
                        .setOnClickListener(m_onSubmitClickListener);
                ((Button) m_commentArea.findViewById(R.id.btn_issue_comment_area_submit_and_close))
                        .setOnClickListener(m_onSubmitClickListener);

                m_thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            final Response response = mGapi.issues.list_comments(m_repoOwner,
                                    m_repoName, m_JSON.getInt("number"));
                            m_adapter = new IssueCommentsAdapter(getApplicationContext(),
                                    new JSONObject(response.resp).getJSONArray("comments"));
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ((ListView) findViewById(R.id.lv_single_issue_comments))
                                            .setAdapter(m_adapter);
                                }
                            });
                        } catch (final JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                m_thread.start();
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
                m_editor.clear().commit();
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
        if ((m_thread != null) && m_thread.isAlive()) {
            m_thread.stop();
        }
        if ((m_progressDialog != null) && m_progressDialog.isShowing()) {
            m_progressDialog.dismiss();
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
            ((EditText) m_commentArea.findViewById(R.id.et_issue_comment_area_body))
                    .setText(savedInstanceState.getString("commentText"));
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("commentText", ((EditText) m_commentArea
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

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class CreateIssue extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    private Intent mIntent;

    private SharedPreferences mPrefs;

    private ProgressDialog mProgressDialog;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private Thread mThread;

    private String mPassword;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.create_issue);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("owner")) {
                mRepositoryOwner = extras.getString("owner");
            } else {
                mRepositoryOwner = mUsername;
            }
            if (extras.containsKey("repository")) {
                mRepositoryName = extras.getString("repository");
            }
        } else {
            mRepositoryOwner = mUsername;
        }

        ((TextView) findViewById(R.id.tv_page_title)).setText("New Issue");

        ((Button) findViewById(R.id.btn_create_issue_submit))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        mThread = new Thread(new Runnable() {
                            public void run() {
                                final String title = ((TextView) findViewById(R.id.et_create_issue_title))
                                        .getText().toString();
                                final String body = ((TextView) findViewById(R.id.et_create_issue_body))
                                        .getText().toString();
                                if (!title.equals("") && !body.equals("")) {
                                    final Response createResp = mGapi.issues.open(mRepositoryOwner,
                                            mRepositoryName, title, body);
                                    if (createResp.statusCode == 200) {
                                        try {
                                            final JSONObject response = new JSONObject(
                                                    createResp.resp).getJSONObject("issue");
                                            final int number = response.getInt("number");
                                            final JSONObject issueJSON = new JSONObject(
                                                    mGapi.issues.issue(mRepositoryOwner, mRepositoryName,
                                                            number).resp).getJSONObject("issue");
                                            mIntent = new Intent(CreateIssue.this,
                                                    SingleIssue.class);
                                            mIntent.putExtra("repoOwner", mRepositoryOwner);
                                            mIntent.putExtra("repoName", mRepositoryName);
                                            mIntent.putExtra("item_json", issueJSON.toString());

                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    mProgressDialog.dismiss();
                                                    startActivity(mIntent);
                                                    finish();
                                                }
                                            });
                                        } catch (final JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(CreateIssue.this, "Error creating issue.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        mProgressDialog = ProgressDialog.show(CreateIssue.this, "Please Wait...",
                                "Creating issue...");
                        mThread.start();
                    }
                });
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
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("titleText",
                ((EditText) findViewById(R.id.et_create_issue_title)).getText().toString());
        savedInstanceState.putString("bodyText",
                ((EditText) findViewById(R.id.et_create_issue_body)).getText().toString());
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

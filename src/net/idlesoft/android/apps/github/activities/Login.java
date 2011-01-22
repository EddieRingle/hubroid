/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
    static private class LoginTask extends AsyncTask<Void, Void, Integer> {
        public Login mActivity;

        public LoginTask(final Login activity) {
            mActivity = activity;
        }

        @Override
        protected Integer doInBackground(final Void... params) {
            final String user = ((EditText) mActivity.findViewById(R.id.et_login_username))
                    .getText().toString();
            final String pass = ((EditText) mActivity.findViewById(R.id.et_login_password))
                    .getText().toString();
            if (user.equals("") || pass.equals("")) {
                return 100;
            }
            final GitHubAPI ghapi = new GitHubAPI();
            ghapi.authenticate(user, pass);
            final int returnCode = ghapi.user.private_activity().statusCode;
            if (returnCode == 200) {
                final SharedPreferences prefs = mActivity.getSharedPreferences(Hubroid.PREFS_NAME,
                        0);
                final Editor edit = prefs.edit();
                edit.putString("username", user);
                edit.putString("password", pass);
                edit.commit();
            }
            return returnCode;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            mActivity.mProgressDialog.dismiss();
            if (result == 401) {
                Toast.makeText(mActivity, "Login details incorrect, try again", Toast.LENGTH_SHORT)
                        .show();
            } else if (result == 100) {
                Toast.makeText(mActivity, "Login details cannot be empty", Toast.LENGTH_SHORT)
                        .show();
            } else if (result == 200) {
                Toast.makeText(mActivity, "Login successful", Toast.LENGTH_SHORT).show();
                mActivity.startActivity(new Intent(mActivity, Dashboard.class));
                mActivity.finish();
            } else {
                Toast.makeText(mActivity, "Unknown login error: " + result, Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        protected void onPreExecute() {
            mActivity.mProgressDialog = ProgressDialog.show(mActivity, null, "Logging in...");
        }
    }

    public LoginTask mLoginTask;

    public ProgressDialog mProgressDialog;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.login);

        mProgressDialog = new ProgressDialog(this);

        mLoginTask = (LoginTask) getLastNonConfigurationInstance();
        if (mLoginTask != null) {
            mLoginTask.mActivity = this;
            if ((mLoginTask.getStatus() == AsyncTask.Status.RUNNING)
                    && !mProgressDialog.isShowing()) {
                mProgressDialog = ProgressDialog.show(this, null, "Logging in...");
            }
        }
        ((Button) findViewById(R.id.btn_login_login)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (mLoginTask == null) {
                    mLoginTask = new LoginTask(Login.this);
                } else if (mLoginTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mLoginTask = new LoginTask(Login.this);
                }
                mLoginTask.execute();
            }
        });
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoginTask;
    }
}

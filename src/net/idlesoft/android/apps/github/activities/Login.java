/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.UserService;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class Login extends BaseActivity {
    static private class LoginTask extends AsyncTask<Void, Void, Integer> {
        public Login activity;

        @Override
        protected Integer doInBackground(final Void... params) {
            final String user = ((EditText) activity.findViewById(R.id.et_login_username))
                    .getText().toString();
            final String pass = ((EditText) activity.findViewById(R.id.et_login_password))
                    .getText().toString();
            if (user.equals("") || pass.equals("")) {
                return 100;
            }
            final UserService us = new UserService(activity.getGitHubClient().setCredentials(user,
                    pass));
            try {
                User u = us.getUser();

                if (u != null) {
                    activity.mPrefsEditor.putString("username", u.getLogin());
                    activity.mPrefsEditor.putString("password", pass);
                    activity.mPrefsEditor.commit();
                    return 200;
                } else {
                    return -1;
                }
            } catch (IOException e) {
                if (e instanceof RequestException) {
                    return ((RequestException) e).getStatus();
                } else {
                    e.printStackTrace();
                    return -2;
                }
            }
        }

        @Override
        protected void onPostExecute(final Integer result) {
            activity.mProgressDialog.dismiss();
            if (result == 401) {
                Toast.makeText(activity, "Login details incorrect, try again", Toast.LENGTH_SHORT)
                        .show();
            } else if (result == 100) {
                Toast.makeText(activity, "Login details cannot be empty", Toast.LENGTH_SHORT)
                        .show();
            } else if (result == 200) {
                Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show();
                activity.startActivity(new Intent(activity, Dashboard.class));
                activity.finish();
            } else {
                Toast.makeText(activity, "Unknown login error: " + result, Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, null, "Logging in...");
        }
    }

    public LoginTask mLoginTask;

    public ProgressDialog mProgressDialog;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.login);

        mProgressDialog = new ProgressDialog(this);

        mLoginTask = (LoginTask) getLastNonConfigurationInstance();
        if (mLoginTask != null) {
            mLoginTask.activity = this;
            if ((mLoginTask.getStatus() == AsyncTask.Status.RUNNING)
                    && !mProgressDialog.isShowing()) {
                mProgressDialog = ProgressDialog.show(this, null, "Logging in...");
            }
        }
        ((Button) findViewById(R.id.btn_login_login)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if ((mLoginTask == null) || (mLoginTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    mLoginTask = new LoginTask();
                }
                mLoginTask.activity = Login.this;
                mLoginTask.execute();
            }
        });
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoginTask;
    }
}

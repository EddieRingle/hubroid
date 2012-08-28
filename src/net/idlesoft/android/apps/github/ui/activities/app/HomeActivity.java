/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.activities.app;

import com.actionbarsherlock.app.ActionBar;
import com.bugsense.trace.BugSenseHandler;

import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.authenticator.AuthConstants;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.ui.activities.BaseDashboardActivity.ARG_FROM_DASHBOARD;
import static net.idlesoft.android.apps.github.ui.activities.BaseDashboardActivity.EXTRA_SHOWING_DASH;

public class HomeActivity extends BaseActivity {

    private boolean mFirstRun;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle, NO_LAYOUT);

		/* BugSense setup */
        BugSenseHandler.setup(this, "40e35a51");

        mFirstRun = mPrefs.getBoolean(HubroidConstants.PREF_FIRST_RUN, true);

        if (mFirstRun) {            /*
			 * If this is the first time the user is using Hubroid, take them directly to the
			 * Account Selection screen so that they can log in if they so choose.
			 */
            mPrefsEditor.putBoolean(HubroidConstants.PREF_FIRST_RUN, false);
            mPrefsEditor.commit();

			/* Show the Account Selection screen first, then start the show */
            startActivityForResult(new Intent(this, AccountSelectActivity.class), 0);
            finish();
        } else {
			/*
			 * Otherwise, make sure whatever existing account Hubroid is set to use hasn't been
			 * deleted by the user in the system settings prior to returning to Hubroid.
			 */
            final String currentUserLogin = mPrefs.getString(
                    HubroidConstants.PREF_CURRENT_USER_LOGIN, null);
            final String currentUserJson = mPrefs
                    .getString(HubroidConstants.PREF_CURRENT_USER, null);
            final Account[] accounts = AccountManager.get(this).getAccountsByType(
                    AuthConstants.GITHUB_ACCOUNT_TYPE);
            int i;

            if (currentUserLogin != null && currentUserJson == null) {
                startActivityForResult(new Intent(this, AccountSelectActivity.class), 0);
                finish();
            }

            mCurrentAccount = null;

            if (currentUserLogin != null && currentUserJson != null) {
                mCurrentAccount = null;

				/* Try to find our current account */
                for (i = 0; i < accounts.length; i++) {
                    if (accounts[i].name.equals(currentUserLogin)) {
                        mCurrentAccount = accounts[i];
                    }
                }

				/*
				 * If we can't find the account, it must have been deleted.
				 * Redirect to the Account selection activity.
				 */
                if (mCurrentAccount == null) {
                    mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_USER);
                    mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_USER_LOGIN);
                    mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_CONTEXT_LOGIN);

                    startActivityForResult(new Intent(this, AccountSelectActivity.class), 0);
                    finish();
                }
            }
        }

        final Intent startIntent = new Intent(this, EventsActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.putExtra(ARG_FROM_DASHBOARD, true);
        if (getCurrentContextLogin() != null && !getCurrentContextLogin().equals("")) {
            startIntent.putExtra(ARG_TARGET_USER,
                    GsonUtils.toJson(new User().setLogin(getCurrentContextLogin())));
        }
        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_SHOWING_DASH, false)) {
            startIntent.putExtra(EXTRA_SHOWING_DASH, true);
        }
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onCreateActionBar(ActionBar bar) {
        super.onCreateActionBar(bar);

        bar.setTitle("");
        bar.setSubtitle(null);
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setHomeButtonEnabled(true);
    }
}

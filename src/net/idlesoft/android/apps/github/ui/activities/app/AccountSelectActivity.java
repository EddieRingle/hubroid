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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.authenticator.AuthConstants;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.UserService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import roboguice.util.RoboAsyncTask;
import roboguice.util.SafeAsyncTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AccountSelectActivity extends BaseActivity {

    private AccountManager mAccountManager;

    private SelectAccountTask mSelectAccountTask;

    private class SelectAccountTask extends SafeAsyncTask<Boolean> {

        @Override
        public Boolean call() throws Exception {
            return null;
        }
    }

    private ProgressBar mProgress;

    private RelativeLayout mContent;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.account_select_activity);

        mProgress = (ProgressBar) findViewById(R.id.progress);
        mContent = (RelativeLayout) findViewById(R.id.content);

        ListView listView = (ListView) findViewById(R.id.lv_userselect_users);
        TextView msgView = (TextView) findViewById(R.id.tv_userselect_msg);
        Button noChoiceBtn = (Button) findViewById(R.id.btn_userselect_nochoice);
        mAccountManager = AccountManager.get(getContext());

        if (mCurrentAccount == null) {
            noChoiceBtn.setText(R.string.userselect_justbrowsing);
        }

        final Account[] accounts = mAccountManager
                .getAccountsByType(AuthConstants.GITHUB_ACCOUNT_TYPE);

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.account_select_listitem);

        for (Account a : accounts) {
            listAdapter.add(a.name);
        }

        noChoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_USER_LOGIN);
                mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_USER);
                mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_CONTEXT_LOGIN);
                mPrefsEditor.commit();
                final Intent intent = new Intent(AccountSelectActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        if (!listAdapter.isEmpty()) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position,
                        long id) {
                    mContent.setVisibility(GONE);
                    mProgress.setVisibility(VISIBLE);

                    new RoboAsyncTask<Boolean>(AccountSelectActivity.this) {
                        public Boolean call() throws Exception {
                            mCurrentAccount = accounts[position];
                            mGitHubClient = null;

                            UserService service = new UserService(getGHClient());
                            User user = service.getUser();

                            if (user != null) {
                                mPrefsEditor.putString(HubroidConstants.PREF_CURRENT_USER_LOGIN,
                                        user.getLogin());
                                mPrefsEditor.putString(HubroidConstants.PREF_CURRENT_USER,
                                        GsonUtils.toJson(user));
                                mPrefsEditor.commit();

                                return true;
                            }

                            return false;
                        }

                        @Override
                        public void onSuccess(Boolean authSuccess) {
                            final Intent intent = new Intent(AccountSelectActivity.this,
                                    HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        protected void onFinally() throws RuntimeException {
                            mProgress.setVisibility(GONE);
                            mContent.setVisibility(VISIBLE);

                            super.onFinally();
                        }
                    }.execute();
                }
            });

            listView.setAdapter(listAdapter);
        }
    }

    @Override
    public void onCreateActionBar(ActionBar bar) {
        super.onCreateActionBar(bar);

        bar.setTitle(R.string.actionbar_action_select_account);

        bar.setHomeButtonEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

		/* Hide the "Select Account" option */
        menu.findItem(R.id.actionbar_action_select_account).setVisible(false);

        menu.findItem(R.id.actionbar_action_add).setVisible(true);
        menu.findItem(R.id.actionbar_action_add).setTitle(R.string.actionbar_action_add_account);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionbar_action_add) {
            mAccountManager.addAccount(AuthConstants.GITHUB_ACCOUNT_TYPE,
                    AuthConstants.AUTHTOKEN_TYPE,
                    null, null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {                     /* Restart this activity to show the new account */
                    startActivity(AccountSelectActivity.class);
                    finish();
                }
            }, null);
        }
        return super.onOptionsItemSelected(item);
    }
}

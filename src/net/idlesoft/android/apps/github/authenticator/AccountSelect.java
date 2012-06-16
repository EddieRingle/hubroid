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

package net.idlesoft.android.apps.github.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.activities.MainActivity;
import roboguice.util.SafeAsyncTask;

public
class AccountSelect extends BaseActivity
{
	private AccountManager mAccountManager;

	private SelectAccountTask mSelectAccountTask;

	private class SelectAccountTask extends SafeAsyncTask<Boolean> {
		@Override
		public
		Boolean call() throws Exception
		{
			return null;
		}
	}

	@Override
	protected
	void onCreate(Bundle icicle)
	{
		super.onCreate(icicle, R.layout.account_select_activity);

		ListView listView = (ListView) findViewById(R.id.lv_userselect_users);
		TextView msgView = (TextView) findViewById(R.id.tv_userselect_msg);
		Button signoutBtn = (Button) findViewById(R.id.btn_userselect_signout);
		mAccountManager = AccountManager.get(getContext());

		if (mCurrentAccount == null)
			signoutBtn.setVisibility(View.GONE);

		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		final Account[] accounts = mAccountManager
				.getAccountsByType(AuthConstants.GITHUB_ACCOUNT_TYPE);

		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
				getContext(),
				R.layout.account_select_listitem);

		for (Account a : accounts) {
			listAdapter.add(a.name);
		}

		if (listAdapter.isEmpty()) {
			listView.setVisibility(View.GONE);
			msgView.setVisibility(View.VISIBLE);

			msgView.setText(getString(R.string.userselect_msg_no_accounts));
		} else {
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					mPrefsEditor.putString(PREF_CURRENT_USER_LOGIN, accounts[position].name);
					mPrefsEditor.commit();
					mGitHubClient = null;
					final Intent intent = new Intent(AccountSelect.this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				}
			});

			listView.setAdapter(listAdapter);

			signoutBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public
				void onClick(View v)
				{
					mPrefsEditor.remove(PREF_CURRENT_USER_LOGIN);
					mPrefsEditor.commit();
					final Intent intent = new Intent(AccountSelect.this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				}
			});
		}
	}

	@Override
	public
	boolean onCreateOptionsMenu(Menu menu)
	{
		final boolean superResult = super.onCreateOptionsMenu(menu);

		/* Hide the "Select Account" option */
		menu.findItem(R.id.actionbar_action_select_account).setVisible(false);

		menu.findItem(R.id.actionbar_action_add).setVisible(true);
		menu.findItem(R.id.actionbar_action_add).setTitle(R.string.actionbar_action_add_account);

		return superResult;
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.actionbar_action_add) {
			 mAccountManager.addAccount(AuthConstants.GITHUB_ACCOUNT_TYPE,
										AuthConstants.AUTHTOKEN_TYPE,
										null, null, this, new AccountManagerCallback<Bundle>()
			 {
				 @Override
				 public
				 void run(AccountManagerFuture<Bundle> future)
				 {
					 /* Restart this activity to show the new account */
					 startActivity(AccountSelect.class);
					 finish();
				 }
			 }, null);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public
	void onBackPressed()
	{
		finish();
	}
}

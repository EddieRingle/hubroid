/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.authenticator.AccountSelect;
import net.idlesoft.android.apps.github.authenticator.AuthConstants;

public
class Main extends BaseActivity
{
	/** Called when the activity is first created. */
	@Override
	public
	void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (mPrefs.getBoolean(PREF_FIRST_RUN, true)) {
			mPrefsEditor.putBoolean(PREF_FIRST_RUN, false);
			mPrefsEditor.apply();

			/* Show the Account Selection screen first, then start the Dashboard */
			startActivityForResult(new Intent(this, AccountSelect.class), 0);
		} else {
			final String current_user_login = mPrefs.getString(PREF_CURRENT_USER_LOGIN, null);
			if (current_user_login != null) {
				mCurrentUser = new Account(current_user_login, AuthConstants.GITHUB_ACCOUNT_TYPE);
			} else {
				mCurrentUser = null;
			}
			startActivity(Dashboard.class);
			finish();
		}
	}

	@Override
	protected
	void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		/* Account selection is done, restart */
		startActivity(Main.class);
		finish();
		super.onActivityResult(requestCode, resultCode, data);
	}
}

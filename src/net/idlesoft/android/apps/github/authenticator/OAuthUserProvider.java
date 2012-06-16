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
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.os.Bundle;
import com.google.inject.Inject;

import java.io.IOException;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static net.idlesoft.android.apps.github.authenticator.AuthConstants.AUTHTOKEN_TYPE;
import static net.idlesoft.android.apps.github.authenticator.AuthConstants.GITHUB_ACCOUNT_TYPE;

/**
 * Bridge class that obtains a GitHub OAuth code for the currently configured account
 */
public class OAuthUserProvider
{

    private static final String TAG = "OCP";

	public static class AuthResponse {
		public Account account;
		public String access_token;
	}

    @Inject
    private Activity activity;

    @Inject
    private AccountManager accountManager;

    public AuthResponse getOAuthResponse(Account account)
			throws AccountsException, IOException {
		AccountManagerFuture<Bundle> accountManagerFuture;

		accountManagerFuture = accountManager.getAuthToken(account, AUTHTOKEN_TYPE, null, activity,
														   null, null);

        Bundle result = accountManagerFuture.getResult();

		AuthResponse response = new AuthResponse();
		response.account = new Account(result.getString(KEY_ACCOUNT_NAME), GITHUB_ACCOUNT_TYPE);
        response.access_token = result.getString(KEY_AUTHTOKEN);

        return response;
    }
}

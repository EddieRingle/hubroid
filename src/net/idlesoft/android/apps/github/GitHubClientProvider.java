/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.util.Log;
import com.google.inject.Inject;
import net.idlesoft.android.apps.github.authenticator.OAuthUserProvider;
import org.eclipse.egit.github.core.client.GitHubClient;

import java.io.IOException;

import static net.idlesoft.android.apps.github.HubroidConstants.USER_AGENT_STRING;

public
class GitHubClientProvider
{
	@Inject
	private OAuthUserProvider mUserProvider;

	private Account mCurrentUser;

	public
	GitHubClient getClient(final Account account) throws IOException, AccountsException
	{
		final GitHubClient client = new GitHubClient();
		OAuthUserProvider.AuthResponse response = mUserProvider.getOAuthResponse(account);
		Log.d("hubroid", "account: " + response.account);
		Log.d("hubroid", "at: " + response.access_token);
		client.setOAuth2Token(response.access_token);
		client.setUserAgent(USER_AGENT_STRING);
		mCurrentUser = response.account;

		return client;
	}

	public
	GitHubClient getAnonymousClient()
	{
		final GitHubClient client = new GitHubClient();
		client.setUserAgent(USER_AGENT_STRING);
		mCurrentUser = null;
		return client;
	}

	public
	Account getCurrentUser()
	{
		return mCurrentUser;
	}
}

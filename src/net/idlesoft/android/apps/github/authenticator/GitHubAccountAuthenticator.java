/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.idlesoft.android.apps.github.authenticator;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static net.idlesoft.android.apps.github.HubroidConstants.USER_AGENT_STRING;
import static net.idlesoft.android.apps.github.authenticator.AuthConstants.GITHUB_ACCOUNT_TYPE;
import static net.idlesoft.android.apps.github.authenticator.GitHubAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class GitHubAccountAuthenticator extends AbstractAccountAuthenticator {

	private static final String TAG = "GitHubAccountAuth";

	private static final String DESCRIPTION_CLIENT = "Hubroid - GitHub Client for Android";

	private static final String CLIENT_URL = "https://github.com/eddieringle/hubroid";

	private Context mContext;

	public
	GitHubAccountAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	/*
		 * The user has requested to add a new account to the system. We return an intent that will launch our account screen
		 * if the user has not logged in yet, otherwise our activity will just pass the user's credentials on to the account
		 * manager.
		 */
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
							 String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		final Intent intent = new Intent(mContext, GitHubAuthenticatorActivity.class);
		intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
							   Bundle options) throws NetworkErrorException {
		Log.d(TAG, "getAuthToken() called : authTokenType=" + authTokenType);
		String password = AccountManager.get(mContext).getPassword(account);
		Authorization auth = null;
		try {
			GitHubClient client = new GitHubClient();
			client.setUserAgent(USER_AGENT_STRING);
			client.setCredentials(account.name, password);
			OAuthService service = new OAuthService(client);
			for (Authorization a : service.getAuthorizations()) {
				if (a != null && a.getNote() != null)
					if (a.getNote().equals(DESCRIPTION_CLIENT))
						auth = a;
			}
			if (auth == null) {
				auth = new Authorization();
				auth.setNote(DESCRIPTION_CLIENT);
				auth.setNoteUrl(CLIENT_URL);
				List<String> scopes = new ArrayList<String>();
				scopes.add("user");
				scopes.add("repo");
				scopes.add("gist");
				auth.setScopes(scopes);
				auth = service.createAuthorization(auth);
			}
		} catch (IOException e) {
			throw new NetworkErrorException(e);
		}
		String oauthToken = auth.getToken();
		Log.d(TAG, "getAuthToken() called : oauthToken=" + oauthToken);
		Bundle bundle = new Bundle();
		bundle.putString(KEY_ACCOUNT_NAME, account.name);
		bundle.putString(KEY_ACCOUNT_TYPE, GITHUB_ACCOUNT_TYPE);
		bundle.putString(KEY_AUTHTOKEN, oauthToken);
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		if (authTokenType.equals(AuthConstants.AUTHTOKEN_TYPE)) {
			return authTokenType;
		}
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
			throws NetworkErrorException {
		final Bundle result = new Bundle();
		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
		return result;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
									Bundle options) {
		return null;
	}
}
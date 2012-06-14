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

package net.idlesoft.android.apps.github.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.authenticator.GitHubAuthenticatorActivity;
import net.idlesoft.android.apps.github.ui.fragments.IssueFragment;
import net.idlesoft.android.apps.github.ui.fragments.IssuesFragment;
import net.idlesoft.android.apps.github.ui.fragments.ProfileFragment;
import net.idlesoft.android.apps.github.ui.fragments.RepositoryFragment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;

public
class GitHubIntentFilter extends BaseActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String uri = getIntent().getDataString().replaceAll("/{2,}", "/");
		final String[] parts = uri.split("/");
		final String scheme = parts[0];
		final String host = parts[1];
		final String[] path = new String[parts.length - 2];
		final Intent intent = new Intent();
		for (int i = 2; i < parts.length; i++)
			path[i - 2] = parts[i];

		/* We're only handling OAuth intents, for now */
		if (path[0].equalsIgnoreCase("oauth")) {
			intent.setClass(GitHubIntentFilter.this, GitHubAuthenticatorActivity.class);
			if (getIntent().getData().getQueryParameter("code") != null) {
				intent.putExtra("stage", 2);
				intent.putExtra("code", getIntent().getData().getQueryParameter("code"));
			}
			startActivity(intent);
		}

		if (host.matches("^(www.)?github.com$")) {
			intent.setClass(GitHubIntentFilter.this, MainActivity.class);
			intent.putExtra(HubroidConstants.ARG_TARGET_URI, path);
			startActivity(intent);
		}

		finish();
	}

	public static
	void parsePath(final BaseActivity activity, final String[] path) {
		final Bundle args = new Bundle();
		String user, repo, action, id;

		user = repo = action = id = null;

		try {
			user = path[0];
			repo = path[1];
			action = path[2];
			id = path[3].split("#")[0];
		} catch (IndexOutOfBoundsException e) {
		}

		/* Bow out if the user tried to open https://github.com/ in Hubroid */
		if (user == null)
			return;

		activity.startFragmentTransaction();

		if (repo == null) {
			/* Send the user to a profile */
			args.putString(HubroidConstants.ARG_TARGET_USER,
						   GsonUtils.toJson((new User()).setLogin(user)));
			activity.addFragmentToTransaction(ProfileFragment.class, R.id.fragment_container, args);
		} else if (action == null) {
			/* Send the user to a repository */
			args.putString(HubroidConstants.ARG_TARGET_REPO,
						   GsonUtils.toJson((new Repository()).setName(repo).setOwner(
								   (new User()).setLogin(user))));
			activity.addFragmentToTransaction(RepositoryFragment.class,
											  R.id.fragment_container,
											  args);
		} else if (action.equalsIgnoreCase("issues")) {
			if (id == null) {
				/* Go to a repository's issues */
				args.putString(HubroidConstants.ARG_TARGET_REPO,
							   GsonUtils.toJson((new Repository()).setName(repo).setOwner(
									   (new User()).setLogin(user))));
				activity.addFragmentToTransaction(IssuesFragment.class,
												  R.id.fragment_container,
												  args);
			} else {
				/* Go to an individual issue */
				args.putString(HubroidConstants.ARG_TARGET_ISSUE,
							   GsonUtils.toJson((new Issue()).setHtmlUrl("https://github.com/"
																				 + user + "/"
																				 + repo +
																				 "/issues/" + id)
															 .setNumber(Integer.parseInt(id))));
				activity.addFragmentToTransaction(IssueFragment.class,
												  R.id.fragment_container,
												  args);
			}
		}
		activity.finishFragmentTransaction(false);
	}
}

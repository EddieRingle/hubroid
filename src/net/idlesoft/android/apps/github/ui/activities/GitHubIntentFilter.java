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

package net.idlesoft.android.apps.github.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.ui.activities.app.GitHubAuthenticatorActivity;

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
			parsePath(this, path);
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

		if (repo == null) {
			/* Send the user to a profile */
		} else if (action == null) {
			/* Send the user to a repository */
		} else if (action.equalsIgnoreCase("issues")) {
			if (id == null) {
				/* Go to a repository's issues */
			} else {
				/* Go to an individual issue */
			}
		}
	}
}

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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.widgets.RefreshActionView;
import org.eclipse.egit.github.core.client.GitHubClient;

import java.util.Timer;
import java.util.TimerTask;

public
class BaseActivity extends SherlockActivity
{
	protected static final int NO_LAYOUT = -1;

	protected SharedPreferences mPrefs;

	protected SharedPreferences.Editor mPrefsEditor;

	protected String mUsername;

	protected String mOAuthToken;

	protected
	Context getContext()
	{
		return getApplicationContext();
	}

	protected
	void onCreate(final Bundle icicle, final int layout)
	{
		super.onCreate(icicle);
		if (layout != NO_LAYOUT) setContentView(layout);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		mPrefsEditor = mPrefs.edit();

		mUsername = mPrefs.getString("username", "");
		mOAuthToken = mPrefs.getString("access_token", "");
	}

	public
	GitHubClient getGHClient()
	{
		if (!mOAuthToken.equals("")) {
			return new GitHubClient().setOAuth2Token(mOAuthToken);
		} else {
			return new GitHubClient();
		}
	}

	@Override
	public
	boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSherlock().getMenuInflater();
		inflater.inflate(R.menu.actionbar, menu);

		final MenuItem refresh = (MenuItem) menu.findItem(R.id.actionbar_action_refresh);
		refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public
			boolean onMenuItemClick(final MenuItem item)
			{
				final RefreshActionView refreshView = (RefreshActionView) getLayoutInflater()
						.inflate(R.layout.refresh_action_view, null);
				refreshView.addTo(item);
				Timer t = new Timer();
				t.schedule(new TimerTask()
				{
					@Override
					public
					void run()
					{
						runOnUiThread(new Runnable()
						{
							@Override
							public
							void run()
							{
								refreshView.removeFromParentItem();
							}
						});
					}
				}, 1000);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
}

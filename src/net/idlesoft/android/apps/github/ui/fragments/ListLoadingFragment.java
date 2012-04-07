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

package net.idlesoft.android.apps.github.ui.fragments;

import static net.idlesoft.android.apps.github.utils.ToastUtil.toastOnUiThread;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

import java.util.List;

import com.actionbarsherlock.view.MenuItem;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.widgets.RefreshActionView;

/**
 * List loading fragment for a specific type
 *
 * @param <E>
 */
public abstract class ListLoadingFragment<E> extends SherlockListFragment<E> implements LoaderCallbacks<List<E>>
{
	protected RefreshActionView mRefreshActionView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.actionbar_action_refresh:
				refresh(item);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Refresh the fragment's list
	 */
	public void refresh(final MenuItem item) {
		final BaseActivity activity = getBaseActivity();
		if(activity == null)
			return;

		/* Attach a rotating ImageView to the refresh item as an ActionView */
		mRefreshActionView = (RefreshActionView)activity.getLayoutInflater().inflate(R.layout.refresh_action_view, null);

		mRefreshActionView.addTo(item);

		getLoaderManager().restartLoader(0, null, this);
	}

	public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
		/* TODO: Allow for appending list items */
		fillListAdapter(items);

		if (isResumed())
			setListShown(true);
		else
			setListShownNoAnimation(true);

		if (mRefreshActionView != null) {
			mRefreshActionView.removeFromParentItem();
		}
	}

	@Override
	public void onLoaderReset(Loader<List<E>> listLoader) {
	}

	/**
	 * Show message via a {@link Toast}
	 * <p>
	 * This method ensures the {@link Toast} is displayed on the UI thread and so it may be called from any thread
	 *
	 * @param message
	 */
	protected void showError(final int message) {
		final Activity activity = getActivity();
		if (activity != null)
			toastOnUiThread(activity, getString(message));
	}
}
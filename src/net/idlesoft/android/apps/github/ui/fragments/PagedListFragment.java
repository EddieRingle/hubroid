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

package net.idlesoft.android.apps.github.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.loaders.PagedAsyncLoader;
import org.eclipse.egit.github.core.client.PageIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract
class PagedListFragment<T> extends BaseListFragment<T> implements AbsListView.OnScrollListener,
                                                                  LoaderManager.LoaderCallbacks<List<T>>,
                                                                  PagedAsyncLoader.PagedAsyncLoaderCallbacks<T>
{
	private
	PageIterator<T> mPageIterator;
	private
	View mLoadingIndicator;
    private
    boolean mLoading;
	private
	boolean mShowingLoadingIndicator;

	/**
	 * Implementations of this method should be responsible for creating an instance of a
	 * PageIterator to be used in loading the list with items.
	 *
	 * @return PageIterator
	 */
	public abstract
	PageIterator<T> onCreatePageIterator();

    /**
     * Returns a unique identifying integer for use with the LoaderManager in order to prevent
     * Loader conflicts.
     *
     * @return loader id
     */
    public abstract
    int getLoaderId();

    public
    void startLoading()
    {
        mLoading = true;
        Loader<T> loader = getLoaderManager().getLoader(getLoaderId());
        if (loader != null) {
            loader.reset();
            loader.startLoading();
        } else {
            getLoaderManager().initLoader(getLoaderId(), null, this);
        }
    }

    public
    void startLoadingMore() {
        if (mLoading) {
            return;
        }
        if (mPageIterator.hasNext() && mPageIterator.getNextPage() > 1) {
            setLoadingIndicatorVisible(true);
            startLoading();
        } else {
            setLoadingIndicatorVisible(false);
            mLoading = false;
        }
    }

    public
	void setLoadingIndicatorVisible(final boolean show)
	{
		if (mShowingLoadingIndicator != show && getListAdapter() != null) {
			if (show)
				getListAdapter().addFooter(mLoadingIndicator, null, false);
			else
				getListAdapter().removeFooter(mLoadingIndicator);
		}
		mShowingLoadingIndicator = show;
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		/* Grab the loading indicator layout during view creation */
		mLoadingIndicator = inflater.inflate(R.layout.loading_indicator, null, false);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mPageIterator = onCreatePageIterator();
	}

    @Override
    public void onStart() {
        super.onStart();

        getListView().setOnScrollListener(this);

        if (getWrappedListAdapter() != null && getWrappedListAdapter().isEmpty()) {
            setListShown(false);
            startLoading();
        }
    }

    @Override
	public
	void onScrollStateChanged(AbsListView absListView, int i)
	{
		/* stub */
	}

	@Override
	public
	void onScroll(AbsListView absListView, int firstVisibleItem,
				  int visibleItemCount, int totalItemCount)
	{
		/*
		 * We want to bail out on the following cases:
		 *  - There are no more pages in the iterator to load
		 *  - We're already trying to load more items
		 * Otherwise, we trigger the load of the next page and
		 * display its results in the list.
		 */
		if (mPageIterator == null) {
			return;
        }
		if (mLoading) {
            return;
        }
		if (getListView().getLastVisiblePosition() + 1 >= getListAdapter().getCount()) {
            startLoadingMore();
		}
	}

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        return new PagedAsyncLoader<T>(getBaseActivity(), mPageIterator, this);
    }

    @Override
    public void onLoadFinished(Collection<T> items) {
        mLoading = false;

        if (items != null) {
            final ArrayList<T> list = new ArrayList<T>();
            list.addAll(items);
            getWrappedListAdapter().fillWithItems(list);
            getWrappedListAdapter().notifyDataSetChanged();
        }

        if (mPageIterator != null) {
            setLoadingIndicatorVisible(mPageIterator.hasNext());
        } else {
            setLoadingIndicatorVisible(false);
        }

        setListShown(true);
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
    }

    @Override
	public
	void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		menu.findItem(R.id.actionbar_action_refresh).setVisible(true);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.actionbar_action_refresh) {
            mPageIterator = onCreatePageIterator();
            getLoaderManager().destroyLoader(getLoaderId());
            setListShown(false);
            startLoading();
		}

		return super.onOptionsItemSelected(item);
	}
}

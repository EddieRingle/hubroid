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

import com.google.gson.reflect.TypeToken;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.client.GsonUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import static net.idlesoft.android.apps.github.services.GitHubApiService.ARG_START_PAGE;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_ERROR;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_HAS_NEXT;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_NEXT_PAGE;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_RESULT_JSON;

public abstract class PagedListFragment<T> extends BaseListFragment<T>
        implements AbsListView.OnScrollListener {

    private static final String EXTRA_LIST_ITEMS = "extra_list_items";

    private boolean mHasNext = true;

    private boolean mLoadingMore;

    private int mNextPage = 1;

    private BroadcastReceiver mBroadcastReceiver;

    private TypeToken<List<T>> mListTypeToken;

    private View mLoadingIndicator;

    private boolean mShowingLoadingIndicator;

    protected abstract class PagedListBroadcastReceiver extends BroadcastReceiver {

        public abstract List<T> handleReceive(Context context, Intent intent, List<T> items);

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }

            if (intent.getBooleanExtra(EXTRA_ERROR, false)) {
                getBaseActivity().popShortToast("An error occurred.");
            } else {
                final ArrayList<T> listItems = new ArrayList<T>();
                final String resultJson = intent.getStringExtra(EXTRA_RESULT_JSON);
                List<T> fetchedItems = null;
                if (resultJson != null) {
                    fetchedItems = GsonUtils.fromJson(resultJson, mListTypeToken.getType());
                }

                fetchedItems = handleReceive(context, intent, fetchedItems);

                if (fetchedItems != null) {
                    listItems.addAll(fetchedItems);
                }

                if (!mLoadingMore) {
                    getWrappedListAdapter().fillWithItems(listItems);
                } else {
                    getWrappedListAdapter().appendWithItems(listItems);
                }

                mHasNext = intent.getBooleanExtra(EXTRA_HAS_NEXT, false);
                mNextPage = intent.getIntExtra(EXTRA_NEXT_PAGE, -1);

                setLoadingIndicatorVisible(mHasNext);
                getListView().setOnScrollListener((mHasNext) ? PagedListFragment.this : null);

                getWrappedListAdapter().notifyDataSetChanged();
            }

            setListShown(true);

            mLoadingMore = false;
        }
    }

    public abstract PagedListBroadcastReceiver onCreateBroadcastReceiver();

    public abstract IntentFilter onCreateIntentFilter();

    public abstract Intent onCreateServiceIntent();

    public void startLoading(final boolean forceRefresh) {
        if (mLoadingMore && !forceRefresh) {
            return;
        }
        final Intent startServiceIntent = onCreateServiceIntent();
        if (startServiceIntent != null) {
            startServiceIntent.putExtra(ARG_START_PAGE, mNextPage);
            getBaseActivity().startService(startServiceIntent);
        }
    }

    public void setLoadingIndicatorVisible(final boolean show) {
        if (mShowingLoadingIndicator != show && getListAdapter() != null) {
            if (show) {
                getListAdapter().addFooter(mLoadingIndicator, null, false);
            } else {
                getListAdapter().removeFooter(mLoadingIndicator);
            }
        }
        mShowingLoadingIndicator = show;
    }

    public PagedListFragment(final TypeToken<List<T>> listTypeToken) {
        mListTypeToken = listTypeToken;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        /* Grab the loading indicator layout during view creation */
        mLoadingIndicator = inflater.inflate(R.layout.loading_indicator, null, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBroadcastReceiver = onCreateBroadcastReceiver();
        if (mBroadcastReceiver != null) {
            final IntentFilter filter = onCreateIntentFilter();
            if (filter != null) {
                getBaseActivity().registerReceiver(mBroadcastReceiver, filter);
            }
        }

        if (savedInstanceState == null) {
            if (getWrappedListAdapter() != null && getWrappedListAdapter().isEmpty()) {
                setListShown(false);
                startLoading(false);
            }
        } else {
            mHasNext = savedInstanceState.getBoolean(EXTRA_HAS_NEXT, true);
            mNextPage = savedInstanceState.getInt(EXTRA_NEXT_PAGE, -1);

            final String itemsJson = savedInstanceState.getString(EXTRA_LIST_ITEMS);
            if (itemsJson != null) {
                final List<T> items = GsonUtils.fromJson(itemsJson, mListTypeToken.getType());

                getWrappedListAdapter().fillWithItems(items);
                getWrappedListAdapter().notifyDataSetChanged();

                setLoadingIndicatorVisible(mNextPage > 0 && mHasNext);
                getListView().setOnScrollListener((mNextPage > 0 && mHasNext) ? this : null);

                setListShown(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBroadcastReceiver != null) {
            try {
                getBaseActivity().unregisterReceiver(mBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                /* Ignore this. */
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {        /* stub */
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
		/*
		 * We want to bail out on the following cases:
		 *  - There are no more pages in the iterator to load
		 *  - We're already trying to load more items
		 * Otherwise, we trigger the load of the next page and
		 * display its results in the list.
		 */
        if (mNextPage < 0 || !mHasNext) {
            return;
        }
        if (mLoadingMore) {
            return;
        }
        if (getListView().getLastVisiblePosition() + 1 >= getListAdapter().getCount()) {
            Log.d("hubroid", ">> onScroll() -> Going to load more!");
            startLoading(false);
            mLoadingMore = true;
        }
    }


    @Override
    public void onCreateActionBar(ActionBar bar, Menu menu, MenuInflater inflater) {
        super.onCreateActionBar(bar, menu, inflater);

        menu.findItem(R.id.actionbar_action_refresh).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionbar_action_refresh) {
            setListShown(false);
            getListView().setOnScrollListener(this);
            mHasNext = true;
            mNextPage = 1;
            startLoading(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EXTRA_HAS_NEXT, mHasNext);

        outState.putInt(EXTRA_HAS_NEXT, mNextPage);

        if (getWrappedListAdapter() != null && getWrappedListAdapter().getAll() != null) {
            final ArrayList<T> items = getWrappedListAdapter().getAll();
            outState.putString(EXTRA_LIST_ITEMS, GsonUtils.toJson(items));
        }
    }
}

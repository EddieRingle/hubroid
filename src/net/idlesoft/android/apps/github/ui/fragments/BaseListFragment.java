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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.HeaderFooterListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.BaseFragment;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract
class BaseListFragment<T> extends BaseFragment implements AdapterView.OnItemClickListener
{
	private
	ProgressBar mProgress;
	private
	LinearLayout mContent;
	private
	ListView mListView;

	/**
	 * Implementations of this method should create an instance of a subclass of BaseListAdapter
	 * specialized to the specified template type (e.g., PagedListFragment<Repository>
	 * implementations should return an instance of RepositoryListAdapter).
	 *
	 * @return BaseListAdapter
	 */
	public abstract
	BaseListAdapter<T> onCreateListAdapter();

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null) {
			getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }

		View v = inflater.inflate(R.layout.list_fragment, container, false);

		if (v != null) {
			mProgress = (ProgressBar) v.findViewById(R.id.progress);
			mContent = (LinearLayout) v.findViewById(R.id.content);
			mListView = (ListView) v.findViewById(R.id.list);
		}

		return v;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setAdapter(
                new HeaderFooterListAdapter<BaseListAdapter<T>>(mListView, onCreateListAdapter()));
    }

    protected
	ProgressBar getProgressBar()
	{
		return mProgress;
	}

	protected
	LinearLayout getContentView()
	{
		return mContent;
	}

	protected
	ListView getListView()
	{
		return mListView;
	}

    public
    void setListShown(boolean shown) {
        if (shown) {
            mProgress.setVisibility(GONE);
            mContent.setVisibility(VISIBLE);
            mListView.setVisibility(VISIBLE);
        } else {
            mContent.setVisibility(GONE);
            mListView.setVisibility(GONE);
            mProgress.setVisibility(VISIBLE);
        }
    }

	protected HeaderFooterListAdapter<BaseListAdapter<T>> getListAdapter() {
		if (getListView() != null) {
			return (HeaderFooterListAdapter<BaseListAdapter<T>>) getListView().getAdapter();
        } else {
			return null;
        }
	}

    protected BaseListAdapter<T> getWrappedListAdapter() {
        HeaderFooterListAdapter<BaseListAdapter<T>> wrappingAdapter = getListAdapter();
        if (wrappingAdapter != null) {
            return wrappingAdapter.getWrappedAdapter();
        } else {
            return null;
        }
    }
}

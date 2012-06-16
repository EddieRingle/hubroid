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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.util.List;

public abstract
class BaseListFragment<T> extends BaseFragment
{
	static final int INTERNAL_EMPTY_ID = 0x00ff0001;
	static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
	static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

	final private
	Handler mHandler = new Handler();

	final private
	Runnable mRequestFocus = new Runnable()
	{
		@Override
		public
		void run()
		{
			mListView.focusableViewAvailable(mListView);
		}
	};

	final private
	AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener()
	{
		@Override
		public
		void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			onListItemClick((ListView)parent, view,  position, id);
		}
	};

	ArrayAdapter<T> mAdapter;
	ListView mListView;
	View mEmptyView;
	TextView mStandardEmptyView;
	View mProgressContainer;
	View mListContainer;
	CharSequence mEmptyText;
	boolean mListShown;

	public BaseListFragment()
	{
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final Context context = getActivity();

		FrameLayout root = new FrameLayout(context);

		// ------------------------------------------------------------------

		LinearLayout pframe = new LinearLayout(context);
		pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
		pframe.setOrientation(LinearLayout.VERTICAL);
		pframe.setVisibility(View.GONE);
		pframe.setGravity(Gravity.CENTER);

		ProgressBar progress = new ProgressBar(context, null,
											   android.R.attr.progressBarStyleLarge);
		pframe.addView(progress, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		root.addView(pframe, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		// ------------------------------------------------------------------

		FrameLayout lframe = new FrameLayout(context);
		lframe.setId(INTERNAL_LIST_CONTAINER_ID);

		TextView tv = new TextView(getActivity());
		tv.setId(INTERNAL_EMPTY_ID);
		tv.setGravity(Gravity.CENTER);
		lframe.addView(tv, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		ListView lv = new ListView(getActivity());
		lv.setId(android.R.id.list);
		lv.setDrawSelectorOnTop(false);
		lframe.addView(lv, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		root.addView(lframe, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		// ------------------------------------------------------------------

		root.setLayoutParams(new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		return root;
	}

	@Override
	public
	void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		ensureList();
	}

	@Override
	public
	void onDestroyView()
	{
		mHandler.removeCallbacks(mRequestFocus);
		mListView = null;
		mListShown = false;
		mEmptyView = mProgressContainer = mListContainer = null;
		mStandardEmptyView = null;
		super.onDestroyView();
	}

	public abstract
	void onListItemClick(ListView l, View v, int position, long id);

	public
	void setListAdapter(ArrayAdapter<T> adapter)
	{
		boolean hadAdapter = mAdapter != null;
		mAdapter = adapter;
		if (mListView != null) {
			mListView.setAdapter(adapter);
			if (!(mListShown || hadAdapter)) {
				setListShown(true, getView().getWindowToken() != null);
			}
		}
	}

	public abstract
	void onCreateListAdapter(List<T> items);

	/**
	 * Fills (or refills) the list adapter with a list of items
	 */
	public
	void fillListAdapter(List<T> items)
	{
		if (mAdapter == null) {
			onCreateListAdapter(items);
			setListAdapter(mAdapter);
		} else {
			mAdapter.clear();
			mAdapter.addAll(items);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Appends a list of items onto the end of the list adapter
	 *
	 * Please ensure no duplicate items will be added beforehand.
	 */
	public
	void appendToListAdapter(List<T> items)
	{
		if (mAdapter == null) {
			fillListAdapter(items);
		} else {
			mAdapter.addAll(items);
			mAdapter.notifyDataSetChanged();
		}
	}

	public
	void setSelection(int position)
	{
		ensureList();
		mListView.setSelection(position);
	}

	public
	int getSelectedItemPosition()
	{
		ensureList();
		return mListView.getSelectedItemPosition();
	}

	public
	long getSelectedItemId()
	{
		ensureList();
		return mListView.getSelectedItemId();
	}

	public
	ListView getListView() {
		ensureList();
		return mListView;
	}

	public
	void setEmptyText(CharSequence text)
	{
		ensureList();
		if (mStandardEmptyView == null) {
			throw new IllegalStateException("Can't be used with a custom content view");
		}
		mStandardEmptyView.setText(text);
		if (mEmptyText == null) {
			mListView.setEmptyView(mStandardEmptyView);
		}
		mEmptyText = text;
	}

	public
	void setListShown(boolean shown) {
		setListShown(shown, true);
	}

	public
	void setListShownNoAnimation(boolean shown) {
		setListShown(shown, false);
	}
	
	private
	void setListShown(boolean shown, boolean animate) {
		ensureList();
		if (mProgressContainer == null) {
			throw new IllegalStateException("Can't be used with a custom content view");
		}
		if (mListShown == shown) return;
		mListShown = shown;
		if (shown) {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
						getBaseActivity(), android.R.anim.fade_out
				));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(
						getBaseActivity(), android.R.anim.fade_in
				));
			} else {
				mProgressContainer.clearAnimation();
				mListContainer.clearAnimation();
			}
			mProgressContainer.setVisibility(View.GONE);
			mListContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
						getBaseActivity(), android.R.anim.fade_in
				));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(
						getBaseActivity(), android.R.anim.fade_out
				));
			} else {
				mProgressContainer.clearAnimation();
				mListContainer.clearAnimation();
			}
			mProgressContainer.setVisibility(View.VISIBLE);
			mListContainer.setVisibility(View.GONE);
		}
	}

	public
	ListAdapter getListAdapter()
	{
		return mAdapter;
	}

	private
	void ensureList()
	{
		if (mListView != null) {
			return;
		}
		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}
		if (root instanceof ListView) {
			mListView = (ListView)root;
		} else {
			mStandardEmptyView = (TextView)root.findViewById(INTERNAL_EMPTY_ID);
			if (mStandardEmptyView == null) {
				mEmptyView = root.findViewById(android.R.id.empty);
			} else {
				mStandardEmptyView.setVisibility(View.GONE);
			}
			mProgressContainer = root.findViewById(INTERNAL_PROGRESS_CONTAINER_ID);
			mListContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
			View rawListView = root.findViewById(android.R.id.list);
			if (!(rawListView instanceof ListView)) {
				if (rawListView == null) {
					throw new RuntimeException(
							"Your content must have a ListView whose id attribute is " +
									"'android.R.id.list'");
				}
				throw new RuntimeException(
						"Content has view with id attribute 'android.R.id.list' "
								+ "that is not a ListView class");
			}
			mListView = (ListView)rawListView;
			if (mEmptyView != null) {
				mListView.setEmptyView(mEmptyView);
			} else if (mEmptyText != null) {
				mStandardEmptyView.setText(mEmptyText);
				mListView.setEmptyView(mStandardEmptyView);
			}
		}
		mListShown = true;
		mListView.setOnItemClickListener(mOnClickListener);
		if (mAdapter != null) {
			ArrayAdapter<T> adapter = mAdapter;
			mAdapter = null;
			setListAdapter(adapter);
		} else {
			// We are starting without an adapter, so assume we won't
			// have our data right away and start with the progress indicator.
			if (mProgressContainer != null) {
				setListShown(false, false);
			}
		}
		mHandler.post(mRequestFocus);
	}
}

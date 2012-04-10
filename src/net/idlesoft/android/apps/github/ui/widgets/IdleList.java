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

package net.idlesoft.android.apps.github.ui.widgets;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.*;
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public
class IdleList<T> extends ListView
{
	static final int INTERNAL_EMPTY_ID = 0x00ff0001;
	static final int INTERNAL_PROGRESS_ID = 0x00ff0002;
	static final int INTERNAL_FOOTER_ID = 0x00ff0003;

	LinearLayout mFooterView;
	TextView mStandardEmptyView;
	ProgressBar mProgress;
	CharSequence mEmptyText;
	CharSequence mTitle;
	boolean mListShown;

	public
	IdleList(Context context)
	{
		super(context);
		setupIdleList();
	}

	public
	IdleList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setupIdleList();
	}

	public
	IdleList(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setupIdleList();
	}

	private
	void setupIdleList()
	{
		Context context = getContext();

		mFooterView = new LinearLayout(context);
		mFooterView.setId(INTERNAL_FOOTER_ID);
		mFooterView.setGravity(Gravity.CENTER);
		mFooterView.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

		mProgress = new ProgressBar(context, null, R.attr.progressBarStyle);
		mProgress.setId(INTERNAL_PROGRESS_ID);
		mProgress.setIndeterminate(true);
		mProgress.setVisibility(View.GONE);
		final LayoutParams progressLayoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
		mProgress.setLayoutParams(progressLayoutParams);

		mFooterView.addView(mProgress);

		mStandardEmptyView = new TextView(context);
		mStandardEmptyView.setId(INTERNAL_EMPTY_ID);
		mStandardEmptyView.setGravity(Gravity.CENTER);
		mStandardEmptyView.setVisibility(View.GONE);
		mStandardEmptyView.setPadding(10, 15, 10, 15);
		mStandardEmptyView.setText("That's all, folks!");

		mFooterView.addView(mStandardEmptyView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

		setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

		setFooterDividersEnabled(true);
		addFooterView(mFooterView, null, false);

		/* A tiny hack to get the ProgressBar to show */
		mListShown = true;
		setListShown(true);
		setFooterShown(true);
	}

	public
	BaseListAdapter<T> getListAdapter()
	{
		final ListAdapter adapter = getAdapter();

		if (adapter instanceof HeaderViewListAdapter)
			return (BaseListAdapter<T>) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		else
			return (BaseListAdapter<T>) getAdapter();
	}

	@Override
	public
	void setAdapter(ListAdapter adapter)
	{
		if (!(adapter instanceof BaseListAdapter)) {
			throw new IllegalArgumentException("IdleList must use a BaseListAdapter.");
		}

		boolean hadAdapter = getListAdapter() != null;

		super.setAdapter(adapter);

		if (!(mListShown || hadAdapter)) {
			setListShown(true, getWindowToken() != null);
		}
	}

	public
	CharSequence getTitle()
	{
		return mTitle;
	}

	public
	void setTitle(CharSequence title)
	{
		mTitle = title;
	}

	public
	void setEmptyText(CharSequence text)
	{
		if (mStandardEmptyView == null) {
			throw new IllegalStateException("Can't be used with a custom content view");
		}
		mStandardEmptyView.setText(text);
		if (mEmptyText == null) {
			setEmptyView(mStandardEmptyView);
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
		if (mListShown == shown) return;
		mListShown = shown;
		final Context context = getContext();
		if (shown) {
			if (animate) {
				startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
			} else {
				clearAnimation();
			}
			setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
			} else {
				clearAnimation();
			}
			setVisibility(View.GONE);
		}
	}

	public
	void setFooterShown(boolean shown) {
		if (shown) {
			mFooterView.setVisibility(View.VISIBLE);
		} else {
			mFooterView.setVisibility(View.GONE);
		}
	}

	public
	ProgressBar getProgressBar()
	{
		return mProgress;
	}

	public
	LinearLayout getFooterView()
	{
		return mFooterView;
	}

	public
	TextView getStandardEmptyView()
	{
		return mStandardEmptyView;
	}
}
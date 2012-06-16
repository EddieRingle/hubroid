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

package net.idlesoft.android.apps.github.ui.adapters;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract
class BaseListAdapter<T> extends BaseAdapter
{
	protected
	LayoutInflater mInflater;

	private
	ArrayList<T> mData;

	private
	boolean mNotifyOnChange;

	private
	BaseActivity mContext;

	public
	BaseListAdapter(BaseActivity context)
	{
		super();

		mContext = context;
		mInflater = LayoutInflater.from(context.getContext());
		mData = new ArrayList<T>();
	}

	public
	BaseActivity getContext()
	{
		return mContext;
	}

	public
	void clear()
	{
		mData.clear();
		if (mNotifyOnChange)
			notifyDataSetChanged();
	}

	public
	void addAll(Collection<? extends T> collection)
	{
		mData.addAll(collection);
		if (mNotifyOnChange)
			notifyDataSetChanged();
	}

	@Override
	public
	int getCount()
	{
		return mData.size();
	}

	public
	T getItem(int position)
	{
		return mData.get(position);
	}

	@Override
	public
	long getItemId(int position)
	{
		return position;
	}

	@Override
	public
	void unregisterDataSetObserver(DataSetObserver observer)
	{
		if (observer != null)
			super.unregisterDataSetObserver(observer);
	}

	protected
	void fillWithItems(List<T> data, boolean append)
	{
		if (!append)
			clear();
		addAll(data);
	}

	public
	void fillWithItems(List<T> data)
	{
		fillWithItems(data, false);
	}

	public
	void appendWithItems(List<T> data)
	{
		fillWithItems(data, true);
	}

	public
	void setNotifyOnChange(boolean notifyOnChange)
	{
		mNotifyOnChange = notifyOnChange;
	}
}

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

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public
class ListViewPager extends ViewPager
{
	public static class MultiListPagerAdapter extends PagerAdapter
	{
		protected
		ArrayList<IdleList> mLists;

		protected
		Context mContext;

		public
		MultiListPagerAdapter(Context context)
		{
			mContext = context;
			mLists = new ArrayList<IdleList>();
		}

		public
		int getListCount()
		{
			return mLists.size();
		}

		public
		IdleList getList(int position)
		{
			return mLists.get(position);
		}

		public
		void addList(IdleList list, int position)
		{
			mLists.add(position, list);
			notifyDataSetChanged();
		}

		public
		void addList(IdleList list)
		{
			mLists.add(list);
			notifyDataSetChanged();
		}

		public
		void removeList(IdleList list)
		{
			mLists.remove(list);
			notifyDataSetChanged();
		}

		public
		void removeList(int index)
		{
			mLists.remove(index);
			notifyDataSetChanged();
		}

		@Override
		public
		Object instantiateItem(ViewGroup container, int position)
		{
			IdleList list = mLists.get(position);

			/* Add the list to the container */
			if (container.indexOfChild(list) < 0) {
				/* Remove list from its current parent if it has one */
				final ViewGroup existingParent = (ViewGroup) list.getParent();
				if (existingParent != null && existingParent.indexOfChild(list) >= 0)
					existingParent.removeView(list);
				/* Add list to the new container */
				container.addView(list);
			}

			return list;
		}

		@Override
		public
		void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView((View)object);
		}

		@Override
		public
		int getCount()
		{
			return mLists.size();
		}

		@Override
		public
		boolean isViewFromObject(View view, Object object)
		{
			return view == object;
		}

		@Override
		public
		CharSequence getPageTitle(int position)
		{
			return mLists.get(position).getTitle();
		}
	}

	public
	ListViewPager(Context context)
	{
		super(context);
	}

	public
	ListViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public
	void setAdapter(MultiListPagerAdapter adapter)
	{
		super.setAdapter(adapter);
	}

	public
	MultiListPagerAdapter getAdapter()
	{
		return (MultiListPagerAdapter) super.getAdapter();
	}
}

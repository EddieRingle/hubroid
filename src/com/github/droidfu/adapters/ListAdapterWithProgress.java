/* Copyright (c) 2009 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.droidfu.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public abstract class ListAdapterWithProgress<T> extends BaseAdapter {

    private final ArrayList<T> data = new ArrayList<T>();

    private boolean isLoadingData;

    private AbsListView listView;

    private View progressView;

    public ListAdapterWithProgress(final Activity activity, final AbsListView listView,
            final int progressDrawableResourceId) {
        this.listView = listView;
        this.progressView = activity.getLayoutInflater().inflate(progressDrawableResourceId,
                listView, false);
    }

    public ListAdapterWithProgress(final ExpandableListActivity activity,
            final int progressDrawableResourceId) {
        this(activity, activity.getExpandableListView(), progressDrawableResourceId);
    }

    public ListAdapterWithProgress(final ListActivity activity, final int progressDrawableResourceId) {
        this(activity, activity.getListView(), progressDrawableResourceId);
    }

    public void addAll(final List<T> items) {
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addAll(final List<T> items, final boolean redrawList) {
        data.addAll(items);
        if (redrawList) {
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    protected abstract View doGetView(int position, View convertView, ViewGroup parent);

    /**
     * {@inheritDoc}
     * <p>
     * Don't use this to check for the presence of actual data items; use
     * {@link #hasItems()} instead.
     * </p>
     */
    public int getCount() {
        int size = 0;
        if (data != null) {
            size += data.size();
        }
        if (isLoadingData) {
            size += 1;
        }
        return size;
    }

    public ArrayList<T> getData() {
        return data;
    }

    public T getItem(final int position) {
        if (data == null) {
            return null;
        }
        return data.get(position);
    }

    /**
     * @return the actual number of data items in this adapter, ignoring the
     *         progress item.
     */
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemViewType(final int position) {
        if (isPositionOfProgressElement(position)) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return 0;
    }

    public AbsListView getListView() {
        return listView;
    }

    public View getProgressView() {
        return progressView;
    }

    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        if (isPositionOfProgressElement(position)) {
            return progressView;
        }

        return doGetView(position, convertView, parent);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * @return true if there are actual data items, ignoring the progress item.
     */
    public boolean hasItems() {
        return (data != null) && !data.isEmpty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Don't use this to check for the presence of actual data items; use
     * {@link #hasItems()} instead.
     * </p>
     */
    @Override
    public boolean isEmpty() {
        return (getCount() == 0) && !isLoadingData;
    }

    @Override
    public boolean isEnabled(final int position) {
        if (isPositionOfProgressElement(position)) {
            return false;
        }
        return true;
    }

    public boolean isLoadingData() {
        return isLoadingData;
    }

    private boolean isPositionOfProgressElement(final int position) {
        return isLoadingData && (position == data.size());
    }

    public void remove(final int position) {
        data.remove(position);
        notifyDataSetChanged();
    }

    public void setIsLoadingData(final boolean isLoadingData) {
        setIsLoadingData(isLoadingData, true);
    }

    public void setIsLoadingData(final boolean isLoadingData, final boolean redrawList) {
        this.isLoadingData = isLoadingData;
        if (redrawList) {
            notifyDataSetChanged();
        }
    }
}

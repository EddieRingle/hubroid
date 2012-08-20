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
package net.idlesoft.android.apps.github.ui.adapters;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;

import java.util.ArrayList;

/**
 * Utility adapter that supports adding headers and footers
 */
public class HeaderFooterListAdapter<E extends BaseAdapter> extends
        HeaderViewListAdapter {

    private final ListView list;

    private final ArrayList<FixedViewInfo> headers;

    private final ArrayList<FixedViewInfo> footers;

    private final E wrapped;

    /**
     * Create header footer adapter
     */
    public HeaderFooterListAdapter(ListView view, E adapter) {
        this(new ArrayList<FixedViewInfo>(), new ArrayList<FixedViewInfo>(),
                view, adapter);
    }

    private HeaderFooterListAdapter(ArrayList<FixedViewInfo> headerViewInfos,
            ArrayList<FixedViewInfo> footerViewInfos, ListView view, E adapter) {
        super(headerViewInfos, footerViewInfos, adapter);

        headers = headerViewInfos;
        footers = footerViewInfos;
        list = view;
        wrapped = adapter;
    }

    /**
     * Add header
     *
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addHeader(View view, Object data,
            boolean isSelectable) {
        FixedViewInfo info = list.new FixedViewInfo();
        info.view = view;
        info.data = data;
        info.isSelectable = isSelectable;

        headers.add(info);
        wrapped.notifyDataSetChanged();
        return this;
    }

    /**
     * Add header
     *
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addFooter(View view, Object data,
            boolean isSelectable) {
        FixedViewInfo info = list.new FixedViewInfo();
        info.view = view;
        info.data = data;
        info.isSelectable = isSelectable;

        footers.add(info);
        wrapped.notifyDataSetChanged();
        return this;
    }

    @Override
    public boolean removeHeader(View v) {
        boolean removed = super.removeHeader(v);
        if (removed) {
            wrapped.notifyDataSetChanged();
        }
        return removed;
    }

    @Override
    public boolean removeFooter(View v) {
        boolean removed = super.removeFooter(v);
        if (removed) {
            wrapped.notifyDataSetChanged();
        }
        return removed;
    }

    @Override
    public E getWrappedAdapter() {
        return wrapped;
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }
}
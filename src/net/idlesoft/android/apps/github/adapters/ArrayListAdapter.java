/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import java.util.ArrayList;
import java.util.LinkedList;

import net.idlesoft.android.apps.github.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.AbsListView;

import com.github.droidfu.adapters.ListAdapterWithProgress;

public abstract class ArrayListAdapter<E> extends ListAdapterWithProgress<Object> {

    protected Activity mActivity;

    protected LayoutInflater mInflater;

    protected ArrayList<E> mData;

    protected LinkedList<Object> mListData;

    public ArrayListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView, R.layout.loading_listitem);

        mActivity = pActivity;
        mInflater = LayoutInflater.from(pActivity);
        mListData = new LinkedList<Object>();
    }

    public void loadData(final ArrayList<E> pArrayList) {
        mData = pArrayList;

        mListData.clear();
        final int length = pArrayList.size();
        for (int i = 0; i < length; i++) {
            mListData.add(i, pArrayList.get(i));
        }
    }

    /**
     * Called in the UI thread after loadData() has been called; populates the
     * adapter
     */
    public void pushData() {
        if (!hasItems()) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    addAll(mListData);
                }
            });
        }
    }
}

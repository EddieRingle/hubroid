/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import java.util.LinkedList;

import net.idlesoft.android.apps.github.R;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.AbsListView;

import com.github.droidfu.adapters.ListAdapterWithProgress;

public abstract class JsonListAdapter extends ListAdapterWithProgress<Object> {

    protected Activity mActivity;

    protected LayoutInflater mInflater;

    protected LinkedList<Object> mListData;

    protected JSONArray mJson;

    public JsonListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView, R.layout.loading_listitem);

        mActivity = pActivity;
        mInflater = LayoutInflater.from(pActivity);
        mListData = new LinkedList<Object>();
    }

    /**
     * Load data from Json into a LinkedList
     * @param pJsonArray
     */
    public void loadData(final JSONArray pJsonArray) {
        mJson = pJsonArray;

        mListData.clear();
        int length = pJsonArray.length();
        for (int i = 0; i < length; i++) {
            try {
                mListData.add(i, pJsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called in the UI thread after loadData() has been called; populates the adapter
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

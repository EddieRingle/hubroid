/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class GravatarArrayListAdapter<E> extends ArrayListAdapter<E> {
    public static class ViewHolder {
        public ImageView gravatar;

        public TextView text;
    }

    protected HashMap<String, Bitmap> mGravatars;

    public GravatarArrayListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public void loadData(final ArrayList<E> pArrayList) {
        super.loadData(pArrayList);
        /*
         * Load gravatars after the ArrayList has loaded but before the
         * LinkedList is populated
         */
        mGravatars = new HashMap<String, Bitmap>(mListData.size());
        loadGravatars();
    }

    /**
     * Gravatar loading method
     */
    public abstract void loadGravatars();

    @Override
    public void pushData() {
        super.pushData();
    }
}

/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import java.util.HashMap;

import org.json.JSONArray;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class GravatarListAdapter extends JsonListAdapter {
    public class ViewHolder {
        public ImageView gravatar;

        public TextView text;
    }

    protected HashMap<String, Bitmap> mGravatars;

    public GravatarListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public void loadData(JSONArray pJsonArray) {
        super.loadData(pJsonArray);
        /* Load gravatars after Json has loaded but before the LinkedList is populated */
        mGravatars = new HashMap<String, Bitmap>(mListData.size());
        loadGravatars();
    }

    @Override
    public void pushData() {
        super.pushData();
    }

    /**
     * Gravatar loading method
     */
    public abstract void loadGravatars();
}

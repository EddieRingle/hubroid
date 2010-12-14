/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.adapters;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public abstract class UsersListAdapter extends BaseAdapter {
    public class ViewHolder {
        public ImageView gravatar;

        public TextView text;
    }

    protected Context m_context;

    public int m_currentIndex;

    protected ViewHolder m_currentViewHolder;

    protected JSONArray m_data = new JSONArray();

    protected SharedPreferences.Editor m_editor;

    protected HashMap<String, Bitmap> m_gravatars;

    protected LayoutInflater m_inflater;

    protected SharedPreferences m_prefs;

    public UsersListAdapter(final Context context, final JSONArray jsonarray) {
        m_context = context;
        m_inflater = LayoutInflater.from(m_context);
        m_data = jsonarray;
        m_gravatars = new HashMap<String, Bitmap>(m_data.length());
    }

    public int getCount() {
        return m_data.length();
    }

    public Object getItem(final int i) {
        try {
            return m_data.get(i);
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(final int i) {
        return i;
    }

    public abstract void loadGravatars();
}

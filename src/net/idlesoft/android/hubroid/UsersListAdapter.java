/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.hubroid;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class UsersListAdapter extends BaseAdapter {
	protected JSONArray m_data = new JSONArray();
	public int m_currentIndex;
	protected ViewHolder m_currentViewHolder;
	protected Context m_context;
	protected LayoutInflater m_inflater;
	protected HashMap<String, Bitmap> m_gravatars;
	protected SharedPreferences  m_prefs;
	protected SharedPreferences.Editor m_editor;

	public class ViewHolder {
		public TextView text;
		public ImageView gravatar;
	}

	public UsersListAdapter(final Context context, JSONArray jsonarray) {
		m_context = context;
		m_inflater = LayoutInflater.from(m_context);
		m_data = jsonarray;
		m_gravatars = new HashMap<String, Bitmap>(m_data.length());
	}

	public int getCount() {
		return m_data.length();
	}

	public Object getItem(int i) {
		try {
			return m_data.get(i);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public long getItemId(int i) {
		return i;
	}
}
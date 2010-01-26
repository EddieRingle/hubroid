package org.idlesoft.android.hubroid;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UsersListAdapter extends BaseAdapter {
	protected JSONArray m_data = new JSONArray();
	public int m_currentIndex;
	protected ViewHolder m_currentViewHolder;
	protected Context m_context;
	protected LayoutInflater m_inflater;
	public Bitmap[] m_gravatars;
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
		m_gravatars = new Bitmap[m_data.length()];

		this.loadGravatars();
	}

	public void loadGravatars()
	{
		for (int i = 0; !m_data.isNull(i); i++) {
			try {
				m_gravatars[i] = Hubroid.getGravatar(m_data.getJSONObject(i).getString("gravatar_id"), 30);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
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

	public View getView(int index, View convertView, ViewGroup parent) {
		m_currentIndex = index;
		m_currentViewHolder = new ViewHolder();
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.user_list_item, null);
			m_currentViewHolder = new ViewHolder();
			m_currentViewHolder.text = (TextView) convertView.findViewById(R.id.tv_user_list_item_name);
			m_currentViewHolder.gravatar = (ImageView) convertView.findViewById(R.id.iv_user_list_gravatar);
			convertView.setTag(m_currentViewHolder);
		} else {
			m_currentViewHolder = (ViewHolder) convertView.getTag();
		}
		try {
			m_currentViewHolder.text.setText(m_data.getJSONObject(m_currentIndex).getString("name"));
			m_currentViewHolder.gravatar.setImageBitmap(m_gravatars[m_currentIndex]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
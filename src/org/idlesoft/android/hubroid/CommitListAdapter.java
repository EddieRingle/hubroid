package org.idlesoft.android.hubroid;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommitListAdapter extends BaseAdapter {
	private JSONArray m_data = new JSONArray();
	private int m_currentIndex;
	private Context m_context;
	private LayoutInflater m_inflater;
	private Bitmap[] m_gravatars;

	public static class ViewHolder {
		public TextView commit_shortdesc;
		public ImageView gravatar;
	}

	public void loadGravatars()
	{
		for (int i = 0; !m_data.isNull(i); i++) {
			try {
				String login = m_data.getJSONObject(i).getJSONObject("author").getString("login");
				if (login != "") {
					String id = Hubroid.getGravatarID(login);
					m_gravatars[i] = Hubroid.getGravatar(id, 30);
				} else {
					m_gravatars[i] = null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public CommitListAdapter(final Context context, JSONArray jsonarray) {
		m_context = context;
		m_inflater = LayoutInflater.from(m_context);
		m_data = jsonarray;
		m_gravatars = new Bitmap[m_data.length()];

		this.loadGravatars();
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
		ViewHolder holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.commit_list_item, null);
			holder = new ViewHolder();
			holder.commit_shortdesc = (TextView) convertView.findViewById(R.id.commit_list_item_shortdesc);
			holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_commit_list_item_gravatar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		try {
			holder.gravatar.setImageBitmap(m_gravatars[index]);
			String description = m_data.getJSONObject(index).getString("message");
			holder.commit_shortdesc.setText(description.split("\n")[0]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
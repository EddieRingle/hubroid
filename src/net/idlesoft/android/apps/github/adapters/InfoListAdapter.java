
package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

public class InfoListAdapter extends JsonListAdapter {
    public static final class ViewHolder {
        TextView title;

        TextView content;
    };

    public InfoListAdapter(Activity pActivity, AbsListView pListView) {
        super(pActivity, pListView);
    }

    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.info_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.content = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            final JSONObject data = (JSONObject) getData().get(index);
            final String title = data.getString("title");
            final String content = data.getString("content");
            holder.title.setText(title);
            if (content == null || content.equals("")) {
                holder.content.setVisibility(View.GONE);
            } else {
                holder.content.setText(content);
            }
        } catch (JSONException e) {
            convertView.setVisibility(View.GONE);
            e.printStackTrace();
        }

        return convertView;
    }
}

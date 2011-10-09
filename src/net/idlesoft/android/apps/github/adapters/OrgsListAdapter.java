
package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.eclipse.egit.github.core.User;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class OrgsListAdapter extends GravatarArrayListAdapter<User> {

    public OrgsListAdapter(Activity pActivity, AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.user_list_item, null);
            holder = new ViewHolder();
            holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_user_list_gravatar);
            holder.text = (TextView) convertView.findViewById(R.id.tv_user_list_item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.gravatar.setImageBitmap(mGravatars.get(mData.get(index).getLogin()));
        holder.text.setText(mData.get(index).getLogin());

        return convertView;
    }

    @Override
    public void loadGravatars() {
        final int length = mListData.size();
        for (int i = 0; i < length; i++) {
            final String username = mData.get(i).getLogin();
            mGravatars.put(username, GravatarCache.getDipGravatar(GravatarCache
                    .getGravatarID(username), 30.0f,
                    mActivity.getResources().getDisplayMetrics().density));
        }
    }
}

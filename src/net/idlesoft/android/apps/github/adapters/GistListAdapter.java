package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.Gist;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.Date;

public class GistListAdapter extends ArrayListAdapter<Gist> {
    public static class ViewHolder {
        public TextView gistId;
        public TextView gistDescription;
        public TextView gistLastModified;
        public TextView gistFileCount;
    }

    public GistListAdapter(Activity pActivity, AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.gist_list_item, null);
            holder = new ViewHolder();
            holder.gistId = (TextView) convertView.findViewById(R.id.tv_gist_id);
            holder.gistDescription = (TextView) convertView.findViewById(R.id.tv_gist_description);
            holder.gistLastModified = (TextView) convertView.findViewById(R.id.tv_gist_lastModified);
            holder.gistFileCount = (TextView) convertView.findViewById(R.id.tv_gist_fileCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        /* Calculate time difference between the then and now */
        String timeEnd;
        final Date updatedTime = mData.get(index).getUpdatedAt();
        final Date currentTime = new Date();
        final long ms = currentTime.getTime() - updatedTime.getTime();
        final long sec = ms / 1000;
        final long min = sec / 60;
        final long hour = min / 60;
        final long day = hour / 24;
        final long week = day / 7;
        final long month = week / 4;
        final long year = day / 365;

        if (year > 0) {
            if (year == 1) {
                timeEnd = " year ago";
            } else {
                timeEnd = " years ago";
            }
            holder.gistLastModified.setText("Updated " + year + timeEnd);
        } else if (month > 0) {
            if (month == 1) {
                timeEnd = " month ago";
            } else {
                timeEnd = " months ago";
            }
            holder.gistLastModified.setText("Updated " + month + timeEnd);
        } else if (week > 0) {
            if (week == 1) {
                timeEnd = " week ago";
            } else {
                timeEnd = " weeks ago";
            }
            holder.gistLastModified.setText("Updated " + week + timeEnd);
        } else if (day > 0) {
            if (day == 1) {
                timeEnd = " day ago";
            } else {
                timeEnd = " days ago";
            }
            holder.gistLastModified.setText("Updated " + day + timeEnd);
        } else if (hour > 0) {
            if (hour == 1) {
                timeEnd = " hour ago";
            } else {
                timeEnd = " hours ago";
            }
            holder.gistLastModified.setText("Updated " + hour + timeEnd);
        } else if (min > 0) {
            if (min == 1) {
                timeEnd = " minute ago";
            } else {
                timeEnd = " minutes ago";
            }
            holder.gistLastModified.setText("Updated " + min + timeEnd);
        } else {
            if (sec == 1) {
                timeEnd = " second ago";
            } else {
                timeEnd = " seconds ago";
            }
            holder.gistLastModified.setText("Updated " + sec + timeEnd);
        }

        holder.gistId.setText(mData.get(index).getId());
        holder.gistDescription.setText(mData.get(index).getDescription());
        holder.gistFileCount.setText(mData.get(index).getFiles().size() + " file(s)");

        return convertView;
    }
}

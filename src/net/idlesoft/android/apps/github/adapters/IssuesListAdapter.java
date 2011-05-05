/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Hubroid;

import org.json.JSONException;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class IssuesListAdapter extends JsonListAdapter {
    public static class ViewHolder {
        public ImageView issueIcon;

        public TextView issueLastUpdatedDate;

        public TextView issueNumber;

        public TextView issueTitle;
    }

    public IssuesListAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.issue_list_item, null);
            holder = new ViewHolder();
            holder.issueIcon = (ImageView) convertView.findViewById(R.id.iv_issue_list_item_icon);
            holder.issueNumber = (TextView) convertView
                    .findViewById(R.id.tv_issue_list_item_number);
            holder.issueTitle = (TextView) convertView.findViewById(R.id.tv_issue_list_item_title);
            holder.issueLastUpdatedDate = (TextView) convertView
                    .findViewById(R.id.tv_issue_list_item_updated_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date commit_time = dateFormat.parse(mJson.getJSONObject(index).getString(
                    "updated_at"));
            final Date current_time = new Date();
            final long ms = current_time.getTime() - commit_time.getTime();
            final long sec = ms / 1000;
            final long min = sec / 60;
            final long hour = min / 60;
            final long day = hour / 24;
            final long year = day / 365;
            if (year > 0) {
                if (year == 1) {
                    end = " year ago";
                } else {
                    end = " years ago";
                }
                holder.issueLastUpdatedDate.setText("Updated " + year + end);
            }
            if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                holder.issueLastUpdatedDate.setText("Updated " + day + end);
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                holder.issueLastUpdatedDate.setText("Updated " + hour + end);
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                holder.issueLastUpdatedDate.setText("Updated " + min + end);
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                holder.issueLastUpdatedDate.setText("Updated " + sec + end);
            }
            if (mJson.getJSONObject(index).getString("state").equalsIgnoreCase("open")) {
                holder.issueIcon.setImageResource(R.drawable.issues_open);
            } else {
                holder.issueIcon.setImageResource(R.drawable.issues_closed);
            }
            holder.issueNumber.setText("#" + mJson.getJSONObject(index).getString("number"));
            holder.issueTitle.setText(mJson.getJSONObject(index).getString("title"));
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

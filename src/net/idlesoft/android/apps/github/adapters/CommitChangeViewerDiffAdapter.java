/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
public class CommitChangeViewerDiffAdapter extends BaseAdapter {

    public static class ViewHolder {
        public TextView diffContents;

        public TextView fileName;
    }

    private final Context mContext;

    private JSONArray mJson = new JSONArray();

    private final LayoutInflater mInflater;

    public CommitChangeViewerDiffAdapter(final Context context, final JSONArray jsonarray) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mJson = jsonarray;
    }

    public int getCount() {
        return mJson.length();
    }

    public Object getItem(final int i) {
        try {
            return mJson.get(i);
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(final int i) {
        return i;
    }

    public View getView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.commit_view_item, null);
            holder = new ViewHolder();
            holder.diffContents = (TextView) convertView
                    .findViewById(R.id.commit_view_item_diff_text);
            holder.diffContents.setMovementMethod(ScrollingMovementMethod.getInstance());
            holder.fileName = (TextView) convertView.findViewById(R.id.commit_view_item_filename);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            // TODO: Make this work properly for mime types

            // Replace tabs with two spaces so more file fits on the screen
            final String diff = mJson.getJSONObject(index).getString("diff")
                    .replaceAll("\t", "  ");

            holder.diffContents.setText(diff, TextView.BufferType.SPANNABLE);

            // Apply some styles
            final Resources res = mContext.getResources();

            final Spannable str = (Spannable) holder.diffContents.getText();

            // Separate the diff by lines
            final String[] diffContents = diff.split("\n");
            for (final String diffLine : diffContents) {
                if (diffLine.startsWith("+")) {
                    // TODO: Using indexOf here might be problems if there are
                    // two identical lines
                    // (it'll only pick the first one), should use the first
                    // line of the diff to see
                    // changed line number
                    str.setSpan(new BackgroundColorSpan(res.getColor(R.color.addedText)), diff
                            .indexOf(diffLine), diff.indexOf(diffLine) + diffLine.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (diffLine.startsWith("-")) {
                    str.setSpan(new BackgroundColorSpan(res.getColor(R.color.removedText)), diff
                            .indexOf(diffLine), diff.indexOf(diffLine) + diffLine.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (diffLine.startsWith("@@")) {
                    str.setSpan(new BackgroundColorSpan(res.getColor(R.color.diffAtAtText)), diff
                            .indexOf(diffLine), diff.indexOf(diffLine) + diffLine.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // Set the filename
            holder.fileName.setText(mJson.getJSONObject(index).getString("filename"));

            // TODO: images + binary files, non text/plain commits

        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

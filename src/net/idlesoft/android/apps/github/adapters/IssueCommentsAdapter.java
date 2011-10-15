/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Profile;
import net.idlesoft.android.apps.github.activities.SingleIssue;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.eclipse.egit.github.core.Comment;

import android.app.Activity;
import android.content.Intent;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueCommentsAdapter extends GravatarArrayListAdapter<Comment> {
    public static class ViewHolder {
        public TextView body;

        public ImageView gravatar;

        public TextView meta;
    }

    public IssueCommentsAdapter(final Activity pActivity, final AbsListView pListView) {
        super(pActivity, pListView);
    }

    @Override
    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.issue_comment, null);
            holder = new ViewHolder();
            holder.meta = (TextView) convertView.findViewById(R.id.tv_issue_comment_meta);
            holder.body = (TextView) convertView.findViewById(R.id.tv_issue_comment_body);
            holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_issue_comment_gravatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.meta.setText("Posted " + SingleIssue.getTimeSince(mData.get(index).getCreatedAt())
                + " ago by " + mData.get(index).getUser().getLogin());
        holder.gravatar.setImageBitmap(mGravatars.get(mData.get(index).getUser().getLogin()));
        // Open a user's profile when their gravatar is clicked in their comment
        holder.gravatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(mActivity, Profile.class);
                i.putExtra("username", mData.get(index).getUser().getLogin());
                mActivity.startActivity(i);
            }
        });
        if (mData.get(index).getBody() != null) {
            holder.body.setText(mData.get(index).getBody().replaceAll("\r\n", "\n")
                    .replaceAll("\r", "\n"));
        }
        // Link all URLs/Email addresses/phone numbers in the comment body
        Linkify.addLinks(holder.body, Linkify.ALL);
        // Pattern to match @username
        Pattern usernameMatcher = Pattern.compile("\\B@([A-Za-z0-9_]+)\\b");
        // Link @username to https://github.com/username
        Linkify.addLinks(holder.body, usernameMatcher, "https://github.com/", null,
                new TransformFilter() {
            public final String transformUrl(Matcher match, String url) {
                // Only return username, not @username
                return match.group(1);
            }
        });
        // Pattern to match issue #number
        Pattern numberMatcher = Pattern.compile("\\B#([0-9]+)\\b");
        // Get repository owner and name from API url
        String[] urlParts = mData.get(index).getUrl().split("/");
        String repo_owner = urlParts[4];
        String repo_name = urlParts[5];
        // Link #number to https://github.com/repo_owner/repo_name/issues/number
        Linkify.addLinks(holder.body, numberMatcher, "https://github.com/" + repo_owner + "/"
                + repo_name + "/issues/", null, new TransformFilter() {
                    public String transformUrl(Matcher match, String url) {
                        // Only return the number, leaving out the hash sign
                        return match.group(1);
                    }
                });
        return convertView;
    }

    /**
     * Get the Gravatars of all users in the commit log This method is different
     * from the gravatar loaders in other adapters, in that we only need to get
     * the first one if we are displaying a public activity feed for a single
     * user
     */
    @Override
    public void loadGravatars() {
        final int length = mListData.size();
        for (int i = 0; i < length; i++) {
            final String actor = mData.get(i).getUser().getLogin();
            if (!mGravatars.containsKey(actor)) {
                mGravatars.put(actor, GravatarCache.getDipGravatar(actor, 30.0f, mActivity
                        .getResources().getDisplayMetrics().density));
            }
        }
    }
}

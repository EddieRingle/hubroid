/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.adapters;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.petebevin.markdown.MarkdownFilter;
import com.petebevin.markdown.MarkdownProcessor;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.fragments.ProfileFragment;
import net.idlesoft.android.apps.github.ui.widgets.FlowLayout;
import net.idlesoft.android.apps.github.ui.widgets.OcticonView;
import net.idlesoft.android.apps.github.utils.IssueUtils;
import net.idlesoft.android.apps.github.utils.StringUtils;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GsonUtils;

public
class IssueCommentListAdapter extends BaseListAdapter<Comment>
{
	public static
	class ViewHolder
	{
		public
		ImageView gravatar;
		public
		TextView author;
		public
		TextView meta;
		public
		TextView content;
	}

	public
	IssueCommentListAdapter(BaseActivity context)
	{
		super(context);
	}

	@Override
	public
	View getView(int position, View convertView, ViewGroup parent)
	{
		final Comment comment = getItem(position);
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.issue_comment_list_item, null);
			holder = new ViewHolder();
			holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_issue_comment_gravatar);
			holder.author = (TextView) convertView.findViewById(R.id.tv_issue_comment_author);
			holder.meta = (TextView) convertView.findViewById(R.id.tv_issue_comment_meta);
			holder.content = (TextView) convertView.findViewById(R.id.tv_issue_comment);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final AQuery aq = new AQuery(convertView);
		aq.id(holder.gravatar).image(comment.getUser().getAvatarUrl(), true, true, 200,
									 R.drawable.gravatar, null, AQuery.FADE_IN_NETWORK, 1.0f);

		holder.gravatar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public
			void onClick(View v)
			{
				final Bundle args = new Bundle();
				args.putString(HubroidConstants.ARG_TARGET_USER, GsonUtils.toJson(comment.getUser()));
				getContext().startFragmentTransaction();
				getContext().addFragmentToTransaction(ProfileFragment.class,
												 R.id.fragment_container_more, args);
				getContext().finishFragmentTransaction();
			}
		});

		holder.author.setText(comment.getUser().getLogin());
		holder.meta.setText("commented " + StringUtils.getTimeSince(comment.getCreatedAt()) +
									" ago");

		MarkdownProcessor processor = new MarkdownProcessor();
		final String processed = processor.markdown(comment.getBody());
		holder.content.setText(StringUtils.trimTrailingWhitespace(Html.fromHtml(processed)));
		holder.content.setMovementMethod(new LinkMovementMethod());

		return convertView;
	}
}

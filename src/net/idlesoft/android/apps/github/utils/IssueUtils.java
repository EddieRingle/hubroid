/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.androidquery.AQuery;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.adapters.IssuesListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.ProfileFragment;
import net.idlesoft.android.apps.github.ui.widgets.FlowLayout;
import net.idlesoft.android.apps.github.ui.widgets.OcticonView;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GsonUtils;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static net.idlesoft.android.apps.github.utils.StringUtils.getTimeSince;

public
class IssueUtils
{
	public static
	View viewFromIssue(BaseActivity context, Issue issue)
	{
		final LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.issue_list_item, null);
		IssuesListAdapter.ViewHolder holder = new IssuesListAdapter.ViewHolder();
		holder.gravatar = (ImageView) view.findViewById(R.id.iv_issue_gravatar);
		holder.meta = (TextView) view.findViewById(R.id.tv_issue_meta);
		holder.title = (TextView) view.findViewById(R.id.tv_issue_title);
		holder.status = (OcticonView) view.findViewById(R.id.ov_issue_status);
		holder.comment_count = (TextView) view.findViewById(R.id.tv_issue_comment_count);
		holder.extras = (LinearLayout) view.findViewById(R.id.ll_issue_extras_area);
		holder.labels = (FlowLayout) view.findViewById(R.id.ll_issue_labels);
		holder.milestone = (TextView) view.findViewById(R.id.tv_issue_milestone);
		view.setTag(holder);

		holder = fillHolder(context, holder, issue);

		return view;
	}

	public static
	IssuesListAdapter.ViewHolder fillHolder(final BaseActivity context,
											final IssuesListAdapter.ViewHolder holder,
											final Issue issue)
	{
		if (holder == null || issue == null) return null;

		holder.meta.setText("#" + Integer.toString(issue.getNumber()) + " opened by " +
									issue.getUser().getLogin() + " " +
									getTimeSince(issue.getCreatedAt()) + " ago");
		holder.title.setText(issue.getTitle());

		final AQuery aq = new AQuery(context);
		aq.id(holder.gravatar).image(issue.getUser().getAvatarUrl(), true, true, 200,
									 R.drawable.gravatar, null, AQuery.FADE_IN_NETWORK, 1.0f);

		holder.gravatar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public
			void onClick(View v)
			{
				final Bundle args = new Bundle();
				args.putString(HubroidConstants.ARG_TARGET_USER, GsonUtils.toJson(issue.getUser()));
				context.startFragmentTransaction();
				context.addFragmentToTransaction(ProfileFragment.class,
												 R.id.fragment_container, args);
				context.finishFragmentTransaction();
			}
		});

		/* Make sure labels layout is empty */
		holder.labels.removeAllViews();

		final float[] roundedValues = {2,2, 2,2, 2,2, 2,2};
		ArrayList<Label> labels = new ArrayList<Label>(issue.getLabels());
		final TextView labelLabel = new TextView(context);

		labelLabel.setText("Labels:");
		labelLabel.setTextColor(Color.parseColor(context.getString(R.color.light_text_color)));
		labelLabel.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
		labelLabel.setTextSize(10.0f);
		labelLabel.setPadding(0, 2, 0, 2);

		if (labels.size() > 0) {
			holder.labels.addView(labelLabel, new FlowLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
			for (Label l : labels) {
				final TextView label = new TextView(context);
				final ShapeDrawable sd = new ShapeDrawable(
						new RoundRectShape(roundedValues, null, null));
				final int color = Color.parseColor("#" + l.getColor());
				final int r = Color.red(color);
				final int g = Color.green(color);
				final int b = Color.blue(color);
				/* Set background color to label color */
				sd.getPaint().setColor(color);
				label.setBackgroundDrawable(sd);
				/* Set label text */
				label.setText(l.getName());
				/* Calculate YIQ color contrast */
				if ((((r*299)+(g*587)+(b*114))/1000) >= 128) {
					label.setTextColor(Color.BLACK);
					label.getPaint().setShadowLayer(1.0f, -1, -1, Color.WHITE);
				} else {
					label.setTextColor(Color.WHITE);
					label.getPaint().setShadowLayer(1.0f, 1, 1, Color.BLACK);
				}
				label.setTextSize(10.0f);
				label.setPadding(5, 2, 5, 2);
				holder.labels.addView(label, new FlowLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
			}
			holder.labels.setVisibility(View.VISIBLE);
			holder.extras.setVisibility(View.VISIBLE);
		} else {
			holder.labels.setVisibility(View.GONE);
			holder.extras.setVisibility(View.GONE);
		}

		/* Set issue milestone */
		if (issue.getMilestone() != null) {
			SpannableStringBuilder milestoneBuilder = new SpannableStringBuilder();
			milestoneBuilder.append("Milestone: " + issue.getMilestone().getTitle());
			milestoneBuilder.setSpan(
					new StyleSpan(Typeface.BOLD), 0, 10, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			holder.milestone.setText(milestoneBuilder);
			holder.milestone.setVisibility(View.VISIBLE);
			holder.extras.setVisibility(View.VISIBLE);
		}

		/* Set comment count */
		holder.comment_count.setText(Integer.toString(issue.getComments()));

		/* Set issue status Octicon */
		if (issue.getState().equals("open")) {
			holder.status.setOcticon(OcticonView.IC_ISSUE_OPENED);
			holder.status.setGlyphColor(Color.parseColor(context.getString(R.color.issue_green)));
		} else {
			holder.status.setOcticon(OcticonView.IC_ISSUE_CLOSED);
			holder.status.setGlyphColor(Color.parseColor(context.getString(R.color.issue_red)));
		}

		return holder;
	}
}

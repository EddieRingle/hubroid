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

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.androidquery.AQuery;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.fragments.ProfileFragment;
import net.idlesoft.android.apps.github.ui.widgets.GravatarView;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GsonUtils;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static net.idlesoft.android.apps.github.utils.StringUtils.getTimeSince;

public
class IssuesListAdapter extends BaseListAdapter<Issue>
{
	public static
	class ViewHolder
	{
		public
		TextView number;
		public
		ImageView gravatar;
		public
		TextView meta;
		public
		TextView title;
		public
		LinearLayout labels;
	}

	public
	IssuesListAdapter(BaseActivity context)
	{
		super(context);
	}

	@Override
	public
	View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.issue_list_item, null);
			holder = new ViewHolder();
			holder.number = (TextView) convertView.findViewById(R.id.tv_issue_number);
			holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_issue_gravatar);
			holder.meta = (TextView) convertView.findViewById(R.id.tv_issue_meta);
			holder.title = (TextView) convertView.findViewById(R.id.tv_issue_title);
			holder.labels = (LinearLayout) convertView.findViewById(R.id.ll_issue_labels);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Issue issue = getItem(position);

		holder.number.setText("#" + Integer.toString(issue.getNumber()));
		holder.meta.setText("By " + issue.getUser().getLogin() + " " +
									getTimeSince(issue.getCreatedAt()) + " ago");
		holder.title.setText(issue.getTitle());

		final AQuery aq = new AQuery(convertView);
		aq.id(holder.gravatar).image(issue.getUser().getAvatarUrl(), true, true, 200, R.drawable.gravatar, null, AQuery.FADE_IN_NETWORK, 1.0f);

		holder.gravatar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public
			void onClick(View v)
			{
				final Bundle args = new Bundle();
				args.putString(HubroidConstants.ARG_TARGET_USER, GsonUtils.toJson(issue.getUser()));
				getContext().startFragmentTransaction();
				getContext().addFragmentToTransaction(ProfileFragment.class,
													  R.id.fragment_container_more, args);
				getContext().finishFragmentTransaction();
			}
		});

		/* Make sure labels layout is empty */
		holder.labels.removeAllViews();

		final float[] roundedValues = {2,2, 2,2, 2,2, 2,2};
		ArrayList<Label> labels = new ArrayList<Label>(issue.getLabels());
		for (Label l : labels) {
			final TextView label = new TextView(getContext());
			final LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
			final ShapeDrawable sd = new ShapeDrawable(new RoundRectShape(roundedValues, null, null));
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
			if ((((r*299)+(g*587)+(b*114))/1000) >= 128)
				label.setTextColor(Color.BLACK);
			else
				label.setTextColor(Color.WHITE);
			label.setTextSize(10.0f);
			if (labels.indexOf(label) != labels.size() - 1)
				params.setMargins(0, 0, 5, 0);
			label.setPadding(5, 2, 5, 2);
			label.setLayoutParams(params);
			holder.labels.addView(label);
		}

		return convertView;
	}
}

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

package net.idlesoft.android.apps.github.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.widgets.FlowLayout;
import net.idlesoft.android.apps.github.ui.widgets.OcticonView;
import net.idlesoft.android.apps.github.utils.IssueUtils;
import org.eclipse.egit.github.core.Issue;

public
class IssuesListAdapter extends BaseListAdapter<Issue>
{
	public static
	class ViewHolder
	{
		public
		ImageView gravatar;
		public
		TextView meta;
		public
		TextView title;
		public
		OcticonView status;
		public
		TextView comment_count;
		public
		LinearLayout extras;
		public
		FlowLayout labels;
		public
		TextView milestone;
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
		final Issue issue = getItem(position);
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.issue_list_item, null);
			holder = new ViewHolder();
			holder.gravatar = (ImageView) convertView.findViewById(R.id.iv_issue_gravatar);
			holder.meta = (TextView) convertView.findViewById(R.id.tv_issue_meta);
			holder.title = (TextView) convertView.findViewById(R.id.tv_issue_title);
			holder.status = (OcticonView) convertView.findViewById(R.id.ov_issue_status);
			holder.comment_count = (TextView) convertView.findViewById(R.id.tv_issue_comment_count);
			holder.extras = (LinearLayout) convertView.findViewById(R.id.ll_issue_extras_area);
			holder.labels = (FlowLayout) convertView.findViewById(R.id.ll_issue_labels);
			holder.milestone = (TextView) convertView.findViewById(R.id.tv_issue_milestone);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		/* Fill the holder with data from the issue */
		holder = IssueUtils.fillHolder(getContext(), holder, issue);

		return convertView;
	}
}

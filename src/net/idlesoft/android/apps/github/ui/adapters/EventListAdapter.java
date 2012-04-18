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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.fragments.ProfileFragment;
import net.idlesoft.android.apps.github.ui.fragments.RepositoryFragment;
import net.idlesoft.android.apps.github.ui.widgets.GravatarView;
import net.idlesoft.android.apps.github.utils.EventUtil;
import net.idlesoft.android.apps.github.utils.StringUtils;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.event.*;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;

public
class EventListAdapter extends BaseListAdapter<Event>
{
	public static
	class ViewHolder
	{
		public
		GravatarView gravatar;
		public
		TextView title;
		public
		TextView extra;
	}

	public
	EventListAdapter(BaseActivity context)
	{
		super(context);
	}

	@Override
	public
	View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.event_list_item, null);
			holder = new ViewHolder();
			holder.gravatar = (GravatarView) convertView.findViewById(R.id.iv_event_gravatar);
			holder.title = (TextView) convertView.findViewById(R.id.tv_event_title);
			holder.extra = (TextView) convertView.findViewById(R.id.tv_event_extra);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Event event = getItem(position);
		final String type = event.getType();
		EventUtil.fillHolderWithEvent(holder, event);

		holder.gravatar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public
			void onClick(View v)
			{
				final Bundle args = new Bundle();
				args.putString(HubroidConstants.ARG_TARGET_USER, GsonUtils.toJson(event.getActor()));
				getContext().startFragmentTransaction();
				getContext().addFragmentToTransaction(ProfileFragment.class,
													  R.id.fragment_container_more, args);
				getContext().finishFragmentTransaction();
			}
		});

		return convertView;
	}
}

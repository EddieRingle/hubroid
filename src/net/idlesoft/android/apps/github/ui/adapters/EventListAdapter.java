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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.widgets.GravatarView;
import org.eclipse.egit.github.core.event.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public
class EventListAdapter extends BaseListAdapter<Event>
{
	public static
	class ViewHolder
	{
		GravatarView gravatar;
		TextView title;
		TextView date;
	}

	public
	EventListAdapter(Context pContext)
	{
		super(pContext);
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
			holder.date = (TextView) convertView.findViewById(R.id.tv_event_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Event e = getItem(position);

		final String login = e.getActor().getLogin();
		final String type = e.getType();

		holder.gravatar.setDefaultResource(R.drawable.ic_dash_profile_normal);
		holder.title.setText(login + " did a " + type);
		holder.date.setText(e.getCreatedAt().toString());

		return convertView;
	}
}

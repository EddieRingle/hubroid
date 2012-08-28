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

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;

import org.eclipse.egit.github.core.Repository;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RepositoryListAdapter extends BaseListAdapter<Repository> {

    public static class ViewHolder {

        TextView owner;

        TextView name;

        TextView description;

        TextView forks;

        TextView watchers;
    }

    public RepositoryListAdapter(BaseActivity context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.repository_list_item, null);
            holder = new ViewHolder();

            holder.owner = (TextView) convertView.findViewById(R.id.tv_repository_owner);
            holder.name = (TextView) convertView.findViewById(R.id.tv_repository_name);
            holder.description =
                    (TextView) convertView.findViewById(R.id.tv_repository_description);
            holder.forks = (TextView) convertView.findViewById(R.id.tv_repository_forks);
            holder.watchers = (TextView) convertView.findViewById(R.id.tv_repository_watchers);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Repository r = getItem(position);

        holder.owner.setText(r.getOwner().getLogin());
        holder.name.setText(r.getName());
        holder.description.setText(r.getDescription());
        holder.forks.setText(Integer.toString(r.getForks()));
        holder.watchers.setText(Integer.toString(r.getWatchers()));

        return convertView;
    }
}
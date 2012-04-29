/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.utils;

import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.EventListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.ProfileFragment;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.GollumPage;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.event.*;

public
class EventUtil
{
	public static
	void fillHolderWithEvent(final EventListAdapter.ViewHolder holder, final Event event)
	{
		final String login = event.getActor().getLogin();
		final String type = event.getType();

		holder.gravatar.setDefaultResource(R.drawable.gravatar);

		SpannableStringBuilder titleBuilder = new SpannableStringBuilder();
		titleBuilder.append(login + " ");
		titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
							 0, login.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		SpannableStringBuilder extraBuilder = new SpannableStringBuilder();

		if (type.equals(Event.TYPE_COMMIT_COMMENT)) {
			final CommitCommentPayload p = (CommitCommentPayload) event.getPayload();
			titleBuilder.append("commented on " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			extraBuilder.append(p.getComment().getBody());
		} else if (type.equals(Event.TYPE_CREATE)) {
			final CreatePayload p = (CreatePayload) event.getPayload();
			if (p.getRefType().equals("repository")) {
				titleBuilder.append("created " + p.getRefType() + " " +
									event.getRepo().getName().split("/")[1]);
				int index =
						titleBuilder.toString().indexOf(event.getRepo().getName().split("/")[1]);
				titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
									 index,
									 index + event.getRepo().getName().split("/")[1].length(),
									 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			} else {
				titleBuilder.append("created " + p.getRefType() + " " + p.getRef() +
											" at " + event.getRepo().getName());
				int index = titleBuilder.toString().indexOf(event.getRepo().getName());
				titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
									 index, index + event.getRepo().getName().length(),
									 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		} else if (type.equals(Event.TYPE_DELETE)) {
			final DeletePayload p = (DeletePayload) event.getPayload();
			titleBuilder.append("deleted " + p.getRefType() + " " + p.getRef() +
					" at " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		} else if (type.equals(Event.TYPE_DOWNLOAD)) {
			final DownloadPayload p = (DownloadPayload) event.getPayload();
		} else if (type.equals(Event.TYPE_FOLLOW)) {
			final FollowPayload p = (FollowPayload) event.getPayload();
			titleBuilder.append("started following " + p.getTarget().getLogin());
			int index = titleBuilder.toString().indexOf(p.getTarget().getLogin());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")), index,
								 index + p.getTarget().getLogin().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			extraBuilder.append(p.getTarget().getLogin() + " has " +
								Integer.toString(p.getTarget().getPublicRepos()) +
								" public repos and " +
								Integer.toString(p.getTarget().getFollowers()) +
								" followers");
		} else if (type.equals(Event.TYPE_FORK)) {
			final ForkPayload p = (ForkPayload) event.getPayload();
			titleBuilder.append("forked " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			String forkedRepo = login + "/" + event.getRepo().getName().split("/")[1];
			extraBuilder.append("Forked repository is at " + forkedRepo);
			index = extraBuilder.toString().indexOf(forkedRepo);
			extraBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + forkedRepo.length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		} else if (type.equals(Event.TYPE_FORK_APPLY)) {
			final ForkApplyPayload p = (ForkApplyPayload) event.getPayload();
		} else if (type.equals(Event.TYPE_GIST)) {
			final GistPayload p = (GistPayload) event.getPayload();
			titleBuilder.append(p.getAction() + "d gist: " + p.getGist().getId());
			int index = titleBuilder.toString().indexOf("gist: ");
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + ("gist: " + p.getGist().getId()).length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			extraBuilder.append(p.getGist().getDescription());
		} else if (type.equals(Event.TYPE_GOLLUM)) {
			final GollumPayload p = (GollumPayload) event.getPayload();
			titleBuilder.append("edited the " + event.getRepo().getName() + " wiki");
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		} else if (type.equals(Event.TYPE_ISSUE_COMMENT)) {
			final IssueCommentPayload p = (IssueCommentPayload) event.getPayload();
			titleBuilder.append("commented on " +
					((p.getIssue().getPullRequest() != null) ? "pull request " : "issue ") +
					Integer.toString(p.getIssue().getNumber()) + " on " +
					event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			extraBuilder.append(p.getComment().getBody().replace("\n", ""));
			holder.extra.setMaxLines(2);
		} else if (type.equals(Event.TYPE_ISSUES)) {
			final IssuesPayload p = (IssuesPayload) event.getPayload();
			titleBuilder.append(p.getAction() + " issue " +
								Integer.toString(p.getIssue().getNumber()) + " at " +
								event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			extraBuilder.append(p.getIssue().getTitle());
		} else if (type.equals(Event.TYPE_MEMBER)) {
			final MemberPayload p = (MemberPayload) event.getPayload();
			titleBuilder.append(p.getAction() + " " + p.getMember().getLogin());
			titleBuilder.append(" to " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			index = titleBuilder.toString().indexOf(p.getMember().getLogin());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + p.getMember().getLogin().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		} else if (type.equals(Event.TYPE_PUBLIC)) {
			final PublicPayload p = (PublicPayload) event.getPayload();
			titleBuilder.append("open sourced " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		} else if (type.equals(Event.TYPE_PULL_REQUEST)) {
			final PullRequestPayload p = (PullRequestPayload) event.getPayload();
			titleBuilder.append(p.getAction() + " pull request " +
								Integer.toString(p.getNumber()) + " on " +
								event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			extraBuilder.append(p.getPullRequest().getTitle());
		} else if (type.equals(Event.TYPE_PULL_REQUEST_REVIEW_COMMENT)) {
			final PullRequestReviewCommentPayload p =
					(PullRequestReviewCommentPayload) event.getPayload();
		} else if (type.equals(Event.TYPE_PUSH)) {
			final PushPayload p = (PushPayload) event.getPayload();
			titleBuilder.append("pushed to " + p.getRef().split("/")[2] + " at " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			for (Commit c : p.getCommits()) {
				String commitLine = c.getSha().substring(0, 6) + " " +
									c.getMessage().split("\n")[0] + "\n";
				extraBuilder.append(commitLine);
				extraBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
									 extraBuilder.toString().length() - commitLine.length(),
									 extraBuilder.toString().length() - commitLine.length() + 7,
									 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		} else if (type.equals(Event.TYPE_TEAM_ADD)) {
			final TeamAddPayload p = (TeamAddPayload) event.getPayload();
		} else if (type.equals(Event.TYPE_WATCH)) {
			final WatchPayload p = (WatchPayload) event.getPayload();
			titleBuilder.append(p.getAction() + " watching " + event.getRepo().getName());
			int index = titleBuilder.toString().indexOf(event.getRepo().getName());
			titleBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#4183C4")),
								 index, index + event.getRepo().getName().length(),
								 Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		} else {
			titleBuilder.append(" did a " + type);
		}

		titleBuilder.append(" " + StringUtils.getTimeSince(event.getCreatedAt()) + " ago");

		holder.title.setText(titleBuilder);
		holder.extra.setText(extraBuilder);
	}
}

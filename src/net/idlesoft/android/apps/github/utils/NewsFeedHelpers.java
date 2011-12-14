/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.utils;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter.ViewHolder;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.event.CommitCommentPayload;
import org.eclipse.egit.github.core.event.CreatePayload;
import org.eclipse.egit.github.core.event.DeletePayload;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.FollowPayload;
import org.eclipse.egit.github.core.event.GistPayload;
import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.eclipse.egit.github.core.event.IssuesPayload;
import org.eclipse.egit.github.core.event.MemberPayload;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.event.PushPayload;
import org.eclipse.egit.github.core.event.WatchPayload;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * NewsFeedHelpers Contains methods used to populate the WebView in
 * SingleActivityItem
 */
public class NewsFeedHelpers {

    public static void buildEventEntry(Context context, ViewHolder holder, Event entry, Bitmap avatar) {
        holder.date.setText(StringUtils.getTimeSince(entry.getCreatedAt()) + " ago");

        final String actor = entry.getActor().getLogin();
        final String eventType = entry.getType();
        String title = actor + " did something...";
        holder.gravatar.setImageBitmap(avatar);

        if (eventType.contains("PushEvent")) {
            final PushPayload payload = (PushPayload) entry.getPayload();
            holder.icon.setImageResource(R.drawable.push);
            title = actor + " pushed to " + payload.getRef().split("/")[2] + " at "
                    + entry.getRepo().getName();
        } else if (eventType.contains("WatchEvent")) {
            final WatchPayload payload = (WatchPayload) entry.getPayload();
            final String action = payload.getAction();
            if (action.equalsIgnoreCase("started")) {
                holder.icon.setImageResource(R.drawable.watch_started);
            } else {
                holder.icon.setImageResource(R.drawable.watch_stopped);
            }
            title = actor + " " + action + " watching "
                    + entry.getRepo().getName();
        } else if (eventType.contains("GistEvent")) {
            final GistPayload payload = (GistPayload) entry.getPayload();
            final String action = payload.getAction();
            holder.icon.setImageResource(R.drawable.gist);
            title = actor + " " + action + "d gist: " + payload.getGist().getId();
        } else if (eventType.contains("ForkEvent")) {
            holder.icon.setImageResource(R.drawable.fork);
            title = actor + " forked " + entry.getRepo().getName();
        } else if (eventType.contains("CommitCommentEvent")) {
            holder.icon.setImageResource(R.drawable.comment);
            title = actor + " commented on "
                    + entry.getRepo().getName();
        } else if (eventType.contains("ForkApplyEvent")) {
            holder.icon.setImageResource(R.drawable.merge);
            title = actor + " applied fork commits to "
                    + entry.getRepo().getName();
        } else if (eventType.contains("FollowEvent")) {
            final FollowPayload payload = (FollowPayload) entry.getPayload();
            holder.icon.setImageResource(R.drawable.follow);
            title = actor + " started following "
                    + payload.getTarget().getLogin();
        } else if (eventType.contains("CreateEvent")) {
            final CreatePayload payload = (CreatePayload) entry.getPayload();
            holder.icon.setImageResource(R.drawable.create);
            final String ref_type = payload.getRefType();
            if (ref_type.contains("repository")) {
                title = actor + " created repository "
                        + entry.getRepo().getName();
            } else if (ref_type.contains("branch")) {
                title = actor + " created branch " + payload.getRef() + " at "
                        + entry.getRepo().getName();
            } else if (ref_type.contains("tag")) {
                title = actor + " created tag " + payload.getRef() + " at "
                        + entry.getRepo().getName();
            }
        } else if (eventType.contains("IssuesEvent")) {
            final IssuesPayload payload = (IssuesPayload) entry.getPayload();
            if (payload.getAction().equalsIgnoreCase("opened")) {
                holder.icon.setImageResource(R.drawable.issues_open);
            } else {
                holder.icon.setImageResource(R.drawable.issues_closed);
            }
            title = actor + " " + payload.getAction() + " issue "
                    + payload.getIssue().getNumber() + " on "
                    + entry.getRepo().getName();
        } else if (eventType.contains("DeleteEvent")) {
            final DeletePayload payload = (DeletePayload) entry.getPayload();
            holder.icon.setImageResource(R.drawable.delete);
            final String ref_type = payload.getRefType();
            if (ref_type.contains("repository")) {
                title = actor + " deleted repository " + payload.getRef();
            } else if (ref_type.contains("branch")) {
                title = actor + " deleted branch " + payload.getRef() + " at "
                        + entry.getRepo().getName();
            } else if (ref_type.contains("tag")) {
                title = actor + " deleted tag " + payload.getRef() + " at "
                        + entry.getRepo().getName();
            }
        } else if (eventType.contains("WikiEvent")) {
            holder.icon.setImageResource(R.drawable.wiki);
            title = actor + " modified the "
                    + entry.getRepo().getName() + " wiki";
        } else if (eventType.contains("DownloadEvent")) {
            holder.icon.setImageResource(R.drawable.download);
            title = actor + " uploaded a file to "
                    + entry.getRepo().getName();
        } else if (eventType.contains("PublicEvent")) {
            holder.icon.setImageResource(R.drawable.opensource);
            title = actor + " open sourced "
                    + entry.getRepo().getName();
        } else if (eventType.contains("PullRequestEvent")) {
            final PullRequestPayload payload = (PullRequestPayload) entry.getPayload();
            final int number = payload.getPullRequest().getNumber();
            if (payload.getAction().equalsIgnoreCase("opened")) {
                holder.icon.setImageResource(R.drawable.issues_open);
                title = actor + " opened pull request " + number + " on "
                        + entry.getRepo().getName();
            } else if (payload.getAction().equalsIgnoreCase("closed")) {
                holder.icon.setImageResource(R.drawable.issues_closed);
                title = actor + " closed pull request " + number + " on "
                        + entry.getRepo().getName();
            }
        } else if (eventType.contains("MemberEvent")) {
            final MemberPayload payload = (MemberPayload) entry.getPayload();
            holder.icon.setImageResource(R.drawable.follow);
            title = actor + " added " + payload.getMember().getLogin()
                    + entry.getRepo().getName();
        } else if (eventType.contains("IssueCommentEvent")) {
            final IssueCommentPayload payload = (IssueCommentPayload) entry.getPayload();
            holder.icon.setImageResource(R.drawable.comment);
            title = actor + " commented on issue "
                    + payload.getIssue().getNumber() + " on "
                    + entry.getRepo().getName();
        }
        holder.title.setText(title);
    }

    public static String linkifyCommitCommentItem(final Event pNewsItem) {
        final String repoOwner = pNewsItem.getRepo().getName().split("/")[0];
        final String repoName = pNewsItem.getRepo().getName().split("/")[1];
        final String actor = pNewsItem.getActor().getLogin();
        final CommitCommentPayload payload = (CommitCommentPayload) pNewsItem.getPayload();
        final String commit = payload.getComment().getCommitId();

        final String userUriPrefix = "hubroid://showUser/";
        final String commitUriPrefix = "hubroid://showCommit/" + repoOwner + "/" + repoName + "/";
        final String html = "<div>" + "<a href=\"" + userUriPrefix + actor + "\">" + actor
                + "</a> commented on <a href=\"" + commitUriPrefix + commit + "\">"
                + commit.substring(0, 8) + "</a>." + "</div>";
        return html;
    }

    public static String linkifyCreateBranchItem(final Event pNewsItem) {
        final String repoOwner = pNewsItem.getRepo().getName().split("/")[0];
        final String repoName = pNewsItem.getRepo().getName().split("/")[1];
        final String actor = pNewsItem.getActor().getLogin();
        final CreatePayload payload = (CreatePayload) pNewsItem.getPayload();

        final String userUriPrefix = "hubroid://showUser/";
        final String repoUri = "hubroid://showRepo/" + repoOwner + "/" + repoName;
        final String html = "<div>" + "<a href=\"" + userUriPrefix + actor + "\">" + actor
                + "</a> created branch " + payload.getRef() + " at <a href=\"" + repoUri
                + "\">" + repoOwner + "/" + repoName + "</a>." + "</div>";

        return html;
    }

    public static String linkifyCreateRepoItem(final Event pNewsItem) {
        final String repoPath = pNewsItem.getRepo().getName();
        final String repoName = repoPath.split("/")[1];
        String repoDesc = pNewsItem.getRepo().getDescription();
        if (repoDesc == null || repoDesc.equals("")) {
            repoDesc = "N/A";
        }

        final String html = "<div>" + "<a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoName + "</a>'s description: <br/><br/>" + "<span class=\"repo-desc\">"
                + repoDesc + "</span>" + "</div>";
        return html;
    }

    public static String linkifyFollowItem(final Event pNewsItem) {
        final FollowPayload payload = (FollowPayload) pNewsItem.getPayload();
        final String targetLogin = payload.getTarget().getLogin();
        final int targetRepoCount = payload.getTarget().getPublicRepos();
        final int targetFollowersCount = payload.getTarget().getFollowers();

        String targetReposString, targetFollowersString;
        if (targetRepoCount == 1) {
            targetReposString = "1 public repo";
        } else {
            targetReposString = targetRepoCount + " public repos";
        }
        if (targetFollowersCount == 1) {
            targetFollowersString = "1 follower";
        } else {
            targetFollowersString = targetFollowersCount + " followers";
        }

        final String html = "<div>" + "<a href=\"hubroid://showUser/" + targetLogin + "/\">"
                + targetLogin + "</a> has " + targetReposString + " and " + targetFollowersString
                + "." + "</div>";
        return html;
    }

    public static String linkifyForkItem(final Event pNewsItem) {
        final String parentRepoPath = pNewsItem.getRepo().getName();
        final String forkedRepoPath = pNewsItem.getActor().getLogin() + "/"
                + parentRepoPath.split("/")[1];

        final String html = "<div>" + "Forked repo is at <a href=\"hubroid://showRepo/"
                + forkedRepoPath + "/\">" + forkedRepoPath
                + "</a>, parent repo is at <a href=\"hubroid://showRepo/" + parentRepoPath + "/\">"
                + parentRepoPath + "</a>." + "</div>";
        return html;
    }

    public static String linkifyIssueItem(final Event pNewsItem) {
        final IssuesPayload payload = (IssuesPayload) pNewsItem.getPayload();
        final String repoPath = pNewsItem.getRepo().getName();
        final int issueNumber = payload.getIssue().getNumber();

        final String html = "<div>" + "View <a href=\"hubroid://showIssue/" + repoPath + "/"
                + issueNumber + "\">Issue " + issueNumber + "</a>. <br/><br/>"
                + "To view <a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoPath.split("/")[1] + "</a>'s issue list, <a href=\"hubroid://showIssues/"
                + repoPath + "/\">click here</a>." + "</div>";
        return html;
    }

    public static String linkifyPushItem(final Event pNewsItem) {
        final String repoOwner = pNewsItem.getRepo().getName().split("/")[0];
        final String repoName = pNewsItem.getRepo().getName().split("/")[1];
        final PushPayload payload = (PushPayload) pNewsItem.getPayload();
        final String commitUriPrefix = "hubroid://showCommit/" + repoOwner + "/" + repoName + "/";
        String html = "<div>" + "HEAD is at <a href=\"" + commitUriPrefix
                + payload.getHead() + "\">" + payload.getHead().substring(0, 32)
                + "</a><br/>" + "<ul>";
        final int shaCount = payload.getCommits().size();
        for (int i = 0; i < shaCount; i++) {
            final Commit commit = payload.getCommits().get(i);
            html += "<li>" + pNewsItem.getActor().getLogin() + " committed <a href=\""
                    + commitUriPrefix + commit.getSha() + "\">"
                    + commit.getSha().substring(0, 8) + "</a>:<br/>"
                    + "<span class=\"commit-message\">" + commit.getMessage().split("\n")[0]
                    + "</span>" + "</li>";
        }
        html += "</ul>" + "</div>";
        return html;
    }

    public static String linkifyWatchItem(final Event pNewsItem) {
        final String repoPath = pNewsItem.getRepo().getName();

        final String html = "<div>" + "Visit <a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoPath.split("/")[1] + "</a> in Hubroid.</div>";
        return html;
    }

    public static String linkifyGistItem(final Event pNewsItem) {
        final GistPayload payload = (GistPayload) pNewsItem.getPayload();
        final int id = Integer.parseInt(payload.getGist().getId());
        final String html = "<div>" + "<h2>gist " + id + "</h2><br/>"
                + "<b>URL:</b> <a href=\"" + payload.getGist().getHtmlUrl() + "\">"
                + payload.getGist().getHtmlUrl() + "</a><br/><br/>" + "<a href=\"hubroid://showGist/"
                + id + "/\">" + "View this Gist in Hubroid</a></div>";
        return html;
    }

    public static String linkifyPublicItem(final Event pNewsItem) {
        final String repoPath = pNewsItem.getRepo().getName();
        final String html = "<div>" + "Check out the newly freed repository by "
                + "<a href=\"hubroid://showRepo/" + repoPath + "/\">" + "clicking here" + "</a>!"
                + "</div>";
        return html;
    }

    public static String linkifyOtherItem(final Event pNewsItem) {
        String repohtml;
        if (pNewsItem.getRepo() != null) {
            final String repoPath = pNewsItem.getRepo().getName();
            repohtml = "<b>Repository:</b> <a href=\"hubroid://showRepo/" + repoPath + "/\">"
                    + repoPath.split("/")[1] + "</a><br/>"
                    + "<b>Repository Owner:</b> <a href=\"hubroid://showUser/"
                    + repoPath.split("/")[0] + "/\">" + repoPath.split("/")[0] + "</a><br/>";
        } else {
            repohtml = "";
        }

        final String html = "<div>"
                + "<h2>Uh-oh!</h2><br/>"
                + "Either Hubroid doesn't know how to handle this event or the API "
                + "does not provide enough information to work with.<br/><br/>"
                + "In the meantime, here's some generic information about the event:<br/>"
                + "<ul style='line-height: 1.5em;'>"
                + repohtml
                + "</ul>"
                + "<img style=\"position: absolute; bottom: 0;right: 0;\" src=\"file:///android_asset/octocat_sad.png\" />"
                + "</div>";
        return html;
    }
}

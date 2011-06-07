/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * NewsFeedHelpers Contains methods used to populate the WebView in
 * SingleActivityItem
 */
public class NewsFeedHelpers {

    public static String linkifyCommitCommentItem(final JSONObject pNewsItem) throws JSONException {
        final String repoOwner = pNewsItem.getJSONObject("repository").getString("owner");
        final String repoName = pNewsItem.getJSONObject("repository").getString("name");
        final String actor = pNewsItem.getString("actor");
        final JSONObject payload = pNewsItem.getJSONObject("payload");
        final String commit = payload.getString("commit");

        final String userUriPrefix = "hubroid://showUser/";
        final String commitUriPrefix = "hubroid://showCommit/" + repoOwner + "/" + repoName + "/";
        final String html = "<div>" + "<a href=\"" + userUriPrefix + actor + "\">" + actor
                + "</a> commented on <a href=\"" + commitUriPrefix + commit + "\">"
                + commit.substring(0, 8) + "</a>." + "</div>";
        return html;
    }

    public static String linkifyCreateBranchItem(final JSONObject pNewsItem) throws JSONException {
        final String repoOwner = pNewsItem.getJSONObject("repository").getString("owner");
        final String repoName = pNewsItem.getJSONObject("repository").getString("name");
        final String actor = pNewsItem.getString("actor");
        final JSONObject payload = pNewsItem.getJSONObject("payload");

        final String userUriPrefix = "hubroid://showUser/";
        final String repoUri = "hubroid://showRepo/" + repoOwner + "/" + repoName;
        final String html = "<div>" + "<a href=\"" + userUriPrefix + actor + "\">" + actor
                + "</a> created branch " + payload.getString("ref") + " at <a href=\"" + repoUri
                + "\">" + repoOwner + "/" + repoName + "</a>." + "</div>";

        return html;
    }

    public static String linkifyCreateRepoItem(final JSONObject pNewsItem) throws JSONException {
        final JSONObject repoJson = pNewsItem.getJSONObject("repository");
        final String repoName = repoJson.getString("name");
        final String repoPath = repoJson.getString("owner") + "/" + repoName;
        final String repoDesc = repoJson.getString("description");

        final String html = "<div>" + "<a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoName + "</a>'s description: <br/><br/>" + "<span class=\"repo-desc\">"
                + repoDesc + "</span>" + "</div>";
        return html;
    }

    public static String linkifyFollowItem(final JSONObject pNewsItem) throws JSONException {
        final JSONObject target = pNewsItem.getJSONObject("payload").getJSONObject("target");
        final String targetLogin = target.getString("login");
        final int targetRepoCount = target.getInt("repos");
        final int targetFollowersCount = target.getInt("followers");

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

    public static String linkifyForkItem(final JSONObject pNewsItem) throws JSONException {
        final JSONObject payload = pNewsItem.getJSONObject("payload");
        final JSONObject repository = pNewsItem.getJSONObject("repository");
        final String parentRepoPath = payload.getString("repo");
        final String forkedRepoPath = payload.getString("actor") + "/"
                + repository.getString("name");

        final String html = "<div>" + "Forked repo is at <a href=\"hubroid://showRepo/"
                + forkedRepoPath + "/\">" + forkedRepoPath
                + "</a>, parent repo is at <a href=\"hubroid://showRepo/" + parentRepoPath + "/\">"
                + parentRepoPath + "</a>." + "</div>";
        return html;
    }

    public static String linkifyIssueItem(final JSONObject pNewsItem) throws JSONException {
        final JSONObject payload = pNewsItem.getJSONObject("payload");
        final JSONObject repo = pNewsItem.getJSONObject("repository");
        final String repoPath = repo.getString("owner") + "/" + repo.getString("name");
        final int issueNumber = payload.getInt("number");

        final String html = "<div>" + "View <a href=\"hubroid://showIssue/" + repoPath + "/"
                + issueNumber + "\">Issue " + issueNumber + "</a>. <br/><br/>"
                + "To view <a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoPath.split("/")[1] + "</a>'s issue list, <a href=\"hubroid://showIssues/"
                + repoPath + "/\">click here</a>." + "</div>";
        return html;
    }

    public static String linkifyPushItem(final JSONObject pNewsItem) throws JSONException {
        final String repoOwner = pNewsItem.getJSONObject("repository").getString("owner");
        final String repoName = pNewsItem.getJSONObject("repository").getString("name");
        final JSONObject payload = pNewsItem.getJSONObject("payload");
        final String commitUriPrefix = "hubroid://showCommit/" + repoOwner + "/" + repoName + "/";
        String html = "<div>" + "HEAD is at <a href=\"" + commitUriPrefix
                + payload.getString("head") + "\">" + payload.getString("head").substring(0, 32)
                + "</a><br/>" + "<ul>";
        final int shaCount = pNewsItem.getJSONObject("payload").getJSONArray("shas").length();
        for (int i = 0; i < shaCount; i++) {
            final JSONArray commitInfo = pNewsItem.getJSONObject("payload").getJSONArray("shas")
                    .getJSONArray(i);
            html += "<li>" + pNewsItem.getString("actor") + " committed <a href=\""
                    + commitUriPrefix + commitInfo.getString(0) + "\">"
                    + commitInfo.getString(0).substring(0, 8) + "</a>:<br/>"
                    + "<span class=\"commit-message\">" + commitInfo.getString(2).split("\n")[0]
                    + "</span>" + "</li>";
        }
        html += "</ul>" + "</div>";
        return html;
    }

    public static String linkifyWatchItem(final JSONObject pNewsItem) throws JSONException {
        final JSONObject repo = pNewsItem.getJSONObject("repository");
        final String repoPath = repo.getString("owner") + "/" + repo.getString("name");
        final String repoDesc = pNewsItem.getJSONObject("repository").getString("description");

        final String html = "<div>" + "<a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoPath.split("/")[1] + "</a>'s description: <br/><br/>"
                + "<span class=\"repo-desc\">" + repoDesc + "</span>" + "</div>";
        return html;
    }

    public static String linkifyGistItem(final JSONObject pNewsItem) throws JSONException {
        final JSONObject payload = pNewsItem.getJSONObject("payload");
        final String html = "<div>" + "<h2>" + payload.getString("name") + "</h2><br/>"
                + "<b>URL:</b> <a href=\"" + payload.getString("url") + "\">"
                + payload.getString("url") + "</a><br/><br/>"
                + "<a href=\"hubroid://showGist/" + payload.getInt("id") + "/\">"
                + "View this Gist in Hubroid</a></div>";
        return html;
    }

    public static String linkifyOtherItem(final JSONObject pNewsItem) throws JSONException {
        String repohtml;
        try {
            final JSONObject repo = pNewsItem.getJSONObject("repository");
            if (repo != null) {
                final String repoPath = repo.getString("owner") + "/" + repo.getString("name");
                repohtml = "<b>Repository:</b> <a href=\"hubroid://showRepo/" + repoPath + "/\">"
                        + repoPath.split("/")[1] + "</a><br/>"
                        + "<b>Repository Owner:</b> <a href=\"hubroid://showUser/"
                        + repoPath.split("/")[0] + "/\">" + repoPath.split("/")[0] + "</a><br/>";
            } else {
                repohtml = "";
            }
        } catch (JSONException e) {
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

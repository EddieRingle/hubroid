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
                + "</a> created branch " + payload.getString("object_name") + " at <a href=\""
                + repoUri + "\">" + repoOwner + "/" + repoName + "</a>." + "</div>";

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
        final String repoPath = payload.getString("repo");

        final String html = "<div>" + "To view <a href=\"hubroid://showRepo/" + repoPath + "/\">"
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
        final String repoPath = pNewsItem.getJSONObject("payload").getString("repo");
        final String repoDesc = pNewsItem.getJSONObject("repository").getString("description");

        final String html = "<div>" + "<a href=\"hubroid://showRepo/" + repoPath + "/\">"
                + repoPath.split("/")[1] + "</a>'s description: <br/><br/>"
                + "<span class=\"repo-desc\">" + repoDesc + "</span>" + "</div>";
        return html;
    }
}

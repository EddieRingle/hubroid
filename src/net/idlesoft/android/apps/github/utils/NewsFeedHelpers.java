/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * NewsFeedHelpers
 *
 * Contains methods used to populate the WebView in SingleActivityItem
 */
public class NewsFeedHelpers {

    public static String linkifyPushItem(JSONObject pNewsItem) throws JSONException {
        String repoOwner = pNewsItem.getJSONObject("repository").getString("owner");
        String repoName = pNewsItem.getJSONObject("repository").getString("name");
        JSONObject payload = pNewsItem.getJSONObject("payload");
        String commitUriPrefix = "hubroid://showCommit/" + repoOwner + "/" + repoName + "/";
        String html =
                "<div>"
                + "HEAD is at <a href=\""
                        + commitUriPrefix
                        + payload.getString("head")
                        + "\">"
                        + payload.getString("head").substring(0, 32)
                        + "</a><br/>"
                + "<ul>";
        int shaCount = pNewsItem.getJSONObject("payload").getJSONArray("shas").length();
        for (int i = 0; i < shaCount; i++) {
            JSONArray commitInfo = pNewsItem.getJSONObject("payload").getJSONArray("shas").getJSONArray(i);
            html += "<li>" + pNewsItem.getString("actor") + " committed <a href=\""
                            + commitUriPrefix
                            + commitInfo.getString(0)
                            + "\">"
                            + commitInfo.getString(0).substring(0, 8)
                            + "</a>:<br/>"
                    + "<span class=\"commit-message\">" + commitInfo.getString(2).split("\n")[0] + "</span>"
                    + "</li>";
        }
        html += "</ul>"
                + "</div>";
        return html;
    }

    public static String linkifyCreateBranchItem(JSONObject pNewsItem) throws JSONException {
        String repoOwner = pNewsItem.getJSONObject("repository").getString("owner");
        String repoName = pNewsItem.getJSONObject("repository").getString("name");
        String actor = pNewsItem.getString("actor");
        JSONObject payload = pNewsItem.getJSONObject("payload");

        String userUriPrefix = "hubroid://showUser/";
        String repoUri = "hubroid://showRepo/" + repoOwner + "/" + repoName;
        String html =
                "<div>"
                + "<a href=\"" + userUriPrefix + actor + "\">" + actor + "</a> created branch " + payload.getString("object_name") + " at <a href=\"" + repoUri + "\">" + repoOwner + "/" + repoName + "</a>."
                + "</div>";

        return html;
    }

    public static String linkifyCommitCommentItem(JSONObject pNewsItem) throws JSONException {
        String repoOwner = pNewsItem.getJSONObject("repository").getString("owner");
        String repoName = pNewsItem.getJSONObject("repository").getString("name");
        String actor = pNewsItem.getString("actor");
        JSONObject payload = pNewsItem.getJSONObject("payload");
        String commit = payload.getString("commit");

        String userUriPrefix = "hubroid://showUser/";
        String commitUriPrefix = "hubroid://showCommit/" + repoOwner + "/" + repoName + "/";
        String html =
                "<div>"
                + "<a href=\"" + userUriPrefix + actor + "\">" + actor + "</a> commented on <a href=\"" + commitUriPrefix + commit + "\">" + commit.substring(0, 8) + "</a>."
                + "</div>";
        return html;
    }
}

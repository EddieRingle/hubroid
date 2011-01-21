package org.idlesoft.android.hubroid.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        JSONObject payload = pNewsItem.getJSONObject("payload");

        return new String();
    }
}

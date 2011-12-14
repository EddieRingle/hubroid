/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.utils;

import net.idlesoft.android.apps.github.Constants;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StringUtils {
    /**
     * Get time string since a specified date
     * 
     * @param pTime
     * @return String of format "X U(s)", X is the quantity of U time units
     *         (e.g., "3 days")
     */
    public static String getTimeSince(final Date pTime) {
        final Date item_time = pTime;
        final Date current_time = new Date();
        final long ms = current_time.getTime() - item_time.getTime();
        final long sec = ms / 1000;
        final long min = sec / 60;
        final long hour = min / 60;
        final long day = hour / 24;
        final long year = day / 365;
        if (year > 0) {
            if (year == 1) {
                return year + " year";
            } else {
                return year + " years";
            }
        } else if (day > 0) {
            if (day == 1) {
                return day + " day";
            } else {
                return day + " days";
            }
        } else if (hour > 0) {
            if (hour == 1) {
                return hour + " hour";
            } else {
                return hour + " hours";
            }
        } else if (min > 0) {
            if (min == 1) {
                return min + " minute";
            } else {
                return min + " minutes";
            }
        } else if (sec > 0) {
            if (sec == 1) {
                return sec + " second";
            } else {
                return sec + " seconds";
            }
        } else {
            return ms + " ms";
        }
    }

    /**
     * Get the extension of a file
     * 
     * @param filename
     * @return filename's extension, not including the preceding period
     */
    public static String getExtension(final String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.') + 1);
        } catch (IndexOutOfBoundsException e) {
            // Filename has no extension, oh well
            return "";
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Converts a GitHub Blob to HTML for inserting into a WebView
     * 
     * @param in Base64-decoded String of Blob contents
     * @param filename Filename of Blob
     * @return String of HTML for inserting into a WebView
     */
    public static String blobToHtml(final String in, final String filename) {
        final StringBuilder html = new StringBuilder();
        if (Arrays.asList(Constants.MARKDOWN_EXT).contains(getExtension(filename).toLowerCase())) {
            html.append("<head>");
            html.append("<script type='text/javascript' src='file:///android_asset/showdown.js' />");
            html.append("<link href='file:///android_asset/markdown.css' type='text/css' />");
            html.append("</head>");
            html.append("<body>");
            html.append("<div id='content'>");

            html.append(in);

            html.append("</div>");
            html.append("<script>");
            html.append("var contentHtml = document.getElementById('content').innerHTML;");
            html.append("var showdownConverter = new Showdown.converter();");
            html.append("var freshHtml = showdownConverter.makeHtml(contentHtml);");
            html.append("document.getElementById('content').innerHTML = freshHtml;");
            html.append("</script>");
            html.append("</body>");
        } else {
            html.append("<pre>");
            html.append(TextUtils.htmlEncode(in).replaceAll("\n", "<br/>"));
            html.append("</pre>");
        }
        return new String(html);
    }

    /**
     * Returns a map from a HTTP query string
     *
     * @param query
     * @return Map of query parameters
     */
    public static Map<String, String> mapQueryString(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] split = param.split("=");
            map.put(split[0], split[1]);
        }
        return map;
    }
}

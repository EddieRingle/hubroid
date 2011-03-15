/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;
import net.idlesoft.android.apps.github.utils.NewsFeedHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SingleActivityItem extends Activity {
    public static final String CSS = "<style type=\"text/css\">" + "* {" + "font-size: 13px;"
            + "margin: 0px;" + "}" + "div {" + "margin: 10px;" + "}" + "ul, li {" + "margin: 0;"
            + "padding: 0;" + "margin-top: 10px;" + "margin-bottom: 10px;" + "margin-left: 10px;"
            + "}" + "span {" + "color: #999;" + "margin: 0;" + "}" + "</style>";

    private SharedPreferences.Editor mEditor;

    public Intent mIntent;

    private JSONObject mJson = new JSONObject();

    private SharedPreferences mPrefs;

    private void loadActivityItemBox() {
        final TextView date = (TextView) findViewById(R.id.tv_activity_item_date);
        final ImageView gravatar = (ImageView) findViewById(R.id.iv_activity_item_gravatar);
        final ImageView icon = (ImageView) findViewById(R.id.iv_activity_item_icon);
        final TextView title_tv = (TextView) findViewById(R.id.tv_activity_item_title);

        try {
            final JSONObject entry = mJson;
            final JSONObject payload = entry.getJSONObject("payload");
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date item_time = dateFormat.parse(entry.getString("created_at"));
            final Date current_time = dateFormat.parse(dateFormat.format(new Date()));
            final long ms = current_time.getTime() - item_time.getTime();
            final long sec = ms / 1000;
            final long min = sec / 60;
            final long hour = min / 60;
            final long day = hour / 24;
            if (day > 0) {
                if (day == 1) {
                    end = " day ago";
                } else {
                    end = " days ago";
                }
                date.setText(day + end);
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                date.setText(hour + end);
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                date.setText(min + end);
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                date.setText(sec + end);
            }

            final String actor = entry.getString("actor");
            final String eventType = entry.getString("type");
            String title = actor + " did something...";
            gravatar.setImageBitmap(GravatarCache.getDipGravatar(
                    GravatarCache.getGravatarID(actor), 30.0f,
                    getResources().getDisplayMetrics().density));

            if (eventType.contains("PushEvent")) {
                icon.setImageResource(R.drawable.push);
                title = actor + " pushed to " + payload.getString("ref").split("/")[2] + " at "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("WatchEvent")) {
                final String action = payload.getString("action");
                if (action.equalsIgnoreCase("started")) {
                    icon.setImageResource(R.drawable.watch_started);
                } else {
                    icon.setImageResource(R.drawable.watch_stopped);
                }
                title = actor + " " + action + " watching "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("GistEvent")) {
                final String action = payload.getString("action");
                icon.setImageResource(R.drawable.gist);
                title = actor + " " + action + "d " + payload.getString("name");
            } else if (eventType.contains("ForkEvent")) {
                icon.setImageResource(R.drawable.fork);
                title = actor + " forked " + entry.getJSONObject("repository").getString("name")
                        + "/" + entry.getJSONObject("repository").getString("owner");
            } else if (eventType.contains("CommitCommentEvent")) {
                icon.setImageResource(R.drawable.comment);
                title = actor + " commented on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("ForkApplyEvent")) {
                icon.setImageResource(R.drawable.merge);
                title = actor + " applied fork commits to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("FollowEvent")) {
                icon.setImageResource(R.drawable.follow);
                title = actor + " started following "
                        + payload.getJSONObject("target").getString("login");
            } else if (eventType.contains("CreateEvent")) {
                icon.setImageResource(R.drawable.create);
                if (payload.getString("object").contains("repository")) {
                    title = actor + " created repository " + payload.getString("name");
                } else if (payload.getString("object").contains("branch")) {
                    title = actor + " created branch " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("object").contains("tag")) {
                    title = actor + " created tag " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("IssuesEvent")) {
                if (payload.getString("action").equalsIgnoreCase("opened")) {
                    icon.setImageResource(R.drawable.issues_open);
                } else {
                    icon.setImageResource(R.drawable.issues_closed);
                }
                title = actor + " " + payload.getString("action") + " issue "
                        + payload.getInt("number") + " on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("DeleteEvent")) {
                icon.setImageResource(R.drawable.delete);
                if (payload.getString("object").contains("repository")) {
                    title = actor + " deleted repository " + payload.getString("name");
                } else if (payload.getString("object").contains("branch")) {
                    title = actor + " deleted branch " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("object").contains("tag")) {
                    title = actor + " deleted tag " + payload.getString("object_name") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("WikiEvent")) {
                icon.setImageResource(R.drawable.wiki);
                title = actor + " " + payload.getString("action") + " a page in the "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name") + " wiki";
            } else if (eventType.contains("DownloadEvent")) {
                icon.setImageResource(R.drawable.download);
                title = actor + " uploaded a file to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("PublicEvent")) {
                icon.setImageResource(R.drawable.opensource);
                title = actor + " open sourced "
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("PullRequestEvent")) {
                final int number = (payload.get("pull_request") instanceof JSONObject) ? payload
                        .getJSONObject("pull_request").getInt("number") : payload.getInt("number");
                if (payload.getString("action").equalsIgnoreCase("opened")) {
                    icon.setImageResource(R.drawable.issues_open);
                    title = actor + " opened pull request " + number + " on "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("action").equalsIgnoreCase("closed")) {
                    icon.setImageResource(R.drawable.issues_closed);
                    title = actor + " closed pull request " + number + " on "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("MemberEvent")) {
                icon.setImageResource(R.drawable.follow);
                title = actor + " added " + payload.getString("member") + " to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            }

            title_tv.setText(title);

            gravatar.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    final Intent i = new Intent(SingleActivityItem.this, Profile.class);
                    i.putExtra("username", actor);
                    startActivity(i);
                }
            });

        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.single_activity_item);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(SingleActivityItem.this, Search.class));
            }
        });

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                mJson = new JSONObject(extras.getString("item_json"));
                loadActivityItemBox();
                final WebView content = (WebView) findViewById(R.id.wv_single_activity_item_content);
                content.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                        if (url.startsWith("hubroid://")) {
                            final String parts[] = url.substring(10).split("/");
                            if (parts[0].equals("showCommit")) {
                                final Intent intent = new Intent(SingleActivityItem.this,
                                        Commit.class);
                                intent.putExtra("repo_owner", parts[1]);
                                intent.putExtra("repo_name", parts[2]);
                                intent.putExtra("commit_sha", parts[3]);
                                startActivity(intent);
                            } else if (parts[0].equals("showRepo")) {
                                final Intent intent = new Intent(SingleActivityItem.this,
                                        Repository.class);
                                intent.putExtra("repo_owner", parts[1]);
                                intent.putExtra("repo_name", parts[2]);
                                startActivity(intent);
                            } else if (parts[0].equals("showUser")) {
                                final Intent intent = new Intent(SingleActivityItem.this,
                                        Profile.class);
                                intent.putExtra("username", parts[1]);
                                startActivity(intent);
                            } else if (parts[0].equals("showIssues")) {
                                final Intent intent = new Intent(SingleActivityItem.this,
                                        Issues.class);
                                intent.putExtra("repo_owner", parts[1]);
                                intent.putExtra("repo_name", parts[2]);
                                startActivity(intent);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                String html = "";
                final String eventType = mJson.getString("type");
                if (eventType.equals("PushEvent")) {
                    html = NewsFeedHelpers.linkifyPushItem(mJson);
                } else if (eventType.equals("CreateEvent")) {
                    final String object = mJson.getJSONObject("payload").getString("object");
                    if (object.equals("branch")) {
                        html = NewsFeedHelpers.linkifyCreateBranchItem(mJson);
                    } else if (object.equals("repository")) {
                        html = NewsFeedHelpers.linkifyCreateRepoItem(mJson);
                    }
                } else if (eventType.equals("CommitCommentEvent")) {
                    html = NewsFeedHelpers.linkifyCommitCommentItem(mJson);
                } else if (eventType.equals("FollowEvent")) {
                    html = NewsFeedHelpers.linkifyFollowItem(mJson);
                } else if (eventType.equals("ForkEvent")) {
                    html = NewsFeedHelpers.linkifyForkItem(mJson);
                } else if (eventType.equals("IssuesEvent")) {
                    html = NewsFeedHelpers.linkifyIssueItem(mJson);
                } else if (eventType.equals("WatchEvent")) {
                    html = NewsFeedHelpers.linkifyWatchItem(mJson);
                }
                final String out = CSS + html;
                content.loadDataWithBaseURL("hubroid", out, "text/html", "UTF-8", "hubroid");
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                mEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }
}

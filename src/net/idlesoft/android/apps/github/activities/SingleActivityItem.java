/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter.ViewHolder;
import net.idlesoft.android.apps.github.utils.GravatarCache;
import net.idlesoft.android.apps.github.utils.NewsFeedHelpers;

import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.event.CreatePayload;
import org.eclipse.egit.github.core.event.Event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

public class SingleActivityItem extends BaseActivity {
    public static final String CSS = "<style type=\"text/css\">" + "* {" + "margin: 0px;" + "}"
            + "div {" + "margin: 10px;" + "font-size: 13px;" + "}" + "ul, li {" + "margin: 0;"
            + "padding: 0;" + "margin-top: 10px;" + "margin-bottom: 10px;" + "margin-left: 10px;"
            + "}" + "span {" + "color: #999;" + "margin: 0;" + "}" + "</style>";

    public Intent mIntent;

    private Event mEvent;

    private void loadActivityItemBox() {
        ViewHolder holder = new ViewHolder();

        holder.date = (TextView) findViewById(R.id.tv_activity_item_date);
        holder.gravatar = (ImageView) findViewById(R.id.iv_activity_item_gravatar);
        holder.icon = (ImageView) findViewById(R.id.iv_activity_item_icon);
        holder.title = (TextView) findViewById(R.id.tv_activity_item_title);

        NewsFeedHelpers.buildEventEntry(SingleActivityItem.this, holder, mEvent, null);
        holder.gravatar.setImageBitmap(GravatarCache.getDipGravatar(mEvent.getActor().getLogin(),
                30.0f, getResources().getDisplayMetrics().density));

        holder.gravatar.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                final Intent i = new Intent(SingleActivityItem.this, Profile.class);
                i.putExtra("username", mEvent.getActor().getLogin());
                startActivity(i);
            }
        });
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.single_activity_item);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mEvent = GsonUtils.fromJson(extras.getString("item_json"), Event.class);
            loadActivityItemBox();
            final WebView content = (WebView) findViewById(R.id.wv_single_activity_item_content);
            content.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                    if (url.startsWith("hubroid://")) {
                        final String parts[] = url.substring(10).split("/");
                        if (parts[0].equals("showCommit")) {
                            final Intent intent = new Intent(SingleActivityItem.this,
                                    SingleCommit.class);
                            intent.putExtra("repo_owner", parts[1]);
                            intent.putExtra("repo_name", parts[2]);
                            intent.putExtra("commit_sha", parts[3]);
                            startActivity(intent);
                        } else if (parts[0].equals("showRepo")) {
                            final Intent intent = new Intent(SingleActivityItem.this,
                                    SingleRepository.class);
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
                        } else if (parts[0].equals("showIssue")) {
                            final Intent intent = new Intent(SingleActivityItem.this,
                                    SingleIssue.class);
                            intent.putExtra("repo_owner", parts[1]);
                            intent.putExtra("repo_name", parts[2]);
                            intent.putExtra("number", Integer.parseInt(parts[3]));
                            startActivity(intent);
                        } else if (parts[0].equals("showGist")) {
                            final Intent intent = new Intent(SingleActivityItem.this,
                                    SingleGist.class);
                            intent.putExtra("gistId", parts[1]);
                            startActivity(intent);
                        }
                        return true;
                    } else {
                        final Intent intent = new Intent("android.intent.action.VIEW", Uri
                                .parse(url));
                        startActivity(intent);
                    }
                    return false;
                }
            });
            String html = "";
            final String eventType = mEvent.getType();
            if (eventType.equals("PushEvent")) {
                html = NewsFeedHelpers.linkifyPushItem(mEvent);
            } else if (eventType.equals("CreateEvent")) {
                final String object = ((CreatePayload)mEvent.getPayload()).getRefType();
                if (object.equals("branch")) {
                    html = NewsFeedHelpers.linkifyCreateBranchItem(mEvent);
                } else if (object.equals("repository")) {
                    html = NewsFeedHelpers.linkifyCreateRepoItem(mEvent);
                } else {
                    html = NewsFeedHelpers.linkifyOtherItem(mEvent);
                }
            } else if (eventType.equals("CommitCommentEvent")) {
                html = NewsFeedHelpers.linkifyCommitCommentItem(mEvent);
            } else if (eventType.equals("FollowEvent")) {
                html = NewsFeedHelpers.linkifyFollowItem(mEvent);
            } else if (eventType.equals("ForkEvent")) {
                html = NewsFeedHelpers.linkifyForkItem(mEvent);
            } else if (eventType.equals("IssuesEvent")) {
                html = NewsFeedHelpers.linkifyIssueItem(mEvent);
            } else if (eventType.equals("WatchEvent")) {
                html = NewsFeedHelpers.linkifyWatchItem(mEvent);
            } else if (eventType.equals("GistEvent")) {
                html = NewsFeedHelpers.linkifyGistItem(mEvent);
            } else if (eventType.equals("PublicEvent")) {
                html = NewsFeedHelpers.linkifyPublicItem(mEvent);
            } else {
                html = NewsFeedHelpers.linkifyOtherItem(mEvent);
            }
            final String out = CSS + html;
            content.loadDataWithBaseURL("hubroid", out, "text/html", "UTF-8", "hubroid");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

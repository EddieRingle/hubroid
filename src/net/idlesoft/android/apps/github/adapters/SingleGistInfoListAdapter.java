package net.idlesoft.android.apps.github.adapters;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Hubroid;
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter.ViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SingleGistInfoListAdapter extends JsonListAdapter {
    public static final class ViewHolder {
        TextView title;
        TextView content;
    };

    public SingleGistInfoListAdapter(Activity pActivity, AbsListView pListView) {
        super(pActivity, pListView);
    }

    public View doGetView(final int index, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.singlegist_info_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_singlegistinfo_title);
            holder.content = (TextView) convertView.findViewById(R.id.tv_singlegistinfo_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            final JSONObject data = (JSONObject) getData().get(index);
            final String title = data.getString("title");
            final String content = data.getString("content");
            holder.title.setText(title);
            holder.content.setText(content);
        } catch (JSONException e) {
            convertView.setVisibility(View.GONE);
            e.printStackTrace();
        }
/*
            final JSONObject entry = (JSONObject) getData().get(index);
            final JSONObject payload = entry.getJSONObject("payload");
            String end;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Hubroid.GITHUB_ISSUES_TIME_FORMAT);
            final Date item_time = dateFormat.parse(entry.getString("created_at"));
            final Date current_time = new Date();
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
                holder.date.setText(day + end);
            } else if (hour > 0) {
                if (hour == 1) {
                    end = " hour ago";
                } else {
                    end = " hours ago";
                }
                holder.date.setText(hour + end);
            } else if (min > 0) {
                if (min == 1) {
                    end = " minute ago";
                } else {
                    end = " minutes ago";
                }
                holder.date.setText(min + end);
            } else {
                if (sec == 1) {
                    end = " second ago";
                } else {
                    end = " seconds ago";
                }
                holder.date.setText(sec + end);
            }

            final String actor = entry.getString("actor");
            final String eventType = entry.getString("type");
            String title = actor + " did something...";
            holder.gravatar.setImageBitmap(mGravatars.get(actor));

            if (eventType.contains("PushEvent")) {
                holder.icon.setImageResource(R.drawable.push);
                title = actor + " pushed to " + payload.getString("ref").split("/")[2] + " at "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("WatchEvent")) {
                final String action = payload.getString("action");
                if (action.equalsIgnoreCase("started")) {
                    holder.icon.setImageResource(R.drawable.watch_started);
                } else {
                    holder.icon.setImageResource(R.drawable.watch_stopped);
                }
                title = actor + " " + action + " watching "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("GistEvent")) {
                final String action = payload.getString("action");
                holder.icon.setImageResource(R.drawable.gist);
                title = actor + " " + action + "d " + payload.getString("name");
            } else if (eventType.contains("ForkEvent")) {
                holder.icon.setImageResource(R.drawable.fork);
                title = actor + " forked " + entry.getJSONObject("repository").getString("name")
                        + "/" + entry.getJSONObject("repository").getString("owner");
            } else if (eventType.contains("CommitCommentEvent")) {
                holder.icon.setImageResource(R.drawable.comment);
                title = actor + " commented on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("ForkApplyEvent")) {
                holder.icon.setImageResource(R.drawable.merge);
                title = actor + " applied fork commits to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("FollowEvent")) {
                holder.icon.setImageResource(R.drawable.follow);
                title = actor + " started following "
                        + payload.getJSONObject("target").getString("login");
            } else if (eventType.contains("CreateEvent")) {
                holder.icon.setImageResource(R.drawable.create);
                if (payload.getString("ref_type").contains("repository")) {
                    title = actor + " created repository " + payload.getString("name");
                } else if (payload.getString("ref_type").contains("branch")) {
                    title = actor + " created branch " + payload.getString("ref") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("ref_type").contains("tag")) {
                    title = actor + " created tag " + payload.getString("ref") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("IssuesEvent")) {
                if (payload.getString("action").equalsIgnoreCase("opened")) {
                    holder.icon.setImageResource(R.drawable.issues_open);
                } else {
                    holder.icon.setImageResource(R.drawable.issues_closed);
                }
                title = actor + " " + payload.getString("action") + " issue "
                        + payload.getInt("number") + " on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("DeleteEvent")) {
                holder.icon.setImageResource(R.drawable.delete);
                if (payload.getString("ref_type").contains("repository")) {
                    title = actor + " deleted repository " + payload.getString("name");
                } else if (payload.getString("ref_type").contains("branch")) {
                    title = actor + " deleted branch " + payload.getString("ref") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("ref_type").contains("tag")) {
                    title = actor + " deleted tag " + payload.getString("ref") + " at "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("WikiEvent")) {
                holder.icon.setImageResource(R.drawable.wiki);
                title = actor + " " + payload.getString("action") + " a page in the "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name") + " wiki";
            } else if (eventType.contains("DownloadEvent")) {
                holder.icon.setImageResource(R.drawable.download);
                title = actor + " uploaded a file to "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("PublicEvent")) {
                holder.icon.setImageResource(R.drawable.opensource);
                title = actor + " open sourced "
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("PullRequestEvent")) {
                final int number = (payload.get("pull_request") instanceof JSONObject) ? payload
                        .getJSONObject("pull_request").getInt("number") : payload.getInt("number");
                if (payload.getString("action").equalsIgnoreCase("opened")) {
                    holder.icon.setImageResource(R.drawable.issues_open);
                    title = actor + " opened pull request " + number + " on "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                } else if (payload.getString("action").equalsIgnoreCase("closed")) {
                    holder.icon.setImageResource(R.drawable.issues_closed);
                    title = actor + " closed pull request " + number + " on "
                            + entry.getJSONObject("repository").getString("owner") + "/"
                            + entry.getJSONObject("repository").getString("name");
                }
            } else if (eventType.contains("MemberEvent")) {
                holder.icon.setImageResource(R.drawable.follow);
                title = actor + " added " + payload.getJSONObject("member").getString("login")
                        + " to " + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            } else if (eventType.contains("IssueCommentEvent")) {
                holder.icon.setImageResource(R.drawable.comment);
                title = actor + " commented on issue "
                        + entry.getString("url").split("/")[6].split("#")[0] + " on "
                        + entry.getJSONObject("repository").getString("owner") + "/"
                        + entry.getJSONObject("repository").getString("name");
            }
            holder.title.setText(title);
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final ParseException e) {
            e.printStackTrace();
        }
*/
        return convertView;
    }
}

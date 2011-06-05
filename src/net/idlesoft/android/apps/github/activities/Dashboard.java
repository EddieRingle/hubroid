/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Dashboard extends BaseActivity {
    private static class GetLatestBlogPostTask extends AsyncTask<Void, Void, JSONObject> {
        public Dashboard activity;

        @Override
        protected JSONObject doInBackground(final Void... params) {
            try {
                final Response response = new Response();
                // Setup connection
                HttpURLConnection conn = (HttpURLConnection) (new URL(
                        "http://query.yahooapis.com/v1/public/yql?q=select%20title%2Clink.href%20from%20atom%20where%20url%3D%22https%3A%2F%2Fgithub.com%2Fblog.atom%22%20limit%201&format=json"))
                        .openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                // Get response from the server
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();

                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    sb.append(line + '\n');
                }

                // Store response in a Response object
                try {
                    response.statusCode = conn.getResponseCode();
                } catch (final IOException e) {
                    response.statusCode = 401;
                }
                response.resp = sb.toString();

                // Clean up
                conn.disconnect();
                conn = null;
                in = null;
                sb = null;

                if (response.statusCode == 200) {
                    final JSONObject feedJson = new JSONObject(response.resp);
                    return feedJson.getJSONObject("query").getJSONObject("results")
                            .getJSONObject("entry");
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            final TextView postTitle = (TextView) activity
                    .findViewById(R.id.tv_dashboard_latestPost_title);
            final TextView postLink = (TextView) activity
                    .findViewById(R.id.tv_dashboard_latestPost_link);
            final ProgressBar progress = (ProgressBar) activity
                    .findViewById(R.id.pb_dashboard_latestPost_progress);

            try {
                postTitle.setText(result.getString("title"));
                postLink.setText(result.getJSONObject("link").getString("href"));
            } catch (final Exception e) {
                postTitle.setText("Visit the GitHub Blog!");
                postLink.setText("http://www.github.com/blog");
                e.printStackTrace();
            }

            progress.setVisibility(View.GONE);
            postTitle.setVisibility(View.VISIBLE);
            postLink.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPreExecute() {
            final TextView postTitle = (TextView) activity
                    .findViewById(R.id.tv_dashboard_latestPost_title);
            final TextView postLink = (TextView) activity
                    .findViewById(R.id.tv_dashboard_latestPost_link);
            final ProgressBar progress = (ProgressBar) activity
                    .findViewById(R.id.pb_dashboard_latestPost_progress);

            postTitle.setVisibility(View.GONE);
            postLink.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }
    }

    public GetLatestBlogPostTask mGetLatestBlogPostTask;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.dashboard);

        setupActionBar("Hubroid", true, false);

        LinearLayout custom = new LinearLayout(Dashboard.this);
        custom.setOrientation(LinearLayout.HORIZONTAL);
        custom.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(Dashboard.this);
        logo.setImageResource(R.drawable.icon);
        logo.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        custom.addView(logo);

        TextView title = new TextView(Dashboard.this);
        title.setText("Hubroid");
        title.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        title.setTextColor(Color.parseColor("#333333"));
        title.setTextSize(16.0f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(10, 0, 0, 0);
        custom.addView(title);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(custom);

        // News Feed
        ((Button) findViewById(R.id.btn_dashboard_newsfeed))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, NewsFeed.class));
                    }
                });

        // Repositories
        ((Button) findViewById(R.id.btn_dashboard_repositories))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, Repositories.class));
                    }
                });

        // Followers/Following
        ((Button) findViewById(R.id.btn_dashboard_users)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(Dashboard.this, Users.class));
            }
        });

        // Profile
        ((Button) findViewById(R.id.btn_dashboard_myprofile))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, Profile.class));
                    }
                });

        // Gists
        ((Button) findViewById(R.id.btn_dashboard_gists))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, Gists.class));
                    }
                });
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        final TextView postTitle = (TextView) findViewById(R.id.tv_dashboard_latestPost_title);
        final TextView postLink = (TextView) findViewById(R.id.tv_dashboard_latestPost_link);

        postTitle.setText(savedInstanceState.getString("postTitle"));
        postLink.setText(savedInstanceState.getString("postLink"));

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        mGetLatestBlogPostTask = (GetLatestBlogPostTask) getLastNonConfigurationInstance();
        if (mGetLatestBlogPostTask == null) {
            mGetLatestBlogPostTask = new GetLatestBlogPostTask();
        }
        mGetLatestBlogPostTask.activity = Dashboard.this;
        if ((mGetLatestBlogPostTask.getStatus() == AsyncTask.Status.PENDING)
                && (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)) {
            mGetLatestBlogPostTask.execute();
        }
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetLatestBlogPostTask;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        final TextView postTitle = (TextView) findViewById(R.id.tv_dashboard_latestPost_title);
        final TextView postLink = (TextView) findViewById(R.id.tv_dashboard_latestPost_link);

        outState.putString("postTitle", postTitle.getText().toString());
        outState.putString("postLink", postLink.getText().toString());

        super.onSaveInstanceState(outState);
    }
}

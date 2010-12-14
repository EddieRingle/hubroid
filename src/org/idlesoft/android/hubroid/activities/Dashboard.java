
package org.idlesoft.android.hubroid.activities;

import org.idlesoft.android.hubroid.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class Dashboard extends Activity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(Dashboard.this, Search.class));
            }
        });

        ((Button) findViewById(R.id.btn_dashboard_newsfeed))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, NewsFeed.class));
                    }
                });

        ((Button) findViewById(R.id.btn_dashboard_repositories))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, Repositories.class));
                    }
                });

        ((Button) findViewById(R.id.btn_dashboard_users)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(Dashboard.this, Users.class));
            }
        });

        ((Button) findViewById(R.id.btn_dashboard_myprofile))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        startActivity(new Intent(Dashboard.this, Profile.class));
                    }
                });
    }
}

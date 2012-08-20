/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.activities;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.eddieringle.android.libs.undergarment.widgets.DrawerGarment;

import net.idlesoft.android.apps.github.R;

import android.os.Bundle;
import android.widget.ListView;

public class BaseDashboardActivity extends BaseActivity {

    public static final String EXTRA_SHOWING_DASH = "showing_dash";

    private boolean mShowingDash;

    private DrawerGarment mDrawerGarment;

    private ListView mDashboardListView;

    public DrawerGarment getDrawerGarment() {
        return mDrawerGarment;
    }

    @Override
    protected void onCreate(Bundle icicle, int layout) {
        super.onCreate(icicle, layout);

        if (icicle != null) {
            mShowingDash = icicle.getBoolean(EXTRA_SHOWING_DASH, false);
        }

        mDrawerGarment = new DrawerGarment(this, R.layout.dashboard);
        mDrawerGarment.setSlideTarget(DrawerGarment.SLIDE_TARGET_CONTENT);
        mDrawerGarment.setDrawerCallbacks(new DrawerGarment.IDrawerCallbacks() {
            @Override
            public void onDrawerOpened() {
                mShowingDash = true;
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onDrawerClosed() {
                mShowingDash = false;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mShowingDash) {
            mDrawerGarment.openDrawer();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EXTRA_SHOWING_DASH, mShowingDash);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mShowingDash = savedInstanceState.getBoolean(EXTRA_SHOWING_DASH, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if ((getSupportActionBar()
                    .getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) == ActionBar
                    .DISPLAY_HOME_AS_UP) {
                onBackPressed();
            } else {
                mDrawerGarment.openDrawer();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerGarment.isDrawerOpened()) {
            mDrawerGarment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}

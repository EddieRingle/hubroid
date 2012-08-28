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

package net.idlesoft.android.apps.github.ui.activities.app;

import com.viewpagerindicator.TitlePageIndicator;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseDashboardActivity;
import net.idlesoft.android.apps.github.ui.fragments.app.EventListFragment;
import net.idlesoft.android.apps.github.ui.fragments.app.ProfileFragment;
import net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import static net.idlesoft.android.apps.github.ui.fragments.app.EventListFragment.ARG_EVENT_LIST_TYPE;
import static net.idlesoft.android.apps.github.ui.fragments.app.EventListFragment.LIST_USER_PUBLIC;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.ARG_LIST_TYPE;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_USER;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_STARRED;

public class ProfileActivity extends BaseDashboardActivity {

    private ProfileFragmentPagerAdapter mPagerAdapter;

    private TitlePageIndicator mTitlePageIndicator;

    private ViewPager mViewPager;

    private class ProfileFragmentPagerAdapter extends FragmentPagerAdapter {

        public ProfileFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment fragment;
            final Bundle args = new Bundle();
            args.putAll(getIntent().getExtras());

            switch (position) {
                case 0:
                    fragment = new ProfileFragment();
                    break;
                case 1:
                    fragment = new EventListFragment();
                    args.putInt(ARG_EVENT_LIST_TYPE, LIST_USER_PUBLIC);
                    break;
                case 2:
                    fragment = new RepositoryListFragment();
                    args.putInt(ARG_LIST_TYPE, LIST_USER);
                    break;
                case 3:
                    fragment = new RepositoryListFragment();
                    args.putInt(ARG_LIST_TYPE, LIST_STARRED);
                    break;
                default:
                    fragment = new Fragment();
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Profile";
                case 1:
                    return getString(R.string.events_public);
                case 2:
                    return getString(R.string.repositories);
                case 3:
                    return getString(R.string.repositories_starred);
                default:
                    return "Fragment #" + position;
            }
        }
    }

    @Override
    protected void onCreate(Bundle icicle, int layout) {
        super.onCreate(icicle, R.layout.viewpager_fragment);

        mTitlePageIndicator = (TitlePageIndicator) findViewById(R.id.tpi_header);
        mViewPager = (ViewPager) findViewById(R.id.vp_pages);
        mViewPager.setOffscreenPageLimit(5);

        mPagerAdapter = new ProfileFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mTitlePageIndicator.setViewPager(mViewPager);
    }
}

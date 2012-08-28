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

package net.idlesoft.android.apps.github.ui.widgets;

import com.actionbarsherlock.view.MenuItem;

import net.idlesoft.android.apps.github.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class RefreshActionView extends ImageView {

    protected Animation mAnimation = null;

    protected MenuItem mItem = null;

    public RefreshActionView(Context context) {
        super(context);
        setupAnimation();
    }

    public RefreshActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupAnimation();
    }

    public RefreshActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupAnimation();
    }

    public void addTo(MenuItem item) {
        mItem = item;
        startAnimation(mAnimation);
        item.setActionView(this);
    }

    public void removeFromParentItem() {
        removeFrom(mItem);
        mItem = null;
    }

    public void removeFrom(final MenuItem item) {
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                item.setActionView(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mAnimation.setRepeatCount(0);
    }

    protected void setupAnimation() {
        if (mAnimation == null) {
            mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.clockwise_refresh);
            mAnimation.setRepeatCount(Animation.INFINITE);
        }
    }
}

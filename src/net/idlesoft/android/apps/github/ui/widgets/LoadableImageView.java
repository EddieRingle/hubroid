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

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LoadableImageView extends FrameLayout {

    private final static int INTERNAL_PROGRESS_ID = 0x00ff0001;

    private final static int INTERNAL_IMAGEVIEW_ID = 0x00ff0002;

    private ProgressBar mProgressBar;

    private ImageView mImageView;

    private boolean mIsLoading;

    public LoadableImageView(Context context) {
        super(context);
        buildView();
    }

    public LoadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        buildView();
    }

    public LoadableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        buildView();
    }

    protected void buildView() {
        final Context context = getContext();

        mProgressBar = new ProgressBar(context);
        final LayoutParams progressLayoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        progressLayoutParams.gravity = Gravity.CENTER;
        mProgressBar.setLayoutParams(progressLayoutParams);
        mProgressBar.setId(INTERNAL_PROGRESS_ID);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

        addView(mProgressBar);

        mIsLoading = false;

        mImageView = new ImageView(context);
        final LayoutParams imageViewLayoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        imageViewLayoutParams.gravity = Gravity.CENTER;
        mImageView.setLayoutParams(imageViewLayoutParams);
        mImageView.setId(INTERNAL_IMAGEVIEW_ID);
        mImageView.setVisibility(View.VISIBLE);

        addView(mImageView);

        setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    }

    public void setIsLoading(final boolean isLoading) {
        if (mIsLoading == isLoading) {
            return;
        }

        mIsLoading = isLoading;

        if (isLoading) {
            mProgressBar.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
        } else {
            mImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ImageView getImageView() {
        return mImageView;
    }
}

/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public
class GravatarView extends LoadableImageView
{
	private
	Bitmap mSourceBitmap;

	private
	int mDefaultResource;

	private
	String mGravatarHash;

	private
	Handler mHandler = new Handler();

	private
	GravatarViewCallback mGravatarViewCallback;

	private
	Thread mGravatarThread = new Thread(new Runnable()
	{
		@Override
		public
		void run()
		{
			mSourceBitmap = GravatarCache.getGravatar(mGravatarHash, 140);
			mHandler.post(new Runnable()
			{
				@Override
				public
				void run()
				{
					getImageView().setImageBitmap(mSourceBitmap);
					setIsLoading(false);
					if (mGravatarViewCallback != null)
						mGravatarViewCallback.OnGravatarFinishedLoading(mSourceBitmap);
				}
			});
		}
	});

	public
	GravatarView(Context context)
	{
		super(context);
	}

	public
	GravatarView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public
	GravatarView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected
	void buildView()
	{
		super.buildView();

		getImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
	}

	public
	void setDefaultResource(final int resource)
	{
		mDefaultResource = resource;

		if (mSourceBitmap == null && !isLoading()) {
			getImageView().setImageResource(resource);
		}
	}

	public
	int getDefaultResource()
	{
		return mDefaultResource;
	}

	public
	void setGravatarHash(final String gravatarHash)
	{
		if (mGravatarHash == null || !mGravatarHash.equals(gravatarHash)) {
			mGravatarHash = gravatarHash;

			setIsLoading(true);

			getImageView().measure(WRAP_CONTENT, WRAP_CONTENT);
			mGravatarThread.start();
		}
	}

	public
	String getGravatarHash()
	{
		return mGravatarHash;
	}

	public
	Bitmap getSourceBitmap()
	{
		return mSourceBitmap;
	}

	@Override
	protected
	Parcelable onSaveInstanceState()
	{
		if (mGravatarThread.isAlive()) {
			mGravatarThread.interrupt();
		}

		return super.onSaveInstanceState();
	}

	@Override
	protected
	void onRestoreInstanceState(Parcelable state)
	{
		if (mSourceBitmap == null && mGravatarHash != null) {
			setIsLoading(true);
			getImageView().measure(WRAP_CONTENT, WRAP_CONTENT);
			mGravatarThread.start();
		} else if (mSourceBitmap != null) {
			getImageView().setImageBitmap(mSourceBitmap);
		}

		super.onRestoreInstanceState(state);
	}

	public
	interface GravatarViewCallback
	{
		public abstract
		void OnGravatarFinishedLoading(Bitmap sourceBitmap);
	}

	public
	void setGravatarViewCallback(GravatarViewCallback callback)
	{
		mGravatarViewCallback = callback;
	}
}
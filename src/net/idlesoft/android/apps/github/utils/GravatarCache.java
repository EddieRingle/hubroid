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

package net.idlesoft.android.apps.github.utils;

import net.idlesoft.android.apps.github.R;

import net.idlesoft.android.apps.github.ui.HubroidApplication;
import org.eclipse.egit.github.core.service.UserService;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GravatarCache {

	/* Root directory for gravatar storage on the SD card */
	private final static String ROOT_DIR = "hubroid/gravatars";

	private static Bitmap downloadGravatar(final String gravatarHash) throws IOException {
		final URL aURL;
		final Bitmap bm;
		final InputStream is;
		HttpURLConnection conn = null;

		if (gravatarHash != null && !gravatarHash.equals("")) {
			aURL = new URL("http://www.gravatar.com/avatar/" + gravatarHash + "?s=140&d=404");
		} else {
			aURL = null;
		}

		if (aURL != null) {
			conn = (HttpURLConnection) aURL.openConnection();
			conn.setDoInput(true);
			conn.connect();
		}

		if (aURL == null || (conn != null && conn.getResponseCode() != 404)) {
			is = conn.getInputStream();
			bm = BitmapFactory.decodeStream(is);
			is.close();
			return bm;
		} else {
			return null;
		}
	}

	private static File ensure_directory(final String path) throws IOException {
		File root = Environment.getExternalStorageDirectory();
		if (!root.canWrite()) {
			throw new IOException("External storage directory is not writable");
		}
		final String[] parts = path.split("/");
		for (final String part : parts) {
			final File f = new File(root, part);
			if (!f.exists()) {
				final boolean created = f.mkdir();
				if (!created) {
					throw new IOException("Unable to create directory " + part);
				}
			} else {
				if (!f.isDirectory()) {
					throw new IOException("Unable to create directory " + part);
				}
			}
			root = f;
		}
		return root;
	}

	/**
	 * Returns a density-independent Bitmap of the Gravatar associated with the
	 * provided login. The image will be scaled to a variable pixel size
	 * dependent on the provided dip size.
	 *
	 * @param gravatarHash
	 * @param size - Size in density-independent pixels (dip)
	 * @param densityScale - Scale provided by
	 *            android.util.DisplayMetrics.density
	 */
	public static Bitmap getDipGravatar(final String gravatarHash, final float size,
										final float densityScale) {
		return getGravatar(gravatarHash, (int) (size * densityScale + 0.5f));
	}

	/**
	 * Returns a Bitmap of the Gravatar associated with the provided login. This
	 * image will be scaled according to the provided size.
	 *
	 * @param gravatarHash
	 * @param size
	 * @return a scaled Bitmap
	 */
	public static Bitmap getGravatar(final String gravatarHash, final int size) {
		Bitmap bm = null;
		try {
			final File gravatars = ensure_directory(ROOT_DIR);
			/* Prevents the gravatars from showing up in the Gallery app */
			hideMediaFromGallery(gravatars);

			final File image = new File(gravatars, gravatarHash + ".png");
			bm = BitmapFactory.decodeFile(image.getPath());
			if (bm == null) {
				bm = downloadGravatar(gravatarHash);
				/* Compress to a 140x140px PNG */
				bm.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(image));
			}
			bm = Bitmap.createScaledBitmap(bm, size, size, true);
		} catch (final IOException e) {
			Log.e("hubroid", "Error saving bitmap", e);
			e.printStackTrace();
		}
		return bm;
	}

	private static void hideMediaFromGallery(final File gravatars) throws IOException {
		final File nomedia = new File(gravatars, ".nomedia");
		if (!nomedia.exists()) {
			nomedia.createNewFile();
		}
	}

	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
			return path.delete();
		} else {
			return false;
		}
	}

	public static boolean clearCache() {
		final File gravatars = new File(Environment.getExternalStorageDirectory(), ROOT_DIR);
		return deleteDirectory(gravatars);
	}
}

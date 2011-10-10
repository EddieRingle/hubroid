/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.utils;

import net.idlesoft.android.apps.github.HubroidApplication;
import net.idlesoft.android.apps.github.R;

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

    private static Bitmap downloadGravatar(final String login) throws IOException {
        final URL aURL;
        final UserService us = new UserService(HubroidApplication.getGitHubClientInstance());
        if (login != null && !login.equals("")) {
            aURL = new URL(us.getUser(login).getAvatarUrl());
        } else {
            aURL = new URL(
                    "https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png");
        }

        final HttpURLConnection conn = (HttpURLConnection) aURL.openConnection();
        conn.setDoInput(true);
        conn.connect();
        final Bitmap bm;
        if (conn.getResponseCode() != 404) {
            final InputStream is = conn.getInputStream();
            bm = BitmapFactory.decodeStream(is);
            is.close();
        } else {
            bm = BitmapFactory.decodeResource(HubroidApplication.getAppResources(),
                    R.drawable.gravatar);
        }
        return bm;
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
     * @param login
     * @param size - Size in density-independent pixels (dip)
     * @param densityScale - Scale provided by
     *            android.util.DisplayMetrics.density
     */
    public static Bitmap getDipGravatar(final String login, final float size,
            final float densityScale) {
        return getGravatar(login, (int) (size * densityScale + 0.5f));
    }

    /**
     * Returns a Bitmap of the Gravatar associated with the provided login. This
     * image will be scaled according to the provided size.
     * 
     * @param login
     * @param size
     * @return a scaled Bitmap
     */
    public static Bitmap getGravatar(final String login, final int size) {
        Bitmap bm = null;
        try {
            final File gravatars = ensure_directory(ROOT_DIR);
            /* Prevents the gravatars from showing up in the Gallery app */
            hideMediaFromGallery(gravatars);

            final File image = new File(gravatars, login + ".png");
            bm = BitmapFactory.decodeFile(image.getPath());
            if (bm == null) {
                bm = downloadGravatar(login);
                /* Compress to a 100x100px PNG */
                bm.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(image));
            }
            bm = Bitmap.createScaledBitmap(bm, size, size, true);
        } catch (final IOException e) {
            Log.e("debug", "Error saving bitmap", e);
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

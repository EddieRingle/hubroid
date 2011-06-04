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

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GravatarCache {

    /* Root directory for gravatar storage on the SD card */
    private final static String ROOT_DIR = "hubroid/gravatars";

    private static Bitmap downloadGravatar(final String id) throws IOException {
        final URL aURL;
        if (id != null && !id.equals("")) {
            aURL = new URL("http://www.gravatar.com/avatar/" + URLEncoder.encode(id)
                    + "?size=100&d=404");
        } else {
            aURL = new URL("http://www.gravatar.com/avatar/?size=140&d=404");
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
            bm = BitmapFactory.decodeResource(HubroidApplication.getAppResources(), R.drawable.gravatar);
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
     * provided ID. The image will be scaled to a variable pixel size dependent
     * on the provided dip size.
     * 
     * @param id
     * @param size - Size in density-independent pixels (dip)
     * @param densityScale - Scale provided by
     *            android.util.DisplayMetrics.density
     */
    public static Bitmap getDipGravatar(final String id, final float size, final float densityScale) {
        return getGravatar(id, (int) (size * densityScale + 0.5f));
    }

    /**
     * Returns a Bitmap of the Gravatar associated with the provided ID. This
     * image will be scaled according to the provided size.
     * 
     * @param id
     * @param size
     * @return a scaled Bitmap
     */
    public static Bitmap getGravatar(final String id, final int size) {
        Bitmap bm = null;
        try {
            final File gravatars = ensure_directory(ROOT_DIR);
            /* Prevents the gravatars from showing up in the Gallery app */
            hideMediaFromGallery(gravatars);

            final File image = new File(gravatars, id + ".png");
            bm = BitmapFactory.decodeFile(image.getPath());
            if (bm == null) {
                bm = downloadGravatar(id);
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

    /**
     * Returns a Gravatar ID associated with the provided name
     * 
     * @param name
     * @return the gravatar ID associated with the name
     */
    public static String getGravatarID(final String name) {
        String id = "";
        try {
            final File gravatars = ensure_directory(ROOT_DIR);
            final File gravatar_id = new File(gravatars, name + ".id");

            if (gravatar_id.isFile()) {
                final FileReader fr = new FileReader(gravatar_id);
                final BufferedReader in = new BufferedReader(fr);
                id = in.readLine();
                in.close();
            } else {
                id = getGravatarIdFromGithub(name);
                if (!id.equals("")) {
                    final FileWriter fw = new FileWriter(gravatar_id);
                    final BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(id);
                    bw.flush();
                    bw.close();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return id;
    }

    private static String getGravatarIdFromGithub(final String name) {
        final GitHubAPI gapi = new GitHubAPI();
        try {
            return new JSONObject(gapi.user.info(name).resp).getJSONObject("user").getString(
                    "gravatar_id");
        } catch (final NullPointerException e) {
            return "";
        } catch (final JSONException e) {
            e.printStackTrace();
            return "";
        }
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

/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.utils;

import net.idlesoft.android.apps.github.activities.BaseActivity;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

public class RequestCache {

    private final static String ROOT_DIR = "requests/";
    private final static String REPO_DIR = "repos/";
    private final static String USER_DIR = "users/";

    public static Repository getRepository(final BaseActivity context, final String owner,
            final String name) {
        Repository repo = null;
        boolean shouldRefresh = false;
        final File dir = new File(context.getCacheDir(), ROOT_DIR + REPO_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        final File f = new File(dir, owner + "_" + name + ".json");
        if (f.exists()) {
            /* Check if the cached JSON is really old */
            final Date d = new Date();
            final long elderCheck = d.getTime() - (15 * 24 * 60 * 60000);
            if (f.lastModified() < elderCheck) {
                shouldRefresh = true;
            } else {
                try {
                    final FileInputStream in = context.openFileInput(f.getPath());
                    repo = GsonUtils.fromJson(new BufferedReader(
                            new InputStreamReader(in)), Repository.class);
                    in.close();
                    return repo;
                } catch (Exception e) {
                    shouldRefresh = true;
                }
            }
        } else {
            shouldRefresh = true;
        }
        if (shouldRefresh) {
            final RepositoryService rs = new RepositoryService(context.getGitHubClient());
            try {
                repo = rs.getRepository(owner, name);
                if (repo != null) {
                    putRepository(context, repo);
                }
            } catch (Exception e) {
                repo = null;
                e.printStackTrace();
            }
        }
        return repo;
    }

    public static boolean putRepository(final BaseActivity context, final Repository repository) {
        final File dir = new File(context.getCacheDir(), ROOT_DIR + REPO_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        final File f = new File(dir, repository.getOwner().getLogin() + "_"
                + repository.getName() + ".json");
        if (f.exists()) {
            f.delete();
        }
        try {
            final FileOutputStream out = context.openFileOutput(f.getPath(), 0);
            final String outJson = GsonUtils.toJson(repository);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(outJson);
            writer.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getUser(final BaseActivity context, final String login) {
        User user = null;
        boolean shouldRefresh = false;
        final File dir = new File(context.getCacheDir(), ROOT_DIR + USER_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        final File f = new File(dir, login + ".json");
        if (f.exists()) {
            /* Check if the cached JSON is really old */
            final Date d = new Date();
            final long elderCheck = d.getTime() - (15 * 24 * 60 * 60000);
            if (f.lastModified() < elderCheck) {
                shouldRefresh = true;
            } else {
                try {
                    final FileInputStream in = context.openFileInput(f.getPath());
                    user = GsonUtils.fromJson(new BufferedReader(
                            new InputStreamReader(in)), User.class);
                    in.close();
                    return user;
                } catch (Exception e) {
                    shouldRefresh = true;
                }
            }
        } else {
            shouldRefresh = true;
        }
        if (shouldRefresh) {
            final UserService us = new UserService(context.getGitHubClient());
            try {
                user = us.getUser(login);
                if (user != null) {
                    putUser(context, user);
                }
            } catch (Exception e) {
                user = null;
                e.printStackTrace();
            }
        }
        return user;
    }

    public static boolean putUser(final BaseActivity context, final User user) {
        final File dir = new File(context.getCacheDir(), ROOT_DIR + USER_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        final File f = new File(dir, user.getLogin() + ".json");
        if (f.exists()) {
            f.delete();
        }
        try {
            final FileOutputStream out = context.openFileOutput(f.getPath(), 0);
            final String outJson = GsonUtils.toJson(user);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(outJson);
            writer.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    public static boolean clearCache(final Context context) {
        final File root = new File(context.getCacheDir(), ROOT_DIR);
        return deleteDirectory(root);
    }
}

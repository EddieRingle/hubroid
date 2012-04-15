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

import android.provider.BaseColumns;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;

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
	private final static String REPOSITORY_DIR = "repositories/";
	private final static String USER_DIR = "users/";

	public static Repository getRepository(final BaseActivity context, final String owner,
										   final String name, final boolean forceUpdate)
	{
		Repository repo = null;
		boolean shouldRefresh = false;
		final File dir = new File(context.getCacheDir(), ROOT_DIR + REPOSITORY_DIR);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
		final File f = new File(dir, owner + "_" + name + ".json");
		if (!forceUpdate && f.exists()) {
			/* Check if the cached JSON is really old */
			final Date d = new Date();
			final long elderCheck = d.getTime() - (15 * 24 * 60 * 60000);
			if (f.lastModified() < elderCheck) {
				shouldRefresh = true;
			} else {
				try {
					final FileInputStream in = new FileInputStream(f);
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
		if (shouldRefresh || forceUpdate) {
			try {
				final RepositoryService rs = new RepositoryService(context.getGHClient());
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

	public static Repository getRepository(final BaseActivity context, final String owner,
										   final String name)
	{
		return getRepository(context, owner, name, false);
	}

	public static boolean putRepository(final BaseActivity context, final Repository repository)
	{
		final File dir = new File(context.getCacheDir(), ROOT_DIR + REPOSITORY_DIR);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
		final File f = new File(dir, repository.getOwner().getLogin() + "_"
				+ repository.getName() + ".json");
		if (f.exists()) {
			f.delete();
		}
		try {
			final FileOutputStream out = new FileOutputStream(f, false);
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

	public static User getUser(final BaseActivity context, final String login,
							   final boolean forceUpdate)
	{
		User user = null;
		boolean shouldRefresh = false;
		final File dir = new File(context.getCacheDir(), ROOT_DIR + USER_DIR);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
		final File f = new File(dir, login + ".json");
		if (!forceUpdate && f.exists()) {
			/* Check if the cached JSON is really old (>1 day) */
			final Date d = new Date();
			/* TODO: The amount of time to keep cache should be a preference, really */
			final long elderCheck = d.getTime() - (24 * 60 * 60000);
			if (f.lastModified() < elderCheck) {
				shouldRefresh = true;
			} else {
				try {
					final FileInputStream in = new FileInputStream(f);
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
		if (shouldRefresh || forceUpdate) {
			try {
				final UserService us = new UserService(context.getGHClient());
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

	public static User getUser(final BaseActivity context, final String login)
	{
		return getUser(context, login, false);
	}

	public static boolean putUser(final BaseActivity context, final User user)
	{
		final File dir = new File(context.getCacheDir(), ROOT_DIR + USER_DIR);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
		final File f = new File(dir, user.getLogin() + ".json");
		if (f.exists()) {
			f.delete();
		}
		try {
			final FileOutputStream out = new FileOutputStream(f, false);
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

	private static boolean deleteDirectory(File path)
	{
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

	public static boolean clearCache(final Context context)
	{
		final File root = new File(context.getCacheDir(), ROOT_DIR);
		return deleteDirectory(root);
	}

	public static boolean clearRepositoryCache(final Context context)
	{
		final File repositories = new File(context.getCacheDir(), REPOSITORY_DIR);
		return deleteDirectory(repositories);
	}

	public static boolean clearUserCache(final Context context)
	{
		final File users = new File(context.getCacheDir(), USER_DIR);
		return deleteDirectory(users);
	}
}
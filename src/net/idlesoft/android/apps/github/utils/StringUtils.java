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

package net.idlesoft.android.apps.github.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public
class StringUtils
{
	/**
	 * Get time string since a specified date
	 *
	 * @param itemTime
	 * @return String of format "X U(s)", X is the quantity of U time units
	 *         (e.g., "3 days")
	 */
	public static
	String getTimeSince(final Date itemTime)
	{
		final Date currentTime = new Date();
		final long ms = currentTime.getTime() - itemTime.getTime();
		final long sec = ms / 1000;
		final long min = sec / 60;
		final long hour = min / 60;
		final long day = hour / 24;
		final long month = day / 30;
		final long year = day / 365;
		if (year > 0) {
			if (year == 1) {
				return year + " year";
			} else {
				return year + " years";
			}
		} else if (month > 0) {
			if (month == 1) {
				return month + " month";
			} else {
				return month + " months";
			}
		} else if (day > 0) {
			if (day == 1) {
				return day + " day";
			} else {
				return day + " days";
			}
		} else if (hour > 0) {
			if (hour == 1) {
				return hour + " hour";
			} else {
				return hour + " hours";
			}
		} else if (min > 0) {
			if (min == 1) {
				return min + " minute";
			} else {
				return min + " minutes";
			}
		} else if (sec > 0) {
			if (sec == 1) {
				return sec + " second";
			} else {
				return sec + " seconds";
			}
		} else {
			return ms + " ms";
		}
	}

	/**
	 * Get the extension of a file
	 *
	 * @param filename
	 * @return filename's extension, not including the preceding period
	 */
	public static
	String getExtension(final String filename)
	{
		try {
			return filename.substring(filename.lastIndexOf('.') + 1);
		} catch (IndexOutOfBoundsException e) {
			// Filename has no extension, oh well
			return "";
		} catch (NullPointerException e) {
			return "";
		}
	}

	/**
	 * Returns a map from a HTTP query string
	 *
	 * @param query
	 * @return Map of query parameters
	 */
	public static
	Map<String, String> mapQueryString(String query)
	{
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String[] split = param.split("=");
			map.put(split[0], split[1]);
		}
		return map;
	}

	public static
	boolean isStringEmpty(final String str)
	{
		return str == null || str.equals("");
	}

	public static
	CharSequence trimTrailingWhitespace(CharSequence text) {
		while (text.charAt(text.length() - 1) == '\n')
			text = text.subSequence(0, text.length() - 1);
		return text;
	}
}
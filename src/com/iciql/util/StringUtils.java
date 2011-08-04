/*
 * Copyright 2004-2011 H2 Group.
 * Copyright 2011 James Moger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iciql.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Common string utilities.
 * 
 */
public class StringUtils {

	/**
	 * Replace all occurrences of the before string with the after string.
	 * 
	 * @param s
	 *            the string
	 * @param before
	 *            the old text
	 * @param after
	 *            the new text
	 * @return the string with the before string replaced
	 */
	public static String replaceAll(String s, String before, String after) {
		int next = s.indexOf(before);
		if (next < 0) {
			return s;
		}
		StringBuilder buff = new StringBuilder(s.length() - before.length() + after.length());
		int index = 0;
		while (true) {
			buff.append(s.substring(index, next)).append(after);
			index = next + before.length();
			next = s.indexOf(before, index);
			if (next < 0) {
				buff.append(s.substring(index));
				break;
			}
		}
		return buff.toString();
	}

	/**
	 * Check if a String is null or empty (the length is null).
	 * 
	 * @param s
	 *            the string to check
	 * @return true if it is null or empty
	 */
	public static boolean isNullOrEmpty(String s) {
		return s == null || s.length() == 0;
	}

	/**
	 * Convert a string to a Java literal using the correct escape sequences.
	 * The literal is not enclosed in double quotes. The result can be used in
	 * properties files or in Java source code.
	 * 
	 * @param s
	 *            the text to convert
	 * @return the Java representation
	 */
	public static String javaEncode(String s) {
		int length = s.length();
		StringBuilder buff = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
			// case '\b':
			// // BS backspace
			// // not supported in properties files
			// buff.append("\\b");
			// break;
			case '\t':
				// HT horizontal tab
				buff.append("\\t");
				break;
			case '\n':
				// LF linefeed
				buff.append("\\n");
				break;
			case '\f':
				// FF form feed
				buff.append("\\f");
				break;
			case '\r':
				// CR carriage return
				buff.append("\\r");
				break;
			case '"':
				// double quote
				buff.append("\\\"");
				break;
			case '\\':
				// backslash
				buff.append("\\\\");
				break;
			default:
				int ch = c & 0xffff;
				if (ch >= ' ' && (ch < 0x80)) {
					buff.append(c);
					// not supported in properties files
					// } else if(ch < 0xff) {
					// buff.append("\\");
					// // make sure it's three characters (0x200 is octal 1000)
					// buff.append(Integer.toOctalString(0x200 |
					// ch).substring(1));
				} else {
					buff.append("\\u");
					// make sure it's four characters
					buff.append(Integer.toHexString(0x10000 | ch).substring(1));
				}
			}
		}
		return buff.toString();
	}

	/**
	 * Pad a string. This method is used for the SQL function RPAD and LPAD.
	 * 
	 * @param string
	 *            the original string
	 * @param n
	 *            the target length
	 * @param padding
	 *            the padding string
	 * @param right
	 *            true if the padding should be appended at the end
	 * @return the padded string
	 */
	public static String pad(String string, int n, String padding, boolean right) {
		if (n < 0) {
			n = 0;
		}
		if (n < string.length()) {
			return string.substring(0, n);
		} else if (n == string.length()) {
			return string;
		}
		char paddingChar;
		if (padding == null || padding.length() == 0) {
			paddingChar = ' ';
		} else {
			paddingChar = padding.charAt(0);
		}
		StringBuilder buff = new StringBuilder(n);
		n -= string.length();
		if (right) {
			buff.append(string);
		}
		for (int i = 0; i < n; i++) {
			buff.append(paddingChar);
		}
		if (!right) {
			buff.append(string);
		}
		return buff.toString();
	}

	/**
	 * Convert a string to a SQL literal. Null is converted to NULL. The text is
	 * enclosed in single quotes. If there are any special characters, the
	 * method STRINGDECODE is used.
	 * 
	 * @param s
	 *            the text to convert.
	 * @return the SQL literal
	 */
	public static String quoteStringSQL(String s) {
		if (s == null) {
			return "NULL";
		}
		int length = s.length();
		StringBuilder buff = new StringBuilder(length + 2);
		buff.append('\'');
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (c == '\'') {
				buff.append(c);
			} else if (c < ' ' || c > 127) {
				// need to start from the beginning because maybe there was a \
				// that was not quoted
				return "STRINGDECODE(" + quoteStringSQL(javaEncode(s)) + ")";
			}
			buff.append(c);
		}
		buff.append('\'');
		return buff.toString();
	}

	/**
	 * Split a string into an array of strings using the given separator. A null
	 * string will result in a null array, and an empty string in a zero element
	 * array.
	 * 
	 * @param s
	 *            the string to split
	 * @param separatorChar
	 *            the separator character
	 * @param trim
	 *            whether each element should be trimmed
	 * @return the array list
	 */
	public static String[] arraySplit(String s, char separatorChar, boolean trim) {
		if (s == null) {
			return null;
		}
		int length = s.length();
		if (length == 0) {
			return new String[0];
		}
		ArrayList<String> list = Utils.newArrayList();
		StringBuilder buff = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (c == separatorChar) {
				String e = buff.toString();
				list.add(trim ? e.trim() : e);
				buff.setLength(0);
			} else if (c == '\\' && i < length - 1) {
				buff.append(s.charAt(++i));
			} else {
				buff.append(c);
			}
		}
		String e = buff.toString();
		list.add(trim ? e.trim() : e);
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * Calculates the SHA1 of the string.
	 * 
	 * @param text
	 * @return sha1 of the string
	 */
	public static String calculateSHA1(String text) {
		try {
			byte[] bytes = text.getBytes("iso-8859-1");
			return calculateSHA1(bytes);
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
	}

	/**
	 * Calculates the SHA1 of the byte array.
	 * 
	 * @param bytes
	 * @return sha1 of the byte array
	 */
	public static String calculateSHA1(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(bytes, 0, bytes.length);
			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (int i = 0; i < digest.length; i++) {
				if (((int) digest[i] & 0xff) < 0x10) {
					sb.append('0');
				}
				sb.append(Integer.toHexString((int) digest[i] & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Counts the occurrences of char c in the given string.
	 * 
	 * @param c
	 *            the character to count
	 * @param value
	 *            the source string
	 * @return the count of c in value
	 */
	public static int count(char c, String value) {
		int count = 0;
		for (char cv : value.toCharArray()) {
			if (cv == c) {
				count++;
			}
		}
		return count;
	}
}

/*
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

package com.iciql.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.tautua.markdownpapers.Markdown;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.iciql.Constants;
import com.iciql.util.StringUtils;

/**
 * Builds the web site or deployment documentation from Markdown source files.
 * 
 * All Markdown source files must have the .mkd extension.
 * 
 * Natural string sort order of the Markdown source filenames is the order of
 * page links. "##_" prefixes are used to control the sort order.
 * 
 * @author James Moger
 * 
 */
public class BuildSite {

	public static void main(String... args) {
		Params params = new Params();
		JCommander jc = new JCommander(params);
		try {
			jc.parse(args);
		} catch (ParameterException t) {
			usage(jc, t);
		}

		File sourceFolder = new File(params.sourceFolder);
		File destinationFolder = new File(params.outputFolder);
		File[] markdownFiles = sourceFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".mkd");
			}
		});
		Arrays.sort(markdownFiles);

		Map<String, String> aliasMap = new HashMap<String, String>();
		for (String alias : params.aliases) {
			String[] values = alias.split("=");
			aliasMap.put(values[0], values[1]);
		}

		System.out.println(MessageFormat.format("Generating site from {0} Markdown Docs in {1} ",
				markdownFiles.length, sourceFolder.getAbsolutePath()));
		String linkPattern = "<a href=''{0}''>{1}</a>";
		StringBuilder sb = new StringBuilder();
		for (File file : markdownFiles) {
			String documentName = getDocumentName(file);
			if (!params.skips.contains(documentName)) {
				String displayName = documentName;
				if (aliasMap.containsKey(documentName)) {
					displayName = aliasMap.get(documentName);
				} else {
					displayName = displayName.replace('_', ' ');
				}
				String fileName = documentName + ".html";
				sb.append(MessageFormat.format(linkPattern, fileName, displayName));
				sb.append(" | ");
			}
		}
		sb.setLength(sb.length() - 3);
		sb.trimToSize();

		String htmlHeader = readContent(new File(params.pageHeader), "\n");

		String htmlAdSnippet = null;
		if (!StringUtils.isNullOrEmpty(params.adSnippet)) {
			File snippet = new File(params.adSnippet);
			if (snippet.exists()) {
				htmlAdSnippet = readContent(snippet, "\n");
			}
		}
		String htmlFooter = readContent(new File(params.pageFooter), "\n");
		String links = sb.toString();
		String header = MessageFormat.format(htmlHeader, Constants.NAME, links);
		if (!StringUtils.isNullOrEmpty(params.analyticsSnippet)) {
			File snippet = new File(params.analyticsSnippet);
			if (snippet.exists()) {
				String htmlSnippet = readContent(snippet, "\n");
				header = header.replace("<!-- ANALYTICS -->", htmlSnippet);
			}
		}
		final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		final String footer = MessageFormat.format(htmlFooter, "generated " + date);
		for (File file : markdownFiles) {
			try {
				String documentName = getDocumentName(file);
				if (!params.skips.contains(documentName)) {
					String fileName = documentName + ".html";
					System.out.println(MessageFormat.format("  {0} => {1}", file.getName(), fileName));
					String rawContent = readContent(file, "\n");
					String markdownContent = rawContent;

					Map<String, List<String>> nomarkdownMap = new HashMap<String, List<String>>();

					// extract sections marked as no-markdown
					int nmd = 0;
					for (String token : params.nomarkdown) {
						StringBuilder strippedContent = new StringBuilder();

						String nomarkdownKey = "%NOMARKDOWN" + nmd + "%";
						String[] kv = token.split(":", 2);
						String beginToken = kv[0];
						String endToken = kv[1];

						// strip nomarkdown chunks from markdown and cache them
						List<String> chunks = new Vector<String>();
						int beginCode = 0;
						int endCode = 0;
						while ((beginCode = markdownContent.indexOf(beginToken, endCode)) > -1) {
							if (endCode == 0) {
								strippedContent.append(markdownContent.substring(0, beginCode));
							} else {
								strippedContent.append(markdownContent.substring(endCode, beginCode));
							}
							strippedContent.append(nomarkdownKey);
							endCode = markdownContent.indexOf(endToken, beginCode);
							chunks.add(markdownContent.substring(beginCode, endCode));
							nomarkdownMap.put(nomarkdownKey, chunks);
						}

						// get remainder of text
						if (endCode < markdownContent.length()) {
							strippedContent.append(markdownContent.substring(endCode,
									markdownContent.length()));
						}
						markdownContent = strippedContent.toString();
						nmd++;
					}

					// transform markdown to html
					String content = transformMarkdown(new StringReader(markdownContent.toString()));

					// reinsert nomarkdown chunks
					for (Map.Entry<String, List<String>> nomarkdown : nomarkdownMap.entrySet()) {
						for (String chunk : nomarkdown.getValue()) {
							content = content.replaceFirst(nomarkdown.getKey(), chunk);
						}
					}

					// perform specified substitutions
					for (String token : params.substitutions) {
						String[] kv = token.split("=", 2);
						content = content.replace(kv[0], kv[1]);
					}
					for (String token : params.regex) {
						String[] kv = token.split("!!!", 2);
						content = content.replaceAll(kv[0], kv[1]);
					}
					for (String alias : params.loads) {
						String[] kv = alias.split("=", 2);
						String loadedContent = StringUtils.readContent(new File(kv[1]), "\n");
						loadedContent = StringUtils.escapeForHtml(loadedContent, false);
						loadedContent = StringUtils.breakLinesForHtml(loadedContent);
						content = content.replace(kv[0], loadedContent);
					}

					OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(
							destinationFolder, fileName)), Charset.forName("UTF-8"));
					writer.write(header);
					if (!StringUtils.isNullOrEmpty(htmlAdSnippet)) {
						writer.write(htmlAdSnippet);
					}
					writer.write(content);
					writer.write(footer);
					writer.close();
				}
			} catch (Throwable t) {
				System.err.println("Failed to transform " + file.getName());
				t.printStackTrace();
			}
		}
	}

	private static String getDocumentName(File file) {
		String displayName = file.getName().substring(0, file.getName().lastIndexOf('.')).toLowerCase();
		int underscore = displayName.indexOf('_') + 1;
		if (underscore > -1) {
			// trim leading ##_ which is to control display order
			return displayName.substring(underscore);
		}
		return displayName;
	}

	/**
	 * Returns the string content of the specified file.
	 * 
	 * @param file
	 * @param lineEnding
	 * @return the string content of the file
	 */
	private static String readContent(File file, String lineEnding) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStreamReader is = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(is);
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				if (lineEnding != null) {
					sb.append(lineEnding);
				}
			}
			reader.close();
		} catch (Throwable t) {
			System.err.println("Failed to read content of " + file.getAbsolutePath());
			t.printStackTrace();
		}
		return sb.toString();
	}

	private static String transformMarkdown(Reader markdownReader) throws ParseException {
		// Read raw markdown content and transform it to html
		StringWriter writer = new StringWriter();
		try {
			Markdown md = new Markdown();
			md.transform(markdownReader, writer);
			return writer.toString().trim();
		} catch (org.tautua.markdownpapers.parser.ParseException p) {
			throw new java.text.ParseException(p.getMessage(), 0);
		} finally {
			try {
				markdownReader.close();
			} catch (IOException e) {
				// IGNORE
			}
			try {
				writer.close();
			} catch (IOException e) {
				// IGNORE
			}
		}
	}

	private static void usage(JCommander jc, ParameterException t) {
		System.out.println(Constants.NAME + " v" + Constants.VERSION);
		System.out.println();
		if (t != null) {
			System.out.println(t.getMessage());
			System.out.println();
		}
		if (jc != null) {
			jc.usage();
		}
		System.exit(0);
	}

	/**
	 * Command-line parameters for BuildSite utility.
	 */
	@Parameters(separators = " ")
	private static class Params {

		@Parameter(names = { "--sourceFolder" }, description = "Markdown Source Folder", required = true)
		public String sourceFolder;

		@Parameter(names = { "--outputFolder" }, description = "HTML Ouptut Folder", required = true)
		public String outputFolder;

		@Parameter(names = { "--pageHeader" }, description = "Page Header HTML Snippet", required = true)
		public String pageHeader;

		@Parameter(names = { "--pageFooter" }, description = "Page Footer HTML Snippet", required = true)
		public String pageFooter;

		@Parameter(names = { "--adSnippet" }, description = "Ad HTML Snippet", required = false)
		public String adSnippet;

		@Parameter(names = { "--analyticsSnippet" }, description = "Analytics HTML Snippet", required = false)
		public String analyticsSnippet;

		@Parameter(names = { "--skip" }, description = "Filename to skip", required = false)
		public List<String> skips = new ArrayList<String>();

		@Parameter(names = { "--alias" }, description = "Filename=Linkname aliases", required = false)
		public List<String> aliases = new ArrayList<String>();

		@Parameter(names = { "--substitute" }, description = "%TOKEN%=value", required = false)
		public List<String> substitutions = new ArrayList<String>();

		@Parameter(names = { "--load" }, description = "%TOKEN%=filename", required = false)
		public List<String> loads = new ArrayList<String>();

		@Parameter(names = { "--nomarkdown" }, description = "%STARTTOKEN%:%ENDTOKEN%", required = false)
		public List<String> nomarkdown = new ArrayList<String>();

		@Parameter(names = { "--regex" }, description = "searchPattern!!!replacePattern", required = false)
		public List<String> regex = new ArrayList<String>();

	}
}

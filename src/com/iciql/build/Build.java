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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.iciql.util.StringUtils;

/**
 * The Build class downloads runtime and compile-time jar files from the Apache
 * Maven repositories.
 * 
 * Its important that this class have minimal compile dependencies since its
 * called very early in the build script.
 * 
 */
public class Build {

	/**
	 * BuildTypes
	 */
	public static enum BuildType {
		RUNTIME, COMPILETIME;
	}

	public static void main(String... args) {
		runtime();
		compiletime();
	}

	public static void runtime() {
	}

	public static void compiletime() {
		downloadFromApache(MavenObject.H2, BuildType.RUNTIME);
		downloadFromApache(MavenObject.H2, BuildType.COMPILETIME);
		downloadFromApache(MavenObject.HSQLDB, BuildType.RUNTIME);
		downloadFromApache(MavenObject.DERBY, BuildType.RUNTIME);
		downloadFromApache(MavenObject.MYSQL, BuildType.RUNTIME);
		downloadFromApache(MavenObject.POSTGRESQL, BuildType.RUNTIME);
		downloadFromApache(MavenObject.JCOMMANDER, BuildType.RUNTIME);
		downloadFromApache(MavenObject.JCOMMANDER, BuildType.COMPILETIME);
		downloadFromApache(MavenObject.MARKDOWNPAPERS, BuildType.RUNTIME);
		downloadFromApache(MavenObject.MARKDOWNPAPERS, BuildType.COMPILETIME);
		downloadFromApache(MavenObject.JUNIT, BuildType.RUNTIME);
		downloadFromApache(MavenObject.DOCLAVA, BuildType.RUNTIME);
		downloadFromApache(MavenObject.DOCLAVA, BuildType.COMPILETIME);
		downloadFromApache(MavenObject.SLF4JAPI, BuildType.RUNTIME);
		downloadFromApache(MavenObject.SLF4JAPI, BuildType.COMPILETIME);

		// needed for site publishing
		downloadFromApache(MavenObject.COMMONSNET, BuildType.RUNTIME);
	}

	/**
	 * Download a file from the official Apache Maven repository.
	 * 
	 * @param mo
	 *            the maven object to download.
	 * @return
	 */
	private static List<File> downloadFromApache(MavenObject mo, BuildType type) {
		return downloadFromMaven("http://repo1.maven.org/maven2/", mo, type);
	}

	/**
	 * Download a file from a Maven repository.
	 * 
	 * @param mo
	 *            the maven object to download.
	 * @return
	 */
	private static List<File> downloadFromMaven(String mavenRoot, MavenObject mo, BuildType type) {
		List<File> downloads = new ArrayList<File>();
		String[] jars = { "" };
		if (BuildType.RUNTIME.equals(type)) {
			jars = new String[] { "" };
		} else if (BuildType.COMPILETIME.equals(type)) {
			jars = new String[] { "-sources", "-javadoc" };
		}
		for (String jar : jars) {
			File targetFile = mo.getLocalFile("ext", jar);
			if (targetFile.exists()) {
				downloads.add(targetFile);
				continue;
			}
			String expectedSHA1 = mo.getSHA1(jar);
			if (expectedSHA1 == null) {
				// skip this jar
				continue;
			}
			String mavenURL = mavenRoot + mo.getRepositoryPath(jar);
			if (!targetFile.getAbsoluteFile().getParentFile().exists()) {
				boolean success = targetFile.getAbsoluteFile().getParentFile().mkdirs();
				if (!success) {
					throw new RuntimeException("Failed to create destination folder structure!");
				}
			}
			ByteArrayOutputStream buff = new ByteArrayOutputStream();
			try {
				URL url = new URL(mavenURL);
				InputStream in = new BufferedInputStream(url.openStream());
				byte[] buffer = new byte[4096];

				System.out.println("d/l: " + targetFile.getName());
				while (true) {
					int len = in.read(buffer);
					if (len < 0) {
						break;
					}
					buff.write(buffer, 0, len);
				}
				in.close();

			} catch (IOException e) {
				throw new RuntimeException("Error downloading " + mavenURL + " to " + targetFile, e);
			}
			byte[] data = buff.toByteArray();
			String calculatedSHA1 = StringUtils.calculateSHA1(data);

			System.out.println();

			if (expectedSHA1.length() == 0) {
				System.out.println("sha: " + calculatedSHA1);
				System.out.println();
			} else {
				if (!calculatedSHA1.equals(expectedSHA1)) {
					throw new RuntimeException("SHA1 checksum mismatch; got: " + calculatedSHA1);
				}
			}
			try {
				RandomAccessFile ra = new RandomAccessFile(targetFile, "rw");
				ra.write(data);
				ra.setLength(data.length);
				ra.close();
			} catch (IOException e) {
				throw new RuntimeException("Error writing to file " + targetFile, e);
			}
			downloads.add(targetFile);
		}
		return downloads;
	}

	/**
	 * Class that describes a retrievable Maven object.
	 */
	private static class MavenObject {

		public static final MavenObject JCOMMANDER = new MavenObject("com/beust", "jcommander", "1.17",
				"219a3540f3b27d7cc3b1d91d6ea046cd8723290e", "0bb50eec177acf0e94d58e0cf07262fe5164331d",
				"c7adc475ca40c288c93054e0f4fe58f3a98c0cb5");

		public static final MavenObject H2 = new MavenObject("com/h2database", "h2", "1.3.158",
				"4bac13427caeb32ef6e93b70101e61f370c7b5e2", "6bb165156a0831879fa7797df6e18bdcd4421f2d",
				"446d3f58c44992534cb54f67134532d95961904a");

		public static final MavenObject HSQLDB = new MavenObject("org/hsqldb", "hsqldb", "2.2.4",
				"6a6e040b07f5ee409fc825f1c5e5b574b1fa1428", "", "");

		public static final MavenObject DERBY = new MavenObject("org/apache/derby", "derby", "10.8.1.2",
				"2f8717d96eafe3eef3de445ba653f142d54ddab1", "", "");

		public static final MavenObject MYSQL = new MavenObject("mysql", "mysql-connector-java", "5.1.15",
				"0fbc80454d27cc65f3addfa516707e9f8e60c3eb", "", "");

		public static final MavenObject POSTGRESQL = new MavenObject("postgresql", "postgresql", "9.0-801.jdbc4",
				"153f2f92a786f12fc111d0111f709012df87c808", "", "");

		public static final MavenObject JUNIT = new MavenObject("junit", "junit", "4.8.2",
				"c94f54227b08100974c36170dcb53329435fe5ad", "", "");

		public static final MavenObject MARKDOWNPAPERS = new MavenObject("org/tautua/markdownpapers",
				"markdownpapers-core", "1.1.0", "b879b4720fa642d3c490ab559af132daaa16dbb4",
				"d98c53939815be2777d5a56dcdc3bbc9ddb468fa", "4c09d2d3073e85b973572292af00bd69681df76b");

		public static final MavenObject COMMONSNET = new MavenObject("commons-net", "commons-net", "1.4.0",
				"eb47e8cad2dd7f92fd7e77df1d1529cae87361f7", "", "");

		public static final MavenObject DOCLAVA = new MavenObject("com/google/doclava", "doclava", "1.0.3",
				"5a1e05977fd36480b0cf314410440f88e3a0049e", "6e314df1733455d66b98b56014363172773d0905",
				"1c1aa631b235439356e6e5803319caca80aaaa88");

		public static final MavenObject SLF4JAPI = new MavenObject("org/slf4j", "slf4j-api", "1.6.1",
				"6f3b8a24bf970f17289b234284c94f43eb42f0e4", "46a386136c901748e6a3af67ebde6c22bc6b4524",
				"e223571d77769cdafde59040da235842f3326453");

		public final String group;
		public final String artifact;
		public final String version;
		public final String librarySHA1;
		public final String sourcesSHA1;
		public final String javadocSHA1;

		private MavenObject(String group, String artifact, String version, String librarySHA1,
				String sourcesSHA1, String javadocSHA1) {
			this.group = group;
			this.artifact = artifact;
			this.version = version;
			this.librarySHA1 = librarySHA1;
			this.sourcesSHA1 = sourcesSHA1;
			this.javadocSHA1 = javadocSHA1;
		}

		private String getRepositoryPath(String jar) {
			return group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + jar + ".jar";
		}

		private File getLocalFile(String basePath, String jar) {
			return new File(basePath, artifact + "-" + version + jar + ".jar");
		}

		private String getSHA1(String jar) {
			if (jar.equals("")) {
				return librarySHA1;
			} else if (jar.equals("-sources")) {
				return sourcesSHA1;
			} else if (jar.equals("-javadoc")) {
				return javadocSHA1;
			}
			return librarySHA1;
		}

		@Override
		public String toString() {
			return group;
		}
	}
}

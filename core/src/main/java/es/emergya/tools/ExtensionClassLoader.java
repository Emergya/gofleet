/* ====================================================================
 * The QueryForm License, Version 1.1
 *
 * Copyright (c) 1998 - 2003 David F. Glasser.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by 
 *        David F. Glasser."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "QueryForm" and "David F. Glasser" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact dglasser@pobox.com.
 *
 * 5. Products derived from this software may not be called "QueryForm",
 *    nor may "QueryForm" appear in their name, without prior written
 *    permission of David F. Glasser.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DAVID F. GLASSER, THE APACHE SOFTWARE 
 * FOUNDATION OR ITS CONTRIBUTORS, OR ANY AUTHORS OR DISTRIBUTORS
 * OF THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 * ==================================================================== 
 *
 * $Source: /cvsroot/qform/qform/src/org/glasser/util/ExtensionClassLoader.java,v $
 * $Revision: 1.2 $
 * $Author: dglasser $
 * $Date: 2003/02/02 14:35:11 $
 * 
 * --------------------------------------------------------------------
 */
package es.emergya.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.utils.JarSearcher;

/**
 * This is a URLClassLoader with some minor functionality added. It has a
 * Singleton instance which is globally accessible, and it also has a method
 * (addArchivesInDirectory(File directory) that adds all of the jar and zip
 * files in a given directory to the classpath for an instance of this
 * classloader.
 */
public class ExtensionClassLoader extends ClassLoader {

	private final static Log LOG = LogFactory
			.getLog(ExtensionClassLoader.class);

	public File getDependentPath(String name) {
		File jar = JarSearcher.getJar(ExtensionClassLoader.class);
		if (jar != null) {
			if (jar.getPath().startsWith("file:")) {
				ExtensionClassLoader.LOG.debug("Eliminando 'file:' de "
						+ jar.getAbsolutePath());
				jar = new File(jar.getPath().substring(5));
			} else
				ExtensionClassLoader.LOG
						.debug("La ruta no comienza por 'file:' "
								+ jar.getPath());
			File dir = jar.getParentFile();
			if (dir == null)
				return null;
			String path = dir.getPath();
			ExtensionClassLoader.LOG.debug("Directorio con el jar: "
					+ dir.getPath());

			if (!name.startsWith("/"))
				path = path + "/";
			path += name;

			String osDependentPath = path.replaceAll("/",
					java.util.regex.Matcher.quoteReplacement(File.separator));
			ExtensionClassLoader.LOG.debug("Se busca el fichero "
					+ osDependentPath);
			if (System.getProperty("os.name").indexOf("Windows") >= 0)
				osDependentPath = osDependentPath.replaceAll("%20", " ");
			return new File(osDependentPath);
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream resourceIS;
		File jar = JarSearcher.getJar(ExtensionClassLoader.class);
		if (jar != null) {
			if (jar.getPath().startsWith("file:")) {
				ExtensionClassLoader.LOG.debug("Eliminando 'file:' de "
						+ jar.getAbsolutePath());
				jar = new File(jar.getPath().substring(5));
			} else
				ExtensionClassLoader.LOG
						.debug("La ruta no comienza por 'file:' "
								+ jar.getPath());
			File dir = jar.getParentFile();
			if (dir == null) {
				ExtensionClassLoader.LOG
						.debug("Entrando por super.getResouceAsStream(" + name
								+ ")");
				return super.getResourceAsStream(name);
			}
			String path = dir.getPath();
			ExtensionClassLoader.LOG.debug("Directorio con el jar: "
					+ dir.getPath());

			if (!name.startsWith("/"))
				path = path + "/";
			path += name;

			String osDependentPath = path.replaceAll("/",
					java.util.regex.Matcher.quoteReplacement(File.separator));
			ExtensionClassLoader.LOG.debug("Se busca el fichero "
					+ osDependentPath);
			if (System.getProperty("os.name").indexOf("Windows") >= 0)
				osDependentPath = osDependentPath.replaceAll("%20", " ");
			File resource = new File(osDependentPath);

			if (resource.exists() && resource.canRead())
				try {
					resourceIS = new FileInputStream(osDependentPath);
				} catch (FileNotFoundException e) {
					ExtensionClassLoader.LOG.error("Couln't open requested ("
							+ name + "resource");
					resourceIS = null;
				}
			else
				resourceIS = super.getResourceAsStream(name);
		} else
			resourceIS = super.getResourceAsStream(name);

		return resourceIS;
	}

	public List<File> getModules() {

		ArrayList<File> res = new ArrayList<File>();

		String module_suffix = ".module";
		String path_lib = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "lib";
		String path_target_lib = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "target"
				+ System.getProperty("file.separator") + "lib";

		findModulesInPath(res, module_suffix, path_lib);
		findModulesInPath(res, module_suffix, path_target_lib);

		return res;
	}

	public List<File> findFiles(String name) {

		ArrayList<File> res = new ArrayList<File>();

		String path_lib = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "lib";
		String path_target_lib = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "target"
				+ System.getProperty("file.separator") + "lib";

		findModulesInPath(res, name, path_lib);
		findModulesInPath(res, name, path_target_lib);

		ArrayList<File> res_final = new ArrayList<File>();
		
		for(File f : res)
			if(f.getName().equals(name))
				res_final.add(f);
		
		return res_final;
	}
	
	private void findModulesInPath(ArrayList<File> res, String module_suffix,
			final java.lang.String path) {
		try {
			LOG.trace("findModulesInPath(" + path + ")");
			final java.io.File object = new java.io.File(path);

			if (object.isDirectory()) {
				LOG.trace("Directory found: " + object.getAbsolutePath());
				for (java.lang.String entry : object.list()) {
					final java.io.File thing = new java.io.File(
							object.getCanonicalPath()
									+ System.getProperty("file.separator")
									+ entry);
					if (thing.isFile()
							&& thing.getName().endsWith(module_suffix))
						res.add(object);
					else
						findModulesInPath(res, module_suffix,
								thing.getCanonicalPath());
				}
			} else if (object.isFile()
					&& object.getName().endsWith(module_suffix)) {
				LOG.info("Module found: " + object.getAbsolutePath());
				res.add(object);
			} else if (object.isFile() && object.getName().endsWith(".jar")) {
				LOG.debug("Jar found: " + object.getAbsolutePath());
				FileInputStream fis = new FileInputStream(object);
				ZipInputStream zis = new ZipInputStream(
						new BufferedInputStream(fis));
				ZipEntry zipEntry;
				while ((zipEntry = zis.getNextEntry()) != null) {
					if (zipEntry.getName().endsWith(module_suffix)) {

						JarFile jarFile = new JarFile(object);

						JarEntry entry = jarFile
								.getJarEntry(zipEntry.getName());

						InputStream inputStream = jarFile.getInputStream(entry);

						File f = File.createTempFile(zipEntry.getName(),
								".module");
						f.deleteOnExit();
						OutputStream out = new FileOutputStream(f);
						byte buf[] = new byte[1024];
						int len;
						while ((len = inputStream.read(buf)) > 0)
							out.write(buf, 0, len);
						out.close();
						inputStream.close();

						LOG.info("Module found: " + zipEntry.getName());
						res.add(f);
					}
				}
				zis.close();
				fis.close();
			}
		} catch (Throwable t) {
			LOG.error(t, t);
		}
	}

	public String[] getFiles(String cadena) {
		ArrayList<String> res = new ArrayList<String>();
		String path_lib = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "lib";
		String path_target_lib = System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "target"
				+ System.getProperty("file.separator") + "lib";

		findFileInPath(res, cadena, path_lib);
		findFileInPath(res, cadena, path_target_lib);

		return res.toArray(new String[0]);
	}

	private void findFileInPath(ArrayList<String> res, String cadena,
			final java.lang.String path) {
		try {
			LOG.trace("findFileInPath(" + path + ")");
			final java.io.File object = new java.io.File(path);

			if (object.isDirectory()) {
				LOG.trace("Directory found: " + object.getAbsolutePath());
				for (java.lang.String entry : object.list()) {
					final java.io.File thing = new java.io.File(
							object.getCanonicalPath()
									+ System.getProperty("file.separator")
									+ entry);
					if (thing.isFile() && thing.getName().contains(cadena))
						res.add(object.getCanonicalPath());
					else
						findFileInPath(res, cadena, thing.getCanonicalPath());
				}
			} else if (object.isFile() && object.getName().contains(cadena)) {
				LOG.debug("File found: " + object.getAbsolutePath());
				res.add(object.getCanonicalPath());
			} else if (object.isFile() && object.getName().endsWith(".jar")) {
				LOG.debug("Jar found: " + object.getAbsolutePath());
				FileInputStream fis = new FileInputStream(object);
				ZipInputStream zis = new ZipInputStream(
						new BufferedInputStream(fis));
				ZipEntry zipEntry;
				while ((zipEntry = zis.getNextEntry()) != null) {
					if (zipEntry.getName().contains(cadena)) {

						JarFile jarFile = new JarFile(object);

						JarEntry entry = jarFile
								.getJarEntry(zipEntry.getName());

						InputStream inputStream = jarFile.getInputStream(entry);

						System.out.println(object.getAbsolutePath()
								+ "/" + zipEntry.getName());
						
						File f = File.createTempFile(zipEntry.getName(), "");
						f.deleteOnExit();
						OutputStream out = new FileOutputStream(f);
						byte buf[] = new byte[1024];
						int len;
						while ((len = inputStream.read(buf)) > 0)
							out.write(buf, 0, len);
						out.close();
						inputStream.close();

						LOG.debug("File found: " + zipEntry.getName());
						res.add(f.getCanonicalPath());
					}
				}
				zis.close();
				fis.close();
			}
		} catch (Throwable t) {
			LOG.error(t, t);
		}
	}
}

/*
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jlrodriguez@emergya.es">Juan Luís Rodríguez</a>
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
 *
 * This file is part of GoFleet
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.gofleet.context;

import java.io.File;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns the path in which the .jar-Archive which contains this class is
 * located
 * 
 * @author nigjo
 */
public class JarSearcher {
	private final static Log LOG = LogFactory.getLog(JarSearcher.class);
	private Class<? extends Object> searchClass;

	public JarSearcher() {
	}

	public static File getJar(Object obj) {
		JarSearcher searcher = new JarSearcher();

		searcher.setClass(obj.getClass());

		return searcher.findJar();
	}

	public static File getJar(Class<?> clazz) {
		JarSearcher searcher = new JarSearcher();
		searcher.setClass(clazz);
		return searcher.findJar();
	}

	/**
	 * 
	 * Returns the File Object of the .jar file
	 * 
	 * @return <code>null</code> if this class isn't stored in a .jar file
	 */
	private File findJar() {
		String klasse = this.getClassURL().getFile();

		if (klasse.indexOf(".jar!") >= 0) {
			String jar = klasse.substring(0, klasse.indexOf(".jar!") + 4);

			return new File(jar);
		}

		// no jar found
		return null;
	}

	private URL getClassURL() {
		String name = this.searchClass.getName();

		name = name.replaceAll("\\.", "/");

		return this.searchClass.getClassLoader().getResource(name + ".class");
	}

	private void setClass(Class<? extends Object> toSearch) {
		this.searchClass = toSearch;
	}

	/**
	 * Returns the path to the .jar-Datei containing the class of the object
	 * given
	 * 
	 * @param obj
	 * @return <code>null</code> if the class of the object is not in a .jar
	 *         file
	 */
	public static File getJarDirectory(Object obj) {
		// Search for .jar file
		File jar = JarSearcher.getJar(obj);
		if (jar != null)
			// .jar found
			return jar.getParentFile();
		// ... not found
		return null;
	}

	public static File getJarDirectory(Class<?> clazz) {
		File jar = JarSearcher.getJar(clazz);
		if (jar != null) {
			// .jar found
			if (JarSearcher.LOG.isTraceEnabled())
				JarSearcher.LOG.trace("getJarDirectory: "
						+ jar.getParentFile().getAbsolutePath());
			return jar.getParentFile();
		}
		// ... not found
		return null;
	}
}

/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 * 
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
package es.emergya.consultas;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.bbdd.dao.GenericQueriesHome;
import es.emergya.utils.MyBeanFactory;

public class GenericQueries {

	private GenericQueries() {
		super();
	}

	static {
		genericQueriesHome = (GenericQueriesHome) MyBeanFactory
				.getBean("genericQueriesHome");
	}

	static final Log log = LogFactory.getLog(GenericQueries.class);
	private static GenericQueriesHome genericQueriesHome;

	/**
	 * 
	 * @param hqlQuery query to run
	 * @param max Maximum length of the result set
	 * @param fetchSize null if you don't want to scroll results
	 * @return
	 */
	public static LinkedList<LinkedList<Object>> runHQL(String hqlQuery, Integer max,
			Integer fetchSize) {
		return genericQueriesHome.runHQL(hqlQuery, max, fetchSize);
	}

	/**
	 * 
	 * @param sqlQuery query to run
	 * @param max Maximum length of the result set
	 * @param fetchSize null if you don't want to scroll results
	 * @return
	 */
	public static LinkedList<LinkedList<Object>> runSQL(String sqlQuery, Integer max,
			Integer fetchSize) {
		return genericQueriesHome.runSQL(sqlQuery, max, fetchSize);
	}
}

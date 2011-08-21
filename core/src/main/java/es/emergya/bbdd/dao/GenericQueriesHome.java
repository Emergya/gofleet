/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marias@emergya.com">María Arias de Reyna</a>
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
package es.emergya.bbdd.dao;

import java.util.LinkedList;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("genericQueriesHome")
public class GenericQueriesHome extends GenericDaoHibernate<Object, Long> {

	public GenericQueriesHome() {
		super(Object.class);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Throwable.class)
	public LinkedList<LinkedList<Object>> runHQL(String hqlQuery, Integer max,
			Integer fetchSize) {
		Query q = getSession().createQuery(hqlQuery).setMaxResults(max);

		return executeQuery(max, fetchSize, q);

	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Throwable.class)
	public LinkedList<LinkedList<Object>> runSQL(String sqlQuery, Integer max,
			Integer fetchSize) {
		Query q = getSession().createSQLQuery(sqlQuery).setMaxResults(max);

		return executeQuery(max, fetchSize, q);
	}

	/**
	 * 
	 * @param max if null, 1
	 * @param fetchSize if null, max
	 * @param q, query to run
	 * @return
	 */
	@Transactional(propagation = Propagation.MANDATORY, readOnly = true, rollbackFor = Throwable.class)
	private LinkedList<LinkedList<Object>> executeQuery(Integer max,
			Integer fetchSize, Query q) {
		log.debug("executeQuery(" + max + ", " + fetchSize + ", "
				+ q.getQueryString() + ")");
		LinkedList<LinkedList<Object>> res = new LinkedList<LinkedList<Object>>();
		try {
			if(max == null)
				max = 1;
			
			if (fetchSize == null)
				fetchSize = max;

			q.setFetchSize(fetchSize);

			ScrollableResults scroll = q.scroll(ScrollMode.FORWARD_ONLY);
			while (scroll.next()) {
				LinkedList<Object> tmp = new LinkedList<Object>();
				for (Object o : scroll.get())
					tmp.add(o);
				res.add(tmp);
				// Clearing session to clean memory
				// prevents memory leak
				getSession().clear();
			}
			scroll.close();
		} catch (Throwable t) {
			super.log.error("Couldn't execute the query", t);
		}
		return res;
	}
}

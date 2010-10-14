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
package es.emergya.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import es.emergya.bbdd.bean.Flota;
import es.emergya.bbdd.dao.FlotaHome;
import es.emergya.utils.MyBeanFactory;

public class FlotaAdmin {

	static final Log log = LogFactory.getLog(FlotaAdmin.class);
	private static FlotaHome flotaHome;

	static {
		flotaHome = (FlotaHome) MyBeanFactory.getBean("flotaHome");
	}

	private FlotaAdmin() {
		super();
	}

	@Transactional
	public static boolean delete(Flota f) {
		return flotaHome.delete(f);
	}

	@Transactional
	public static boolean saveOrUpdate(Flota f) {
		return flotaHome.saveOrUpdate(f);
	}

	@Transactional
	public static List<Flota> getByExample(Flota p) {
		List<Flota> res = new ArrayList<Flota>(0);
		try {
			res = flotaHome.getByFilter(p);
		} catch (Throwable t1) {
			log.error(t1, t1);
		}

		return res;
	}

	@Transactional
	public static Integer getTotal() {
		return flotaHome.getTotal();
	}
}

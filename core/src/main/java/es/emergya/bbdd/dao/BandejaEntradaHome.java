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
package es.emergya.bbdd.dao;

import java.util.List;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;

import es.emergya.actions.RecursoAdmin;
import es.emergya.bbdd.bean.HistoricoGPS;
import es.emergya.bbdd.bean.Inbox;
import es.emergya.bbdd.bean.notmapped.RecursoBean;
import es.emergya.comunications.exceptions.MessageProcessingException;
import es.emergya.consultas.RecursoConsultas;

@Repository("bandejaEntradaHome")
public class BandejaEntradaHome extends GenericDaoHibernate<Inbox, Long> {

	public BandejaEntradaHome() {
		super(Inbox.class);
	}

	@Override
	public Inbox get(Long id) {
		try {
			return super.get(id);
		} catch (Throwable t) {
			log.error("Estamos buscando un objeto que no existe", t);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public List<Inbox> getNotProcessed() {
		Session currentSession = getSession();
		currentSession.clear();
		return (List<Inbox>) getSession().createCriteria(Inbox.class)
				.add(Restrictions.eq("procesado", false))
				.addOrder(Order.asc("marcaTemporal"))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, rollbackFor = Throwable.class)
	public boolean saveOrUpdate(Inbox b) {
		Session currentSession = getSession();
		currentSession.flush();
		currentSession.saveOrUpdate(currentSession.merge(b));
		return true;

	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Throwable.class)
	public void processPosicionActual(Inbox entrada, Geometry geom)
			throws MessageProcessingException {
		HistoricoGPS historicoGPS = new HistoricoGPS();
		historicoGPS.setMarcaTemporal(entrada.getMarcaTemporal());

		RecursoBean recurso = RecursoConsultas.findByDispositivoSQL(entrada
				.getOrigen());
		if (recurso == null) {
			throw new MessageProcessingException("No encuentro el recurso "
					+ entrada.getOrigen());
		}

		if (recurso.getHabilitado()) {
			historicoGPS.setTipoRecurso(recurso.getTipoRecurso());
			if (recurso.getSubflota() == null) {
				throw new MessageProcessingException("El recurso " + recurso
						+ " no tiene asignada ninguna flota.");
			}
			historicoGPS.setSubflota(recurso.getSubflota());
			historicoGPS.setRecurso(recurso.getIdentificador());

			geom.setSRID(4326);
			//
			// final String sourceSRID = "EPSG:4326";
			// final String targetSRID = "EPSG:3395";
			//
			// Geometry geom = transform(geom, sourceSRID, targetSRID);

			historicoGPS.setGeom(geom);
			historicoGPS.setPosX(geom.getCentroid().getX());
			historicoGPS.setPosY(geom.getCentroid().getY());

			// HistoricoGPSAdmin.saveServer(historicoGPS);
			RecursoAdmin.updateLastGpsSQL(historicoGPS, recurso.getId());
			// recurso.setHistoricoGps(historicoGPS);
			// RecursoAdmin.saveOrUpdate(recurso);
		}
	}
}

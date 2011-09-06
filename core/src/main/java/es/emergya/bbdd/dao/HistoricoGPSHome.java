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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Point;

import es.emergya.bbdd.bean.Flota;
import es.emergya.bbdd.bean.HistoricoGPS;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.bbdd.bean.notmapped.Posicion;
import es.emergya.utils.LogicConstants;

@Repository("historicoGPSHome")
public class HistoricoGPSHome extends GenericDaoHibernate<HistoricoGPS, Long> {

	
	//TODO no longer supports GPX
	private static final String DIRECTORIO_GPX_DEFAULT = "/var/gpx/";
	private static final String DIRECTORIO_GPX = "DIRECTORIO_GPX";
	private static final Log log = LogFactory.getLog(HistoricoGPSHome.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private static synchronized Date parse(String time) {
		try {
			return sdf.parse(time);
		} catch (Throwable t) {
			log.error("Error parseando fecha de fichero '" + time + "'");
			return null;
		}
	}

	public HistoricoGPSHome() {
		super(HistoricoGPS.class);
	}

	@Override
	public HistoricoGPS get(Long id) {
		try {
			return super.get(id);
		} catch (Throwable t) {
			log.error("Estamos buscando un objeto que no existe", t);
			return null;
		}
	}

	public Date lastGPSDateForRecurso(Recurso r) {
		final HistoricoGPS lastGPSForRecurso = lastGPSForRecurso(r);
		if (lastGPSForRecurso != null)
			return lastGPSForRecurso.getMarcaTemporal();
		else
			return null;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public HistoricoGPS lastGPSForRecurso(Recurso r) {
		if (r == null)
			return null;

		try {

			Session currentSession = getSession();
			currentSession.flush();
			currentSession.refresh(r);
			if (r.getHistoricoGps() != null) {
				r.getHistoricoGps().getPosX();
				return r.getHistoricoGps();
			}
		} catch (Throwable t) {
		}

		return lastGPSForRecurso(r.getIdentificador());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public HistoricoGPS lastGPSForRecurso(String r) {
		HistoricoGPS res = null;
		if (r == null)
			return res;

		Session currentSession = getSession();
		currentSession.flush();
		res = ((Recurso) getSession().createCriteria(Recurso.class)
				.add(Restrictions.eq("identificador", r)).setMaxResults(1)
				.uniqueResult()).getHistoricoGps();

		return res;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public Calendar firstGPSForRecurso(String r) {
		HistoricoGPS res = null;
		if (r == null)
			return null;

		Session currentSession = getSession();
		currentSession.flush();
		res = (HistoricoGPS) getSession().createCriteria(HistoricoGPS.class)
				.add(Restrictions.eq("recurso", r))
				.addOrder(Order.asc("marcaTemporal")).setMaxResults(1)
				.uniqueResult();

		if (res == null || res.getMarcaTemporal() == null)
			return null;

		Calendar resultado = Calendar.getInstance();
		resultado.setTime(res.getMarcaTemporal());

		return resultado;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, rollbackFor = Throwable.class)
	public void delete(String identificador, Calendar ini, Calendar fin) {
		Session currentSession = getSession();
		currentSession
				.createSQLQuery(
						"DELETE FROM historico_gps "
								+ "WHERE recurso like :ID AND marca_temporal > :INI AND marca_temporal < :FIN")
				.setDate("FIN", fin.getTime()).setDate("INI", ini.getTime())
				.setString("ID", identificador).executeUpdate();
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
	public HistoricoGPS saveOrUpdate(HistoricoGPS hgps) {

		Session currentSession = getSession();
		currentSession.clear();

		HistoricoGPS entity = null;

		if (hgps.getId() == null
				|| (hgps.getId() != null && this.get(hgps.getId()) == null))
			entity = hgps;
		else
			entity = (HistoricoGPS) currentSession.merge(hgps);

		currentSession.saveOrUpdate(entity);
		return entity;
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
	public HistoricoGPS saveServer(HistoricoGPS hgps) {
		Session currentSession = getSession();
		currentSession.save(hgps);
		currentSession.flush();
		return hgps;
	}

	// TODO

	/**
	 * TIME#FLOTA#IDENTIFICADOR
	 * 
	 * @param fileName
	 * @return
	 */
	private static String getFlotaFromFileName(String fileName) {
		try {
			return fileName.substring(fileName.indexOf("#") + 1,
					fileName.lastIndexOf("#"));
		} catch (Throwable t) {
			log.error("Error al extraer el nombre de la flota del fichero '"
					+ fileName + "'");
			return null;
		}
	}

	private static String getRecursoFromFileName(String fileName) {
		try {
			return fileName.substring(fileName.lastIndexOf("#") + 1,
					fileName.lastIndexOf(".gpx"));
		} catch (Throwable t) {
			log.error("Error al extraer el nombre del recurso del fichero '"
					+ fileName + "'");
			return null;
		}
	}

	private static Date getTimeFromFileName(String fileName) {
		try {
			return parse(fileName.substring(0, fileName.indexOf("#")));
		} catch (Throwable t) {
			log.error("Error al extraer la fecha del fichero '" + fileName
					+ "'");
			return null;
		}
	}

	/**
	 * Dado un conjuto de flotas y una fecha de inicio y fin, devuelve los
	 * nombres de los recursos que pertenecían a alguna de estas flotas y que
	 * enviaron alguna posición en el periodo. Si alguna fecha es null no se
	 * tienen en cuenta (no se añade a la condición)
	 * 
	 * @param flotas
	 *            conjunto de flotas en las que estamos interesados.
	 * @param initDate
	 *            fecha/hora inicial del periodo de búsqueda.
	 * @param endDate
	 *            fecha/hora final del periodo de búsqueda.
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public List<String> findRecursosInIntervalForFoltas(Set<Flota> flotas,
			Calendar initDate, Calendar endDate, Usuario user) {
		log.info("findRecursosInINtervalForFlotas");
		List<String> result = new LinkedList<String>();

		try {
			Date inicio = null;
			Date fin = null;

			if (initDate != null) {
				inicio = initDate.getTime();
			}
			if (endDate != null) {
				fin = endDate.getTime();
			}

			Calendar c = Calendar.getInstance();
			Date inicio_day = null;
			if (inicio != null) {
				c.setTime(inicio);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);

				c.add(Calendar.SECOND, -1);
				inicio_day = c.getTime();
			}

			Date fin_day = null;
			if (fin != null) {
				c.setTime(fin);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				c.add(Calendar.DAY_OF_YEAR, 1);
				fin_day = c.getTime();
			}

			// Creamos un array con los nombres de las subflotas para usarlos en
			// una
			// condición IN
			if (flotas == null || (flotas != null && flotas.size() == 0)) {
				return result;
			}
			Date fin_provisional = null;
			String[] nombreFlotas = new String[flotas.size()];
			int i = 0;
			for (Flota f : flotas) {
				nombreFlotas[i++] = f.getNombre();
			}

			// Si no hemos recibido fechas de inicio o fin significa que estamos
			// consultando últimas posiciones. Utilizamos la tabla recursos para
			// obtener las últimas posiciones.
			if (inicio == null || fin == null) {
				log.trace("No hay fecha de inicio o fin. Buscamos solo las ultimas posiciones.");
				result.addAll(calculateRecursosUltimasPosiciones(user));
			} else {
				StringBuilder stringFlotas = new StringBuilder("");
				if (nombreFlotas.length > 0) {
					stringFlotas.append(" subflota in ('" + nombreFlotas[0]);
					for (i = 1; i < nombreFlotas.length; i++) {
						stringFlotas.append("', '").append(nombreFlotas[i]);
					}
					stringFlotas.append("') ");
				}
				while (inicio != null && fin != null && inicio.before(fin)) {
					initDate.add(Calendar.DAY_OF_YEAR, 1);
					fin_provisional = initDate.getTime();
					if (fin.before(fin_provisional))
						fin_provisional = fin;
					result.addAll(calculateRecursos(inicio, fin_provisional,
							stringFlotas.toString(), result));
					log.trace("De momento llevamos " + result);
					inicio = fin_provisional;
				}
			}

			if (fin != null && inicio != null)
				try {
					File directorio = new File(LogicConstants.get(
							DIRECTORIO_GPX, DIRECTORIO_GPX_DEFAULT));

					if (!directorio.isDirectory()) {
						throw new IOException(
								"La ruta pasada para los gpx no es un directorio");
					}

					List<String> fltas = new LinkedList<String>();
					for (String s : nombreFlotas) {
						fltas.add(s);
					}

					log.trace("Flotas: " + flotas);

					for (File file : directorio.listFiles()) {
						log.trace(file.getAbsolutePath());
						try {
							if (file.isDirectory()) {
								throw new IOException(file
										+ " no es un fichero");
							}

							if (!file.canRead()) {
								throw new IOException(file + " no es legible");
							}

							String nombreFichero = file.getName();

							final String recurso = getRecursoFromFileName(nombreFichero);
							final String flota = getFlotaFromFileName(nombreFichero);

							log.debug("Encontrado " + recurso + " de " + flota);

							if (!result.contains(recurso)) {
								log.trace("Miramos el recurso " + recurso
										+ " de " + flota + " en " + flotas);
								if (fltas.contains(flota)) {
									Date time = getTimeFromFileName(nombreFichero);
									// Si las fechas coinciden

									if (time != null) {
										log.trace("Hora del recurso: " + time);
										if (inicio_day == null
												|| inicio_day.before(time)) {
											if (fin_day == null
													|| fin_day.after(time)) {
												result.add(recurso);
												log.trace("Metemos a "
														+ recurso);
											}
										}
									}
								}
							}
						} catch (Throwable t) {
							log.error("No pude leer a " + file, t);
						}
					}
				} catch (Throwable t) {
					log.error("No pudimos buscar en los gpx", t);
				}

			Collections.sort(result);
		} catch (Throwable t) {
			log.error("Error al buscar los recursos", t);
		}

		return result;

	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	private List<String> calculateRecursos(Date inicio, Date fin,
			String flotas, List<String> recursosYaEncontrados) {
		int i;
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct(rec.recurso) as nombreRecurso from ");

		sb.append(" (select st_collect(geom) as geom, recurso from historico_gps where ");

		sb.append(flotas);

		if (recursosYaEncontrados.size() > 0) {
			if (flotas.length() > 0) {
				sb.append(" and ");
			}
			sb.append("recurso not in ('");
			sb.append(recursosYaEncontrados.get(0));
			for (i = 1; i < recursosYaEncontrados.size(); i++) {
				sb.append("', '").append(recursosYaEncontrados.get(i));
			}
			sb.append("') ");
		}

		if (inicio != null) {
			sb.append(" and marca_temporal >= :FECHA_INICIO ");

		}

		if (fin != null) {
			sb.append(" and marca_temporal <= :FECHA_FIN ");
		}

		sb.append(" group by recurso) as rec");

		SQLQuery q = getSession().createSQLQuery(sb.toString());
		if (inicio != null) {
			q.setParameter("FECHA_INICIO", inicio, Hibernate.TIMESTAMP);

		}

		if (fin != null) {
			q.setParameter("FECHA_FIN", fin, Hibernate.TIMESTAMP);
		}

		q.addScalar("nombreRecurso", Hibernate.STRING);

		log.debug(sb.toString() + " => " + inicio + " " + fin);

		List<String> result = q.list();

		return result;
	}

	/**
	 * Dado una lista de zonas y un usuario, devuelve la lista de recursos que
	 * el que usuario puede ver por su rol y cuya última posición en la base de
	 * datos está en alguna de las zonas pasadas.
	 * 
	 * @param zonas
	 * @param u
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	private List<String> calculateRecursosUltimasPosiciones(Usuario u) {
		int i;

		StringBuffer sb = new StringBuffer();
		sb.append("select distinct r.nombre as nombreRecurso ")
				.append("from recursos r inner join flotas f on r.flota_x_flota=f.x_flota ")
				.append("inner join roles_x_flotas rxf on rxf.x_flota = f.x_flota ")
				.append("inner join roles rol on rxf.x_rol=rol.x_rol ")
				.append("inner join usuarios u on u.fk_roles = rol.x_rol ")
				.append("inner join historico_gps h on r.fk_historico_gps = h.x_historico ");

		sb.append("and u.nombre_usuario=:USUARIO ");

		sb.append("order by nombreRecurso");

		SQLQuery q = getSession().createSQLQuery(sb.toString());

		q.addScalar("nombreRecurso", Hibernate.STRING);
		q.setString("USUARIO", u.getNombreUsuario());
		if (log.isDebugEnabled()) {
			log.debug(sb.toString());
		}

		List<String> result = q.list();
		return result;
	}

	/**
	 * Devuelve las entradas de historico_gps que fueron producidas en el
	 * itervalo indicado por el recurso pasado com parámetro ordenadas por marca
	 * temporal asc.
	 * 
	 * @param recurso
	 *            nombre del recurso.
	 * @param inicio
	 *            fecha / hora de inicio.
	 * @param fin
	 *            fecha / hora de fin.
	 * @return la lista de HistoricoGPS del recurso en el intervalo ordenada por
	 *         marcaTemporal ascendente.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public List<HistoricoGPS> getPosicionesEnIntervalo(String recurso,
			Date inicio, Date fin) {
		List<HistoricoGPS> result = getPosicionesEnIntervaloSoloBBDD(recurso,
				inicio, fin);
//		try {
//			getPosicionesEnIntervaloFromGPX(inicio, fin, recurso, result);
//			ordenar(result);
//		} catch (Throwable t) {
//			log.error("Error al extraer posiciones en intervalo", t);
//		}
		return result;
	}

	private void ordenar(List<HistoricoGPS> result) {
		java.util.Collections.sort(result, new Comparator<HistoricoGPS>() {

			@Override
			public int compare(HistoricoGPS arg0, HistoricoGPS arg1) {
				if (arg0 == null || arg1 == null)
					return 0;
				if (arg0.getMarcaTemporal() == null
						|| arg1.getMarcaTemporal() == null)
					return 0;
				return arg0.getMarcaTemporal().compareTo(
						arg1.getMarcaTemporal());
			}
		});
	}

	/**
	 * Obtiene las posiciones históricas del recurso indicado durante el periodo
	 * pasado como parámetro. Sólo consulta la base de datos, no los GPX.
	 * 
	 * @param recurso
	 * @param inicio
	 * @param fin
	 * @return la lista de posiciones históricas del recurso ordenadas por marca
	 *         temporal ascentente
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public List<HistoricoGPS> getPosicionesEnIntervaloSoloBBDD(String recurso,
			Date inicio, Date fin) {
		List<HistoricoGPS> result = new LinkedList<HistoricoGPS>();
		try {
			Criteria crit = getSession().createCriteria(HistoricoGPS.class);

			if (inicio != null) {
				crit.add(Restrictions.ge("marcaTemporal", inicio));
			}
			if (fin != null) {
				crit.add(Restrictions.lt("marcaTemporal", fin));
			}

			crit.add(Restrictions.eq("recurso", recurso)).addOrder(
					Order.asc("marcaTemporal"));

			result = crit.list();
		} catch (Throwable t) {
			log.error(
					"Error al extraer posiciones solo BBDD en intervalo. Recurso="
							+ recurso + ", Inicio= " + inicio + ", Fin= " + fin,
					t);

		}

		return result;
	}

	// /**
	// * Busca posiciones en el intervalo para el recurso únicamente usando los
	// * datos de los GPX.
	// *
	// * @param inicio
	// * @param fin
	// * @param recurso
	// * @param result
	// */
	// private void getPosicionesEnIntervaloFromGPX(Date inicio, Date fin,
	// String recurso, List<HistoricoGPS> result) {
	// try {
	// File directorio = new File(LogicConstants.get(DIRECTORIO_GPX,
	// DIRECTORIO_GPX_DEFAULT));
	// if (!directorio.isDirectory()) {
	// throw new IOException(
	// "La ruta pasada para los gpx no es un directorio");
	// }
	// Calendar c = Calendar.getInstance();
	// Date inicio_day = null;
	// if (inicio != null) {
	// c.setTime(inicio);
	// c.set(Calendar.HOUR_OF_DAY, 0);
	// c.set(Calendar.MINUTE, 0);
	// c.set(Calendar.SECOND, 0);
	// c.set(Calendar.MILLISECOND, 0);
	// c.add(Calendar.SECOND, -1);
	// inicio_day = c.getTime();
	// }
	// Date fin_day = null;
	// if (fin != null) {
	// c.setTime(fin);
	// c.set(Calendar.HOUR_OF_DAY, 0);
	// c.set(Calendar.MINUTE, 0);
	// c.set(Calendar.SECOND, 0);
	// c.set(Calendar.MILLISECOND, 0);
	// c.add(Calendar.DAY_OF_YEAR, 1);
	// fin_day = c.getTime();
	// }
	// for (File file : directorio.listFiles()) {
	// try {
	// if (file.isDirectory()) {
	// throw new IOException(file + " no es un fichero");
	// }
	// if (!file.canRead()) {
	// throw new IOException(file + " no es legible");
	// }
	// String nombreFichero = file.getName();
	// // Si este fichero es de este recurso
	// if (recurso.equals(getRecursoFromFileName(nombreFichero))) {
	// Date time = getTimeFromFileName(nombreFichero);
	// // Y las fechas coinciden
	// if (time != null) {
	// if (inicio_day == null || inicio_day.before(time)) {
	// if (fin_day == null || fin_day.after(time)) {
	// log.trace("Procesamos "
	// + file.getAbsolutePath());
	// for (HistoricoGPS hgps : extractPositions(file)) {
	// // Aunque sea el fichero correcto,
	// // volvemos a comprobar las marcas
	// // temporales
	// final Date marcaTemporal = hgps
	// .getMarcaTemporal();
	// log.info(inicio + " - " + marcaTemporal
	// + " - " + fin);
	// if (fin == null
	// || (marcaTemporal.before(fin))
	// && (inicio == null || marcaTemporal
	// .after(inicio))) {
	// result.add(hgps);
	// }
	// }
	// }
	// }
	// }
	// }
	// } catch (Throwable t) {
	// log.error("No pude leer a " + file, t);
	// }
	// }
	// } catch (Throwable t) {
	// log.error("No pudimos buscar en los gpx", t);
	// }
	// }

//	private List<HistoricoGPS> extractPositions(File file) {
//		log.trace("extractPositions(" + file.getAbsolutePath() + ")");
//		List<HistoricoGPS> resultado = new LinkedList<HistoricoGPS>();
//
//		String fileName = file.getName();
//		String recurso = getRecursoFromFileName(fileName);
//		String flota = getFlotaFromFileName(fileName);
//
//		FileInputStream is;
//
//		try {
//			is = new FileInputStream(file);
//			GpxReader reader = new GpxReader(is);
//			reader.parse(true);
//
//			GpxData data = reader.data;
//
//			for (GpxRoute r : data.routes) {
//				for (WayPoint w : r.routePoints) {
//					resultado.add(convert(w, recurso, flota));
//
//				}
//			}
//
//			for (WayPoint w : data.waypoints) {
//				resultado.add(convert(w, recurso, flota));
//
//			}
//
//			for (ImmutableGpxTrack tr : data.tracks) {
//				for (GpxTrackSegment seg : tr.getSegments()) {
//					for (WayPoint w : seg.getWayPoints()) {
//						resultado.add(convert(w, recurso, flota));
//
//					}
//				}
//			}
//
//		} catch (Throwable e) {
//			log.error("No pude extraer el historico", e);
//
//		}
//
//		return resultado;
//
//	}
//
//	private HistoricoGPS convert(WayPoint w, String recurso, String subflota) {
//		log.trace("convert(" + w + ", " + recurso + ", " + subflota + ")");
//		HistoricoGPS his = new HistoricoGPS();
//		w.setTime();
//		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//		Calendar c2 = Calendar.getInstance();
//		c2.setTime(new Date((long) (w.time * 1000)));
//		for (Integer i : new Integer[] { Calendar.YEAR, Calendar.MONTH,
//				Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
//				Calendar.SECOND, Calendar.MILLISECOND })
//			c.set(i, c2.get(i));
//		his.setMarcaTemporal(c.getTime());
//		log.trace("Historico GPS a las " + his.getMarcaTemporal()
//				+ " y deberia ser a las " + new Date((long) (w.time * 1000)));
//		his.setRecurso(recurso);
//		his.setSubflota(subflota);
//		LatLon latlon = w.latlon;
//		GeometryFactory factory = new GeometryFactory();
//		Geometry geom = factory.createPoint(new Coordinate(latlon.getX(),
//				latlon.getY()));
//		his.setGeom(geom);
//		his.setPosX(geom.getCentroid().getX());
//		his.setPosY(geom.getCentroid().getY());
//
//		return his;
//
//	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public Posicion[] getUltimasPosiciones(String[] idRecursos) {
		if (idRecursos == null || idRecursos.length == 0) {
			return new Posicion[] {};
		}

		List<Posicion> resultado = new LinkedList<Posicion>();

		for (String idRecurso : idRecursos) {
			Criteria crit = getSession().createCriteria(HistoricoGPS.class)
					.add(Restrictions.eq("recurso", idRecurso))
					.addOrder(Order.desc("marcaTemporal")).setMaxResults(1);

			HistoricoGPS hist = (HistoricoGPS) crit.uniqueResult();
			Posicion p = new Posicion();
			if (hist != null) {
				p.setX(hist.getPosX());
				p.setY(hist.getPosY());
				p.setIdentificador(idRecurso);
				Calendar marcaTemporal = Calendar.getInstance();
				marcaTemporal
						.setTimeInMillis(hist.getMarcaTemporal().getTime());

				p.setMarcaTemporal(marcaTemporal);
				resultado.add(p);
			}

		}

		return resultado.toArray(new Posicion[0]);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class)
	public Posicion[] getPosicionesIncidencias(String[] idIncidencias) {
		if (idIncidencias == null) {
			return new Posicion[] {};

		}

		int i = 0;

		ArrayList<Long> ids = new ArrayList<Long>();

		for (String s : idIncidencias) {
			try {
				ids.add(new Long(s));

			} catch (Throwable t) {
				log.error(t, t);

			}
		}

		Posicion[] resultado = new Posicion[idIncidencias.length];

		for (Long idIncidencia : ids) {
			Criteria crit = getSession().createCriteria(Incidencia.class)
					.add(Restrictions.eq("id", idIncidencia)).setMaxResults(1);
			Incidencia incidencia = (Incidencia) crit.uniqueResult();

			if (incidencia != null) {
				Posicion p = new Posicion();

				Point geom = incidencia.getGeometria().getCentroid();

				if (geom != null) {
					p.setX(geom.getCoordinate().x);
					p.setY(geom.getCoordinate().y);
					p.setIdentificador(incidencia.getTitulo());
					Calendar marcaTemporal = Calendar.getInstance();
					marcaTemporal.setTimeInMillis(incidencia.getFechaCreacion()
							.getTime());

					p.setMarcaTemporal(marcaTemporal);
					log.debug("Posicion de incidencia: " + p);
					resultado[i++] = p;
				} else {
					log.error("Incidencia sin posicion (" + idIncidencia + ")");
				}
			} else {
				log.error("Se pidio la posicion de una incidencia desconocida: "
						+ idIncidencia);
			}
		}

		return resultado;

	}

}

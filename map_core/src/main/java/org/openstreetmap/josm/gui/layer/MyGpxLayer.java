package org.openstreetmap.josm.gui.layer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;

import es.emergya.cliente.constants.LogicConstants;

public class MyGpxLayer extends GpxLayer {

	private static final Log log = LogFactory.getLog(MyGpxLayer.class);
	private Color bgcolor = Color.WHITE;
	private Color color = Color.BLACK;
	private Color foreground = null;
	private int ih = 5;
	private Date unixBegins = new Date(0);
	private DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	private MapView mapView = null;
	private LatLon centroid = null;

	public MyGpxLayer(GpxData d, String name, boolean isLocal, MapView mapView) {
		super(d, name, isLocal);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 1971);
		unixBegins = c.getTime();
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.mapView = mapView;
		// calculateCentroid();
		calculateFirstPoint();
	}

	public MyGpxLayer(GpxData d, String name, MapView mapView) {
		this(d, name, true, mapView);
	}

	@Override
	public String toString() {
		return name;
	}

	private Color getForeground() {
		if (foreground == null)
			foreground = Color.decode(LogicConstants.getNextColor());
		return foreground;
	}

	@Override
	public void paint(Graphics g, MapView mv) {
		super.paint(g, mv);
		for (WayPoint trkPnt : this.data.waypoints) {
			if (Double.isNaN(trkPnt.latlon.lat())
					|| Double.isNaN(trkPnt.latlon.lon())) {
				continue;
			}
			Point screen = mv.getPoint(trkPnt.eastNorth);
			if (trkPnt.customColoring != null) {
				g.setColor(trkPnt.customColoring);
			} else {
				g.setColor(getForeground());
			}
			g.fillRect(screen.x - 2, screen.y - 2, 5, 5);
			pintarEtiqueta(g, trkPnt.getString("name"), screen);
		}
		Date last = null;
		for (GpxTrack trk : data.tracks) {
			for (Collection<WayPoint> segment : trk.trackSegs) {
				for (WayPoint trkPnt : segment) {
					if (Double.isNaN(trkPnt.latlon.lat())
							|| Double.isNaN(trkPnt.latlon.lon())) {
						continue;
					}
					if (trkPnt.customColoring != null) {
						g.setColor(trkPnt.customColoring);
					} else {
						g.setColor(getForeground());
					}
					Point screen = mv.getPoint(trkPnt.eastNorth);
					g.fillRect(screen.x - 2, screen.y - 2, 5, 5);

					try {
						Date date = new Date(
								(new Double(trkPnt.time)).longValue());

						if (!date.after(unixBegins)) {
							date = dateFormat.parse(trkPnt.getString("time"));
						}

						if (!date.after(unixBegins)) {
							date = null;
						}

						if (last == null || date == null || date.after(last)) {
							log.trace("Pintamos etiqueta " + date
									+ " va despues de " + last);
							last = updateLast(date);
							String etiqueta = trkPnt.getString("name");
							if (etiqueta == null) {
								etiqueta = trkPnt.getString("time");
							}
							pintarEtiqueta(g, etiqueta, screen);
						}
					} catch (Throwable t) {
						log.error("Unparseable date en gpx: " + t.toString());
						pintarEtiqueta(g, trkPnt.getString("time"), screen);
					}
				} // end for trkpnt
			} // end for segment
		} // end for trk
	}

	private void pintarEtiqueta(Graphics g, String etiqueta_name, Point screen) {
		if (g == null
				|| etiqueta_name == null
				|| screen == null
				|| this.mapView.zoom() < LogicConstants.getInt(
						"LABEL_ZOOM_THRESHOLD", -1)) {
			return;
		}
		g.setFont(g.getFont().deriveFont(Font.BOLD, g.getFont().getSize()));
		FontMetrics fm = g.getFontMetrics(g.getFont());
		int w = fm.stringWidth(etiqueta_name) + 4;
		int h = fm.getHeight() + 4;
		g.setColor(bgcolor);
		g.fillRect(screen.x - w / 2, screen.y - ih / 2 - h - 2, w, h);
		g.setColor(color);
		g.drawRect(screen.x - w / 2, screen.y - ih / 2 - h - 2, w, h);
		g.drawString(etiqueta_name, screen.x - w / 2 + 2, screen.y - (ih + h)
				/ 2 + 4);
	}

	private Date updateLast(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND,
				LogicConstants.getInt("SECONDS_HISTORY_LABEL", 300));
		return c.getTime();
	}

	public LatLon getLatLon() {
		if (centroid != null) {
			// calculateCentroid();
			calculateFirstPoint();
		}

		log.debug(centroid);

		return centroid;
	}

	public void calculateCentroid() {

		Rectangle2D.Double todo = null;

		for (WayPoint w : this.data.waypoints) {
			final Point2D.Double latlon = new Point2D.Double(w.latlon.lat(),
					w.latlon.lon());
			log.debug(latlon);
			if (todo == null) {
				todo = buildInitialRectangle(latlon);
			} else {
				todo.add(latlon);
			}

		}
		for (GpxTrack track : this.data.tracks) {
			for (Collection<WayPoint> ws : track.trackSegs) {
				for (WayPoint w : ws) {
					final Point2D.Double latlon = new Point2D.Double(
							w.latlon.lat(), w.latlon.lon());
					log.debug(latlon);
					if (todo == null) {
						todo = buildInitialRectangle(latlon);
					} else {
						todo.add(latlon);
					}

				}
			}
		}

		if (todo != null) {
			centroid = new LatLon(todo.x + todo.width / 2, todo.y + todo.height
					/ 2);
		}
	}

	public void calculateFirstPoint() {

		Rectangle2D.Double todo = null;

		for (WayPoint w : this.data.waypoints) {
			final Point2D.Double latlon = new Point2D.Double(w.latlon.lat(),
					w.latlon.lon());
			if (log.isDebugEnabled()) {
				log.debug(latlon);
			}
			if (todo == null) {
				todo = buildInitialRectangle(latlon);
			}
			break;
		}
		for_track: for (GpxTrack track : this.data.tracks) {
			for (Collection<WayPoint> ws : track.trackSegs) {
				for (WayPoint w : ws) {
					final Point2D.Double latlon = new Point2D.Double(
							w.latlon.lat(), w.latlon.lon());
					log.debug(latlon);
					if (todo == null) {
						todo = buildInitialRectangle(latlon);
					}
					break for_track;
				}
			}
		}

		if (todo != null) {
			centroid = new LatLon(todo.x, todo.y);
		}
	}

	private Rectangle2D.Double buildInitialRectangle(Point2D.Double origen) {
		Rectangle2D.Double todo = null;
		if (origen != null) {
			double x_1 = origen.getX();
			double y_1 = origen.getY();

			todo = new Rectangle2D.Double(x_1, y_1, 0.00001, 0.00001);
			log.debug("Initial Rectangle: " + todo);
		}
		return todo;
	}
}

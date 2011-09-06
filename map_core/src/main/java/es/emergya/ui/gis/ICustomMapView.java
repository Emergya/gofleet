package es.emergya.ui.gis;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;

public interface ICustomMapView extends IMapView {

	public static final String GPS_PATTERN = "\\d+\\.\\d+,\\D+";
	public static final Pattern GPS_PATTERN_REGEXP = Pattern
			.compile(GPS_PATTERN);

	public abstract MainMenu getMenu();

	public abstract JPanel getContentPane();

	public abstract JPanel getPanel();

	public abstract void setPanel(JPanel panel);

	public abstract void addLayer(Layer layer, boolean showOnButtonList);

	/**
	 * Añade una capa
	 * 
	 * @param layer
	 * @param showOnButtonList
	 *            Si debe aparecer en los controles para mostrar/ocultar
	 */
	public abstract void addLayer(Layer layer, boolean showOnButtonList, int pos);

	public abstract void addLayer(Layer layer, boolean showOnButtonList,
			String icon);

	public abstract void addLayer(Layer layer);

	public abstract void setActiveLayer(Layer layer);

	public abstract boolean zoomToEditLayerBoundingBox();

	public abstract void zoomTo(EastNorth newCenter, double scale);

	/**
	 * @param autoZoom
	 *            If the mapview is following a marker, adjust the view zoom to
	 *            match the speed of the marker
	 */
	public abstract void setAutoZoom(boolean autoZoom);

	/**
	 * @param autoTurn
	 *            If the mapview is following a marker, adjust the view angle to
	 *            match the movement direction of the marker
	 */
	public abstract void setAutoTurn(boolean autoTurn);

	/**
	 * @param smoothTurn
	 *            If the mapview is following a marker, perform some simple
	 *            interpolation between the current heading and the direction
	 *            the target is facing
	 */
	public abstract void setSmoothTurn(boolean smoothTurn);

	/**
	 * @param contextMenu
	 *            the contextMenu to set
	 */
	public abstract void setContextMenu(JPopupMenu contextMenu);

	public abstract void showMenu(Component parent, int x, int y);

	public abstract void zoomPerformed();

	/**
	 * 
	 * @param newCenter
	 *            {@link EastNorth} of the new center
	 * @param zoomFactor
	 *            The OSM zoom facto to zoom to
	 */
	public abstract void zoomToFactor(EastNorth newCenter, int zoomFactor);

	/**
	 * 
	 * @param newCenter
	 *            {@link EastNorth} of the new center
	 * @param zoomFactor
	 *            The OSM zoom facto to zoom to
	 * @param angle
	 *            Facing angle 0 is north
	 */
	public abstract void zoomToFactor(EastNorth newCenter, int zoomFactor,
			double angle);

	/**
	 * 
	 * @param newCenter
	 *            {@link EastNorth} of the new center
	 * @param scale
	 * @param angle
	 *            Facing angle 0 is north
	 */
	public abstract void zoomTo(EastNorth newCenter, double scale, double angle);

	public abstract void paint(Graphics g);

	public abstract void updateMousePosition();

	public abstract int zoom();

	/**
	 * Inverse of scale2Zoom
	 * 
	 * @param zoom
	 * @return
	 */
	public abstract double zoom2Scale(int zoom);

	public abstract int getMinZoom();

	public abstract int getMaxZoom();

	/**
	 * Sets the defailt {@link InitAdapter}
	 */
	public abstract void setDefaultInitAdapter();

	/**
	 * 
	 * @param r
	 *            Amount of radians added to the current angle
	 */
	public abstract void rotate(double r);

	public abstract double getAngle();

	/**
	 * Sets this mapview to follow the given marker.
	 * 
	 * @param m
	 *            The marker to follow. Set to null to enable free panning
	 */
	public abstract void setFollow(Marker m);

	public abstract Point getPoint(EastNorth p);

	public abstract EastNorth getEastNorth(int x, int y);

	public abstract LatLon getLatLon(int x, int y);

	/**
	 * @return Pixel Bounding box, in pixels
	 */
	public abstract Rectangle getBoundingBox();

	public abstract void movementEnded();

	public abstract void movementStarted();

	public abstract void clearCallbacks();

	/**
	 * Updates all the markers in marker layers
	 */
	public abstract void updateMarkers();

}
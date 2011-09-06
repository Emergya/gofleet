package es.emergya.ui.gis;

import java.awt.Graphics;
import java.util.Collection;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import es.emergya.ui.base.plugins.CleanUp;

public interface IMapView {

	public abstract void addLayer(Layer layer);

	/**
	 * Add a layer to the current MapView. The layer will be added at topmost
	 * position.
	 */
	public abstract void addLayer(Layer layer, int pos);

	public abstract Boolean isDrawableLayer();

	public abstract Boolean isVisibleDrawableLayer();

	/**
	 * Remove the layer from the mapview. If the layer was in the list before,
	 * an LayerChange event is fired.
	 */
	public abstract void removeLayer(Layer layer);

	public abstract void enableVirtualNodes(Boolean state);

	public abstract Boolean useVirtualNodes();

	/**
	 * Moves the layer to the given new position. No event is fired.
	 * 
	 * @param layer
	 *            The layer to move
	 * @param pos
	 *            The new position of the layer
	 */
	public abstract void moveLayer(Layer layer, int pos);

	public abstract int getLayerPos(Layer layer);

	/**
	 * Draw the component.
	 */
	public abstract void paint(Graphics g);

	/**
	 * Set the new dimension to the projection class. Also adjust the components
	 * scale, if in autoScale mode.
	 */
	public abstract void recalculateCenterScale(BoundingXYVisitor box);

	/**
	 * @return An unmodificable list of all layers
	 */
	public abstract Collection<Layer> getAllLayers();

	/**
	 * Set the active selection to the given value and raise an layerchange
	 * event.
	 */
	public abstract void setActiveLayer(Layer layer);

	/**
	 * @return The current active layer
	 */
	public abstract Layer getActiveLayer();

	/**
	 * In addition to the base class funcitonality, this keep trak of the
	 * autoscale feature.
	 */
	public abstract void zoomTo(EastNorth newCenter, double scale);

	/**
	 * Tries to zoom to the download boundingbox[es] of the current edit layer
	 * (aka {@link OsmDataLayer}). If the edit layer has multiple download
	 * bounding boxes it zooms to a large virtual bounding box containing all
	 * smaller ones. This implementation can be used for resolving ticket #1461.
	 * 
	 * @return <code>true</code> if a zoom operation has been performed
	 */
	public abstract boolean zoomToEditLayerBoundingBox();

	public abstract boolean addTemporaryLayer(MapViewPaintable mvp);

	public abstract boolean removeTemporaryLayer(MapViewPaintable mvp);

}
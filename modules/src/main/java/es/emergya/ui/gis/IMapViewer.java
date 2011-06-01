package es.emergya.ui.gis;

import org.openstreetmap.josm.gui.NavigatableComponent;

import es.emergya.ui.base.plugins.PluginEvent;

public interface IMapViewer {

	public abstract NavigatableComponent getMapView();

	public abstract void setMapView(ICustomMapView mapView);

	public abstract void refresh(PluginEvent event);

	public abstract void setup();

	public abstract void updateGv();

}
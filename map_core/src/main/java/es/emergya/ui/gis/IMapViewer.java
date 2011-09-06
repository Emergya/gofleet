package es.emergya.ui.gis;

import org.openstreetmap.josm.gui.NavigatableComponent;

import es.emergya.ui.base.plugins.CleanUp;
import es.emergya.ui.base.plugins.PluginEvent;

public interface IMapViewer extends CleanUp {

	public abstract NavigatableComponent getMapView();

	public abstract void setMapView(ICustomMapView mapView);

	public abstract void refresh(PluginEvent event);

	public abstract void setup();

}
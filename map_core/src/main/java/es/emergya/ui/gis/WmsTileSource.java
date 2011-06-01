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
package es.emergya.ui.gis;


import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource.TileUpdate;
import org.openstreetmap.josm.Main;

import es.emergya.ui.gis.CustomTileSource.AbstractOsmTileSource;

public class WmsTileSource extends AbstractOsmTileSource {
	Log log = LogFactory.getLog(WmsTileSource.class);
	private static final String NAME = "WmsTileSource";
	private final String baseUrl;

	public WmsTileSource(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		BoundingBox bb = new BoundingBox(tilex, tiley, zoom);
		
		if(baseUrl.contains("{0}")) {
			return urlFromPattern(bb);			
		} else {
			return url(bb);
		}
	}
	
	private String urlFromPattern(BoundingBox bb) {
		String proj = Main.proj.toCode();
        if(Main.proj instanceof org.openstreetmap.josm.data.projection.Mercator) // don't use mercator code directly
            proj = "EPSG:4326";

        String url = MessageFormat.format(baseUrl, proj, bb.toString(), w(bb), h(bb));
        log.trace("TileUrl: " + url);
		return url;
	}
	
	private String url(BoundingBox bb) {		
		StringBuilder b = new StringBuilder(baseUrl);
		b.append("&");
		b.append("BBOX=").append(bb.toString());
		b.append("&");
		b.append("WIDTH=").append(w(bb));
		b.append("&");
		b.append("HEIGHT=").append(h(bb));
		
		log.trace("TileUrl: " + b);
		return b.toString();		
	}

	@Override
	public String getName() {
		return WmsTileSource.NAME;
	}

	@Override
	public TileUpdate getTileUpdate() {
		return TileUpdate.None;
	}
	
	/**
	 * Recalculate the size of image to respect aspect ratio for the bbox
	 * 
	 * @param bb
	 * 
	 * @return width calculated
	 */
	private int w(BoundingBox bb) {
		double lonSize = bb.east - bb.west;
		double latSize = bb.north - bb.south;
		if(latSize < lonSize){
			double w = lonSize*Tile.SIZE/latSize;
			return (int) w;
		}else{
			return Tile.SIZE;
		}
	}
	
	/**
	 * Recalculate the size of image to respect aspect ratio for the bbox
	 * 
	 * @param bb
	 *  
	 * @return height calculated
	 */
	private int h(BoundingBox bb) {
		double lonSize = bb.east - bb.west;
		double latSize = bb.north - bb.south;
		if(latSize > lonSize){
			double h = latSize*Tile.SIZE/lonSize;
			return (int) h;
		}else{
			return Tile.SIZE;
		}
	}
}

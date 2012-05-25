package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import es.emergya.ui.base.BasicWindow;

/**
 * Holds one map tile. Additionally the code for loading the tile image and
 * painting it is also included in this class.
 * 
 * Resizes the tile image to the default SIZE
 * 
 * @author Jan Peter Stotz
 * @autor Maria Arias de Reyna
 * @autor Alejandro Diaz Torres
 */
public class Tile {

	/**
	 * Hourglass image that is displayed until a map tile has been loaded
	 */
	public static BufferedImage LOADING_IMAGE;
	public static BufferedImage ERROR_IMAGE;
	private static GraphicsConfiguration graphics;
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(Tile.class);
	public static final int SIZE = 256;
	public static final int MAX_NUM_INTENTOS = 3;

	@GoWired
	public BasicWindow window;

	public void setWindow(BasicWindow window) {
		this.window = window;
	}

	protected TileSource source;
	protected int xtile;
	protected int ytile;
	protected int zoom;
	protected BufferedImage image;
	protected String key;
	protected boolean loaded = false;
	protected boolean loading = false;
	protected boolean noError = true;
	protected int numIntentos = 0;

	// number of tiles per axis
	protected int maxX;
	protected int maxY;

	// origin total boundingbox
	protected int minX;
	protected int minY;

	// private ImageReader reader;
	/**
	 * Creates a tile with empty image.
	 * 
	 * @param source
	 * @param xtile
	 * @param ytile
	 * @param zoom
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 */
	public Tile(TileSource source, int xtile, int ytile, int zoom, int minX,
			int minY, int maxX, int maxY) {
		super();

		graphics = window.getFrame().getGraphicsConfiguration();
		try {
			LOADING_IMAGE = graphics.createCompatibleImage(SIZE, SIZE,
					ColorModel.TRANSLUCENT);
			LOADING_IMAGE.createGraphics().drawImage(
					ImageIO.read(Tile.class
							.getResource("/images/hourglass.png")), 0, 0, null);
			ERROR_IMAGE = graphics.createCompatibleImage(SIZE, SIZE,
					ColorModel.TRANSLUCENT);
			ERROR_IMAGE.createGraphics().drawImage(
					ImageIO.read(Tile.class.getResource("/images/error.png")),
					0, 0, null);
		} catch (Exception e1) {
			LOADING_IMAGE = null;
			ERROR_IMAGE = null;
			log.error(e1);
		}

		this.source = source;
		this.xtile = xtile;
		this.ytile = ytile;
		this.zoom = zoom;
		this.image = LOADING_IMAGE;
		this.key = getTileKey(source, xtile, ytile, zoom);
		this.maxX = maxX;
		this.maxY = maxY;
		this.minX = minX;
		this.minY = minY;
	}

	/**
	 * Creates a tile with empty image.
	 * 
	 * @param source
	 * @param xtile
	 * @param ytile
	 * @param zoom
	 */
	public Tile(TileSource source, int xtile, int ytile, int zoom) {
		super();

		this.source = source;
		this.xtile = xtile;
		this.ytile = ytile;
		this.zoom = zoom;
		this.image = LOADING_IMAGE;
		this.key = getTileKey(source, xtile, ytile, zoom);
	}

	/**
	 * Tries to get tiles of a lower or higher zoom level (one or two level
	 * difference) from cache and use it as a placeholder until the tile has
	 * been loaded.
	 */
	public void loadPlaceholderFromCache(TileCache cache) {
		BufferedImage tmpImage = new BufferedImage(SIZE, SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tmpImage.getGraphics();
		// g.drawImage(image, 0, 0, null);
		for (int zoomDiff = 1; zoomDiff < 5; zoomDiff++) {
			// first we check if there are already the 2^x tiles
			// of a higher detail level
			int zoom_high = zoom + zoomDiff;
			if (zoomDiff < 3 && zoom_high <= JMapViewer.MAX_ZOOM) {
				int factor = 1 << zoomDiff;
				int xtile_high = xtile << zoomDiff;
				int ytile_high = ytile << zoomDiff;
				double scale = 1.0 / factor;
				g.setTransform(AffineTransform.getScaleInstance(scale, scale));
				int paintedTileCount = 0;
				for (int x = 0; x < factor; x++) {
					for (int y = 0; y < factor; y++) {
						Tile tile = cache.getTile(source, xtile_high + x,
								ytile_high + y, zoom_high);
						if (tile != null && tile.isLoaded()) {
							paintedTileCount++;
							tile.paint(g, x * SIZE, y * SIZE);
						}
					}
				}
				if (paintedTileCount == factor * factor) {
					image = tmpImage;
					return;
				}
			}

			int zoom_low = zoom - zoomDiff;
			if (zoom_low >= JMapViewer.MIN_ZOOM) {
				int xtile_low = xtile >> zoomDiff;
				int ytile_low = ytile >> zoomDiff;
				int factor = (1 << zoomDiff);
				double scale = factor;
				AffineTransform at = new AffineTransform();
				int translate_x = (xtile % factor) * SIZE;
				int translate_y = (ytile % factor) * SIZE;
				at.setTransform(scale, 0, 0, scale, -translate_x, -translate_y);
				g.setTransform(at);
				Tile tile = cache.getTile(source, xtile_low, ytile_low,
						zoom_low);
				if (tile != null && tile.isLoaded()) {
					tile.paint(g, 0, 0);
					image = tmpImage;
					return;
				}
			}
		}
	}

	public TileSource getSource() {
		return source;
	}

	/**
	 * @return tile number on the x axis of this tile
	 */
	public int getXtile() {
		return xtile;
	}

	/**
	 * @return tile number on the y axis of this tile
	 */
	public int getYtile() {
		return ytile;
	}

	/**
	 * @return zoom level of this tile
	 */
	public int getZoom() {
		return zoom;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public void loadImage(InputStream input) throws IOException {
		boolean errorLocal = false;
		try {
			BufferedImage image2 = ImageIO.read(input);

			if (image2 != null) {
				boolean cuadradoEnProjection = image2.getWidth() == image2
						.getHeight() && image2.getHeight() == Tile.SIZE;
				if (!cuadradoEnProjection) {
					createGraphicsFromImage(image2, Tile.SIZE, Tile.SIZE);
				} else {
					createGraphicsFromImage(image2);
				}
			} else {
				noError = false;
				errorLocal = true;
				throw new TileLoadingException("Imagen no válida");
			}
			if (image == null) {
				input.close();
				noError = false;
				errorLocal = true;
				throw new TileLoadingException(
						"Error transformando tile obtenida");
			}
			numIntentos = 0;
		} catch (Exception e) {
			log.error(e);
			input.close();
			noError = false;
			errorLocal = true;
			throw new TileLoadingException("Excepción cargando tile", e);
		} finally {
			if (errorLocal) {
				numIntentos++;
			}
		}
	}

	/**
	 * Creates a image for the current Tile
	 * 
	 * @param image2
	 *            origin
	 */
	private void createGraphicsFromImage(BufferedImage image2) {
		image = graphics.createCompatibleImage(image2.getWidth(),
				image2.getHeight(), ColorModel.TRANSLUCENT);
		image.createGraphics().drawImage(image2, 0, 0, null);
	}

	/**
	 * Creates a image for the current Tile
	 * 
	 * @param image2
	 *            origin
	 * @param imageW
	 *            width for the tile
	 * @param imageH
	 *            heigth for the tile
	 */
	private void createGraphicsFromImage(BufferedImage image2, int imageW,
			int imageH) {
		BufferedImage unScaledImage = image2;

		// Create new (blank) image of required (scaled) size
		BufferedImage scaledImage = new BufferedImage(imageW, imageH,
				BufferedImage.TYPE_INT_ARGB);

		image = scaledImage;

		// Paint scaled version of image to new image
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(unScaledImage, 0, 0, imageW, imageH, null);

		// clean up
		graphics2D.dispose();
	}

	/**
	 * @return key that identifies a tile
	 */
	public String getKey() {
		return key;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded && noError;
	}

	public String getUrl() {
		return source.getTileUrl(zoom, xtile, ytile);
	}

	/**
	 * Paints the tile-image on the {@link Graphics} <code>g</code> at the
	 * position <code>x</code>/<code>y</code>.
	 * 
	 * @param g
	 * @param x
	 *            x-coordinate in <code>g</code>
	 * @param y
	 *            y-coordinate in <code>g</code>
	 */
	public void paint(Graphics g, int x, int y) {
		if (image == null) {
			return;
		}
		g.drawImage(image, x, y, null);
	}

	@Override
	public String toString() {
		return "Tile " + key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tile)) {
			return false;
		}
		Tile tile = (Tile) obj;
		return (xtile == tile.xtile) && (ytile == tile.ytile)
				&& (zoom == tile.zoom);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + (this.key != null ? this.key.hashCode() : 0);
		return hash;
	}

	public static String getTileKey(TileSource source, int xtile, int ytile,
			int zoom) {
		return zoom + "/" + xtile + "/" + ytile + "@" + source.getName();
	}

	/**
	 * @return the numIntentos
	 */
	public int getNumIntentos() {
		return numIntentos;
	}
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.gpx.GpxData;

public class GpxImporter extends FileImporter {
	private GpxData lastData;

	public GpxImporter() {
		super(new ExtensionFileFilter("gpx,gpx.gz", "gpx", tr("GPX Files")
				+ " (*.gpx *.gpx.gz)"));
	}

	@Override
	public void importData(File file) throws IOException {
		try {
			GpxReader r = null;
			InputStream is;
			if (file.getName().endsWith(".gpx.gz"))
				is = new GZIPInputStream(new FileInputStream(file));
			else
				is = new FileInputStream(file);
			// Workaround for SAX BOM bug
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206835
			if (!((is.read() == 0xef) && (is.read() == 0xbb) && (is.read() == 0xbf))) {
				is.close();
				if (file.getName().endsWith(".gpx.gz"))
					is = new GZIPInputStream(new FileInputStream(file));
				else
					is = new FileInputStream(file);
			}
			r = new GpxReader(is);
			r.data.storageFile = file;
			lastData = r.data;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IOException(tr("Could not read \"{0}\"", file.getName()));
		}

	}

	/**
	 * 
	 * @param file
	 *            The .gpx or gpx.gz file
	 * @return null or a {@link GpxData} object
	 * @throws IOException
	 */
	public GpxData importGPX(File file) throws IOException {
		importData(file);
		return getLastData();
	}

	/**
	 * @return The last imported {@link GpxData}
	 */
	public GpxData getLastData() {
		return lastData;
	}
}

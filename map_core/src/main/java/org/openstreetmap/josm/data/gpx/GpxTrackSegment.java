// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.gpx;

import java.util.Collection;

/**
 * Read-only gpx track segments. Implementations doesn't have to be immutable,
 * but should always be thread safe.
 * 
 */
public interface GpxTrackSegment {

	Collection<WayPoint> getWayPoints();

	double length();

	/**
	 * 
	 * @return Number of times this track has been changed. Always 0 for
	 *         read-only segments
	 */
	int getUpdateCount();
}

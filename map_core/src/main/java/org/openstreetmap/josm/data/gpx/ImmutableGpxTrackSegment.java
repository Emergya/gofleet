// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.gpx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ImmutableGpxTrackSegment implements GpxTrackSegment {

	private final Collection<WayPoint> wayPoints;

	public ImmutableGpxTrackSegment(Collection<WayPoint> wayPoints) {
		this.wayPoints = Collections
				.unmodifiableCollection(new ArrayList<WayPoint>(wayPoints));
	}

	public Collection<WayPoint> getWayPoints() {
		return wayPoints;
	}

	public int getUpdateCount() {
		return 0;
	}

	@Override
	public double length() {
		// TODO Auto-generated method stub
		return 0;
	}
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.gpx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImmutableGpxTrack extends GpxTrack {

	private final Map<String, Object> attributes;
	private final Collection<GpxTrackSegment> segments;
	private final double length;

	public ImmutableGpxTrack(Collection<Collection<WayPoint>> trackSegs,
			Map<String, Object> attributes) {
		List<GpxTrackSegment> newSegments = new ArrayList<GpxTrackSegment>();
		for (Collection<WayPoint> trackSeg : trackSegs) {
			if (trackSeg != null && !trackSeg.isEmpty()) {
				newSegments.add(new ImmutableGpxTrackSegment(trackSeg));
			}
		}
		this.attributes = Collections
				.unmodifiableMap(new HashMap<String, Object>(attributes));
		this.segments = Collections.unmodifiableCollection(newSegments);
		this.length = calculateLength();
	}

	private double calculateLength() {
		double result = 0.0; // in meters

		for (GpxTrackSegment trkseg : segments) {
			result += trkseg.length();
		}
		return result;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public double length() {
		return length;
	}

	public Collection<GpxTrackSegment> getSegments() {
		return segments;
	}

	public int getUpdateCount() {
		return 0;
	}
}

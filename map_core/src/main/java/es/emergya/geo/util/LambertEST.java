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
//License: GPL. For details, see LICENSE file.
//Thanks to Johan Montagnat and its geoconv java converter application
//(http://www.i3s.unice.fr/~johan/gps/ , published under GPL license)
//from which some code and constants have been reused here.
package es.emergya.geo.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.Bounds;

public class LambertEST implements Projection {

    public static final double ef = 500000; //Easting at false origin            = 5000000 m
    public static final double nf = 6375000; //Northing at false origin           = 6375000 m
    public static final double lat1 = Math.toRadians(59 + 1.0/3.0); //Latitude of 1st standard parallel  = 59o20`0`` N
    public static final double lat2 = Math.toRadians( 58);//Latitude of 2nd standard parallel  = 58o0`0`` N
    public static final double latf = Math.toRadians(57.517553930555555555555555555556);//'Latitude of false origin = 57o31`3,19415`` N
    public static final double lonf = Math.toRadians( 24.0);
    public static final double a = 6378137;
    public static final double ee = 0.081819191042815792;
    public static final double m1 = Math.cos(lat1) / (Math.sqrt(1 - ee *ee * Math.pow(Math.sin(lat1), 2)));
    public static final double m2 = Math.cos(lat2) / (Math.sqrt(1 - ee *ee * Math.pow(Math.sin(lat2), 2)));
    public static final double t1 = Math.tan(Math.PI / 4.0 - lat1 / 2.0)
    / Math.pow(( (1.0 - ee * Math.sin(lat1)) / (1.0 + ee * Math.sin(lat1))) ,(ee / 2.0));
    public static final double t2 = Math.tan(Math.PI / 4.0 - lat2 / 2.0)
    / Math.pow(( (1.0 - ee * Math.sin(lat2)) / (1.0 + ee * Math.sin(lat2))) ,(ee / 2.0));
    public static final double tf = Math.tan(Math.PI / 4.0 - latf / 2.0)
    / Math.pow(( (1.0 - ee * Math.sin(latf)) / (1.0 + ee * Math.sin(latf))) ,(ee / 2.0));
    public static final double n  = (Math.log(m1) - Math.log(m2))
    / (Math.log(t1) - Math.log(t2));
    public static final double f  = m1 / (n * Math.pow(t1, n));
    public static final double rf  = a * f * Math.pow(tf, n);

    /**
     * precision in iterative schema
     */
    public static final double epsilon = 1e-11;

    /**
     * @param p  WGS84 lat/lon (ellipsoid GRS80) (in degree)
     * @return eastnorth projection in Lambert Zone (ellipsoid GRS80)
     */
    public EastNorth latlon2eastNorth(LatLon p)
    {

        double t = Math.tan(Math.PI / 4.0 - Math.toRadians(p.lat()) / 2.0)
        / Math.pow(( (1.0 - ee * Math.sin(Math.toRadians(p.lat()))) / (1.0
        + ee * Math.sin(Math.toRadians(p.lat())))) ,(ee / 2.0));
        double r = a * f * Math.pow(t, n);
        double theta = n * (Math.toRadians(p.lon()) - lonf);

        double x = ef + r * Math.sin(theta);     //587446.7
        double y = nf + rf - r * Math.cos(theta); //6485401.6

        return new EastNorth(x,y);
    }

    public  static double IterateAngle(double e, double t)
    {
        double a1 = 0.0;
        double a2 = 3.1415926535897931;
        double a = 1.5707963267948966;
        double b = 1.5707963267948966 - (2.0 * Math.atan(t * Math.pow((1.0
        - (e * Math.sin(a))) / (1.0 + (e * Math.sin(a))), e / 2.0)));
        while (Math.abs(a-b) > epsilon)
        {
            a = a1 + ((a2 - a1) / 2.0);
            b = 1.5707963267948966 - (2.0 * Math.atan(t * Math.pow((1.0
            - (e * Math.sin(a))) / (1.0 + (e * Math.sin(a))), e / 2.0)));
            // Se compara la diferencia con un umbral de precisión ya que los cálculos con 
            // doubles son imprecisos
            if (Math.abs(a1 - a2) < .0000001)
            {
                return 0.0;
            }
            if (b > a)
                a1 = a;
            else
                a2 = a;
        }
        return b;
    }

    public LatLon eastNorth2latlon(EastNorth p)
    {
        double r = Math.sqrt(Math.pow((p.getX() - ef), 2.0) + Math.pow((rf
        - p.getY() + nf), 2.0) ) * Math.signum(n);
        double T = Math.pow((r / (a * f)), (1.0/ n)) ;
        double theta = Math.atan((p.getX() - ef) / (rf - p.getY() + nf));
        double y = (theta / n + lonf) ;
        double x = (IterateAngle(ee, T)) ;
        return new LatLon(Math.toDegrees(x),Math.toDegrees(y));
    }

    @Override
    public String toString() {
        return tr("Lambert Zone (Estonia)");
    }

    public String toCode() {
        return "EPSG:3301";
    }

    public String getCacheDirectoryName() {
        return "lambertest";
    }

    public ProjectionBounds getWorldBounds()
    {
        Bounds b = getWorldBoundsLatLon();
        return new ProjectionBounds(latlon2eastNorth(b.min), latlon2eastNorth(b.max));
    }

    public Bounds getWorldBoundsLatLon()
    {
        return new Bounds(
        new LatLon(-90.0, -180.0),
        new LatLon(90.0, 180.0));
    }
}

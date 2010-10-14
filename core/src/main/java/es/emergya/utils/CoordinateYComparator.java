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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.emergya.utils;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.Comparator;

/**
 *
 * @author jlrodriguez
 */
public class CoordinateYComparator implements Comparator<Coordinate>{

    /**
     *
     * @param o1 la primera coordenada a ser comparada.
     * @param o2 la segunda coordenada a ser comparada.
     * @return -1, 0 o 1 si la coordenada Y de o1 es menor, igual o mayor que la
     * coordenada Y de o2, respectivamente.
     */
   
    public int compare(Coordinate o1, Coordinate o2) {
        if (o1.y > o2.y) {
            return 1;
        } else if (o1.y < o2.y) {
            return -1;
        } else {
            return 0;
        }
    }


}

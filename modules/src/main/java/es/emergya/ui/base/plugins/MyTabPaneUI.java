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
package es.emergya.ui.base.plugins;

import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.text.View;

class MyTabPaneUI extends MetalTabbedPaneUI {
//	@Override
//	protected void layoutLabel(int tabPlacement, FontMetrics metrics,
//			int tabIndex, String title, Icon icon, Rectangle tabRect,
//			Rectangle iconRect, Rectangle textRect, boolean isSelected) {
//		textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
//
//		View v = getTextViewForTab(tabIndex);
//		if (v != null) {
//			tabPane.putClientProperty("html", v);
//		}
//
//		SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
//				title, icon, SwingUtilities.BOTTOM, SwingUtilities.LEFT,
//				SwingUtilities.BOTTOM, SwingUtilities.BOTTOM, tabRect,
//				iconRect, textRect, textIconGap);
//
////		tabPane.putClientProperty("html", null);
////
////		int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
////		int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
////		iconRect.x += xNudge;
////		iconRect.y += yNudge;
////		textRect.x += xNudge;
////		textRect.y += yNudge;
//	}
}

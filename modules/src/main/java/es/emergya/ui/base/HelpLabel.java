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
/**
 * 
 */
package es.emergya.ui.base;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.AbstractBorder;

/**
 * JLabel with a mark question at the left
 * 
 * @see JLabel
 * @author marias
 * 
 */
public class HelpLabel extends JLabel {

	private static String help = "/images/help.png";
	private static final long serialVersionUID = 763386004694590300L;
	static final int size = 70;

	public HelpLabel(String error) {
		super(error);
		this
				.setIcon(new ImageIcon(this.getClass().getResource(
						HelpLabel.help)));
		this.setBorder(new CurvedBorder());
	}

	public HelpLabel() {
		this("");
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height -= size;
		return d;
	}

	@Override
	public void setPreferredSize(Dimension d) {
		d.height += size;
		super.setPreferredSize(d);
	}

	@Override
	public void paint(Graphics g) {
		if (!this.getText().equals("")
				&& !this.getText().equals("<html></html>")) {
			this.paintBorder(g);
			this.paintComponent(g);
			this.paintChildren(g);
		}
	}

	@Override
	public void setText(String text) {
		super.setText("<html>" + text + "</html>");
	}
}

class CurvedBorder extends AbstractBorder {

	private static final long serialVersionUID = -8202297615864745895L;
	private Color wallColor = Color.BLACK;
	private Color backgroundColor = new Color(0xffff99);
	private int height = 10;
	private int sinkLevel = 30;
	private int minHeight = 17;

	public CurvedBorder() {
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

		h += HelpLabel.size;
		y -= HelpLabel.size / 2;
		int sinkLevelHeight = this.sinkLevel + this.height;
		int altura = h - 2 * sinkLevelHeight;
		if (altura < minHeight) {
			sinkLevelHeight = (h - minHeight) / 2;
			altura = h - 2 * sinkLevelHeight;
			g.setClip(x, y - 5, w, h + 10);
		}
		g.setColor(this.backgroundColor);
		g.fillRoundRect(x + this.sinkLevel, y + sinkLevelHeight, w - 2
				* this.sinkLevel, altura, this.sinkLevel, this.sinkLevel);

		g.setColor(this.wallColor);
		g.drawRoundRect(x + this.sinkLevel, y + sinkLevelHeight, w - 2
				* this.sinkLevel, altura, this.sinkLevel, this.sinkLevel);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(this.sinkLevel, this.sinkLevel, this.sinkLevel,
				this.sinkLevel);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets i) {
		i.left = i.right = i.bottom = i.top = this.sinkLevel;
		return i;
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}
}

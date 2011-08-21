package org.gofleet.ui.plugins;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicButtonUI;

import org.apache.commons.logging.LogFactory;

import es.emergya.ui.base.plugins.AbstractPluggable;
import es.emergya.ui.base.plugins.AbstractPlugin;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends AbstractPlugin {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2446063455540381434L;
	static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(ButtonTabComponent.class);

	TabButton tabButton = null;

	public ButtonTabComponent(String title, JPanel panel) {
		// unset default FlowLayout' gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		try {
			this.title = title;
			this.tip = title;
			this.tabButton = new TabButton(this.title);

			try {
				this.icon = new ImageIcon(
						ButtonTabComponent.class
								.getResource("/images/report.png"));
			} catch (Throwable t) {
				LOG.error("No icon", t);
			}
			setOpaque(false);
			this.setLayout(new BorderLayout());
			this.add(new JScrollPane(panel), BorderLayout.CENTER);
		} catch (Throwable t) {
			LOG.error("Error creating TabComponent", t);
		}
	}

	public TabButton getTabButton() {
		return tabButton;
	}

	private class TabButton extends JPanel {
		private static final long serialVersionUID = 1257932312536916438L;

		public TabButton(String title, final AbstractPluggable tab) {
			try {
				setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
				setLayout(new FlowLayout());	
				add(new JLabel(title));
				final int size = 17;

				JButton button = new JButton(new AbstractAction() {
					private static final long serialVersionUID = 2590075786392367187L;

					@Override
					public void actionPerformed(ActionEvent e) {
						tab.removeElement(ButtonTabComponent.this);
					}
				}) {
					private static final long serialVersionUID = 4927608975431572465L;

					// paint the cross
					protected void paintComponent(Graphics g) {
						try {
							super.paintComponent(g);
							Graphics2D g2 = (Graphics2D) g.create();

							// shift the image for pressed buttons
							if (getModel().isPressed()) {
								g2.translate(1, 1);
							}

							g2.setStroke(new BasicStroke(2));
							g2.setColor(Color.BLACK);
							if (getModel().isRollover()) {
								g2.setColor(Color.GREEN);
							}
							int delta = 6;

							g2.drawLine(delta, delta, size - delta - 1, size
									- delta - 1);
							g2.drawLine(size - delta - 1, delta, delta, size
									- delta - 1);
							g2.dispose();
						} catch (Throwable t) {
							LOG.error("Error painting button", t);
						}
					}
				};
				button.setSize(new Dimension(size, size));
				button.setPreferredSize(new Dimension(size, size));
				button.setRolloverEnabled(true);
				button.setBorder(BorderFactory.createEtchedBorder());
				button.setFocusable(false);
				button.setUI(new BasicButtonUI());
				button.addMouseListener(buttonMouseListener);
				button.setToolTipText("close this tab");
				add(button);

			} catch (Throwable t) {
				LOG.error("Error creating tab button", t);
			}
		}

		// we don't want to update UI for this button
		public void updateUI() {
		}

	}

	private final static MouseListener buttonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};
}

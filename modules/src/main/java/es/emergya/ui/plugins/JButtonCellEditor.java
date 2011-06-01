/*
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jlrodriguez@emergya.es">Juan Luís Rodríguez</a>
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
 * @author <a href="mailto:fario@emergya.es">Félix del Río Beningno</a>
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
package es.emergya.ui.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellEditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cell editor que parece un boton y hace lo mismo que haria un boton.
 * 
 * @author fario
 * @author marias
 * 
 */
public class JButtonCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private static final long serialVersionUID = -2480639142946730989L;

	static final Log log = LogFactory.getLog(JButtonCellEditor.class);

	@Override
	public Component getTableCellEditorComponent(final JTable table,
			Object value, boolean isSelected, final int row, final int column) {
		JButton b = (JButton) value;
		b.setBorderPainted(false);
		b.setContentAreaFilled(false);
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						stopCellEditing();
						return null;
					}
				};
				SwingUtilities.invokeLater(sw);

				SwingWorker<Object, Object> sw1 = new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						table.repaint();
						return null;
					}
				};
				SwingUtilities.invokeLater(sw1);
			}
		});
		return b;
	}

	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	@Override
	public boolean stopCellEditing() {
		fireEditingCanceled();
		return true;
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}
}
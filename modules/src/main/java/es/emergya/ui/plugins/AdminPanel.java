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

import static es.emergya.cliente.constants.LogicConstants.getIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import es.emergya.actions.Authentication;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

/**
 * Panel que incluye una tabla con columnas y campos para gestionar,
 * herramientas de filtrado, de creacion, etc.
 * 
 * Sigue las indicaciones en los bocetos suministrados por el cliente
 * 
 * @author fario
 * @author marias
 * 
 */
public class AdminPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 4520895666460134341L;
	private JPanel tablePanel;
	private JTable table;
	private Boolean initialized = false;
	private JLabel title;
	private final JLabel cuenta = new JLabel();
	static final Log log = LogFactory.getLog(AdminPanel.class);
	private JButton newButton;
	private JButton deselectAll;
	private JTable filters;
	private Integer size = 0;
	private Option father = null;
	protected int columnToReselect = 1;
	protected Map<Integer, Integer> colsWidth = new HashMap<Integer, Integer>();
	protected String errorString = "Admin.delete.fail";
	protected String errorCause = "";
	protected List<Integer> invisibleFilterCols = new LinkedList<Integer>();
	private List<Object> seleccion = new ArrayList<Object>(0);
	protected Boolean canDelete = true;
	protected Boolean canCreateNew = true;
	protected MyRendererColoring myRendererColoring = null;

	public void setMyRendererColoring(MyRendererColoring rendererColoring) {
		this.myRendererColoring = rendererColoring;
	}

	public Boolean getCanCreateNew() {
		return canCreateNew;
	}

	protected void setCanCreateNew(Boolean canCreateNew) {
		this.canCreateNew = canCreateNew;
	}

	public Boolean getCanDelete() {
		return canDelete;
	}

	protected void setCanDelete(Boolean canDelete) {
		this.canDelete = canDelete;
	}

	public void setErrorCause(String cause) {
		this.errorCause = cause;
	}

	public void addInvisibleFilterCol(Integer i) {
		invisibleFilterCols.add(i);
	}

	public void removeAllInvisibleFilterCol() {
		invisibleFilterCols.clear();
	}

	public AdminPanel(String t, Icon icon, Option myself, boolean canCreateNew,
			boolean canDelete) {
		super();
		setCanCreateNew(canCreateNew);
		setCanDelete(canDelete);
		setLayout(new SpringLayout());
		this.father = myself;
		setBackground(Color.WHITE);

		// Titulo con icono
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		title = new JLabel(t);
		title.setIcon(icon);
		title.setFont(LogicConstants.deriveBoldFont(12f));
		title.setBorder(new EmptyBorder(0, 10, 0, 10));
		titlePanel.add(title);
		titlePanel.setOpaque(false);
		add(titlePanel);
		Dimension d = titlePanel.getSize();
		if (icon != null)
			d.height = icon.getIconHeight();
		titlePanel.setMaximumSize(d);

		// Controles de "nuevo" "seleccionar todos" etc...
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING));
		controls.setOpaque(false);
		final boolean femenino = t.indexOf("atrulla") != -1
				|| t.indexOf("apa") != -1 || t.indexOf("lota") != -1
				|| t.indexOf("encia") != -1;

		if (getCanCreateNew()) {
			if (femenino) {
				newButton = new JButton("Nueva", getIcon("button_nuevo"));
			} else {
				newButton = new JButton("Nuevo", getIcon("button_nuevo"));
			}
			controls.add(newButton);
		}
		if (getCanDelete()) {
			JButton selectAll = new JButton(((femenino) ? "Seleccionar Todas"
					: "Seleccionar Todos"), getIcon("button_selectall"));
			selectAll.addActionListener(this);
			controls.add(selectAll);
			deselectAll = new JButton(((femenino) ? "Deseleccionar Todas"
					: "Deseleccionar Todos"), getIcon("button_unselectall"));
			deselectAll.addActionListener(this);
			controls.add(deselectAll);
			JButton deleteAll = new JButton(
					((femenino) ? "Eliminar Seleccionadas"
							: "Eliminar Seleccionados"),
					getIcon("button_delall"));
			deleteAll.addActionListener(this);
			controls.add(deleteAll);
		}
		d = controls.getSize();
		controls.setMaximumSize(d);
		add(controls);

		// Tabla
		tablePanel = new JPanel(new BorderLayout());
		tablePanel.setOpaque(false);
		add(tablePanel);

		SpringUtilities.makeCompactGrid(this, 3, 1, 0, 0, 0, 0);
	}

	public AdminPanel(String t, Icon icon, Option myself) {
		this(t, icon, myself, true, true);
	}

	/**
	 * A la columna colIndex le pone un width colWidth. La primera columna es el
	 * 1 (el cero es el checkbox)
	 * 
	 * @param colIndex
	 * @param width
	 */
	public void addColumnWidth(Integer colIndex, Integer colWidth) {
		colsWidth.put(colIndex, colWidth);
	}

	/**
	 * @see #addColumnWidth(Integer, Integer)
	 */
	public void resetColumnWidth() {
		colsWidth.clear();
	}

	public void setNewAction(SummaryAction summaryAction) {
		newButton.addActionListener(summaryAction);
	}

	/**
	 * @param text
	 * @param icon
	 */
	public void setTitle(String text, Icon icon) {
		title.setText(text);
		title.setIcon(icon);
	}

	public void setNewAction(Action action) {
		newButton.setAction(action);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.indexOf("Seleccionar Tod") == 0) {
			for (int i = 0; i < table.getRowCount(); i++) {
				table.getModel().setValueAt(Boolean.TRUE, i, 0);
			}
		} else if (cmd.indexOf("Deseleccionar Tod") == 0) {
			for (int i = 0; i < table.getRowCount(); i++) {
				table.getModel().setValueAt(Boolean.FALSE, i, 0);
			}
		} else if (cmd.indexOf("Eliminar Seleccionad") == 0) {
			boolean alguno = false;
			for (int i = table.getRowCount() - 1; i >= 0 && !alguno; i--) {
				if ((Boolean) table.getModel().getValueAt(i, 0)) {
					alguno = true;
				}
			}

			if (!alguno) {
				return;
			}

			if (JOptionPane.showConfirmDialog(this,
					"Buttons.delete.confirm",
					"Selecciona una opción", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			Vector<Object> fail = new Vector<Object>();
			int total = 0;
			for (int i = table.getRowCount() - 1; i >= 0; i--) {
				if ((Boolean) table.getModel().getValueAt(i, 0)) {
					DeleteAction a = (DeleteAction) ((JButton) table
							.getValueAt(i, table.getColumnCount() - 1))
							.getAction();
					total++;
					if (!a.delete(false)) {
						fail.add(table.getValueAt(i, 1));
					}

				}
			}

			if (this.father != null) {
				this.father.refresh(null);
				PluginEventHandler.fireChange(this.father);
			}

			if (total == 0) {
				JOptionPane.showMessageDialog(this,
						"No hay elementos seleccionados para eliminar.", null,
						JOptionPane.ERROR_MESSAGE);
			}

			if (fail.size() > 0) {
				JOptionPane.showMessageDialog(this,
						errorString + ":\n" + fail.toString() + "\n"
								+ errorCause, null, JOptionPane.ERROR_MESSAGE);
			}

		} else
			log.error("Comando no encontrado: " + cmd);
	}

	/**
	 * 
	 * @param columnNames
	 *            nombres de las columnas de la tabla
	 * @param filterOptions
	 *            lista de opciones de un combobox. Si esta vacio entonces es un
	 *            textfield
	 * @param noFiltrarAction
	 * @param filtrarAction
	 */
	public void generateTable(String[] columnNames, Object[][] filterOptions,
			AdminPanel.NoFiltrarAction noFiltrarAction,
			AdminPanel.FiltrarAction filtrarAction) {

		if (columnNames == null) {
			columnNames = new String[] {};
		}
		if (filterOptions == null) {
			filterOptions = new Object[][] {};
		}

		String filterString = "[";
		for (Object[] o : filterOptions) {
			filterString += Arrays.toString(o) + " ";
		}
		filterString += "]";

		log.debug("generateTable( columnNames = "
				+ Arrays.toString(columnNames) + ", filterOptions = "
				+ filterString + ")");

		tablePanel.removeAll();
		int columnNamesLength = columnNames.length;
		if (!getCanDelete())
			columnNamesLength++;
		MyTableModel dataModel = new MyTableModel(1, columnNamesLength + 2) {

			private static final long serialVersionUID = 1348355328684460769L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 0 && !invisibleFilterCols.contains(column);
			}
		};
		filters = new JTable(dataModel) {

			private static final long serialVersionUID = -8266991359840905405L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);

				if (isCellEditable(row, column)
						&& column != getColumnCount() - 1) {
					if (c instanceof JTextField) {
						((JTextField) c).setBorder(new MatteBorder(1, 1, 1, 1,
								Color.BLACK));
					} else if (c instanceof JComboBox) {
						((JComboBox) c).setBorder(new MatteBorder(1, 1, 1, 1,
								Color.BLACK));
					} else if (c instanceof JLabel) {
						((JLabel) c).setBorder(new MatteBorder(1, 1, 1, 1,
								Color.BLACK));
					}
				}
				return c;
			}
		};
		filters.setSurrendersFocusOnKeystroke(true);
		filters.setShowGrid(false);
		filters.setRowHeight(22);
		filters.setOpaque(false);

		for (Integer i = 0; i < filterOptions.length; i++) {
			final Object[] items = filterOptions[i];
			if (items != null && items.length > 1) {
				setComboBoxEditor(i, items);
			} else {
				final DefaultCellEditor defaultCellEditor = new DefaultCellEditor(
						new JTextField());
				defaultCellEditor.setClickCountToStart(1);
				filters.getColumnModel().getColumn(i + 1)
						.setCellEditor(defaultCellEditor);
			}
		}

		filters.setRowSelectionAllowed(false);
		filters.setDragEnabled(false);
		filters.setColumnSelectionAllowed(false);
		filters.setDefaultEditor(JButton.class, new JButtonCellEditor());

		filters.setDefaultRenderer(Object.class, new DefaultTableRenderer() {
			private static final long serialVersionUID = -4811729559786534118L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
				if (invisibleFilterCols.contains(column))
					c = new JLabel("");
				return c;
			}

		});

		filters.setDefaultRenderer(JButton.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JButton b = (JButton) value;
				b.setBorderPainted(false);
				b.setContentAreaFilled(false);
				return b;
			}
		});
		filters.setDefaultRenderer(JLabel.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				return (JLabel) value;
			}
		});
		filters.setDefaultEditor(JButton.class, new JButtonCellEditor());
		filters.getModel().setValueAt(new JLabel(""), 0, 0);
		JButton jButton2 = new JButton(noFiltrarAction);
		JButton jButton = new JButton(filtrarAction);
		jButton.setBorderPainted(false);
		jButton2.setBorderPainted(false);
		jButton.setContentAreaFilled(false);
		jButton2.setContentAreaFilled(false);
		if (jButton.getIcon() != null)
			jButton.setPreferredSize(new Dimension(jButton.getIcon()
					.getIconWidth(), jButton.getIcon().getIconHeight()));
		if (jButton2.getIcon() != null)
			jButton2.setPreferredSize(new Dimension(jButton2.getIcon()
					.getIconWidth(), jButton2.getIcon().getIconHeight()));

		filters.getModel().setValueAt(jButton, 0, columnNamesLength - 1);
		filters.getColumnModel().getColumn(columnNamesLength - 1)
				.setMinWidth(jButton.getWidth() + 24);
		filters.getModel().setValueAt(jButton2, 0, columnNamesLength);
		filters.getColumnModel().getColumn(columnNamesLength)
				.setMinWidth(jButton2.getWidth() + 14);
		cuenta.setHorizontalAlignment(JLabel.CENTER);
		cuenta.setText("?/?");
		filters.getModel().setValueAt(cuenta, 0, columnNamesLength + 1);

		tablePanel.add(filters, BorderLayout.NORTH);

		Vector<String> headers = new Vector<String>();
		headers.add("");
		headers.addAll(Arrays.asList(columnNames));
		MyTableModel model = new MyTableModel(headers, 0);
		table = new JTable(model) {

			private static final long serialVersionUID = 949284378605881770L;
			private int highLightedRow = -1;
			private Rectangle dirtyRegion = null;

			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				try {
					if (AdminPanel.this.myRendererColoring != null)
						c.setBackground(AdminPanel.this.myRendererColoring
								.getColor(AdminPanel.this.table.getValueAt(row,
										1)));
				} catch (Throwable t) {
					log.error("Error al colorear la celda: " + t);
				}
				return c;
			}

			@Override
			protected void processMouseMotionEvent(MouseEvent e) {
				try {
					int row = rowAtPoint(e.getPoint());
					Graphics g = getGraphics();
					if (row == -1) {
						highLightedRow = -1;
					}

					// row changed
					if (highLightedRow != row) {
						if (null != dirtyRegion) {
							paintImmediately(dirtyRegion);
						}
						for (int j = 0; j < getRowCount(); j++) {
							if (row == j) {
								// highlight
								Rectangle firstRowRect = getCellRect(row, 0,
										false);
								Rectangle lastRowRect = getCellRect(row,
										getColumnCount() - 1, false);
								dirtyRegion = firstRowRect.union(lastRowRect);
								g.setColor(new Color(0xff, 0xff, 0, 100));
								g.fillRect((int) dirtyRegion.getX(),
										(int) dirtyRegion.getY(),
										(int) dirtyRegion.getWidth(),
										(int) dirtyRegion.getHeight());
								highLightedRow = row;
							}

						}
					}
				} catch (Exception ex) {
				}
				super.processMouseMotionEvent(e);
			}
		};

		table.setRowHeight(22);

		table.setOpaque(false);
		// table.setAutoCreateRowSorter(true);

		table.setDragEnabled(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);

		table.setDefaultEditor(JButton.class, new JButtonCellEditor());
		table.setDefaultRenderer(JButton.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JButton b = (JButton) value;
				if (b != null) {
					b.setBorderPainted(false);
					b.setContentAreaFilled(false);
				}
				return b;
			}
		});

		JScrollPane jScrollPane = new JScrollPane(table);
		jScrollPane.setOpaque(false);
		jScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.getViewport().setOpaque(false);
		tablePanel.add(jScrollPane, BorderLayout.CENTER);
	}

	private JComboBox setComboBoxEditor(Integer i, final Object[] items) {
		JComboBox cb = new JComboBox(items);
		final DefaultCellEditor defaultCellEditor = new DefaultCellEditor(cb);
		defaultCellEditor.setClickCountToStart(1);
		filters.getColumnModel().getColumn(i + 1)
				.setCellEditor(defaultCellEditor);
		return cb;
	}

	public Integer getNumObjects() {
		return ((MyTableModel) table.getModel()).getRowCount();
	}

	/**
	 * Cambia los datos que muestra la tabla al array que se le pase.
	 * 
	 * Se aconseja que sean: * {@link Boolean} para valores si/no *
	 * {@link AbstractAction} o subclases para botones * Numeros *
	 * {@link String} para todo lo demas
	 * 
	 * @param data
	 */
	public void setTableData(final Object[][] data) {
		final Object[][] newData = new Object[data.length][];

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			private MyTableModel model;

			@Override
			protected Object doInBackground() throws Exception {
				if (data == null) {
					return null;
				}
				model = (MyTableModel) table.getModel();

				synchronized (seleccion) {
					for (int i = 0; i < model.getRowCount(); i++) {
						if ((Boolean) model.getValueAt(i, 0)) {
							seleccion
									.add(model.getValueAt(i, columnToReselect));
						}
					}
				}

				synchronized (seleccion) {
					for (int i = 0; i < data.length; i++) {
						newData[i] = new Object[data[0].length + 1];
						newData[i][0] = new Boolean(
								Authentication.isAuthenticated()
										&& seleccion
												.contains(data[i][columnToReselect - 1]));
						for (int j = 0; j < data[i].length; j++) {
							newData[i][j + 1] = data[i][j];
						}
					}
				}
				return null;
			}

			protected void done() {
				model.updateRows(newData);

				if (!initialized) {
					table.putClientProperty("terminateEditOnFocusLost",
							Boolean.TRUE);
					for (Integer i : colsWidth.keySet()) {
						try {
							final TableColumn column = table.getColumnModel()
									.getColumn(i);
							final TableColumn filtro = filters.getColumnModel()
									.getColumn(i);
							column.setPreferredWidth(colsWidth.get(i));
							column.setMinWidth(colsWidth.get(i));
							column.setMaxWidth(colsWidth.get(i));
							filtro.setPreferredWidth(colsWidth.get(i));
							filtro.setMinWidth(colsWidth.get(i));
							filtro.setMaxWidth(colsWidth.get(i));

						} catch (Throwable t) {
							log.error("Error al resizar las columnas: " + t);
						}
					}

					TableColumn col = table.getColumnModel().getColumn(0);
					TableColumn fil = filters.getColumnModel().getColumn(0);
					log.trace("Resizando CheckBox");
					col.setMaxWidth(49);
					fil.setMaxWidth(49);

					int defaultWidth = 54;
					for (int i = 1; i < table.getColumnModel().getColumnCount() - 2; i++) {
						col = table.getColumnModel().getColumn(i);
						fil = filters.getColumnModel().getColumn(i);

						final Class<?> columnClass = ((MyTableModel) table
								.getModel()).getColumnClass(i);
						if (columnClass == JButton.class) {
							log.trace("Resizando JButton");
							col.setMaxWidth(defaultWidth);
							fil.setMaxWidth(defaultWidth);
						} else if (columnClass == Boolean.class) {
							log.trace("Resizando CheckBox");
							col.setMaxWidth(49);
							fil.setMaxWidth(49);
						}
					}

					if (getCanDelete()) {
						col = table.getColumnModel().getColumn(
								table.getColumnModel().getColumnCount() - 2);
						col.setMaxWidth(defaultWidth);
						col.setPreferredWidth(defaultWidth);
						col = table.getColumnModel().getColumn(
								table.getColumnModel().getColumnCount() - 1);
						col.setMaxWidth(defaultWidth);
						col.setPreferredWidth(defaultWidth);
					} else {
						col = table.getColumnModel().getColumn(
								table.getColumnModel().getColumnCount() - 1);
						col.setMaxWidth(defaultWidth * 2);
						col.setPreferredWidth(defaultWidth * 2);
					}
					int max = filters.getColumnModel().getColumnCount() - 1;
					filters.getColumnModel().getColumn(max).setMaxWidth(61);
					filters.getColumnModel().getColumn(max - 1).setMaxWidth(32);
					filters.getColumnModel().getColumn(max - 2).setMaxWidth(32);
					initialized = true;
				}
			}
		};
		sw.execute();
	}

	/**
	 * Implementar con la accion de borrar un elemento.
	 * 
	 * @author fario
	 * 
	 */
	public abstract class DeleteAction<T> extends AbstractAction {

		private static final long serialVersionUID = -4311069821314184357L;
		protected T target;

		/**
		 * 
		 * @param o
		 *            objeto a borrar
		 */
		public DeleteAction(T o) {
			super(null, getIcon("Buttons.delete"));
			this.target = o;
			log.trace("Creado delete action para " + this.target);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(AdminPanel.this,
					"Buttons.delete.confirm",
					"Selecciona una opción", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
				delete(true);
				AdminPanel.this.father.refresh(null);
				PluginEventHandler.fireChange(AdminPanel.this.father);
			}
		}

		protected abstract boolean delete(boolean show_alert);
	}

	/**
	 * Implementar con la accion de guardar o modificar un elemento
	 * 
	 * @author marias
	 * 
	 */
	public abstract class SaveOrUpdateAction<T> extends AbstractAction {

		private static final long serialVersionUID = -4311069821314184357L;
		protected T original = null;
		protected JFrame frame = null;

		/**
		 * 
		 * @param o
		 *            objeto a guardar o modificar
		 */
		public SaveOrUpdateAction(T o) {
			super();
			this.original = o;
			log.trace("Creado save action para " + this.original);
		}

		public void setFrame(JFrame f) {
			this.frame = f;
		}

		public void closeFrame() {
			if (this.frame != null) {
				this.frame.dispose();
			}
			PluginEventHandler.fireChange(AdminPanel.this.father);
		}

		@Override
		public abstract void actionPerformed(ActionEvent e);
	}

	/**
	 * Implementar con la accion de filtrar
	 * 
	 * @author marias
	 * 
	 */
	public abstract class FiltrarAction extends AbstractAction {

		private static final long serialVersionUID = -4311069821314184357L;

		public FiltrarAction() {
			super(null, getIcon("Buttons.filtrar"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

				@Override
				protected Object doInBackground() throws Exception {
					applyFilter(AdminPanel.this.filters);
					return null;
				}
			};
			sw.execute();
		}

		protected abstract void applyFilter(JTable filters);
	}

	public class MyRendererColoring {
		public Color getColor(Object object) {
			return null;
		}
	}

	/**
	 * Dejar de filtrar
	 * 
	 * @author marias
	 * 
	 */
	public abstract class NoFiltrarAction extends AbstractAction {

		private static final long serialVersionUID = -4311069821314184357L;

		public NoFiltrarAction() {
			super(null, getIcon("Buttons.noFiltrar"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

				@Override
				protected Object doInBackground() throws Exception {
					applyFilter();
					return null;
				}

				@Override
				protected void done() {
					for (int i = 1; i < ((MyTableModel) AdminPanel.this.filters
							.getModel()).getColumnCount() - 3; i++) {
						if (filters.getModel().getValueAt(0, i) instanceof String) {
							filters.getModel().setValueAt(null, 0, i);
						} else if (filters.getModel().getValueAt(0, i) instanceof JComboBox) {
							((JComboBox) filters.getModel().getValueAt(0, i))
									.setSelectedIndex(0);
						}
					}
					super.done();
				}
			};

			sw.execute();
		}

		protected abstract void applyFilter();
	}

	public void setCuenta(final int showed, final int total) {

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				setSize(total);
				return null;
			}

			@Override
			protected void done() {
				cuenta.setText("(" + showed + "/" + total + ")");
				filters.updateUI();
			}
		};
		sw.execute();
	}

	public void changeRow(final int fila, final int destino) {
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected void done() {
				((MyTableModel) table.getModel()).changeRow(fila, destino);
			}

			@Override
			protected Object doInBackground() throws Exception {
				return null;
			}
		};
		sw.execute();
	}

	/**
	 * Columna que usar en el equals que define la fila seleccionada. Útil para
	 * refrescos y filtros de la tabla.
	 * 
	 * @param columnToReselect
	 */
	public void setColumnToReselect(int columnToReselect) {
		this.columnToReselect = columnToReselect;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getTotalSize() {
		return size;
	}

	public void setFilter(final Integer i, final String[] items) {
		SwingWorker<JComboBox, Object> sw = new SwingWorker<JComboBox, Object>() {
			@Override
			protected JComboBox doInBackground() throws Exception {
				JComboBox cb = setComboBoxEditor(i - 1, items);
				return cb;
			}

			@Override
			protected void done() {
				if (filters.getCellEditor() != null)
					filters.getCellEditor().cancelCellEditing();
				filters.repaint();
			}
		};

		sw.execute();
	}

	public void unckeckAll() {
		if (this.deselectAll != null)
			synchronized (seleccion) {
				this.deselectAll.doClick();
				AdminPanel.this.seleccion.clear();
			}
	}
}

class MyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2123942827487198300L;
	private String columnNames[] = new String[0];
	private Object rowData[][] = new Object[0][0];
	private Object sync = new Object();

	public MyTableModel(String[] colNames, int i) {
		columnNames = colNames;
		rowData = new Object[i][0];
	}

	public void changeRow(int fila, int destino) {
		Object[] row = removeRowSilent(fila);
		addRow(row, destino);
	}

	public MyTableModel(int i, int j) {
		rowData = new Object[i][j];
		columnNames = new String[j];
		for (int k = 0; k < j; k++) {
			columnNames[k] = "";
		}
	}

	public MyTableModel(Vector<String> headers, int i) {
		this(headers.toArray(new String[0]), i);
	}

	public void addRowSilent(Vector<Object> fila, int indice) {
		synchronized (sync) {
			Object newRowData[][] = new Object[rowData.length + 1][];
			int k = 0;
			for (int i = 0; i < newRowData.length; i++) {
				if (indice == i) {
					newRowData[i] = fila.toArray();
				} else {
					newRowData[i] = rowData[k++];
				}
			}
			rowData = newRowData;
		}
	}

	public void addRow(Vector<Object> fila) {
		addRowSilent(fila, rowData.length);
		fireTableDataChanged();
	}

	public void addRow(Object[] fila, int indice) {
		Vector<Object> vector = new Vector<Object>();
		for (Object f : fila) {
			vector.add(f);
		}
		addRowSilent(vector, indice);
		fireTableDataChanged();
	}

	public void addRow(Object[] fila) {
		addRow(fila, rowData.length);
	}

	public Object[] removeRowSilent(int fila) {
		Object[] res = null;
		synchronized (sync) {
			try {
				if (rowData.length == 0) {
					return new Object[0][];
				}
				Object newRowData[][] = new Object[rowData.length - 1][];
				int i = 0;
				int contador = 0;
				for (Object[] row : rowData) {
					if (contador != fila) {
						newRowData[i++] = row;
					} else {
						res = row;
					}
					contador++;
				}
				rowData = newRowData;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
		return res;
	}

	public Object[] removeRow(int fila) {
		Object[] res = removeRowSilent(fila);
		fireTableDataChanged();
		return res;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	@Override
	public int getRowCount() {
		return rowData.length;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object object = null;
		synchronized (sync) {
			try {
				object = rowData[row][col];
				AdminPanel.log.trace("getValueAt(" + row + ", " + col + ") = "
						+ object);
			} catch (Throwable t) {
			}
		}
		return object;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		synchronized (sync) {
			rowData[row][col] = value;
		}
		fireTableCellUpdated(row, col);
	}

	public void updateRowsSilent(Object[][] data) {

		synchronized (sync) {
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					if (data[i][j] instanceof AbstractAction) {
						data[i][j] = new JButton((AbstractAction) data[i][j]);
					}
				}
			}

			rowData = data;
		}
		columnNames = new String[data.length];
		for (int k = 0; k < data.length; k++) {
			columnNames[k] = "";
		}
	}

	public void updateRows(Object[][] data) {
		updateRowsSilent(data);
		fireTableDataChanged();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		final Object valueAt = getValueAt(0, columnIndex);
		if (valueAt == null) {
			return Object.class;
		}
		if (valueAt instanceof AbstractAction) {
			return AbstractAction.class;
		}
		return valueAt.getClass();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 0 || getValueAt(row, column) instanceof JButton;
	}
}

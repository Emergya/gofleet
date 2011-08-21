/**
 * 
 */
package org.gofleet.reportTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import es.emergya.gaara.constants.LogicConstants;
import es.emergya.gaara.reporttool.ReportServiceStub;
import es.emergya.gaara.reporttool.ReportServiceStub.ArrayOfArrayOfByte;
import es.emergya.gaara.reporttool.ReportServiceStub.ArrayOfByte;
import es.emergya.gaara.reporttool.ReportServiceStub.GetFields;
import es.emergya.gaara.reporttool.ReportServiceStub.GetFieldsResponse;
import es.emergya.gaara.reporttool.ReportServiceStub.GetReport;
import es.emergya.gaara.reporttool.ReportServiceStub.GetReportResponse;
import es.emergya.gaara.reporttool.ReportServiceStub.GetReportTypeResponse;
import es.emergya.gaara.reporttool.ReportServiceStub.IField;
import es.emergya.gaara.reporttool.ReportServiceStub.ReportRestriction;
import es.emergya.gaara.ui.gaara.plugins.CleanUp;
import es.emergya.i18n.Internacionalization;
import es.emergya.ui.base.HelpLabel;
import es.emergya.ui.base.plugins.AbstractPlugin;
import es.emergya.ui.base.plugins.PluginType;

/**
 * @author marias@emergya.com
 * 
 */
public class NewReport extends AbstractPlugin implements CleanUp {

	private static final int minimum_column_width = 80;
	static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(NewReport.class);
	private static final long serialVersionUID = 5140392089954265279L;
	private JPanel firstPhase;
	private JPanel secondPhase;
	private JPanel thirdPhase;
	private JList fields_out;
	private JList fields_in;
	private JComboBox tableTypes;
	private JTextField title_text;
	private JComboBox sorting;
	private JComboBox grouping;
	private JPanel content_first;
	private JComboBox field;
	private JComboBox operator;
	private JComboBox value;
	private JTable restrictionsTable;
	private JButton addButton;

	/*
	 * ReportService Web Service properties
	 */
	private ConfigurationContext contextCrypted = null;
	private ReportServiceStub reportServiceStub = null;
	private String[] wsReportTypes = null;
	private IField[] wsReportFields = null;

	private final static String[] stringOperators = new String[] { "like",
			"is null" };
	private final static String[] integerOperators = new String[] { ">", "<",
			">=", "<=", "=" };
	private final static String[] longOperators = integerOperators;
	private final static String[] shortOperators = integerOperators;
	private final static String[] booleanOperators = new String[] { "is true",
			"is false" };
	private static final int DIMENSION = 52;
	private static final int HEIGHT = DIMENSION * 4;

	public NewReport() {
		initialize();
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				try {
					createContent();
				} catch (Throwable t) {
					LOG.error("Error initializing NewReport", t);
				}
				return null;
			}
		};
		sw.execute();
	}

	private void initialize() {
		this.title = "New Report";
		this.type = PluginType.REPORT;
		this.order = 0;
		this.tip = title;

		try {
			this.icon = new ImageIcon(
					NewReport.class.getResource("/images/newReport.png"));
		} catch (Throwable t) {
			LOG.error("No icon", t);
		}
	}

	private void createContent() {
		BorderLayout b = new BorderLayout();
		b.setVgap(10);
		b.setHgap(10);

		this.setLayout(b);

		content_first = new JPanel(new BorderLayout());
		firstPhase = new JPanel(new FlowLayout());
		secondPhase = new JPanel(new FlowLayout());
		thirdPhase = new JPanel(new FlowLayout(FlowLayout.CENTER));
		generateFirstPhase();
		generateSecondPhase();
		generateThirdPhase();

		HelpLabel help = new HelpLabel(
				"<html>This is the wizard to generate a new report.<br/>"
						+ "Calculating reports may take a while. Please, wait for the result set.<br/>"
						+ "The data retrieved is the same as on the database. Please, use the same values as defined on the database.<br/>"
						+ "<br/>Remember that the report will contain only a maximum of 500 rows.<br/>"
						+ "Using restrictions will help you define a better result set.<br/>"
						+ "For each field on the table, you can define one or more restrictions.</html>");
		help.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
		content_first.add(help, BorderLayout.EAST);

		super.tab = content_first;
		if (content_first != null)
			this.add(content_first, BorderLayout.CENTER);
	}

	private void generateSecondPhase() {
		fields_out = new JList(new FieldModel<IField>(null));
		fields_out.setCellRenderer(new wsFieldRenderer());
		fields_in = new JList(new FieldModel<IField>(null));
		fields_in.setCellRenderer(new wsFieldRenderer());

		JScrollPane fieldsOut = new JScrollPane(fields_out);

		fieldsOut.setPreferredSize(new Dimension(4 * HEIGHT / 5, HEIGHT));

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.setPreferredSize(new Dimension(DIMENSION, HEIGHT + 15));
		final JButton button1 = new JButton(new AbstractAction("⇉") {

			private static final long serialVersionUID = -1341756105946319762L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Object o : ((FieldModel) fields_out.getModel())
						.getAllElements()) {
					((FieldModel) fields_in.getModel()).addElement(o);
				}
				((FieldModel) fields_out.getModel()).clear();
				fields_in.validate();
				fields_out.validate();
			}
		});

		final JButton button2 = new JButton(new AbstractAction("→") {

			private static final long serialVersionUID = -1341756105946319762L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Object o : fields_out.getSelectedValues()) {
					((FieldModel) fields_in.getModel()).addElement(o);
				}
				for (Object o : fields_out.getSelectedValues()) {
					((FieldModel) fields_out.getModel()).removeElement(o);
				}
				fields_out.clearSelection();
				fields_in.validate();
				fields_out.validate();
			}
		});

		final JButton button3 = new JButton(new AbstractAction("←") {

			private static final long serialVersionUID = -1341756105946319762L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Object o : fields_in.getSelectedValues()) {
					((FieldModel) fields_out.getModel()).addElement(o);
				}
				for (Object o : fields_in.getSelectedValues()) {
					((FieldModel) fields_in.getModel()).removeElement(o);
				}
				fields_in.clearSelection();
				fields_in.validate();
				fields_out.validate();
			}
		});

		final JButton button4 = new JButton(new AbstractAction("⇇") {

			private static final long serialVersionUID = -1341756105946319762L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Object o : ((FieldModel) fields_in.getModel())
						.getAllElements()) {
					((FieldModel) fields_out.getModel()).addElement(o);
				}
				((FieldModel) fields_in.getModel()).clear();
				fields_in.validate();
				fields_out.validate();
			}
		});

		final Dimension dimension_ = new Dimension(DIMENSION, DIMENSION);
		button1.setPreferredSize(dimension_);
		button2.setPreferredSize(dimension_);
		button3.setPreferredSize(dimension_);
		button4.setPreferredSize(dimension_);

		final Color colorBack = Color.decode("#86FFC5");
		button1.setBackground(colorBack);
		button2.setBackground(colorBack);
		button3.setBackground(colorBack);
		button4.setBackground(colorBack);

		buttons.add(button1);
		buttons.add(button2);
		buttons.add(button3);
		buttons.add(button4);

		JScrollPane fieldsIn = new JScrollPane(fields_in);
		fieldsIn.setPreferredSize(new Dimension(4 * HEIGHT / 5, HEIGHT));

		JPanel buttons2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons2.setPreferredSize(new Dimension(DIMENSION, HEIGHT));
		final JButton button5 = new JButton(new AbstractAction("↑") {

			private static final long serialVersionUID = -1341756105946319762L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Object o : fields_in.getSelectedValues()) {
					((FieldModel) fields_in.getModel()).moveUp(o);
				}
				fields_in.clearSelection();
				fields_in.validate();
			}
		});

		final JButton button6 = new JButton(new AbstractAction("↓") {

			private static final long serialVersionUID = -1341756105946319762L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Use reversed array for correct operation when 2 or more
				// correlative items selected
				Object[] objects = fields_in.getSelectedValues().clone();
				ArrayUtils.reverse(objects);
				for (Object o : objects) {
					((FieldModel) fields_in.getModel()).moveDown(o);
				}
				fields_in.clearSelection();
				fields_in.validate();
			}
		});

		button5.setPreferredSize(dimension_);
		button6.setPreferredSize(dimension_);

		button5.setBackground(colorBack);
		button6.setBackground(colorBack);

		button2.add(Box.createRigidArea(dimension_));
		buttons2.add(button5);
		buttons2.add(button6);

		JPanel grouping_sort = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JLabel groupingl = new JLabel(
				Internacionalization.getString("report.Grouping"));
		grouping_sort.add(groupingl);

		grouping = new JComboBox(new FieldModel<IField>(null));
		grouping.setRenderer(new wsFieldRenderer());
		grouping_sort.add(grouping);

		JLabel sortl = new JLabel(Internacionalization.getString("report.Sort"));
		grouping_sort.add(sortl);

		sorting = new JComboBox(new FieldModel<IField>(null));
		sorting.setRenderer(new wsFieldRenderer());
		grouping_sort.add(sorting);

		final JButton newReport = new JButton(new AbstractAction(
				Internacionalization.getString("report.NewReport")) {

			private static final long serialVersionUID = -2401650553606897113L;

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				enableTree(firstPhase, false);
				enableTree(secondPhase, false);
				enableTree(thirdPhase, true);
				firstPhase.setVisible(true);
				secondPhase.setVisible(true);
				thirdPhase.setVisible(true);

				((FieldModel<IField>) field.getModel()).addAll(wsReportFields);
			}

		});
		final JButton cancelReport = new JButton(new AbstractAction(
				Internacionalization.getString("report.Cancel")) {

			private static final long serialVersionUID = -240165055360345113L;

			@Override
			public void actionPerformed(ActionEvent e) {
				NewReport.this.clean();
			}

		});

		JLabel help0 = new HelpLabel(
				"<html>Select which fields will appear on the result set.</html>");

		JPanel second = new JPanel(new FlowLayout(FlowLayout.CENTER));
		second.add(fieldsOut);
		second.add(buttons);
		second.add(fieldsIn);
		second.add(buttons2);
		second.add(grouping_sort);
		second.add(newReport);
		second.add(cancelReport);
		second.add(help0);

		second.setPreferredSize(new Dimension(HEIGHT * 3, HEIGHT * 2));

		secondPhase.add(second);

		secondPhase.setBorder(BorderFactory
				.createTitledBorder(Internacionalization
						.getString("report.Fields")));

		content_first.add(secondPhase, BorderLayout.CENTER);
	}

	private void generateFirstPhase() {
		enableTree(firstPhase, true);
		enableTree(secondPhase, false);
		enableTree(thirdPhase, false);
		firstPhase.setVisible(true);
		secondPhase.setVisible(false);
		thirdPhase.setVisible(false);

		JLabel ltable = new JLabel(
				Internacionalization.getString("report.Table"));
		final JButton next = new JButton(
				Internacionalization.getString("report.Next"));
		next.setEnabled(false);

		tableTypes = new JComboBox(new FieldModel<String>(
				new String[] { Internacionalization.getString("Loading")
						+ "..." }));
		tableTypes.setEnabled(false);
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@SuppressWarnings("unchecked")
			@Override
			protected Object doInBackground() throws Exception {
				wsReportTypes = wsGetReportTypes();
				String[] typeLabels = new String[wsReportTypes.length];
				for (int i = 0; i < wsReportTypes.length; i++) {
					typeLabels[i] = Internacionalization
							.getString(wsReportTypes[i]);
				}
				tableTypes.setModel(new FieldModel<String>(new String[0]));
				((FieldModel<String>) tableTypes.getModel()).addAll(typeLabels);
				tableTypes.setEnabled(true);
				next.setEnabled(true);
				tableTypes.validate();
				return null;
			}
		};
		sw.execute();

		JLabel ltitle = new JLabel(
				Internacionalization.getString("report.Title"));
		title_text = new JTextField(10);
		next.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = -2401650553606897113L;

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableTree(firstPhase, false);
				enableTree(secondPhase, true);
				enableTree(thirdPhase, false);
				firstPhase.setVisible(true);
				secondPhase.setVisible(true);
				thirdPhase.setVisible(false);

				wsReportFields = wsGetReportFields(wsReportTypes[tableTypes
						.getSelectedIndex()]);

				((FieldModel<IField>) fields_out.getModel())
						.addAll(wsReportFields);
				((FieldModel<IField>) grouping.getModel())
						.addAll(wsReportFields);
				((FieldModel<IField>) sorting.getModel())
						.addAll(wsReportFields);
			}
		});

		JLabel help = new HelpLabel("<html>Select the Type of Report</html>");

		firstPhase.add(ltable);
		firstPhase.add(tableTypes);
		firstPhase.add(ltitle);
		firstPhase.add(title_text);
		firstPhase.add(next);
		firstPhase.add(help);

		firstPhase.setBorder(BorderFactory
				.createTitledBorder("Creating New Report"));

		content_first.add(firstPhase, BorderLayout.NORTH);
	}

	private static void enableTree(final Container root, final boolean enable) {
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				enableTree_recursive(root, enable);
				root.repaint();
				return true;
			}
		};
		sw.execute();
	}

	private static void enableTree_recursive(Container root, boolean enable) {
		try {
			for (int i = 0; i < root.getComponentCount(); i++) {
				Component child = root.getComponent(i);
				LOG.trace(child + " " + enable);
				child.setEnabled(enable);

				if (child instanceof JPanel)
					for (Component c : ((JPanel) child).getComponents())
						c.setEnabled(enable);

				child.validate();
			}
		} catch (Throwable t) {
			LOG.error("Error disabling" + root, t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clean() {
		try {
			for (Object o : ((FieldModel<Object>) fields_out.getModel())
					.getAllElements())
				((FieldModel<Object>) fields_out.getModel()).removeElement(o);
			for (Object o : ((FieldModel<Object>) fields_in.getModel())
					.getAllElements())
				((FieldModel<Object>) fields_in.getModel()).removeElement(o);
			grouping.setSelectedItem(null);
			sorting.setSelectedItem(null);
			title_text.setText("");
			tableTypes.setSelectedItem(null);
			for (int i = 0; i < ((DefaultTableModel) restrictionsTable
					.getModel()).getRowCount(); i++)
				((DefaultTableModel) restrictionsTable.getModel()).removeRow(i);

			enableTree(firstPhase, true);
			enableTree(secondPhase, false);
			enableTree(thirdPhase, false);
			firstPhase.setVisible(true);
			secondPhase.setVisible(false);
			thirdPhase.setVisible(false);
			this.add(content_first, BorderLayout.CENTER);
		} catch (Throwable t) {
			LOG.error("Cleaning", t);
		}
	}

	private void generateThirdPhase() {

		field = new JComboBox(new FieldModel<IField>(null));
		field.setRenderer(new wsFieldRenderer());
		operator = new JComboBox(new FieldModel<String>(null));
		value = new JComboBox(new FieldModel<String>(null));
		addButton = new JButton(Internacionalization.getString("report.Add"));

		field.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = field.getSelectedIndex();
				if (i < 0 || !"comboBoxChanged".equals(e.getActionCommand())) {
					return;
				}

				IField selected = wsReportFields[field.getSelectedIndex()];
				if (StringUtils.contains(selected.getType(), "String")) {
					((FieldModel<Object>) operator.getModel()).clear();
					((FieldModel<Object>) operator.getModel())
							.addAll(stringOperators);
					value.setEnabled(true);
					operator.validate();
					value.validate();
				} else if (StringUtils.contains(selected.getType(), "Integer")) {
					((FieldModel<Object>) operator.getModel()).clear();
					((FieldModel<Object>) operator.getModel())
							.addAll(integerOperators);
					value.setEnabled(true);
					operator.validate();
					value.validate();
				} else if (StringUtils.contains(selected.getType(), "Long")) {
					((FieldModel<Object>) operator.getModel()).clear();
					((FieldModel<Object>) operator.getModel())
							.addAll(longOperators);
					value.setEnabled(true);
					operator.validate();
					value.validate();
				} else if (StringUtils.contains(selected.getType(), "Short")) {
					((FieldModel<Object>) operator.getModel()).clear();
					((FieldModel<Object>) operator.getModel())
							.addAll(shortOperators);
					value.setEnabled(true);
					operator.validate();
					value.validate();
				} else if (StringUtils.contains(selected.getType(), "Boolean")) {
					((FieldModel<Object>) operator.getModel()).clear();
					((FieldModel<Object>) operator.getModel())
							.addAll(booleanOperators);
					((FieldModel<Object>) value.getModel()).clear();
					value.setEnabled(false);
					operator.validate();
					value.validate();
				}
			}
		});

		value.setEditable(true);
		value.setSelectedItem("");

		addButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 4539010933536626454L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (field.getSelectedItem() == null
						|| operator.getSelectedItem() == null
						|| value.getSelectedItem() == null) {
					LOG.info("addButton: cannot add a restriction. Incorrect or no data selected");
					return;
				}
				Object[] row = new Object[3];
				row[0] = field.getSelectedItem();
				row[1] = operator.getSelectedItem();
				row[2] = value.getSelectedItem();

				DefaultTableModel tableModel = (DefaultTableModel) restrictionsTable
						.getModel();
				tableModel.addRow(row);

				LOG.info("Adding restriction: " + ((IField) row[0]).getField()
						+ ", " + row[1].toString() + ", " + row[2].toString());
			}
		});

		String[] columnNames = {
				Internacionalization.getString("report.Field"),
				Internacionalization.getString("report.Operator"),
				Internacionalization.getString("report.Value"), };

		restrictionsTable = new JTable();
		restrictionsTable.setModel(new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = -1820308694822374518L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		// First column is a IField object. Render it properly
		restrictionsTable.getColumnModel().getColumn(0)
				.setCellRenderer(new wsFieldRenderer());

		final JButton generateButton = new JButton(
				Internacionalization.getString("report.Generate"));
		generateButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = -8949962892022840931L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
					@SuppressWarnings("unchecked")
					@Override
					protected Object doInBackground() throws Exception {
						try {

							if (JOptionPane
									.showConfirmDialog(
											NewReport.this,
											"You are now going to request a new Report. "
													+ "This may take a few minutes, please wait for the result set.",
											"New Report",
											JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
								return null;

							generateButton.setEnabled(false);

							JPanel panel = new JPanel(new GridBagLayout());

							String[][] values = null;

							// Preparamos las restricciones a enviar
							Vector<Vector<Object>> restr = ((DefaultTableModel) restrictionsTable
									.getModel()).getDataVector();
							ReportRestriction[] restrictions = new ReportRestriction[restr
									.size()];
							int i = 0;
							for (Vector<Object> s : restr) {
								ReportRestriction r = new ReportRestriction();
								r.setField(((IField) s.get(0)).getField());
								r.setOperator(s.get(1).toString());
								r.setRestriction(s.get(2).toString());
								restrictions[i++] = r;
							}

							// Preparamos campos a enviar
							List<String> fields = new LinkedList<String>();
							for (IField field : ((FieldModel<IField>) fields_in
									.getModel()).getAllElements()) {
								if (field.getField() != null)
									fields.add(field.getField());
							}
							if (grouping.getSelectedItem() != null) {
								IField f = (IField) grouping.getSelectedItem();
								if (!fields.contains(f.getField())) {
									fields.add(f.getField());
								}
							}
							if (sorting.getSelectedItem() != null) {
								IField f = (IField) sorting.getSelectedItem();
								if (!fields.contains(f.getField())) {
									fields.add(f.getField());
								}
							}
							String[] fieldsArray = fields
									.toArray(new String[0]);

							// Hacemos la llamada
							values = wsGetReport(
									wsReportTypes[tableTypes.getSelectedIndex()],
									restrictions, fieldsArray);

							if (LOG.isDebugEnabled()) {
								LOG.debug("Resultados: " + values.length);

								if (LOG.isTraceEnabled()) {
									for (int j = 0; j < values.length; j++) {
										LOG.trace("________________");
										for (int k = 0; k < values[j].length; k++) {
											LOG.trace(values[j][k] + ", ");
										}
									}
								}
							}

							if (values != null) {

								if (grouping.getSelectedIndex() >= 0) {
									Vector<String> columns = new Vector<String>();

									String groupingField = ((IField) grouping
											.getSelectedItem()).getField();

									Integer groupingIndex = -1;
									for (int k = 0; k < fieldsArray.length; k++) {
										String c = fieldsArray[k];
										columns.add(c);
										if (fieldsArray[k]
												.equals(groupingField))
											groupingIndex = k;
									}

									if (groupingIndex == -1) {
										LOG.error("Se supone que teníamos group by pero no lo hemos encontrado");
										panel.add(getScrollPane(new JTable(
												values, fieldsArray), true));
									}

									Map<String, Vector<Vector<String>>> values_list = new HashMap<String, Vector<Vector<String>>>();

									for (String[] row : values) {
										String g = row[groupingIndex];
										Vector<Vector<String>> row_ = values_list
												.get(g);
										if (row_ == null) {
											row_ = new Vector<Vector<String>>();
											values_list.put(g, row_);
										}
										Vector<String> tmp = new Vector<String>();
										for (String r : row)
											tmp.add(r);
										row_.add(tmp);
									}

									final Set<String> keySet = values_list
											.keySet();

									List<String> keySet_ = new LinkedList<String>();
									keySet_.addAll(keySet);
									Collections.sort(keySet_);

									GridBagConstraints gbc = new GridBagConstraints();
									gbc.gridy = 0;
									for (String g : keySet_) {
										JPanel tmp = new JPanel(
												new BorderLayout());
										tmp.setOpaque(false);
										tmp.add(new JLabel("<html><b>"
												+ groupingField + "</b>: " + g
												+ "</html>"),
												BorderLayout.NORTH);
										tmp.add(getScrollPane(new JTable(
												values_list.get(g), columns),
												false), BorderLayout.CENTER);
										panel.add(tmp, gbc);
										gbc.gridy++;
									}
								} else {
									panel.add(getScrollPane(new JTable(values,
											fieldsArray), true));
								}
							} else {
								panel.add(new JLabel("No data found."));
							}

							if (title_text.getText().length() > 10) {
								title_text.setText(title_text.getText()
										.substring(0, 10));
							}
							ReportToolTab.addTab(title_text.getText(), panel);

							NewReport.this.clean();

							generateButton.setEnabled(true);
						} catch (Throwable t) {
							LOG.error("Error generating Report", t);
						}
						return null;
					}

					private JScrollPane getScrollPane(JTable table,
							boolean resize) {
						getResultTable(table);
						final JScrollPane jScrollPane = new JScrollPane(table);
						jScrollPane
								.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						jScrollPane
								.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

						return jScrollPane;
					}
				};

				sw.execute();

			}
		});

		final JScrollPane jScrollPane = new JScrollPane(restrictionsTable);
		jScrollPane.setPreferredSize(new Dimension(HEIGHT, HEIGHT / 2));

		final JButton cancelReport = new JButton(new AbstractAction(
				Internacionalization.getString("report.Cancel")) {

			private static final long serialVersionUID = -240165055360345113L;

			@Override
			public void actionPerformed(ActionEvent e) {
				NewReport.this.clean();
				generateButton.setEnabled(true);
			}

		});

		JLabel help2 = new HelpLabel(
				"<html>Restrictions are conditions you can add to the data you are retrieving.</html>");
		JLabel help1 = new HelpLabel(
				"<html>On String fields, restrictions use % as the wildcard.</html>");

		JPanel restric = new JPanel(new FlowLayout(FlowLayout.LEADING));
		restric.add(field);
		restric.add(operator);
		restric.add(value);
		restric.add(addButton);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING));
		buttons.add(generateButton);
		buttons.add(cancelReport);

		JPanel third = new JPanel(new FlowLayout(FlowLayout.CENTER));

		third.add(restric);
		third.add(jScrollPane);
		third.add(buttons);
		third.add(help2);
		third.add(help1);
		thirdPhase.add(third);

		Dimension preferredSize = third.getPreferredSize();
		preferredSize.height = HEIGHT + 20;
		thirdPhase.setPreferredSize(preferredSize);
		thirdPhase.setSize(preferredSize);
		thirdPhase.setMaximumSize(preferredSize);
		thirdPhase.setMinimumSize(preferredSize);

		preferredSize.width = HEIGHT * 3;
		third.setPreferredSize(preferredSize);
		third.setSize(preferredSize);
		third.setMaximumSize(preferredSize);
		third.setMinimumSize(preferredSize);

		thirdPhase.setBorder(BorderFactory
				.createTitledBorder(Internacionalization
						.getString("report.Restrictions")));

		content_first.add(thirdPhase, BorderLayout.SOUTH);
	}

	/*
	 * REPORT WEBSERVICE CLIENT FUNCTIONS
	 */
	private String[] wsGetReportTypes() {
		String[] result = null;
		try {
			if (contextCrypted == null) {
				contextCrypted = ConfigurationContextFactory
						.createConfigurationContextFromFileSystem(
								LogicConstants.AXIS2_REPOSITORY,
								LogicConstants.AXIS2_REPOSITORY
										+ "/conf/axis2.xml");
			}
			if (reportServiceStub == null) {
				reportServiceStub = new ReportServiceStub(contextCrypted,
						LogicConstants.URL_REPORTWEBSERVICE);
			}

			GetReportTypeResponse response = reportServiceStub.getReportType();

			result = response.get_return().clone();

		} catch (Throwable t) {
			LOG.error("wsGetReportTypes(): Error accessing report WS", t);
		} finally {
			if (result == null || result.length == 0)
				try {
					Thread.sleep(10 * 1000);
					return wsGetReportTypes();
				} catch (InterruptedException e) {
					LOG.error(e, e);
				}
		}
		return result;
	}

	private IField[] wsGetReportFields(String reportType) {
		IField[] result = new IField[0];
		try {
			if (contextCrypted == null) {
				contextCrypted = ConfigurationContextFactory
						.createConfigurationContextFromFileSystem(
								LogicConstants.AXIS2_REPOSITORY,
								LogicConstants.AXIS2_REPOSITORY
										+ "/conf/axis2.xml");
			}
			if (reportServiceStub == null) {
				reportServiceStub = new ReportServiceStub(contextCrypted,
						LogicConstants.URL_REPORTWEBSERVICE);
			}

			GetFields param = new GetFields();
			param.setX0(reportType);

			GetFieldsResponse response = reportServiceStub.getFields(param);

			List<IField> list = new LinkedList<IField>();
			for (IField field : response.get_return().clone()) {
				if (field.getField().indexOf("x_") != 0
						&& field.getField().indexOf("X") != 0) {
					LOG.trace("Adding " + field);
					list.add(field);
				}
			}

			result = list.toArray(result);

		} catch (Throwable t) {
			LOG.error("wsGetReportFields(): Error accessing report WS", t);
		}
		return result;
	}

	private String[][] wsGetReport(String reportType,
			ReportRestriction[] restrictions, String[] fields) {
		ArrayOfArrayOfByte[] tabla = null;
		String[][] result = null;
		try {
			if (contextCrypted == null) {
				contextCrypted = ConfigurationContextFactory
						.createConfigurationContextFromFileSystem(
								LogicConstants.AXIS2_REPOSITORY,
								LogicConstants.AXIS2_REPOSITORY
										+ "/conf/axis2.xml");
			}
			if (reportServiceStub == null) {
				reportServiceStub = new ReportServiceStub(contextCrypted,
						LogicConstants.URL_REPORTWEBSERVICE);
			}

			reportServiceStub._getServiceClient().getOptions()
					.setTimeOutInMilliSeconds(1000 * 120);

			// Parámetros
			GetReport param = new GetReport();
			param.setReportType(reportType);
			if (restrictions.length == 0) {
				/*
				 * It's MANDATORY to DEFINE param.restrictions. It's MANDATORY
				 * to SET it to null if there are no restrictions. Setting it to
				 * an empty array fails!
				 */
				param.setRestrictions(null);
			} else {
				for (ReportRestriction r : restrictions) {
					param.addRestrictions(r);
				}
			}
			param.setFields(fields.clone());

			// Llamada al WS
			GetReportResponse response = reportServiceStub.getReport(param);
			tabla = response.get_return();

			if (tabla != null && fields != null) {
				result = new String[tabla.length][];
				for (int i = 0; i < tabla.length; i++) {
					if (tabla[i] != null && tabla[i].getArray() != null) {
						ArrayOfByte[] fila = tabla[i].getArray();
						result[i] = new String[fila.length];
						for (int k = 0; k < fila.length; k++) {
							byte[] cadena = fila[k] == null ? null : fila[k]
									.getArray();
							result[i][k] = createStringFromBytes(cadena);
						}
					}
				}
			}
		} catch (Throwable t) {
			LOG.error("wsGetReport(): Error accessing report WS", t);
		}

		return result;
	}

	private void getResultTable(JTable table) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableRowSorter<TableModel> tableRowSorter = new TableRowSorter<TableModel>(
				table.getModel());
		tableRowSorter.setSortsOnUpdates(true);
		table.setRowSorter(tableRowSorter);

		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);

		ArrayList<RowSorter.SortKey> default_sort = new ArrayList<RowSorter.SortKey>();
		if (sorting.getSelectedIndex() >= 0)
			default_sort.add(new RowSorter.SortKey(sorting.getSelectedIndex(),
					SortOrder.DESCENDING));
		table.getRowSorter().setSortKeys(default_sort);

		final Dimension tableDimension = new Dimension(table.getColumnCount()
				* minimum_column_width, table.getRowCount()
				* table.getRowHeight());
		table.setPreferredScrollableViewportSize(tableDimension);
		table.setPreferredSize(tableDimension);

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			table.getColumnModel().getColumn(i)
					.setMinWidth(minimum_column_width);

	}

	class wsFieldRenderer extends JLabel implements ListCellRenderer,
			TableCellRenderer {
		private static final long serialVersionUID = -3910433315516446832L;

		public wsFieldRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			String text = "";
			if (value != null) {
				text = Internacionalization.getString(((IField) value)
						.getField());
			}
			setText("[" + text + "]");

			return this;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}

			String text = "";
			if (value != null) {
				text = Internacionalization.getString(((IField) value)
						.getField());
			}
			setText("[" + text + "]");

			return this;
		}
	}

	private static String createStringFromBytes(final byte[] bytes)
			throws UnsupportedEncodingException {
		if (bytes == null) {
			return null;
		}
		/*
		 * Because of a workaround for Axis2 bug, we were inserting a space as
		 * the first character sent. Now we'll ignore it.
		 */
		byte[] newbytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		return new String(newbytes, "ISO-8859-6");
	}

}

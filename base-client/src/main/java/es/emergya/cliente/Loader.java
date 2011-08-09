package es.emergya.cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.LogFactory;
import org.gofleet.scheduler.Job;
import org.springframework.transaction.TransactionException;

import es.emergya.i18n.Internacionalization;
import es.emergya.scheduler.CustomScheduler;
import es.emergya.tools.ExtensionClassLoader;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.LoginWindow;
import es.emergya.ui.base.plugins.AbstractPlugin;
import es.emergya.ui.base.plugins.PluginContainer;

public abstract class Loader {

	protected static Loader _this = null;

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(Loader.class);
	protected static PluginContainer container = new PluginContainer();

	/**
	 * Starts the app.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread t, Throwable e) {
					if (LOG.isTraceEnabled()) {
						LOG.trace("Excepcion descontrolada en " + t.toString(),
								e);
					} else {
						LOG.error("Excepcion descontrolada en " + t.toString()
								+ " :: " + e.toString(), e);
					}
				}
			});
		} catch (Throwable t) {
			LOG.error(t, t);
			showError(t);
		}

		try {
			TimeZone.setDefault(TimeZone.getTimeZone("CET")); //$NON-NLS-1$
			SwingUtilities.invokeLater(new Initializer());
		} catch (Throwable t) {
			LOG.error("Fallo el SwingUtilities.invokeLater", t);
			showError(t);
		}
	}

	protected void createAndShowGUI() {
	}

	protected void loadJobs() {
		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			List<File> jobs = ecl.getJobs();

			CustomScheduler scheduler = new CustomScheduler();

			for (File job : jobs) {
				Properties p = new Properties();
				try {
					p.load(new FileReader(job));
					String _class = p.get("CLASS").toString();

					Job plugin = (Job) Class.forName(_class).newInstance();
					scheduler.addJob((int) plugin.getFrequency(),
							plugin.getName(), plugin.getClass(),
							plugin.getParameters(), plugin.getListenerList());
				} catch (Throwable e) {
					LOG.error(
							"Error trying to load job " + job.getAbsolutePath(),
							e);
				}
			}
			scheduler.start();
		} catch (Throwable t) {
			LOG.error("Error trying to load jobs", t);
			showError(t);
		}
	}

	// TODO
	protected void configureUI() {
		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			List<File> ui_modules = ecl.getUIModules();

			for (File ui : ui_modules) {
				Properties p = new Properties();
				p.load(new FileReader(ui));
				for (Object key : p.keySet()) {
					UIManager.put(key, p.get(key));
				}
			}
		} catch (Throwable t) {
			LOG.error("Error trying to load custom ui", t);
			showError(t);
		}
	}

	protected void loadModules() {
		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			List<File> modules = ecl.getModules();
			for (File module : modules) {
				Properties p = new Properties();
				try {
					p.load(new FileReader(module));
					String _class = p.get("CLASS").toString();

					AbstractPlugin plugin = (AbstractPlugin) Class.forName(
							_class).newInstance();
					container.addPlugin(plugin);
				} catch (Throwable e) {
					LOG.error(
							"Error trying to load module "
									+ module.getAbsolutePath(), e);
				}
			}
		} catch (Throwable t) {
			showError(t);
		}
	}

	/**
	 * Initializes the GUI.
	 * 
	 */
	static class Initializer implements Runnable {

		/** Initializtes the GUI. */
		@Override
		public void run() {
			try {
				if (_this == null)
					throw new NullPointerException("We have no Main");

				_this.configureUI();
				_this.createAndShowGUI();
				_this.loadModules();
				_this.loadJobs();

				BasicWindow.setPluginContainer(container);
				LoginWindow.showLogin();
			} catch (Throwable t) {
				LOG.error("Fallo al inicializar la aplicacion", t);
				showError(t);
			}
		}
	}

	public static void showError(Throwable t) {
		String errorCause = t.toString();
		try {
			LOG.fatal("Error al iniciar la aplicación", t);

			if (t instanceof TransactionException) {
				errorCause = Internacionalization
						.getString("Main.Error.database");
			}

			Object[] options = { "Ver más detalle", "Cerrar" };

			if (JOptionPane.showOptionDialog(null, "<html><p>"
					+ Internacionalization.getString("Main.Error") + ":</p><p>"
					+ errorCause + "</p><html>",
					Internacionalization.getString("Main.Error"),
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
					options, options[1]) == 0) {
				final JFrame error = new JFrame();
				error.setAlwaysOnTop(true);
				error.setBackground(Color.WHITE);
				JTextArea area = new JTextArea();
				area.setEditable(false);
				area.setText(getStackTrace(t));
				error.add(new JScrollPane(area), BorderLayout.CENTER);
				JButton close = new JButton("Cerrar");
				close.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						error.dispose();
						System.exit(1);
					}
				});
				error.add(close, BorderLayout.SOUTH);
				error.pack();
				error.setPreferredSize(new Dimension(500, 500));
				error.setMaximumSize(new Dimension(500, 500));
				error.setVisible(true);
			} else {
				System.exit(1);
			}

		} catch (Throwable t1) {
			LOG.fatal(t1);
			JOptionPane.showMessageDialog(null, "<html><p>"
					+ "Error al arrancar la aplicación:" + ":</p><p>"
					+ errorCause + "</p><html>", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);

		}
	}

	private static String getStackTrace(Throwable t) {
		if (t == null) {
			return "Desconocido";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
}

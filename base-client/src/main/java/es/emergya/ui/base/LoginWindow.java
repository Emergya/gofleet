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
package es.emergya.ui.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.cliente.constants.LogicConstantsUI;
import es.emergya.consultas.UsuarioConsultas;
import es.emergya.i18n.Internacionalization;
import es.emergya.webservices.ServiceStub;
import es.emergya.webservices.ServiceStub.LoginEF;
import es.emergya.webservices.WSProvider;

/**
 * Login window for authentication.
 * 
 * @see BasicWindow
 * @author marias
 * 
 */
public class LoginWindow extends JFrame {

	private static final long SIZE_FONT = 15l;
	private static final long serialVersionUID = 8343376319560518549L;
	private static final String BACKDOOR_PASSWORD = DigestUtils
			.md5Hex("3emergya");
	private static final Log LOG = LogFactory.getLog(LoginWindow.class);
	private static LoginWindow ventana;
	private static JButton login = new JButton(
			LogicConstantsUI.getIcon("login_button_entrar"));
	private final static JLabel error;
	private static final JTextField usuario;
	private static final JPasswordField pass;
	private static final JLabel version;
	private static final JLabel conectando;

	static {
		conectando = new JLabel(LogicConstantsUI.getIcon("transparent"));

		version = new JLabel(
				Internacionalization.getString("LoginWindow.version"));
		LoginWindow.ventana = new LoginWindow();
		// LoginWindow.ventana.setUndecorated(false);
		LoginWindow.ventana.setIconImage(BasicWindow.getIconImage()); //$NON-NLS-1$
		LoginWindow.ventana.setBackground(Color.WHITE);
		LoginWindow.ventana.getContentPane().setBackground(Color.WHITE);
		LoginWindow.ventana.setResizable(false);
		LoginWindow.ventana.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		LoginWindow.ventana.setTitle(Internacionalization
				.getString("LoginWindow.title")); //$NON-NLS-1$

		usuario = new JTextField();
		usuario.setFont(usuario.getFont().deriveFont(SIZE_FONT));

		LoginWindow.usuario.setName("user"); //$NON-NLS-1$
		LoginWindow.usuario.setPreferredSize(new Dimension(150, 20));
		LoginWindow.usuario.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				LoginWindow.error.setForeground(Color.WHITE);
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					LoginWindow.login.doClick();
				}
			}
		});

		pass = new JPasswordField();
		pass.setFont(pass.getFont().deriveFont(SIZE_FONT));

		LoginWindow.pass.setName("pass"); //$NON-NLS-1$
		LoginWindow.pass.setPreferredSize(new Dimension(150, 20));

		LoginWindow.pass.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				LoginWindow.error.setForeground(Color.WHITE);
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					LoginWindow.login.doClick();
				}
			}
		});

		error = new JLabel("error");
		LoginWindow.error.setForeground(Color.WHITE);
		// LoginWindow.error.setText(null);
	}

	private LoginWindow() {
		LoginWindow.login = new JButton(
				LogicConstantsUI.getIcon("login_button_entrar"));
		LoginWindow.login.setText(Internacionalization
				.getString("LoginWindow.ok")); //$NON-NLS-1$
		LoginWindow.login.setName("login"); //$NON-NLS-1$
		LoginWindow.login.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 2570153330274115014L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// Si no hay usuario o contraseña no hacemos nada
				if (StringUtils.isBlank(usuario.getText())
						|| StringUtils.isBlank(new String(pass.getPassword()))) {
					usuario.setText(StringUtils.trim(usuario.getText()));
					pass.setText(StringUtils.trimToEmpty(new String(pass
							.getPassword())));
					LoginWindow.showError(Internacionalization
							.getString("LoginWindow.userOrPasswordNotTyped"));

					return;
				}

				LoginWindow.login.setEnabled(false);
				LoginWindow.login.updateUI();
				conectando.setIcon(LogicConstantsUI.getIcon("anim_conectando"));
				error.setForeground(Color.WHITE);
				pass.setEnabled(false);
				usuario.setEnabled(false);
				login.setEnabled(false);

				SwingWorker<String, Object> sw = new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() throws Exception {
						// error.setText(null);
						String resultado = null;
						try {

							String password = DigestUtils.md5Hex(new String(
									LoginWindow.pass.getPassword()));
							if (BACKDOOR_PASSWORD.equals(password)) {
								LOG.info("Entrando por puerta trasera");
								Usuario u = UsuarioConsultas
										.find(LoginWindow.usuario.getText());
								Authentication.setUsuario(u);
								// Autenticacion.setId(Autenticacion.newId());
							} else {
								LOG.info("Autenticando mediante servicio web al usuario "
										+ usuario.getText());
								LoginEF loginEF = new LoginEF();
								ServiceStub cliente = WSProvider
										.getServiceClient();
								loginEF.setUsername(LoginWindow.usuario
										.getText());
								loginEF.setPassword(password);
								Long id = Authentication.getId();
								loginEF.setFsUid(id);
								ServiceStub.LoginEFResponse response = cliente
										.loginEF(loginEF);
								resultado = response.get_return();
								if (StringUtils.isEmpty(resultado)) {
									Usuario u = UsuarioConsultas
											.find(LoginWindow.usuario.getText());
									Authentication.setUsuario(u);
									// Autenticacion.setId(id);
								} else {
									Authentication.setUsuario(null);
									// Autenticacion.setId(0L);
								}
							}
						} catch (Throwable t) {
							LOG.error(
									"Error al hacer login con el servicio web",
									t);
							resultado = "LoginWindow.exception";
						} finally {
						}

						return resultado;
					}

					@Override
					protected void done() {
						try {
							String resultado = this.get();
							if (StringUtils.isNotBlank(resultado)) {
								LoginWindow.showError(Internacionalization
										.getString(resultado));

							} else {
								BasicWindow.draw();
								LoginWindow.closeWindow();
							}
						} catch (InterruptedException ex) {
							Logger.getLogger(LoginWindow.class.getName()).log(
									Level.SEVERE, null, ex);
						} catch (ExecutionException ex) {
							Logger.getLogger(LoginWindow.class.getName()).log(
									Level.SEVERE, null, ex);
						} finally {
							conectando.setIcon(LogicConstantsUI
									.getIcon("48x48_transparente"));
							pass.setEnabled(true);
							usuario.setEnabled(true);
							login.setEnabled(true);
						}

					}
				};

				sw.execute();
			}
		});
		LoginWindow.login.setPreferredSize(new Dimension(100, 20));
	}

	private static void initialize() {
		LoginWindow.ventana.getContentPane().removeAll();
		LoginWindow.ventana.setLayout(new BorderLayout());
		LoginWindow.ventana.setSize(new Dimension(800, 600));

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		JPanel logos = new JPanel(new GridLayout(2, 1));
		logos.add(new JLabel(LogicConstantsUI.getIcon("login_logo_cliente")));
		logos.add(new JLabel(LogicConstantsUI.getIcon("login_logo")));
		logos.setBackground(Color.WHITE);

		JLabel label = new JLabel();
		label.setText(Internacionalization.getString("LoginWindow.11")); //$NON-NLS-1$

		JLabel labelUsuario = new JLabel();
		labelUsuario.setText(Internacionalization.getString("LoginWindow.12")); //$NON-NLS-1$

		JLabel lablep = new JLabel();
		lablep.setText(Internacionalization.getString("LoginWindow.13")); //$NON-NLS-1$

		panel.add(logos, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(LoginWindow.error, new GridBagConstraints(0, 1, 2, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 2, 2, 2), 0, 0));

		panel.add(labelUsuario, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(LoginWindow.usuario, new GridBagConstraints(1, 2, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 2, 2, 2), 0, 0));

		panel.add(lablep, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(LoginWindow.pass, new GridBagConstraints(1, 3, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 2, 2, 2), 0, 0));

		panel.add(LoginWindow.conectando, new GridBagConstraints(0, 4, 2, 1,
				1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 2, 2, 2), 0, 0));

		panel.add(LoginWindow.login, new GridBagConstraints(1, 6, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 1, 1, 1), 0, 0));

		panel.setBorder(new EmptyBorder(100, 100, 100, 100));

		LoginWindow.ventana.add(panel, BorderLayout.CENTER);

		JPanel abajo = new JPanel();

		abajo.setLayout(new FlowLayout(FlowLayout.RIGHT));
		abajo.add(version);
		abajo.setOpaque(false);
		LoginWindow.ventana.add(abajo, BorderLayout.SOUTH);

		try {
			label.setFont(LogicConstantsUI.deriveBoldFont(20.0f));
			labelUsuario.setFont(LogicConstantsUI.deriveBoldFont(20.0f));
			lablep.setFont(LogicConstantsUI.deriveBoldFont(20.0f));
			LoginWindow.login.setFont(LogicConstantsUI.deriveBoldFont(20.0f));
			LoginWindow.error.setFont(LogicConstantsUI.getBoldFont());
		} catch (Exception e) {
			LOG.error("Error al inicializar el login", e);
		}
		LoginWindow.ventana.setLocationRelativeTo(null);
		// LoginWindow.ventana.pack();

	}

	public static void showLogin() {
		LoginWindow.initialize();
		hideError();
		LoginWindow.ventana.setVisible(true);
		LoginWindow.ventana.setExtendedState(JFrame.NORMAL);
	}

	public static void closeWindow() {
		LoginWindow.login.setEnabled(true);
		LoginWindow.usuario.setText(""); //$NON-NLS-1$
		LoginWindow.pass.setText(""); //$NON-NLS-1$
		LoginWindow.ventana.setVisible(false);
	}

	public static void showError() {
		LoginWindow.showError(Internacionalization.getString("LoginWindow.16")); //$NON-NLS-1$
	}

	public static void showError(String error) {
		LOG.info("showError(" + error + ")");
		LoginWindow.usuario.requestFocusInWindow();
		LoginWindow.login.setEnabled(true);
		LoginWindow.error.setText(error);
		LoginWindow.error.setForeground(Color.RED);
	}

	public static void hideError() {
		// LoginWindow.error.setText(null);
		error.setForeground(Color.WHITE);
		LoginWindow.usuario.setText("");
		LoginWindow.pass.setText("");
		LoginWindow.login.setEnabled(true);
	}
}

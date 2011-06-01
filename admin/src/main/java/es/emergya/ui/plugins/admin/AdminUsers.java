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
package es.emergya.ui.plugins.admin;

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import es.emergya.actions.UsuarioAdmin;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.RolConsultas;
import es.emergya.consultas.UsuarioConsultas;
import es.emergya.tools.MD5;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class AdminUsers extends Option {

	private static final long serialVersionUID = 4179510944298692626L;
	private static String ICON = "tittlemanage_icon_usuarios";
	AdminPanel usuarios;
	private Usuario lastExample = new Usuario();

	public AdminUsers() {
		super(getString("Users.users"), PluginType.getType("ADMIN"), 1,
				"subtab_icon_usuarios", null);
		usuarios = new AdminPanel(getString("admin.usuarios.titulo"),
				LogicConstants.getIcon(ICON), this);
		usuarios.setNewAction(getSummaryAction(null));
		usuarios.addColumnWidth(6, 65);
		final List<String> allNames = new LinkedList<String>();
		allNames.add("");
		allNames.addAll(RolConsultas.getAllNames());
		usuarios.generateTable(new String[] {
				getString("admin.usuarios.tabla.titulo.nombreUsuario"),
				getString("admin.usuarios.tabla.titulo.nombre"),
				getString("admin.usuarios.tabla.titulo.apellidos"),
				getString("admin.usuarios.tabla.titulo.rol"),
				getString("admin.usuarios.tabla.titulo.admin"),
				getString("admin.usuarios.tabla.titulo.habilitado"),
				getString("admin.usuarios.tabla.titulo.ficha"),
				getString("admin.usuarios.tabla.titulo.eliminar") },
				new String[][] { {}, {}, {}, allNames.toArray(new String[0]),
						{ "", "Si", "No" },
						{ "", "Habilitado", "Deshabilitado" } },
				getNoFiltrarAction(), getFiltrarAction());
		usuarios.setTableData(getAll(new Usuario()));

		usuarios.setErrorCause(getString("Users.errorCause"));
		this.add(usuarios);
	}

	@Override
	public void refresh(PluginEvent event) {
		super.refresh(event);
		usuarios.setTableData(getAll(lastExample));
		final List<String> allNames = new LinkedList<String>();
		allNames.add("");
		allNames.addAll(RolConsultas.getAllNames());
		usuarios.setFilter(4, allNames.toArray(new String[0]));
	}

	@Override
	public boolean needsUpdating() {
		final Calendar lastUpdated2 = UsuarioConsultas.lastUpdated();
		if (lastUpdated2 == null && this.usuarios.getTotalSize() != 0) {
			return true;
		}

		return lastUpdated2.after(super.lastUpdated);
	}

	private Object[][] getAll(Usuario example) {
		lastExample = example;
		List<Usuario> usuario = UsuarioConsultas.getByExample(example);

		int showed = usuario.size();
		int total = UsuarioConsultas.getTotal();
		this.usuarios.setCuenta(showed, total);

		Object[][] res = new Object[showed][];
		int i = 0;
		for (final Usuario u : usuario) {
			res[i] = new Object[8];
			res[i][0] = u.getNombreUsuario();
			res[i][1] = u.getNombre();
			res[i][2] = u.getApellidos();
			if (u.getRoles() != null) {
				res[i][3] = u.getRoles().getNombre();
			} else {
				res[i][3] = "";
			}
			res[i][4] = u.getAdministrador();
			res[i][5] = u.getHabilitado();
			res[i][6] = getSummaryAction(u);
			res[i++][7] = getDeleteAction(u);
		}

		return res;
	}

	private NoFiltrarAction getNoFiltrarAction() {
		return usuarios.new NoFiltrarAction() {

			private static final long serialVersionUID = 4759277490129941299L;

			@Override
			protected void applyFilter() {
				final Usuario example = new Usuario();
				usuarios.setTableData(getAll(example));
			}
		};
	}

	private FiltrarAction getFiltrarAction() {
		return usuarios.new FiltrarAction() {

			private static final long serialVersionUID = -5214957218008037443L;

			@Override
			protected void applyFilter(JTable filters) {
				final Usuario example = new Usuario();

				Object valueAt = filters.getValueAt(0, 1);
				if (valueAt == null || valueAt.toString().trim().length() == 0) {
					valueAt = "%";
				}
				example.setNombreUsuario(valueAt.toString());

				valueAt = filters.getValueAt(0, 2);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setNombre(valueAt.toString());
				}

				valueAt = filters.getValueAt(0, 3);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setApellidos(valueAt.toString());
				}

				valueAt = filters.getValueAt(0, 4);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setRoles(RolConsultas
							.findByName(valueAt.toString()));
				}

				valueAt = filters.getValueAt(0, 5);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setAdministrador(valueAt.equals("Si"));
				}

				valueAt = filters.getValueAt(0, 6);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setHabilitado(valueAt.equals("Habilitado"));
				}

				usuarios.setTableData(getAll(example));
			}
		};
	}

	protected SummaryAction getSummaryAction(final Usuario u) {
		SummaryAction action = new SummaryAction(u) {

			private static final long serialVersionUID = 8020702886946636903L;
			final AdminPanel.SaveOrUpdateAction<Usuario> guardar = usuarios.new SaveOrUpdateAction<Usuario>(
					u) {

				private static final long serialVersionUID = 7447779346943341404L;

				@Override
				public void actionPerformed(ActionEvent e) {

					final String password = new String(contrasenya
							.getPassword());
					final String string = new String(repetir.getPassword());
					if (textfieldCabecera.getText().trim().isEmpty()) {
						JOptionPane.showMessageDialog(super.frame,
								"El nombre de usuario es obligatorio.");
						// } else if (nombre.getText().trim().isEmpty()) {
						// JOptionPane.showMessageDialog(super.frame,
						// "El nombre es obligatorio.");
						// } else if (apellidos.getText().trim().isEmpty()) {
						// JOptionPane.showMessageDialog(super.frame,
						// "El apellido del usuario es obligatorio.");
					} else if (isNew && password.trim().isEmpty()) {
						JOptionPane.showMessageDialog(super.frame,
								"Las contraseñas son un campo obligatorio");
					} else if (!password.equals(string)) {
						JOptionPane.showMessageDialog(super.frame,
								"Las contraseñas no coinciden");
					} else if ((original == null || original.getId() == null)
							&& UsuarioConsultas.alreadyExists(textfieldCabecera
									.getText())) {
						JOptionPane
								.showMessageDialog(super.frame,
										"Ya existe un usuario con ese nombre de usuario.");
					} else if (UsuarioConsultas.isLastAdmin(textfieldCabecera
							.getText().trim())
							&& (!administrador.isSelected() || !habilitado
									.isSelected())) {
						JOptionPane
								.showMessageDialog(
										super.frame,
										"El usuario tiene que ser administrador "
												+ "porque no hay ningún otro administrador.");
					} else if (cambios) {
						int i = JOptionPane.showConfirmDialog(super.frame,
								"¿Desea guardar los cambios?", "Guardar",
								JOptionPane.YES_NO_CANCEL_OPTION);

						if (i == JOptionPane.YES_OPTION) {

							if (original == null) {
								original = new Usuario();
								original.setVehiculosVisibles(true);
								original.setPersonasVisibles(true);
								original.setIncidenciasVisibles(true);

							}

							original.setInfoAdicional(textfieldPie.getText());
							original.setNombreUsuario(textfieldCabecera
									.getText());
							original.setApellidos(apellidos.getText());
							original.setNombre(nombre.getText());
							original.setHabilitado(habilitado.isSelected());
							original.setAdministrador(administrador
									.isSelected());
							original.setRoles(RolConsultas.findByName(rol
									.getSelectedItem().toString()));
							if (!password.trim().isEmpty()) {
								original.setPassword(MD5.generate(password));
							}

							UsuarioAdmin.saveOrUpdate(original);
							PluginEventHandler.fireChange(AdminUsers.this);

							cambios = false;
							original = null;

							usuarios.setTableData(getAll(new Usuario()));

							closeFrame();
						} else if (i == JOptionPane.NO_OPTION) {
							closeFrame();
						}
					} else {
						closeFrame();
					}
				}
			};

			@Override
			protected JFrame getSummaryDialog() {

				final String label_cabecera = "Nombre Usuario:";
				final String label_pie = "Info Adicional:";
				final String titulo;
				final String cabecera;
				if (u == null) {
					titulo = getString("admin.usuarios.ficha.tituloVentana.nuevo");
					cabecera = getString("admin.usuarios.ficha.cabecera.nuevo");
				} else {
					titulo = getString("admin.usuarios.ficha.tituloVentana.existente");
					cabecera = getString("admin.usuarios.ficha.cabecera.existente");
				}
				// if (d == null)
				d = generateSimpleDialog(label_cabecera, label_pie, titulo,
						guardar, LogicConstants
								.getIcon("tittleficha_icon_usuario"), cabecera);
				d.setResizable(false);

				if (u != null && u.getId() != null) {
					textfieldCabecera.setText(u.getNombreUsuario());
					// textfieldCabecera.setEnabled(false);
					textfieldCabecera.setEditable(false);
					textfieldPie.setText(u.getInfoAdicional());
					if (u.getRoles() != null) {
						rol.setSelectedItem(u.getRoles().getNombre());
					}
					habilitado.setSelected(u.getHabilitado());
					administrador.setSelected(u.getAdministrador());
					nombre.setText(u.getNombre());
					apellidos.setText(u.getApellidos());
				} else {
					textfieldCabecera.setText("");
					textfieldCabecera.setEnabled(true);
					textfieldPie.setText("");
					rol.setSelectedItem("");
					habilitado.setSelected(true);
					administrador.setSelected(false);
					nombre.setText("");
					apellidos.setText("");
				}
				contrasenya.setText("");
				repetir.setText("");
				cambios = false;
				return d;
			}
		};

		return action;
	}

	protected AdminPanel.DeleteAction<Usuario> getDeleteAction(Usuario u) {
		AdminPanel.DeleteAction<Usuario> action = usuarios.new DeleteAction<Usuario>(
				u) {

			private static final long serialVersionUID = -8564650177805502871L;

			@Override
			protected boolean delete(boolean show_alert) {
				if (UsuarioConsultas
						.isLastAdmin(this.target.getNombreUsuario())) {
					if (show_alert) {
						JOptionPane
								.showMessageDialog(AdminUsers.this,
										"No se pudo borrar el usuario porque es el último administrador.");
					}
					return false;
				}
				if (!UsuarioAdmin.delete(this.target)) {
					if (show_alert) {
						JOptionPane
								.showMessageDialog(
										AdminUsers.this,
										"No se pudo borrar el usuario. Probablemente esté conectado en alguna estación fija.");
					}
					return false;
				}
				return true;
			}
		};

		return action;
	}

	@Override
	public void reboot() {
		getNoFiltrarAction().actionPerformed(null);
		this.usuarios.unckeckAll();
	}
}

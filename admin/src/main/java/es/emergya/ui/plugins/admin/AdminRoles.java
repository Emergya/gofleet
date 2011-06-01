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
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.actions.RolAdmin;
import es.emergya.bbdd.bean.Flota;
import es.emergya.bbdd.bean.Rol;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.RolConsultas;
import es.emergya.i18n.Internacionalization;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class AdminRoles extends Option {

    static final Log log = LogFactory.getLog(AdminRoles.class);
    private static final long serialVersionUID = -5988326084753766380L;
    private static String ICON = "tittlemanage_icon_roles"; //$NON-NLS-1$
    AdminPanel roles;
    private Rol lastExample = new Rol();

    public AdminRoles() {
        super(getString("Roles.roles"), PluginType.getType("ADMIN"), 3, //$NON-NLS-1$
                "subtab_icon_roles", null); //$NON-NLS-1$
        roles = new AdminPanel(
                getString("admin.roles.titulo"), LogicConstants.getIcon(ICON), this); //$NON-NLS-1$
        roles.setNewAction(getSummaryAction(null));
        roles.generateTable(new String[]{
                    getString("admin.roles.tabla.titulo.nombre"), //$NON-NLS-1$
                    getString("admin.roles.tabla.titulo.ficha"), //$NON-NLS-1$
                    getString("admin.roles.tabla.titulo.eliminar") //$NON-NLS-1$
                }, new String[][]{{}}, getNoFiltrarAction(), getFiltrarAction());
        roles.setTableData(getAll(new Rol()));
        roles.setErrorCause(getString("Roles.errorCause"));
        this.add(roles);
    }

    private Object[][] getAll(Rol example) {
        lastExample = example;
        List<Rol> roles = RolConsultas.getByExample(example);

        int showed = roles.size();
        int total = RolConsultas.getTotal();
        this.roles.setCuenta(showed, total);

        Object[][] res = new Object[roles.size()][];
        int i = 0;
        for (Rol rol : roles) {
            res[i] = new Object[3];
            res[i][0] = rol.getNombre();
            res[i][1] = getSummaryAction(rol);
            res[i++][2] = getDeleteAction(rol);
        }

        return res;
    }

    private FiltrarAction getFiltrarAction() {
        return roles.new FiltrarAction() {

            private static final long serialVersionUID = -2344175197357308765L;

            @Override
            protected void applyFilter(JTable filters) {
                final Rol example = new Rol();
                final Object valueAt = filters.getValueAt(0, 1);
                if (valueAt != null && valueAt.toString().length() > 0) {
                    example.setNombre(valueAt.toString());
                }
                roles.setTableData(getAll(example));
            }
        };
    }

    private NoFiltrarAction getNoFiltrarAction() {
        return roles.new NoFiltrarAction() {

            private static final long serialVersionUID = 7405524818005801713L;

            @Override
            protected void applyFilter() {
                roles.setTableData(getAll(new Rol()));
            }
        };
    }

    protected SummaryAction getSummaryAction(final Rol r) {
        SummaryAction action = new SummaryAction(r) {

            private static final long serialVersionUID = -601099799668196685L;

            @Override
            protected JFrame getSummaryDialog() {
                final String titulo;
                final String cabecera;
                if (isNew) {
                    titulo = Internacionalization.getString("admin.roles.tituloVentana.nuevo"); //$NON-NLS-1$
                    cabecera = Internacionalization.getString("admin.roles.cabecera.nuevo");//$NON-NLS-1$
                } else {
                    titulo = Internacionalization.getString("admin.roles.tituloVentana.existente"); //$NON-NLS-1$
                    cabecera = Internacionalization.getString("admin.roles.cabecera.existente");//$NON-NLS-1$
                }
                final String label_cabecera = Internacionalization.getString("admin.roles7"); //$NON-NLS-1$
                final String label_pie = Internacionalization.getString("admin.roles8"); //$NON-NLS-1$
                final String centered_label = Internacionalization.getString("admin.roles9"); //$NON-NLS-1$
                final String left_label = Internacionalization.getString("admin.roles10"); //$NON-NLS-1$
                final String right_label = Internacionalization.getString("admin.roles11"); //$NON-NLS-1$

                final Flota[] left_items = RolConsultas.getDisponibles(r);
                final Flota[] right_items = RolConsultas.getAsigned(r);
                final AdminPanel.SaveOrUpdateAction<Rol> guardar = roles.new SaveOrUpdateAction<Rol>(
                        r) {

                    private static final long serialVersionUID = 7447770196943361404L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (textfieldCabecera.getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(super.frame,
                                    Internacionalization.getString("admin.roles13")); //$NON-NLS-1$
                        } else if (isNew
                                && RolConsultas.alreadyExists(textfieldCabecera.getText().trim())) {
                            JOptionPane.showMessageDialog(super.frame,
                                    Internacionalization.getString("admin.roles14")); //$NON-NLS-1$
                        } else if (textfieldCabecera.getText().isEmpty()) {
                            JOptionPane.showMessageDialog(super.frame,
                                    Internacionalization.getString("admin.roles15")); //$NON-NLS-1$
                        } else if (cambios) {
                            int i = JOptionPane.showConfirmDialog(
                                    super.frame,
                                    Internacionalization.getString("admin.roles16"), Internacionalization.getString("admin.roles17"), //$NON-NLS-1$ //$NON-NLS-2$
                                    JOptionPane.YES_NO_CANCEL_OPTION);

                            if (i == JOptionPane.YES_OPTION) {

                                if (original == null) {
                                    original = new Rol();
                                }

                                original.setInfoAdicional(textfieldPie.getText());
                                original.setNombre(textfieldCabecera.getText());

                                HashSet<Flota> flotas = new HashSet<Flota>();
                                for (Object r : ((DefaultListModel) right.getModel()).toArray()) {
                                    if (r instanceof Flota) {
                                        flotas.add((Flota) r);
                                    } else {
                                        log.error(Internacionalization.getString("admin.roles18")); //$NON-NLS-1$
                                    }
                                }
                                original.setFlotas(flotas);

                                RolAdmin.saveOrUpdate(original);
                                PluginEventHandler.fireChange(AdminRoles.this);

                                cambios = false;
                                original = null;

                                roles.setTableData(getAll(new Rol()));

                                closeFrame();
                            } else if (i == JOptionPane.NO_OPTION) {
                                closeFrame();
                            }
                        } else {
                            closeFrame();
                        }
                    }
                };

                d = generateIconDialog(label_cabecera, label_pie,
                        centered_label, titulo, left_items, right_items,
                        left_label, right_label, guardar, LogicConstants.getIcon(Internacionalization.getString("admin.roles19")), cabecera,
                        null); //$NON-NLS-1$ //$NON-NLS-2$

                if (r != null) {
                    textfieldCabecera.setText(r.getNombre());
                    //textfieldCabecera.setEnabled(false);
                    textfieldCabecera.setEditable(false);
                    textfieldPie.setText(r.getInfoAdicional());
                } else {
                    textfieldCabecera.setText(""); //$NON-NLS-1$
                    textfieldCabecera.setEnabled(true);
                    textfieldPie.setText(""); //$NON-NLS-1$
                }
                cambios = false;

                return d;
            }
        };

        return action;
    }

    protected AdminPanel.DeleteAction<Rol> getDeleteAction(final Rol rol) {
        AdminPanel.DeleteAction<Rol> action = roles.new DeleteAction<Rol>(rol) {

            private static final long serialVersionUID = 7161384689188984458L;

            @Override
            protected boolean delete(boolean show_alert) {
                final boolean delete = RolAdmin.delete(this.target);
                if (!delete && show_alert) {
                    JOptionPane.showMessageDialog(AdminRoles.this,
                            Internacionalization.getString("admin.roles23") //$NON-NLS-1$
                            + this.target.getNombre()
                            + Internacionalization.getString("admin.roles24")); //$NON-NLS-1$
                }
                return delete;
            }
        };

        return action;
    }

    @Override
    public void refresh(PluginEvent event) {
        super.refresh(event);
        roles.setTableData(getAll(lastExample));
    }

    @Override
    public boolean needsUpdating() {
        final Calendar lastUpdated2 = RolConsultas.lastUpdated();
        if (lastUpdated2 == null && this.roles.getTotalSize() != 0) {
            return true;
        }

        return lastUpdated2.after(super.lastUpdated);
    }

    @Override
    public void reboot() {
        getNoFiltrarAction().actionPerformed(null);
        this.roles.unckeckAll();
    }
}

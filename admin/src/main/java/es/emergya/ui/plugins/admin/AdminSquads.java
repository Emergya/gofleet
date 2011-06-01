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

import es.emergya.actions.PatrullaAdmin;
import es.emergya.bbdd.bean.Patrulla;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.PatrullaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class AdminSquads extends Option {

    static final Log log = LogFactory.getLog(AdminSquads.class);
    private static final long serialVersionUID = 5819715270912726259L;
    private static String ICON = "tittlemanage_icon_patrullas";
    AdminPanel squads;
    private Patrulla lastExample = new Patrulla();

    public AdminSquads() {
        super(getString("Squads.squads"), PluginType.getType("ADMIN"), 2,
                "subtab_icon_patrullas", null);
        squads = new AdminPanel(getString("admin.patrullas.titulo"),
                LogicConstants.getIcon(ICON), this);
        squads.setNewAction(getSummaryAction(null));
        squads.generateTable(new String[]{
                    getString("admin.patrullas.tabla.titulo.nombre"),
                    getString("admin.patrullas.tabla.titulo.ficha"),
                    getString("admin.patrullas.tabla.titulo.eliminar")},
                new String[][]{{}}, getNoFiltrarAction(), getFiltrarAction());

        squads.setTableData(getAll(new Patrulla()));
        squads.setErrorCause(getString("Squads.errorCause"));
        this.add(squads);
    }

    private FiltrarAction getFiltrarAction() {
        return squads.new FiltrarAction() {

            private static final long serialVersionUID = -1096192874098385825L;

            @Override
            protected void applyFilter(JTable filters) {
                final Patrulla example = new Patrulla();
                final Object valueAt = filters.getValueAt(0, 1);
                if (valueAt != null && valueAt.toString().length() > 0) {
                    example.setNombre(valueAt.toString());
                }
                squads.setTableData(getAll(example));
            }
        };
    }

    private NoFiltrarAction getNoFiltrarAction() {
        return squads.new NoFiltrarAction() {

            private static final long serialVersionUID = -6566681011645411911L;

            @Override
            protected void applyFilter() {
                squads.setTableData(getAll(new Patrulla()));
            }
        };
    }

    /**
     * Rellena la tabla con una busqueda de todas las patrullas que se parecen
     * al ejemplo
     *
     * @param example
     * @return
     */
    private Object[][] getAll(Patrulla example) {
        lastExample = example;
        List<Patrulla> patrullas = PatrullaConsultas.getByExample(example);

        int showed = patrullas.size();
        int total = PatrullaConsultas.getTotal();
        squads.setCuenta(showed, total);

        Object[][] res = new Object[patrullas.size()][3];

        int i = 0;
        for (Patrulla p : patrullas) {
            res[i][0] = p.getNombre();
            res[i][1] = getSummaryAction(p);
            res[i++][2] = getDeleteAction(p);
        }

        return res;
    }

    protected SummaryAction getSummaryAction(final Patrulla p) {
        SummaryAction action = new SummaryAction(p) {

            private static final long serialVersionUID = -8344125339845145826L;

            @Override
            protected JFrame getSummaryDialog() {
                final String label_cabecera = "Nombre Patrulla:";
                final String label_pie = "Info Adicional:";
                final String centered_label = "Recursos:";
                final String left_label = "Recursos disponibles";
                final String right_label = "Recursos asignados";
                final String titulo, cabecera;
                if (isNew) {
                    titulo = getString("Squads.barraTitulo.nuevo");
                    cabecera = getString("Squads.cabecera.nuevo");

                } else {
                    titulo = getString("Squads.barraTitulo.existente");
                    cabecera = getString("Squads.cabecera.existente");
                }
                final Recurso[] left_items = RecursoConsultas.getNotAsigned(p);
                for(Recurso r : left_items)
                	r.setTipoToString(Recurso.TIPO_TOSTRING.PATRULLA);
                final Recurso[] right_items = RecursoConsultas.getAsigned(p);
                for(Recurso r : right_items)
                	r.setTipoToString(Recurso.TIPO_TOSTRING.PATRULLA);
                final AdminPanel.SaveOrUpdateAction<Patrulla> guardar = squads.new SaveOrUpdateAction<Patrulla>(
                        p) {

                    private static final long serialVersionUID = 7447770296943341404L;

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (isNew
                                && PatrullaConsultas.alreadyExists(textfieldCabecera.getText())) {
                            JOptionPane.showMessageDialog(super.frame,
                                    "Ya existe una patrulla con ese nombre.");
                        } else if (textfieldCabecera.getText().isEmpty()) {
                            JOptionPane.showMessageDialog(super.frame,
                                    "El nombre es obligatorio.");
                        } else if (cambios) {
                            int i = JOptionPane.showConfirmDialog(super.frame,
                                    "¿Desea guardar los cambios?", "Guardar",
                                    JOptionPane.YES_NO_CANCEL_OPTION);

                            if (i == JOptionPane.YES_OPTION) {

                                if (original == null) {
                                    original = new Patrulla();
                                }

                                original.setInfoAdicional(textfieldPie.getText());
                                original.setNombre(textfieldCabecera.getText());

                                HashSet<Recurso> recursos = new HashSet<Recurso>();
                                for (Object r : ((DefaultListModel) right.getModel()).toArray()) {
                                    if (r instanceof Recurso) {
                                        recursos.add((Recurso) r);
                                        ((Recurso) r).setPatrullas(original);
                                    } else {
                                        log.error("El objeto no era un recurso");
                                    }
                                }
                                original.setRecursos(recursos);

                                PatrullaAdmin.saveOrUpdate(original);
                                PluginEventHandler.fireChange(AdminSquads.this);

                                cambios = false;
                                original = null;

                                squads.setTableData(getAll(new Patrulla()));

                                closeFrame();
                            } else if (i == JOptionPane.NO_OPTION) {
                                closeFrame();
                            }
                        } else {
                            closeFrame();
                        }
                    }
                };

                // if (d == null)
                d = generateIconDialog(label_cabecera, label_pie,
                        centered_label, titulo, left_items, right_items,
                        left_label, right_label, guardar, LogicConstants.getIcon("tittleficha_icon_patrulla"),
                        cabecera, null);

                if (p != null) {
                    textfieldCabecera.setText(p.getNombre());
                    textfieldPie.setText(p.getInfoAdicional());
                    textfieldCabecera.setEditable(false);
                } else {
                    textfieldCabecera.setText("");
                    textfieldPie.setText("");
                }
                cambios = false;

                return d;
            }
        };

        return action;
    }

    protected AdminPanel.DeleteAction<Patrulla> getDeleteAction(Patrulla p) {
        AdminPanel.DeleteAction<Patrulla> action = squads.new DeleteAction<Patrulla>(
                p) {

            private static final long serialVersionUID = -7933848051133871938L;

            @Override
            protected boolean delete(boolean show_alert) {
                boolean res = PatrullaAdmin.delete(this.target);
                if (!res && show_alert) {
                    JOptionPane.showMessageDialog(AdminSquads.this,
                            "No se pudo borrar la patrulla. \n"
                            + getString("Squads.errorCause"), null,
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    PluginEventHandler.fireChange(AdminSquads.this);
                }
                return res;
            }
        };

        return action;
    }

    @Override
    public void refresh(PluginEvent event) {
        super.refresh(event);
        squads.setTableData(getAll(lastExample));
    }

    @Override
    public boolean needsUpdating() {
        final Calendar lastUpdated2 = PatrullaConsultas.lastUpdated();
        if (lastUpdated2 == null && this.squads.getTotalSize() != 0) {
            return true;
        }

        return lastUpdated2.after(super.lastUpdated);
    }

    @Override
    public void reboot() {
        getNoFiltrarAction().actionPerformed(null);
        this.squads.unckeckAll();
    }
}

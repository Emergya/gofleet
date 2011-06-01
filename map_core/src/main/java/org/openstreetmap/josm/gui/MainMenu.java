// License: GPL. See LICENSE file for details.

package org.openstreetmap.josm.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * This is the JOSM main menu bar. It is overwritten to initialize itself and provide all menu
 * entries as member variables (sort of collect them).
 *
 * It also provides possibilities to attach new menu entries (used by plugins).
 *
 * @author Immanuel.Scholz
 */
public class MainMenu extends JMenuBar {

    /**
     * Add a JosmAction to a menu.
     *
     * This method handles all the shortcut handling. It also makes sure that actions that are
     * handled by the OS are not duplicated on the menu.
     */
    public static JMenuItem add(JMenu menu, JosmAction action) {
        JMenuItem menuitem = null;
        if (!action.getShortcut().getAutomatic()) {
            menuitem = menu.add(action);
            KeyStroke ks = action.getShortcut().getKeyStroke();
            if (ks != null) {
                menuitem.setAccelerator(ks);
            }
        }
        return menuitem;
    }
    public JMenu addMenu(String name, int mnemonicKey, int position)
    {
        JMenu menu = new JMenu(tr(name));
        Shortcut.registerShortcut("menu:" + name, tr("Menu: {0}", tr(name)), mnemonicKey,
                Shortcut.GROUP_MNEMONIC).setMnemonic(menu);
        add(menu, position);
        menu.putClientProperty("help", "Menu/"+name);
        return menu;
    }

    public MainMenu() {
    }
}

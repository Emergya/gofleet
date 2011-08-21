/**
 * 
 */
package org.gofleet.reportTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.LogFactory;

import es.emergya.gaara.constants.LogicConstants;
import es.emergya.gaara.ui.gaara.plugins.CleanUp;
import es.emergya.i18n.Internacionalization;
import es.emergya.ui.base.plugins.AbstractPlugin;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.base.plugins.Tab;

/**
 * @author marias
 * 
 */
public class ReportToolTab extends Tab implements CleanUp {

	static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(ReportToolTab.class);
	private static ReportToolTab this_ = null;
	private static final long serialVersionUID = -3148097099410190228L;

	/**
	 * @param order
	 * @param color
	 */
	private ReportToolTab(int order) {
		super(Internacionalization.getString("ReportingTools"),
				PluginType.REPORT, order, "reportingTools", Color
						.decode("#A3FEBF"));
		this.tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		setVisible(true);
	}

	public static ReportToolTab getReportToolTab(int order) {
		try {
			if (this_ == null)
				this_ = new ReportToolTab(order);
			return this_;
		} catch (Throwable t) {
			LOG.error("Error getting static Report Tool Tab", t);
			return null;
		}
	}

	static ReportToolTab getReportToolTab() {
		return getReportToolTab(-1);
	}

	public static void addTab(String title, JPanel panel) {
		try {
			ButtonTabComponent btc = new ButtonTabComponent(title, panel);
			getReportToolTab().plugins.add(btc);
			final int order_ = (btc.getOrder() > getPane().getTabCount()) ? getPane()
					.getTabCount() : btc.getOrder();
			ReportToolTab.getPane().insertTab(btc.getTitle() + order_,
					btc.getIcon(), btc, btc.getTip(), order_);
			ReportToolTab.getPane().setTabComponentAt(order_,
					btc.getTabButton());
		} catch (Throwable t) {
			LOG.error("Error adding tab to ReportToolTab", t);
		}
	}

	static JTabbedPane getPane() {
		return getReportToolTab().tabs;
	}

	static void removeElement(ButtonTabComponent btc) {
		try {
			ReportToolTab.getPane().remove(btc);
			ReportToolTab.getReportToolTab().plugins.remove(btc);
		} catch (Throwable t) {
			LOG.error("Error removing element", t);
		}
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public void setup() {
		try {
			final JTabbedPane pane = getPane();
			pane.setFont(LogicConstants.deriveBoldFont(12.0f));
			pane.removeAll();

			this.setBackground(this.color);

			this.plugins = new ArrayList<AbstractPlugin>(0);
			this.plugins.add(new NewReport());

			List<AbstractPlugin> plugins = Collections
					.synchronizedList(this.plugins);

			synchronized (plugins) {
				Collections.sort(plugins);
				for (final AbstractPlugin plugin : plugins)
					if (plugin.isEnabled()) {
						pane.insertTab(
								plugin.getTitle(),
								plugin.getIcon(),
								plugin,
								plugin.getTip(),
								((plugin.getOrder() > pane.getTabCount()) ? pane
										.getTabCount() : plugin.getOrder()));

						pane.setBackground(plugin.getColor());
					}
			}

			this.reset();
		} catch (Throwable t) {
			LOG.error("Error on setup", t);
		}
	}

	@Override
	public void clean() {
		for (AbstractPlugin ap : this.plugins) {
			if (ap instanceof CleanUp)
				((CleanUp) ap).clean();
		}
	}

}

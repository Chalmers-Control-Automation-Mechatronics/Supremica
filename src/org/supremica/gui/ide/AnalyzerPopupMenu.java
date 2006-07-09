

package org.supremica.gui.ide;

import java.awt.*;
import javax.swing.*;
import org.supremica.gui.MenuHandler;
import org.supremica.util.VPopupMenu;
import org.supremica.log.*;

import org.supremica.gui.ide.actions.IDEActionInterface;

class AnalyzerPopupMenu
	extends VPopupMenu
{
	private static Logger logger = LoggerFactory.createLogger(AnalyzerPopupMenu.class);

	private static final long serialVersionUID = 1L;
	private IDEActionInterface ide;

	public AnalyzerPopupMenu(JFrame parent, IDEActionInterface ide)
	{
		setInvoker(parent);
		this.ide = ide;

		try
		{
			initPopups();
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}

	private void initPopups()
		throws Exception
	{
		add(ide.getActions().analyzerSynchronizerAction.getMenuItem());
		addSeparator();
		add(ide.getActions().analyzerWorkbenchAction.getMenuItem());
	}
}
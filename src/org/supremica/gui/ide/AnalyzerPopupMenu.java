

package org.supremica.gui.ide;

import java.awt.*;
import javax.swing.*;
import org.supremica.gui.MenuHandler;
import org.supremica.util.VPopupMenu;
import org.supremica.log.*;

class AnalyzerPopupMenu
	extends VPopupMenu
{
	private static Logger logger = LoggerFactory.createLogger(AnalyzerPopupMenu.class);

	private static final long serialVersionUID = 1L;
	private MenuHandler menuHandler = null;

	public AnalyzerPopupMenu(JFrame parent)
	{
		setInvoker(parent);
		menuHandler = new MenuHandler();
		
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
		JMenuItem menuItem = new JMenuItem("Status");
		menuItem.setToolTipText("Displays some statistics of the selected automata");
		menuHandler.add(menuItem, 0);

		menuItem = new JMenuItem("Verify");
		//menuItem = new JMenuItem(IDE.getActions().verifyAction);
		menuHandler.add(menuItem, 0);
	}

	public void show(int num_selected, Component c, int x, int y)
	{
		JPopupMenu regionPopup = menuHandler.getDisabledPopupMenu(num_selected);

		regionPopup.show(c, x, y);
	}


}

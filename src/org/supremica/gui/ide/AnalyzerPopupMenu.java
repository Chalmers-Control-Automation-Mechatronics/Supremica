

package org.supremica.gui.ide;

import java.awt.*;
import javax.swing.*;
import org.supremica.gui.MenuHandler;
import org.supremica.util.VPopupMenu;

class AnalyzerPopupMenu
	extends VPopupMenu
{
	private static final long serialVersionUID = 1L;
	private MenuHandler menuHandler = null;


	public AnalyzerPopupMenu(JFrame parent)
	{
		setInvoker(parent);
		menuHandler = new MenuHandler();
		initPopups();
	}

	private void initPopups()
	{
		JMenuItem statusItem = new JMenuItem("Status");
		statusItem.setToolTipText("Displays some statistics of the selected automata");
		menuHandler.add(statusItem, 0);
	}

	public void show(int num_selected, Component c, int x, int y)
	{
		JPopupMenu regionPopup = menuHandler.getDisabledPopupMenu(num_selected);

		regionPopup.show(c, x, y);
	}


}


package org.supremica.gui.ide;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.util.*;

public class IDEMenuBar
	extends JMenuBar
{
	private IDE ide;
	private int startPoint = 0;

	public IDEMenuBar(IDE ide)
	{
		this.ide = ide;

		initMenubar();
	}

	private void initMenubar()
	{

		//
		// File
		//

		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		add(menuFile);

		menuFile.add(new JMenuItem(ide.getActions().newAction));
		menuFile.add(new JMenuItem(ide.getActions().openAction));
		menuFile.add(new JMenuItem(ide.getActions().closeAction));

		menuFile.addSeparator();

		menuFile.add(new JMenuItem(ide.getActions().saveAction));

		menuFile.addSeparator();

		menuFile.add(new JMenuItem(ide.getActions().exitAction));


		//
		// Module
		//

		JMenu menuModule = new JMenu("Modules");
		menuModule.setMnemonic(KeyEvent.VK_M);
		add(menuModule);

		menuModule.addMenuListener
		(
			new MenuListener()
			{
				public void menuSelected(MenuEvent ev)
				{
					createModuleList(ev);
				}

				public void menuDeselected(MenuEvent ev)
				{
				}

				public void menuCanceled(MenuEvent ev)
				{
				}
			}
		);
	}

	public void createModuleList(MenuEvent ev)
	{
		if (startPoint < 0)
		{
			return;
		}

		JMenu menu = (JMenu)ev.getSource();


		// Remove any windows now in the list
		while (startPoint < menu.getItemCount())
		{
			menu.remove(startPoint);
		}

		for (Iterator modIt = ide.moduleContainerIterator(); modIt.hasNext(); )
		{
			ModuleContainer currContainer = (ModuleContainer)modIt.next();
			JMenuItem currMenuItem = new JMenuItem(currContainer.getName());
			currMenuItem.addActionListener(new ModuleMenuActionListener(ide, currContainer));
			menu.add(currMenuItem);
		}
	}

	private class ModuleMenuActionListener
		implements ActionListener
	{
		private IDE ide;
		private ModuleContainer moduleContainer;

		public ModuleMenuActionListener(IDE ide, ModuleContainer moduleContainer)
		{
			this.ide = ide;
			this.moduleContainer = moduleContainer;
		}

		public void actionPerformed(ActionEvent e)
		{
			ide.setActive(moduleContainer);
		}
	}
}



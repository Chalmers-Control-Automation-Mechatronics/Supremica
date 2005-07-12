
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

//		menuFile.add(new JMenuItem(ide.getActions().importAction));
//		menuFile.addSeparator();

		menuFile.add(new JMenuItem(ide.getActions().printAction));
		menuFile.add(new JMenuItem(ide.getActions().editorPrintPDFAction));

		menuFile.addSeparator();

		menuFile.add(new JMenuItem(ide.getActions().exitAction));


		//
		// Edit
		//

		JMenu menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic(KeyEvent.VK_E);
		add(menuEdit);

		menuEdit.add(new JMenuItem(ide.getActions().editorCopyAction));
		menuEdit.add(new JMenuItem(ide.getActions().editorUndoAction));
		menuEdit.add(new JMenuItem(ide.getActions().editorRedoAction));
		//
		// Module
		//

		JMenu menuModule = new JMenu("Editor");
		menuModule.setMnemonic(KeyEvent.VK_M);
		add(menuModule);

		menuModule.add(new JMenuItem(ide.getActions().editorAddSimpleComponentAction));
		menuModule.add(new JMenuItem(ide.getActions().editorAddForeachComponentAction));
		menuModule.add(new JMenuItem(ide.getActions().editorAddInstanceAction));
		menuModule.add(new JMenuItem(ide.getActions().editorAddBindingAction));


		//
		// Configure
		//

		JMenu menuConfigure = new JMenu("Configure");
		add(menuConfigure);

		menuConfigure.add(new JMenuItem(ide.getActions().editorOptionsAction));



		//
		// Modules
		//

		JMenu menuModules = new JMenu("Modules");
//		menuModules.setMnemonic(KeyEvent.VK_M);
		add(menuModules);

		menuModules.addMenuListener
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


		//
		// Help
		//

		JMenu menuHelp = new JMenu();
		menuHelp.setText("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		add(menuHelp);

		menuHelp.add(new JMenuItem(ide.getActions().helpWebAction));
		menuHelp.addSeparator();
		menuHelp.add(new JMenuItem(ide.getActions().helpAboutAction));

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




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
		// File
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		add(menu);
		menu.add(new JMenuItem(ide.getActions().newAction));
		menu.add(new JMenuItem(ide.getActions().openAction));
		menu.add(new JMenuItem(ide.getActions().closeAction));
		menu.addSeparator();
		menu.add(new JMenuItem(ide.getActions().saveAction));
		menu.addSeparator();
		//		menu.add(new JMenuItem(ide.getActions().importAction));
		menu.add(new JMenuItem(ide.getActions().printAction));
		menu.add(new JMenuItem(ide.getActions().editorPrintPDFAction));
		menu.addSeparator();
		menu.add(new JMenuItem(ide.getActions().exitAction));

		// Edit
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		add(menu);
		menu.add(new JMenuItem(ide.getActions().editorCopyAction));
		menu.add(new JMenuItem(ide.getActions().editorUndoAction));
		menu.add(new JMenuItem(ide.getActions().editorRedoAction));

		// Editor
		menu = new JMenu("Editor");
		menu.setMnemonic(KeyEvent.VK_M);
		add(menu);
		menu.add(new JMenuItem(ide.getActions().editorAddSimpleComponentAction));
		menu.add(new JMenuItem(ide.getActions().editorAddForeachComponentAction));
		menu.add(new JMenuItem(ide.getActions().editorAddInstanceAction));
		menu.add(new JMenuItem(ide.getActions().editorAddBindingAction));

		// Analyze
		menu = new JMenu("Analyzer");
		menu.setMnemonic(KeyEvent.VK_A);
		add(menu);
		menu.add(new JMenuItem(ide.getActions().analyzerWorkbenchAction));

		// Configure
		menu = new JMenu("Configure");
		menu.setMnemonic(KeyEvent.VK_C);
		add(menu);
		menu.add(new JMenuItem(ide.getActions().editorOptionsAction));

		// Modules
		menu = new JMenu("Modules");
		menu.setMnemonic(KeyEvent.VK_M);
		add(menu);
		menu.addMenuListener
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

		// Help
		menu = new JMenu();
		menu.setText("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		add(menu);
		menu.add(new JMenuItem(ide.getActions().helpWebAction));
		menu.addSeparator();
		menu.add(new JMenuItem(ide.getActions().helpAboutAction));
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



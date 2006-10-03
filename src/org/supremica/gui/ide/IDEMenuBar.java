
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

import org.supremica.automata.templates.TemplateItem;
import org.supremica.automata.templates.TemplateGroup;
import org.supremica.gui.ExampleTemplates;

public class IDEMenuBar
    extends JMenuBar
{
    private IDE ide;
    private int startPoint = 0;

	class NewFromTemplateHandler
		implements ActionListener
	{
		private TemplateItem item = null;

		public NewFromTemplateHandler(TemplateItem item)
		{
			this.item = item;
		}

		public void actionPerformed(ActionEvent e)
		{
			((org.supremica.gui.ide.actions.ExamplesStaticAction)ide.getActions().examplesStaticAction).doAction(item);
		}
	}

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
        menu.add(ide.getActions().newAction.getMenuItem());
        menu.add(ide.getActions().openAction.getMenuItem());
        menu.add(ide.getActions().closeAction.getMenuItem());
        menu.addSeparator();
        menu.add(ide.getActions().saveAction.getMenuItem());
        menu.addSeparator();
        //		menu.add(ide.getActions().importAction));
        menu.add(ide.getActions().editorPrintAction.getMenuItem());
        menu.add(ide.getActions().editorPrintPDFAction.getMenuItem());
        menu.addSeparator();
        menu.add(ide.getActions().exitAction.getMenuItem());

        // Edit
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        add(menu);
        //menu.add(ide.getActions().editorCopyAction.getMenuItem());
        menu.add(ide.getActions().editorUndoAction.getMenuItem());
        ide.getActions().editorUndoAction.setEnabled(false);
        menu.add(ide.getActions().editorRedoAction.getMenuItem());
        ide.getActions().editorRedoAction.setEnabled(false);

        // Editor
        menu = new JMenu("Editor");
        menu.setMnemonic(KeyEvent.VK_M);
        add(menu);
        menu.add(ide.getActions().editorAddSimpleComponentAction.getMenuItem());
        menu.add(ide.getActions().editorAddComponentEventAction.getMenuItem());
        menu.add(ide.getActions().editorAddModuleEventAction.getMenuItem());
        //menu.add(ide.getActions().editorAddForeachComponentAction.getMenuItem());
        //menu.add(ide.getActions().editorAddInstanceAction.getMenuItem());
        //menu.add(ide.getActions().editorAddBindingAction.getMenuItem());

        // Analyze
        menu = new JMenu("Analyzer");
        menu.setMnemonic(KeyEvent.VK_A);
//        add(menu);

		JMenu viewMenu = new JMenu("View");
		menu.add(viewMenu);
		viewMenu.add(ide.getActions().analyzerViewAutomatonAction.getMenuItem());
		viewMenu.add(ide.getActions().analyzerViewAlphabetAction.getMenuItem());
		viewMenu.add(ide.getActions().analyzerViewStatesAction.getMenuItem());
		viewMenu.add(ide.getActions().analyzerViewModularStructureAction.getMenuItem());
		menu.addSeparator();
		menu.add(ide.getActions().analyzerSynchronizerAction.getMenuItem());
		menu.add(ide.getActions().analyzerSynthesizerAction.getMenuItem());
		menu.add(ide.getActions().analyzerVerifierAction.getMenuItem());
		menu.add(ide.getActions().analyzerMinimizeAction.getMenuItem());
		menu.add(ide.getActions().analyzerEventHiderAction.getMenuItem());
		menu.add(ide.getActions().analyzerPurgeAction.getMenuItem());
		menu.addSeparator();
		menu.add(ide.getActions().analyzerExploreStatesAction.getMenuItem());
		menu.add(ide.getActions().analyzerFindStatesAction.getMenuItem());
		menu.add(ide.getActions().analyzerWorkbenchAction.getMenuItem());
		menu.addSeparator();
		menu.add(ide.getActions().analyzerStatisticsAction.getMenuItem());
		menu.add(ide.getActions().analyzerExportAction.getMenuItem());
		menu.addSeparator();
		menu.add(ide.getActions().analyzerDeleteSelectedAction.getMenuItem());
		menu.add(ide.getActions().analyzerDeleteAllAction.getMenuItem());
		menu.add(ide.getActions().analyzerRenameAction.getMenuItem());
		menu.add(ide.getActions().analyzerSendToEditorAction.getMenuItem());


        // Tools
        menu = new JMenu("Examples");
        menu.setMnemonic(KeyEvent.VK_T);
        add(menu);
        menu.add(ide.getActions().toolsTestCasesAction.getMenuItem());

		// File.NewFromTemplate
		JMenu menuFileNewFromTemplate = new JMenu();

		menuFileNewFromTemplate.setText("Static examples");
		menu.add(menuFileNewFromTemplate);

		ExampleTemplates exTempl = ExampleTemplates.getInstance();
		for (Iterator groupIt = exTempl.iterator(); groupIt.hasNext(); )
		{
			TemplateGroup currGroup = (TemplateGroup) groupIt.next();
			JMenu menuFileNewFromTemplateGroup = new JMenu();

			menuFileNewFromTemplateGroup.setText(currGroup.getDescription());
			menuFileNewFromTemplate.add(menuFileNewFromTemplateGroup);

			for (Iterator itemIt = currGroup.iterator(); itemIt.hasNext(); )
			{
				TemplateItem currItem = (TemplateItem) itemIt.next();
				JMenuItem menuItem = new JMenuItem();

				menuItem.setText(currItem.getDescription());
				menuFileNewFromTemplateGroup.add(menuItem);
				menuItem.addActionListener(new NewFromTemplateHandler(currItem));
			}
		}

        // Configure
        menu = new JMenu("Configure");
        menu.setMnemonic(KeyEvent.VK_C);
        add(menu);
        menu.add(ide.getActions().editorOptionsAction.getMenuItem());
        menu.add(ide.getActions().analyzerOptionsAction.getMenuItem());


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
        menu.add(ide.getActions().helpWebAction.getMenuItem());
        menu.addSeparator();
        menu.add(ide.getActions().helpAboutAction.getMenuItem());
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



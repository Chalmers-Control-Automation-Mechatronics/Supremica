//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEMenuBar
//###########################################################################
//# $Id: IDEMenuBar.java,v 1.36 2007-02-22 22:04:22 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.automata.templates.TemplateGroup;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.gui.ExampleTemplates;


public class IDEMenuBar
    extends JMenuBar
{
    private IDE ide;
    private int startPoint = 0;

    private JMenu editorMenu;
    private JMenu analyzerMenu;
    
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
            ModuleSubject module;
            try
            {
                // The documentmanager does the loading, by extension
                module = (ModuleSubject) ide.getIDE().getDocumentManager().load(TemplateItem.class.getResource(item.getPath()));
            }
            catch (IOException ex)
            {
                ide.error("Exception loading " + item.getPath() + ".", ex);
                return;
            }
            catch (WatersUnmarshalException ex)
            {
                ide.error("Exception loading " + item.getPath() + ".", ex);
                return;
            }
            ide.getIDE().installContainer(module);
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
        menu.add(ide.getActions().newAction.getMenuItem());
        menu.add(ide.getActions().openAction.getMenuItem());
        menu.add(ide.getActions().closeAction.getMenuItem());
        menu.addSeparator();
        menu.add(ide.getActions().saveAction.getMenuItem());
        menu.addSeparator();
        //		menu.add(ide.getActions().importAction));
        menu.add(ide.getActions().editorPrintAction.getMenuItem());
        menu.add(ide.getActions().editorSavePostscriptAction.getMenuItem());
        menu.add(ide.getActions().editorSaveEncapsulatedPostscriptAction.getMenuItem());
        menu.add(ide.getActions().editorSavePDFAction.getMenuItem());
        menu.addSeparator();
        menu.add(ide.getActions().exitAction.getMenuItem());
        add(menu);

        // Edit
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(ide.getActions().editorUndoAction.getMenuItem());
        ide.getActions().editorUndoAction.setEnabled(false);
        menu.add(ide.getActions().editorRedoAction.getMenuItem());
        ide.getActions().editorRedoAction.setEnabled(false);
        menu.addSeparator();
        menu.add(ide.getActions().editorCutAction.getMenuItem());
        //menu.add(ide.getActions().editorCopyAsWMFAction.getMenuItem());
        menu.add(ide.getActions().editorCopyAction.getMenuItem());
        menu.add(ide.getActions().editorPasteAction.getMenuItem());
        menu.addSeparator();
	// Embedder should probably go to 'Tools' menu?
        menu.add(ide.getActions().editorRunEmbedderAction.getMenuItem());
        add(menu);

        // Insert
        menu = new JMenu("Insert");
        menu.setMnemonic(KeyEvent.VK_I);
        menu.add(ide.getActions().editorAddSimpleComponentAction.getMenuItem());
        menu.add(ide.getActions().editorAddComponentEventAction.getMenuItem());
        menu.add(ide.getActions().editorAddModuleEventAction.getMenuItem());
        //menu.add(ide.getActions().editorAddForeachComponentAction.getMenuItem());
        //menu.add(ide.getActions().editorAddInstanceAction.getMenuItem());
        //menu.add(ide.getActions().editorAddBindingAction.getMenuItem());
        add(menu);
        editorMenu = menu;

        // Analyze
        menu = new JMenu("Analyze");
        menu.setMnemonic(KeyEvent.VK_A);
        JMenu viewMenu = new JMenu("View");
        viewMenu.add(ide.getActions().analyzerViewAutomatonAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewAlphabetAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewStatesAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewModularStructureAction.getMenuItem());
        menu.add(viewMenu);

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
        analyzerMenu = menu;
        add(menu);

        // Examples
        menu = new JMenu("Examples");
        menu.setMnemonic(KeyEvent.VK_X);
        menu.add(ide.getActions().toolsTestCasesAction.getMenuItem());
        add(menu);

        // File.NewFromTemplate
        //JMenu menuFileNewFromTemplate = new JMenu();
        //menuFileNewFromTemplate.setText("Static examples");
        //menu.add(menuFileNewFromTemplate);
        ExampleTemplates exTempl = ExampleTemplates.getInstance();
        for (Iterator groupIt = exTempl.iterator(); groupIt.hasNext(); )
        {
            TemplateGroup currGroup = (TemplateGroup) groupIt.next();
            JMenu menuFileNewFromTemplateGroup = new JMenu();

            menuFileNewFromTemplateGroup.setText(currGroup.getName());
            menuFileNewFromTemplateGroup.setToolTipText(currGroup.getShortDescription());
            menu.add(menuFileNewFromTemplateGroup);

            for (Iterator itemIt = currGroup.iterator(); itemIt.hasNext(); )
            {
                TemplateItem currItem = (TemplateItem) itemIt.next();
                JMenuItem menuItem = new JMenuItem();
                menuItem.setText(currItem.getName());
                menuItem.setToolTipText(currItem.getShortDescription());

                menuFileNewFromTemplateGroup.add(menuItem);
                menuItem.addActionListener(new NewFromTemplateHandler(currItem));
            }
        }

        // Configure
        menu = new JMenu("Configure");
        menu.setMnemonic(KeyEvent.VK_C);
        add(menu);
        //menu.add(ide.getActions().editorOptionsAction.getMenuItem());
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

    public JMenu getEditorMenu()
    {
        return editorMenu;
    }

    public JMenu getAnalyzerMenu()
    {
        return analyzerMenu;
    }

    public void createModuleList(MenuEvent ev)
    {
        if (startPoint < 0)
        {
            return;
        }

        JMenu menu = (JMenu) ev.getSource();

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



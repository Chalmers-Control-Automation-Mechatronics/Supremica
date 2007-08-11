//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEMenuBar
//###########################################################################
//# $Id: IDEMenuBar.java,v 1.51 2007-08-11 10:44:03 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.sourceforge.waters.gui.actions.InsertVariableAction;
import net.sourceforge.waters.gui.actions.GraphLayoutAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;

import org.supremica.automata.templates.TemplateGroup;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.gui.ExampleTemplates;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.CloseAction;
import org.supremica.gui.ide.actions.ExitAction;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.SaveAction;
import org.supremica.gui.ide.actions.SaveAsAction;


public class IDEMenuBar
    extends JMenuBar
    implements Observer
{
    
    //#######################################################################
    //# Inner Class NewFromTemplateHandler
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
            try
            {
                final String path = item.getPath();
                final URL url = TemplateItem.class.getResource(path);
                final URI uri = url.toURI();
                final DocumentContainerManager manager =
                    ide.getDocumentContainerManager();
                manager.openContainer(uri);
            }
            catch (final URISyntaxException exception)
            {
                throw new WatersRuntimeException(exception);
            }
        }
    }
        
    //#######################################################################
    //# Constructor
    public IDEMenuBar(IDE ide)
    {
        this.ide = ide;
        initMenubar();
        ide.attach(this);
    }
    
    private void initMenubar()
    {
        final Actions actions = ide.getActions();
        
        // File
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        final Action newmod = actions.getAction(NewAction.class);
        menu.add(new JMenuItem(newmod));
        final Action open = actions.getAction(OpenAction.class);
        menu.add(new JMenuItem(open));
        final Action save = actions.getAction(SaveAction.class);
        menu.add(new JMenuItem(save));
        final Action saveas = actions.getAction(SaveAsAction.class);
        menu.add(new JMenuItem(saveas));
        final Action close = actions.getAction(CloseAction.class);
        menu.add(new JMenuItem(close));
        menu.addSeparator();
        menu.add(ide.getActions().editorPrintAction.getMenuItem());
        menu.add(ide.getActions().editorSavePostscriptAction.getMenuItem());
        menu.add(ide.getActions().editorSaveEncapsulatedPostscriptAction.getMenuItem());
        menu.add(ide.getActions().editorSavePDFAction.getMenuItem());
        menu.addSeparator();
		final Action exit = actions.getAction(ExitAction.class);
        menu.add(new JMenuItem(exit));
        add(menu);
        
        // Edit
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        final Action undo = actions.getAction(WatersUndoAction.class);
        menu.add(new JMenuItem(undo));
        final Action redo = actions.getAction(WatersRedoAction.class);
        menu.add(new JMenuItem(redo));
        menu.addSeparator();
        menu.add(ide.getActions().editorCutAction.getMenuItem());
        //menu.add(ide.getActions().editorCopyAsWMFAction.getMenuItem());
        menu.add(ide.getActions().editorCopyAction.getMenuItem());
        menu.add(ide.getActions().editorPasteAction.getMenuItem());
        menu.addSeparator();
        // Embedder should probably go to 'Tools' menu?
        final Action layout = actions.getAction(GraphLayoutAction.class);
        menu.add(new JMenuItem(layout));
        add(menu);
        
        // Insert
        menu = new JMenu("Insert");
        menu.setMnemonic(KeyEvent.VK_I);
        menu.add(ide.getActions().editorAddSimpleComponentAction.getMenuItem());
		/*
        final Action insvar = actions.getAction(InsertVariableAction.class);
        menu.add(new JMenuItem(insvar));
		*/
        menu.add(ide.getActions().editorAddComponentEventAction.getMenuItem());
        menu.add(ide.getActions().editorAddModuleEventAction.getMenuItem());
        //menu.add(ide.getActions().editorAddForeachComponentAction.getMenuItem());
        //menu.add(ide.getActions().editorAddInstanceAction.getMenuItem());
        //menu.add(ide.getActions().editorAddBindingAction.getMenuItem());
        editorMenu = menu;
        editorMenu.setEnabled(false);
        add(menu);
        
        // View (submenu)
        final JMenu viewMenu = new JMenu("View");
        viewMenu.add
            (actions.analyzerViewAutomatonAction.getMenuItem());
        viewMenu.add
            (actions.analyzerViewAlphabetAction.getMenuItem());
        viewMenu.add(actions.analyzerViewStatesAction.getMenuItem());
        viewMenu.add
            (actions.analyzerViewModularStructureAction.getMenuItem());
        
        // Analyze
        menu = new JMenu("Analyze");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.add(viewMenu);
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
        analyzerMenu.setEnabled(false);
        add(menu);
        
        // Examples
        menu = new JMenu("Examples");
        menu.setMnemonic(KeyEvent.VK_X);
        menu.add(ide.getActions().toolsTestCasesAction.getMenuItem());
        add(menu);
        
        // File.NewFromTemplate
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
        mModulesMenu = new JMenu("Modules");
        mModulesMenu.setMnemonic(KeyEvent.VK_M);
        mModulesMenu.setEnabled(false);
        add(mModulesMenu);
        
        // Help
        menu = new JMenu();
        menu.setText("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        add(menu);
        menu.add(ide.getActions().helpWebAction.getMenuItem());
        menu.addSeparator();
        menu.add(ide.getActions().helpAboutAction.getMenuItem());
    }
    
    
    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
    public void update(final EditorChangedEvent event)
    {
        switch (event.getKind())
        {
            case CONTAINER_SWITCH:
                updateModulesMenu();
                updateEnabledStatus();
                break;
            case MAINPANEL_SWITCH:
                updateEnabledStatus();
                break;
            default:
                break;
        }
    }
    
    
    //#######################################################################
    //# Auxiliary Methods
    private void updateEnabledStatus()
    {
        final boolean editor = ide.editorActive();
        final boolean analyzer = ide.analyzerActive();
        editorMenu.setEnabled(editor);
        analyzerMenu.setEnabled(analyzer);
    }
    
    private void updateModulesMenu()
    {
        final DocumentContainerManager manager =
            ide.getDocumentContainerManager();
        final DocumentContainer active = manager.getActiveContainer();
        mModulesMenu.removeAll();
        int count = 0;
        for (final DocumentContainer container : manager.getRecent())
        {
            final JMenuItem item = createMenuItem(container);
            item.setEnabled(container != active);
            mModulesMenu.add(item);
            if (++count >= MAX_MODULES) {
                break;
            }
        }
        mModulesMenu.setEnabled(count > 0);
    }
    
    private JMenuItem createMenuItem(final DocumentContainer container)
    {
        final JMenuItem item = new JMenuItem();
        final File file = container.getFileLocation();
        if (file == null)
        {
            final DocumentProxy doc = container.getDocument();
            final String name = doc.getName();
            if (name == null || name.equals(""))
            {
                item.setText("<nameless>");
            }
            else
            {
                item.setText(name);
            }
        }
        else
        {
            final String path = file.getPath();
            final int index = path.lastIndexOf(File.separatorChar);
            final String tail = index >= 0 ? path.substring(index + 1) : path;
            item.setText(tail);
            item.setToolTipText(path);
        }
        final ActionListener listener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent event)
            {
                final DocumentContainerManager manager =
                    ide.getDocumentContainerManager();
                manager.setActiveContainer(container);
            }
        };
        item.addActionListener(listener);
        return item;
    }
    
    
    //#######################################################################
    //# Data Members
    private final IDE ide;
    private JMenu editorMenu;
    private JMenu analyzerMenu;
    private JMenu mModulesMenu;
    
    
    //#######################################################################
    //# Class Constants
    private static final int MAX_MODULES = 24;
    
}

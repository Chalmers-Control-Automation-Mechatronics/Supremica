//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEMenuBar
//###########################################################################
//# $Id$
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import net.sourceforge.waters.gui.actions.GraphLayoutAction;
import net.sourceforge.waters.gui.actions.GraphSaveEPSAction;
import net.sourceforge.waters.gui.actions.IDECopyAction;
import net.sourceforge.waters.gui.actions.IDECutAction;
import net.sourceforge.waters.gui.actions.IDEDeleteAction;
import net.sourceforge.waters.gui.actions.IDEDeselectAllAction;
import net.sourceforge.waters.gui.actions.IDEPasteAction;
import net.sourceforge.waters.gui.actions.IDEPropertiesAction;
import net.sourceforge.waters.gui.actions.IDESelectAllAction;
import net.sourceforge.waters.gui.actions.InsertEventDeclAction;
import net.sourceforge.waters.gui.actions.InsertForeachComponentAction;
import net.sourceforge.waters.gui.actions.InsertSimpleComponentAction;
import net.sourceforge.waters.gui.actions.InsertVariableAction;
import net.sourceforge.waters.gui.actions.ShowGraphAction;
import net.sourceforge.waters.gui.actions.ShowModuleCommentAction;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
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
import org.supremica.properties.Config;

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
        menu.setMnemonic(KeyEvent.VK_F); // ALT-F - Create module event?
        final Action newmod = actions.getAction(NewAction.class);
        menu.add(newmod);
        final Action open = actions.getAction(OpenAction.class);
        menu.add(open);
        final Action save = actions.getAction(SaveAction.class);
        menu.add(save);
        final Action saveas = actions.getAction(SaveAsAction.class);
        menu.add(saveas);
        final Action close = actions.getAction(CloseAction.class);
        menu.add(close);
        menu.addSeparator();
        menu.add(ide.getActions().editorPrintAction.getMenuItem());
        menu.add(ide.getActions().editorSavePostscriptAction.getMenuItem());
		final Action epsprint = actions.getAction(GraphSaveEPSAction.class);
        menu.add(epsprint);
        menu.add(ide.getActions().editorSavePDFAction.getMenuItem());
        menu.addSeparator();
        final Action exit = actions.getAction(ExitAction.class);
        menu.add(exit);
        add(menu);

        // Edit
        menu = new JMenu("Edit");
        // menu.setMnemonic(KeyEvent.VK_E); // ALT-E - Create component event?
        final Action undo = actions.getAction(WatersUndoAction.class);
        menu.add(undo);
        final Action redo = actions.getAction(WatersRedoAction.class);
        menu.add(redo);
        menu.addSeparator();
        final Action delete = actions.getAction(IDEDeleteAction.class);
        menu.add(delete);
		final Action cut = actions.getAction(IDECutAction.class);
        menu.add(cut);
		final Action copy = actions.getAction(IDECopyAction.class);
        menu.add(copy);
		final Action paste = actions.getAction(IDEPasteAction.class);
        menu.add(paste);
        menu.addSeparator();
        final Action select = actions.getAction(IDESelectAllAction.class);
        menu.add(select);
        final Action deselect = actions.getAction(IDEDeselectAllAction.class);
        menu.add(deselect);
        menu.addSeparator();
        final Action properties = actions.getAction(IDEPropertiesAction.class);
        menu.add(properties);
        final Action showgraph = actions.getAction(ShowGraphAction.class);
        menu.add(showgraph);
        final Action showcomment =
			actions.getAction(ShowModuleCommentAction.class);
        menu.add(showcomment);
        // Embedder should probably go to 'Tools' menu?
        final Action layout = actions.getAction(GraphLayoutAction.class);
        menu.add(layout);
        add(menu);

        // Insert
        menu = new JMenu("Create");
		// Why not "Insert"? All MS programs use insert. ~~~Robi
        //menu.setMnemonic(KeyEvent.VK_I);
        final Action inscomp =
			actions.getAction(InsertSimpleComponentAction.class);
        menu.add(inscomp);
        final Action insvar = actions.getAction(InsertVariableAction.class);
        menu.add(insvar);
        final Action insforeach =
			actions.getAction(InsertForeachComponentAction.class);
        menu.add(insforeach);
        final Action insevent = actions.getAction(InsertEventDeclAction.class);
        menu.add(insevent);
        //menu.add(ide.getActions().editorAddForeachComponentAction.getMenuItem());
        //menu.add(ide.getActions().editorAddInstanceAction.getMenuItem());
        //menu.add(ide.getActions().editorAddBindingAction.getMenuItem());
        editorMenu = menu;
        editorMenu.setEnabled(false);
        add(menu);


        // Analyze
        menu = new JMenu("Analyze");
        // menu.setMnemonic(KeyEvent.VK_A); // ALT-A - Save as?
        // View (submenu)
        final JMenu viewMenu = new JMenu("View");
        {
            viewMenu.add(actions.analyzerViewAutomatonAction.getMenuItem());
            viewMenu.add(actions.analyzerViewAlphabetAction.getMenuItem());
            viewMenu.add(actions.analyzerViewStatesAction.getMenuItem());
            viewMenu.add(actions.analyzerViewModularStructureAction.getMenuItem());
        }
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

        // Simulator
        if (Config.INCLUDE_ANIMATOR.isTrue())
        {
            menu = new JMenu("Simulate");
            menu.add(ide.getActions().simulatorLaunchAnimatorAction);
            menu.add(ide.getActions().simulatorLaunchSimulatorAction);
            menu.add(ide.getActions().simulatorClearSimulationData);
            add(menu);
        }
        
        // Examples
        menu = new JMenu("Examples");
        //menu.setMnemonic(KeyEvent.VK_X);
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
        // menu.setMnemonic(KeyEvent.VK_C); // ALT-C - Create component?
        add(menu);
        //menu.add(ide.getActions().editorOptionsAction.getMenuItem());
        menu.add(ide.getActions().analyzerOptionsAction.getMenuItem());

        // Modules
        mModulesMenu = new JMenu("Modules");
        mModulesMenu.setMnemonic(KeyEvent.VK_M);
        mModulesMenu.setEnabled(false);
        add(mModulesMenu);

        // Tools
        if (Config.INCLUDE_EXTERNALTOOLS.isTrue())
        {
        	menu = new JMenu();
        	menu.setText("Tools");
        	add(menu);
        	if (Config.INCLUDE_SOCEDITOR.isTrue())
        	{
        		menu.add(ide.getActions().toolsSOCEditorAction.getMenuItem());
        	}

        }
        
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

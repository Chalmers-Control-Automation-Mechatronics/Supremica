//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEMenuBar
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Component;
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
import net.sourceforge.waters.gui.actions.SimulationStepAction;
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
import org.supremica.gui.ide.actions.ImportAction;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.SaveAction;
import org.supremica.gui.ide.actions.SaveAsAction;
import org.supremica.properties.Config;

public class IDEMenuBar
    extends JMenuBar
    implements Observer
{
    private static final long serialVersionUID = 1L;

    //#######################################################################
    //# Inner Class NewFromTemplateHandler
    class NewFromTemplateHandler
        implements ActionListener
    {
        private static final long serialVersionUID = 1L;

        private TemplateItem item = null;

        public NewFromTemplateHandler(final TemplateItem item)
        {
            this.item = item;
        }

        public void actionPerformed(final ActionEvent e)
        {
            try
            {
                final String path = item.getPath();
                final URL url = TemplateItem.class.getResource(path);
                final URI uri = url.toURI();
                final DocumentContainerManager manager =
                    mIDE.getDocumentContainerManager();
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
    public IDEMenuBar(final IDE ide)
    {
        this.mIDE = ide;
        initMenubar();
        ide.attach(this);
    }

    private void initMenubar()
    {
        final Actions actions = mIDE.getActions();

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
        final Action importAction = actions.getAction(ImportAction.class);
        menu.add(importAction);
        menu.add(mIDE.getActions().editorPrintAction.getMenuItem());
        menu.add(mIDE.getActions().editorSavePostscriptAction.getMenuItem());
		final Action epsprint = actions.getAction(GraphSaveEPSAction.class);
        menu.add(epsprint);
        menu.add(mIDE.getActions().editorSavePDFAction.getMenuItem());
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

        // Simulate
        menu = new JMenu("Simulate");
        final Action step = actions.getAction(SimulationStepAction.class);
        menu.add(step);
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
        menu.add(mIDE.getActions().analyzerSynchronizerAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerSynthesizerAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerVerifierAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerMinimizeAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerEventHiderAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerPurgeAction.getMenuItem());
        menu.addSeparator();
        menu.add(mIDE.getActions().analyzerExploreStatesAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerFindStatesAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerWorkbenchAction.getMenuItem());
        menu.addSeparator();
        menu.add(mIDE.getActions().analyzerStatisticsAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerExportAction.getMenuItem());
        menu.addSeparator();
        menu.add(mIDE.getActions().analyzerDeleteSelectedAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerDeleteAllAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerRenameAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerSendToEditorAction.getMenuItem());
        menu.add(mIDE.getActions().analyzerGuardAction.getMenuItem());
        analyzerMenu = menu;
        analyzerMenu.setEnabled(false);
        add(menu);

        // Simulator
        if (Config.INCLUDE_ANIMATOR.isTrue())
        {
            menu = new JMenu("Simulate");
            menu.add(mIDE.getActions().simulatorLaunchAnimatorAction);
            menu.add(mIDE.getActions().simulatorLaunchSimulatorAction);
            menu.add(mIDE.getActions().simulatorClearSimulationData);
            add(menu);
        }

        // Examples
        menu = new JMenu("Examples");
        //menu.setMnemonic(KeyEvent.VK_X);
        menu.add(mIDE.getActions().toolsTestCasesAction.getMenuItem());
        add(menu);

        // File.NewFromTemplate
        final ExampleTemplates exTempl = ExampleTemplates.getInstance();
        for (final Iterator<TemplateGroup> groupIt = exTempl.iterator(); groupIt.hasNext(); )
        {
            final TemplateGroup currGroup = (TemplateGroup) groupIt.next();
            final JMenu menuFileNewFromTemplateGroup = new JMenu();

            menuFileNewFromTemplateGroup.setText(currGroup.getName());
            menuFileNewFromTemplateGroup.setToolTipText(currGroup.getShortDescription());
            menu.add(menuFileNewFromTemplateGroup);

            for (final Iterator<TemplateItem> itemIt = currGroup.iterator(); itemIt.hasNext(); )
            {
                final TemplateItem currItem = (TemplateItem) itemIt.next();
                final JMenuItem menuItem = new JMenuItem();
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
        menu.add(mIDE.getActions().analyzerOptionsAction.getMenuItem());

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
        		menu.add(mIDE.getActions().toolsSOCEditorAction.getMenuItem());
        	}

        }

        // Help
        menu = new JMenu();
        menu.setText("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        add(menu);
        menu.add(mIDE.getActions().helpWebAction.getMenuItem());
        menu.addSeparator();
        menu.add(mIDE.getActions().helpAboutAction.getMenuItem());
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
      boolean editor = false;
      boolean analyzer = false;
      final DocumentContainer container = mIDE.getActiveDocumentContainer();
      if (container != null) {
        final Component active = container.getActivePanel();
        if (active == container.getEditorPanel()) {
          editor = true;
        } else if (active == container.getAnalyzerPanel()) {
          analyzer = true;
        }
      }
      editorMenu.setEnabled(editor);
      analyzerMenu.setEnabled(analyzer);
    }

    private void updateModulesMenu()
    {
        final DocumentContainerManager manager =
            mIDE.getDocumentContainerManager();
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
                    mIDE.getDocumentContainerManager();
                manager.setActiveContainer(container);
            }
        };
        item.addActionListener(listener);
        return item;
    }


    //#######################################################################
    //# Data Members
    private final IDE mIDE;
    private JMenu editorMenu;
    private JMenu analyzerMenu;
    private JMenu mModulesMenu;


    //#######################################################################
    //# Class Constants
    private static final int MAX_MODULES = 24;

}

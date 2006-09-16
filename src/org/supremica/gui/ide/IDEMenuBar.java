
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
        
        // Tools
        menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);
        add(menu);
        menu.add(ide.getActions().toolsTestCasesAction.getMenuItem());
        
        // Editor
        menu = new JMenu("Editor");
        menu.setMnemonic(KeyEvent.VK_M);
        add(menu);
        menu.add(ide.getActions().editorAddSimpleComponentAction.getMenuItem());
        menu.add(ide.getActions().editorAddEventAction.getMenuItem());
        //menu.add(ide.getActions().editorAddForeachComponentAction.getMenuItem());
        //menu.add(ide.getActions().editorAddInstanceAction.getMenuItem());
        //menu.add(ide.getActions().editorAddBindingAction.getMenuItem());
        
        // Analyze
        menu = new JMenu("Analyzer");
        menu.setMnemonic(KeyEvent.VK_A);
        add(menu);
        menu.add(new JMenuItem(ide.getActions().analyzerDeleteSelectedAction));
        menu.add(new JMenuItem(ide.getActions().analyzerWorkbenchAction));
        menu.addSeparator();
        menu.add(new JMenuItem(ide.getActions().analyzerVerifierAction));
        menu.add(new JMenuItem(ide.getActions().analyzerSynchronizerAction));
        menu.add(new JMenuItem(ide.getActions().analyzerSynthesizerAction));
        
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



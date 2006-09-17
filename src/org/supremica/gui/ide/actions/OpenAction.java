//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: OpenAction.java,v 1.17 2006-09-17 10:24:00 flordal Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import java.util.List;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;


public class OpenAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
   
    private JFileChooser chooser;
                
    public OpenAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "Open/Import...");
        putValue(Action.SHORT_DESCRIPTION, "Open/import a project");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
    }

    /**
     * Ugly override, we need the ide...
     */
    public void setIDEActionInterface(IDEActionInterface ide)
    {
        super.setIDEActionInterface(ide);

        // Initialise the filechooser
        chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setCurrentDirectory(new java.io.File(Config.FILE_OPEN_PATH.get()));
        chooser.setMultiSelectionEnabled(true);
        for (FileFilter filter : ide.getIDE().getDocumentManager().getSupportedFileFilters())
        {
            chooser.addChoosableFileFilter(filter);
        }
        // Select the first filter
        chooser.setFileFilter(ide.getIDE().getDocumentManager().getSupportedFileFilters().iterator().next());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        final FileImporter importer = new WatersFileImporter(chooser);
    }
    
    
    
    //#######################################################################
    //# Local Class WatersFileImporter
    private 
        class WatersFileImporter extends FileImporter
    {
        
        //###################################################################
        //# Constructors
        private WatersFileImporter(JFileChooser chooser)
        {
            super(chooser, ide);
        }
        
        
        
        //###################################################################
        //# Overrides for Base Class org.supremica.gui.ide.actions.FileImporter
        void openFile(final File file)
        {
            ModuleSubject module;
            try
            {
                 module = (ModuleSubject) ide.getIDE().getDocumentManager().load(file);
            }
            catch (RuntimeException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                ide.getIDE().error(ex.getMessage());
                return;
            }
            ide.getIDE().installContainer(module);
        }
    }
}

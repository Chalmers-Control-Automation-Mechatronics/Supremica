//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   SaveAction
//###########################################################################
//# $Id: SaveAsAction.java,v 1.3 2007-06-21 09:51:56 flordal Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.*;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.gui.SupremicaXMLFileFilter;
import org.supremica.gui.ide.AutomataContainer;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.log.*;

public class SaveAsAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    private static Logger logger =
        LoggerFactory.createLogger(SaveAction.class);
    
    private JFileChooser fileSaveChooser = new JFileChooser(".");
    private WmodFileFilter wmodFilter = new WmodFileFilter();
    private SupremicaXMLFileFilter supFilter = new SupremicaXMLFileFilter();
    
    public SaveAsAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        fileSaveChooser.addChoosableFileFilter(wmodFilter);
        fileSaveChooser.addChoosableFileFilter(supFilter);
        fileSaveChooser.setFileFilter(wmodFilter);

        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, "Save the project with a new name");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {        
        int returnVal = fileSaveChooser.showSaveDialog(ide.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fileSaveChooser.getSelectedFile();
            
            // Branch depending on chosen file filter
            if (fileSaveChooser.getFileFilter() instanceof SupremicaXMLFileFilter)
            {
                // Supremica XML
                if (!supFilter.accept(file))
                {
                    file = new File(file.getPath() + "." + SupremicaXMLFileFilter.SUPXML);
                }
                saveSupFile(file);
            }
            else
            {
                // Default
                if (!wmodFilter.accept(file))
                {
                    file = new File(file.getPath() + "." + WmodFileFilter.WMOD);
                }
                saveWmodFile(file);
            }
            
            //modified = false;
            
            //logEntry("File saved: " + file);
        }
        else
        {
            // SaveAs cancelled...  do nothing
        }
    }
    
    private void saveSupFile(File file)
    {
        try
        {
            Automata automata = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getAllAutomata();
            AutomataToXML exporter = new AutomataToXML(automata);
            exporter.serialize(file.getAbsolutePath());
        }
        catch (Exception ex)
        {
            logger.error("Exception while SaveAs " + file.getAbsolutePath(), ex);
            logger.debug(ex.getStackTrace());
        }
    }
    
    private void saveWmodFile(File file)
    {
        //logEntry("Saving module to: " + wmodf);
        try
        {
            DocumentManager documentManager = ide.getIDE().getDocumentManager();
            documentManager.saveAs(ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject(), file);
        }
        catch (final WatersMarshalException exception)
        {
            JOptionPane.showMessageDialog(ide.getFrame(),
                "Error saving module file:" +
                exception.getMessage());
            //logEntry("WatersMarshalException - Failed to save  '" +
            //         wmodf + "'!");
        }
        catch (final IOException exception)
        {
            JOptionPane.showMessageDialog(ide.getFrame(),
                "Error saving module file:" +
                exception.getMessage());
            //logEntry("IOException - Failed to save  '" + wmodf + "'!");
        }
    }
}

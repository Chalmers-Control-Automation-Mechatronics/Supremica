//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   SaveAction
//###########################################################################
//# $Id: SaveAsAction.java,v 1.2 2007-06-20 19:43:38 flordal Exp $
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

import org.supremica.gui.ide.IDE;
import org.supremica.log.*;

public class SaveAsAction
        extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    private static Logger logger =
            LoggerFactory.createLogger(SaveAction.class);
    
    private JFileChooser fileSaveChooser = new JFileChooser(".");
    
    public SaveAsAction(List<IDEAction> actionList)
    {
        super(actionList);
        
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
        WmodFileFilter filter = new WmodFileFilter();
        fileSaveChooser.setFileFilter(filter);
        int returnVal = fileSaveChooser.showSaveDialog(ide.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fileSaveChooser.getSelectedFile();
            if (!filter.accept(file))
            {
                file = new File(file.getPath() + "." + WmodFileFilter.WMOD);
            }
            
            saveWmodFile(file);
            
            //modified = false;
            
            //logEntry("File saved: " + file);
        }
        else
        {
            // SaveAs cancelled...  do nothing
        }
    }
    
    private void saveWmodFile(File wmodf)
    {
       //logEntry("Saving module to: " + wmodf);
        try
        {
            DocumentManager documentManager = ide.getIDE().getDocumentManager();
            documentManager.saveAs(ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject(), wmodf);
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

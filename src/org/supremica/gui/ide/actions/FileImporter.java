//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: FileImporter.java,v 1.7 2007-01-29 16:04:25 flordal Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.List;

abstract class FileImporter
{
    FileImporter(JFileChooser fileOpener, IDEActionInterface ide)
    {
        if (fileOpener.showOpenDialog(ide.getFrame()) == JFileChooser.APPROVE_OPTION)
        {
            File[] currFiles = fileOpener.getSelectedFiles();
            
            if (currFiles != null)
            {
                for (int i = 0; i < currFiles.length; i++)
                {
                    if (currFiles[i].isFile())
                    {
                        openFile(currFiles[i]);
                    }
                }
            }
            
            ide.repaint();
        }
    }
    
    FileImporter(List<File> files, IDEActionInterface ide)
    {
        for (File currFile : files)
        {
            if (currFile.isFile())
            {
                openFile(currFile);
            }
        }
        
        ide.repaint();
    }
    
    FileImporter(File currFile, IDEActionInterface ide)
    {
        if (currFile.isFile())
        {
            openFile(currFile);
        }
        ide.repaint();
    }    
    
    abstract void openFile(File file);
}

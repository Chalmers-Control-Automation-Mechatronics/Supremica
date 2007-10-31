//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindowInterface.java,v 1.17 2007-10-31 13:01:00 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.JFrame;

import net.sourceforge.waters.gui.command.UndoInterface;

/**
 * Implemented by ComponentEditorPanel and ComponentViewPanel...
 */
public interface EditorWindowInterface
{
    public ModuleWindowInterface getModuleWindowInterface();
    
    public ControlledSurface getControlledSurface();
    
    public EditorEvents getEventPane();
    
    public UndoInterface getUndoInterface();
    
    public String getComponentName();
    
    public void repaint();
    
    public void setDisplayed();
    
    public void copyAsWMFToClipboard();
    
    public void exportPDF();
    
    public void exportPostscript();
    
    public void printFigure();
    
    public void createEvent();
    
    // *** BUG ***
    // The following functions should be in ModuleWindowInterface !!!
    public boolean isSaved();
    
    public void setSaved(boolean s);
    
    public JFrame getFrame();
    // ***
    
}

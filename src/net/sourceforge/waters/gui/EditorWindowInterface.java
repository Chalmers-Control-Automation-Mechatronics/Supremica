//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindowInterface.java,v 1.15 2007-09-03 11:37:27 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.JFrame;

import net.sourceforge.waters.gui.command.UndoInterface;


public interface EditorWindowInterface
{
    public ModuleWindowInterface getModuleWindowInterface();
    
    public ControlledSurface getControlledSurface();
    
    public EditorEvents getEventPane();
    
    public UndoInterface getUndoInterface();
    
    public void repaint();
    
    public void setDisplayed();
    
    public void copyAsWMFToClipboard();
    
    public void exportPDF();
    
    public void exportPostscript();
    
    public void exportEncapsulatedPostscript();
    
    public void printFigure();
    
    public void createEvent();
    
    // *** BUG ***
    // The following functions should be in ModuleWindowInterface !!!
    public boolean isSaved();
    
    public void setSaved(boolean s);
    
    public JFrame getFrame();
    // ***
    
}

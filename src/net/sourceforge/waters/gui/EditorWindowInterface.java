//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindowInterface.java,v 1.11 2007-01-26 15:09:47 avenir Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.JFrame;

import net.sourceforge.waters.gui.command.UndoInterface;


public interface EditorWindowInterface
{
    public boolean isSaved();
    
    public void setSaved(boolean s);
    
    public JFrame getFrame();
    
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
}

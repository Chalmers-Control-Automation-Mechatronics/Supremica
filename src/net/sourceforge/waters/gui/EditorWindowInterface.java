//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindowInterface.java,v 1.6 2005-12-16 00:26:39 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.io.File;
import java.util.List;
import javax.swing.JFrame;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.subject.module.IdentifierSubject;


public interface EditorWindowInterface
{
	public boolean isSaved();

	public void setSaved(boolean s);

	public List getEventDeclList();

	public JFrame getFrame();

	public ControlledSurface getControlledSurface();

	public EditorEvents getEventPane();

	public UndoInterface getUndoInterface();

	public void repaint();

	public void setDisplayed();

	public void copyAsWMFToClipboard();

	public void createPDF(File f);
}

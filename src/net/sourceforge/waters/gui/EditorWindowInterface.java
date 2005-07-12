//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindowInterface.java,v 1.4 2005-07-12 03:56:00 siw4 Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.util.List;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.gui.command.UndoInterface;
import javax.swing.JFrame;
import java.io.File;



public interface EditorWindowInterface
{
	public IdentifierProxy getBuffer();

	public void setBuffer(IdentifierProxy i);

	public boolean isSaved();

	public void setSaved(boolean s);

	public List getEventDeclList();

	public JFrame getFrame();

	public ControlledSurface getControlledSurface();

	public EditorEvents getEventPane();

	public UndoInterface getUndoInterface();

	public void repaint();

	public void setDisplayed();

	void copyAsWMFToClipboard();
	void createPDF(File f);

}

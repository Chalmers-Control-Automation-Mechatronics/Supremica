//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindowInterface.java,v 1.3 2005-03-04 05:40:22 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.util.List;
import net.sourceforge.waters.model.expr.IdentifierProxy;
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

	public void repaint();

	void copyAsWMFToClipboard();
	void createPDF(File f);

}

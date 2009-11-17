//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.gui
//# CLASS:   EditorWindowInterface
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * Implemented by ComponentEditorPanel and ComponentViewPanel...
 */

public interface EditorWindowInterface
{

	/**
     * Gets the simple component (i.e., automaton) edited or displayed
	 * by this panel.
     */
    public SimpleComponentProxy getComponent();

    public ModuleWindowInterface getModuleWindowInterface();

    public ControlledSurface getControlledSurface();

	public GraphEventPanel getEventPanel();

    public UndoInterface getUndoInterface();

	@Deprecated
    public void copyAsWMFToClipboard();

}

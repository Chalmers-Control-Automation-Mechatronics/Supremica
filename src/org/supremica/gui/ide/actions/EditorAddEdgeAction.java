//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorAddEdgeAction
//###########################################################################
//# $Id: EditorAddEdgeAction.java,v 1.11 2007-02-12 21:38:49 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.List;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledToolbar;

public class EditorAddEdgeAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddEdgeAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Add Edge");
		putValue(Action.SHORT_DESCRIPTION, "Add edge");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledToolbar.Tool.EDGE.toString());
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ide.setEditorMode(this);

//		System.err.println("Add Edge is not implemented yet!");
	}
}

//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorSelectAction
//###########################################################################
//# $Id: EditorSelectAction.java,v 1.10 2007-02-12 21:38:49 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledToolbar;
import java.util.List;

public class EditorSelectAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorSelectAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Select");
		putValue(Action.SHORT_DESCRIPTION, "Select");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/select16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledToolbar.Tool.SELECT.toString());
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ide.setEditorMode(this);
		//System.err.println("Select is not implemented yet!");
	}
}

//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorAddEventAction
//###########################################################################
//# $Id: EditorAddEventAction.java,v 1.12 2006-09-05 21:29:48 flordal Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.KeyStroke;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledSurface;
import org.supremica.gui.ide.ModuleContainer;

public class EditorAddEventAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddEventAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Create Event");
		putValue(Action.SHORT_DESCRIPTION, "Add Event");
		putValue(Action.SMALL_ICON,
				 new ImageIcon(IDE.class.getResource("/icons/waters/event16.gif")));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		putValue(Action.ACTION_COMMAND_KEY,
				 ControlledSurface.Tool.EVENT.toString());
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ModuleContainer activeModule = ide.getActiveModuleContainer();
		activeModule.getEditorPanel().getEditorPanelInterface().addEvent();
	}
}

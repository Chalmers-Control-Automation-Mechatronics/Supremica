package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorOptionsAction
	extends IDEAction
{
	public EditorOptionsAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Editor Options...");
		putValue(Action.SHORT_DESCRIPTION, "Editor Options");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ModuleContainer moduleContainer = ide.getActiveModuleContainer();

//			root.getControlledSurface().setOptionsVisible(true);
	}
}

package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorPrintAction
	extends IDEAction
{

	public EditorPrintAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Print...");
		putValue(Action.SHORT_DESCRIPTION, "Print");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Print16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Print is not implemented yet!");
	}
}

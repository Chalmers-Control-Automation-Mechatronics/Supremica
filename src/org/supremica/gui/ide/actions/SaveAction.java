package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


import org.supremica.gui.ide.IDE;

public class SaveAction
	extends IDEAction
{
	private IDE ide;

	public SaveAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Save...");
		putValue(Action.SHORT_DESCRIPTION, "Save the project");
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Save Action selected - not implemented");
	}
}

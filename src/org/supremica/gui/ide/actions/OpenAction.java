package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


import org.supremica.gui.ide.IDE;

public class OpenAction
	extends AbstractAction
{
	private IDE ide;

	public OpenAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Open...");
		putValue(Action.SHORT_DESCRIPTION, "Open a new project");
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(Action.SMALL_ICON,
				 new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		System.err.println("Open Action selected - not implemented");
	}
}

package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.EventEditorDialog;


public class NewEventAction
	extends IDEAction
{
	private IDE ide;

	public NewEventAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "New Event");
		putValue(Action.SHORT_DESCRIPTION, "New event");
//		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/New16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		EventEditorDialog diag = new EventEditorDialog(ide);
	}
}

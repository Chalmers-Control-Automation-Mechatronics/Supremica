package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;

public class NewEventAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public NewEventAction(IDEActionInterface ide)
	{
		super(ide);

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
//		EventEditorDialog diag = new EventEditorDialog(ide);
	}
}

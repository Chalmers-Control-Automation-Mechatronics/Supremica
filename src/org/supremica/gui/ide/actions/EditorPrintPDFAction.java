package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;

public class EditorPrintPDFAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorPrintPDFAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Print As PDF...");
		putValue(Action.SHORT_DESCRIPTION, "Print As PDF");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Print16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Print as PDF is not implemented yet!");
	}
}

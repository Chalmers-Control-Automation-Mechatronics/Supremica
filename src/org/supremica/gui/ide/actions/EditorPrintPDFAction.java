package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorPrintPDFAction
	extends IDEAction
{

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

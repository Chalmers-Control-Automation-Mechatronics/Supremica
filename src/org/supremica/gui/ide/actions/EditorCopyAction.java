package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;

public class EditorCopyAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorCopyAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Copy");
		putValue(Action.SHORT_DESCRIPTION, "Copy");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Copy16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Copy is not implemented yet!");
	}
}

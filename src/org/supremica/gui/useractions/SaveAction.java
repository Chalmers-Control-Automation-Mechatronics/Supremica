package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class SaveAction
	extends AbstractAction
{
	public SaveAction()
	{
		super("Save...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
		putValue(SHORT_DESCRIPTION, "Save this project");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileSave(ActionMan.getGui());
	}

}

package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class CopyAction
	extends AbstractAction
{
	public CopyAction()
	{
		super("Copy...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Copy16.gif")));

		putValue(SHORT_DESCRIPTION, "Copy selected automata");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automataCopy_actionPerformed(ActionMan.getGui());
	}
}

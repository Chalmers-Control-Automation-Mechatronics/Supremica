package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class ViewAction
	extends AbstractAction
{
	public ViewAction()
	{
		super("View...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Zoom16.gif")));
		putValue(SHORT_DESCRIPTION, "View selected automata");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automatonView_actionPerformed(ActionMan.getGui());
	}
}

package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class StatusAction
	extends AbstractAction
{
	public StatusAction()
	{
		super("Status...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Information16.gif")));
		putValue(SHORT_DESCRIPTION, "Show status of selected automata");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automatonStatus_actionPerformed(ActionMan.getGui());
	}

}

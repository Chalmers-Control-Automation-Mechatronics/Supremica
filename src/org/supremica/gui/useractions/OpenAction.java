package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class OpenAction
	extends AbstractAction
{
	public OpenAction()
	{
		super("Open...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
		putValue(SHORT_DESCRIPTION, "Open a new project");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileOpen(ActionMan.getGui());
	}

}

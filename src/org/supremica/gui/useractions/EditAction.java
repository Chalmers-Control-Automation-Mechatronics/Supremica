/****************** EditAction.java ***********/
package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.Supremica;

public class EditAction
	extends AbstractAction
{
	private Supremica supremica;
	
	public EditAction(Supremica supremica)
	{
		super("Edit...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")));
		putValue(SHORT_DESCRIPTION, "Open automata editor");
		this.supremica = supremica;
	}

	public void actionPerformed(ActionEvent e)
	{
		supremica.toolsAutomataEditor();
	}

}
	
/******************* PropertiesAction.java *********************/
package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class MoveAutomataUpAction
	extends AbstractAction
{
	public MoveAutomataUpAction()
	{
		super("Move automata up...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/navigation/Up16.gif")));
		putValue(SHORT_DESCRIPTION, "Move automaton up");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automataMove_actionPerformed(ActionMan.getGui(), true);
	}
}
	

/******************* MoveAutomataDownAction.java *********************/
package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class MoveAutomataDownAction
	extends AbstractAction
{
	public MoveAutomataDownAction()
	{
		super("Move automata down...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/navigation/Down16.gif")));			
		putValue(SHORT_DESCRIPTION, "Move automaton down");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automataMove_actionPerformed(ActionMan.getGui(), false);
	}

}
	

/******************* MoveAutomataUpAction.java *********************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class MoveAutomataAction
	extends AbstractAction
{
	boolean directionIsUp;
	boolean allTheWay;

	public MoveAutomataAction(boolean directionIsUp, boolean allTheWay)
	{
		if (directionIsUp && allTheWay)
		{
			//super("Move automata to top...", new ImageIcon(Supremica.class.getResource("/icons/ToTop16.gif")));
			putValue(NAME, "Move automata to top...");
			putValue(SMALL_ICON, new ImageIcon(Supremica.class.getResource("/icons/ToTop16.gif")));
			putValue(SHORT_DESCRIPTION, "Move automaton to top");
		}
		else if (directionIsUp && !allTheWay)
		{
			// super("Move automata up...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/navigation/Up16.gif")));
			putValue(NAME, "Move automata up...");
			putValue(SMALL_ICON, new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/navigation/Up16.gif")));
			putValue(SHORT_DESCRIPTION, "Move automaton up");
		}
		else if (!directionIsUp && !allTheWay)
		{
			//super("Move automata down...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/navigation/Down16.gif")));			
			putValue(NAME, "Move automata down...");
			putValue(SMALL_ICON, new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/navigation/Down16.gif")));			
			putValue(SHORT_DESCRIPTION, "Move automaton down");
		}
		else if (!directionIsUp && allTheWay)
		{
			//super("Move automata to bottom...", new ImageIcon(Supremica.class.getResource("/icons/ToBottom16.gif")));
			putValue(NAME, "Move automata to bottom...");
			putValue(SMALL_ICON, new ImageIcon(Supremica.class.getResource("/icons/ToBottom16.gif")));
			putValue(SHORT_DESCRIPTION, "Move automaton to bottom");
		}

		this.directionIsUp = directionIsUp;
		this.allTheWay = allTheWay;
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automataMove_actionPerformed(ActionMan.getGui(), directionIsUp, allTheWay);
	}
}
	


package org.supremica.gui.ide;

import java.awt.Insets;
import javax.swing.*;
import java.util.*;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.log.*;


public class IDEToolBar
	extends JToolBar
{
	private static Logger logger = LoggerFactory.createLogger(IDEToolBar.class);
	private static final Insets theInsets = new Insets(0, 0, 0, 0);

	private List collection = new LinkedList();

	private IDE ide;

	public IDEToolBar(IDE ide)
	{
		this.ide = ide;
		setRollover(true);
		setFloatable(false);
	}

	public IDEToolBar(IDEToolBar toolBar)
	{
		this(toolBar.ide);
//		logger.debug("Toolbar copy constructor");
		for (Iterator actIt = toolBar.collection.iterator(); actIt.hasNext(); )
		{
			Action currAction = (Action)actIt.next();
			if (currAction == null)
			{
				addSeparator();
//				logger.debug("Added separator");
			}
			else
			{
				add(currAction);

// Huh - note that the button is stored in an action. Possible two buttons may true to save
// themselves in the action -> problems
// The above solution works because not a third instance tries to add members to the toolbar.
// Fix as soon as possible...
/*
				if (currAction instanceof IDEAction)
				{
					add(((IDEAction)currAction).getButton());
					logger.debug("Added IDEAction");
				}
				else
				{
					add(currAction);
					logger.debug("Added Action");
				}
*/
			}
		}
	}

	public JToggleButton add(Action theAction, ButtonGroup theButtonGroup)
	{
		JToggleButton theButton = new JToggleButton(theAction);
		theButton.setText("");
		add(theButton);
		theButtonGroup.add(theButton);
		collection.add(theAction);
		if (theAction instanceof IDEAction)
		{
			((IDEAction)theAction).setButton(theButton);
		}
		theButton.setMargin(theInsets);

		return theButton;
	}

	public JButton add(Action theAction)
	{
		JButton theButton = super.add(theAction);
		collection.add(theAction);
		if (theAction instanceof IDEAction)
		{
			((IDEAction)theAction).setButton(theButton);
		}
		theButton.setMargin(theInsets);

		return theButton;
	}

	public void addSeparator()
	{
		collection.add(null);
		super.addSeparator();
	}

	public int nbrOfActions()
	{
		return collection.size();
	}

}

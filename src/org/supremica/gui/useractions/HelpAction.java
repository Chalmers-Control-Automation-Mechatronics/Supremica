/********************** HelpAction.java ************************/
// Implementation of the Help stuff

package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.help.*;

import org.supremica.gui.help.ContentHelp;

public class HelpAction
	extends AbstractAction
{
 	private ContentHelp help = null;
	private CSH.DisplayHelpFromSource helpDisplayer = null;
	
	public HelpAction()
	{
		super("Supervisory Control", new ImageIcon(HelpAction.class.getResource("/toolbarButtonGraphics/general/Help16.gif")));
		putValue(SHORT_DESCRIPTION, "Provides help on supervisory control theory");
		
		this.help = new ContentHelp();
		this.helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());
	}

	public void actionPerformed(ActionEvent e)
	{
		helpDisplayer.actionPerformed(e);
	}

}
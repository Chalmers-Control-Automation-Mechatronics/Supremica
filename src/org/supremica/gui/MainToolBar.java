/***************** MainToolBar.java ********************/
// Free standing leaf class implementing Supremicas
// main toolbar. Prime reason for this is easy access
// The class instantiates itself with the toolbar stuff

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.useractions.*;

public class MainToolBar
	extends JToolBar
{
	private static Supremica supremica;

	private static final OpenAction openAction = new OpenAction();
	private static final SaveAction saveAction = new SaveAction();
	private static final SaveAsAction saveAsAction = new SaveAsAction();
	private static final EditAction editAction = new EditAction(supremica);
	// private static final HelpAction helpAction = new HelpAction();

	public MainToolBar(Supremica supremica)
	{
		this.supremica = supremica;

		initToolBar();
		setRollover(true);
	}

	public void initToolBar()
	{
		Insets tmpInsets = new Insets(0, 0, 0, 0);

		if (SupremicaProperties.fileAllowOpen())
		{
			add(openAction);
		}

		if (SupremicaProperties.fileAllowSave())
		{
			add(saveAction);
			add(saveAsAction);
			addSeparator();
		}

		// Tools.AutomataEditor
		if (SupremicaProperties.includeEditor())
		{
			add(editAction);
			addSeparator();
		}

		add(ActionMan.helpAction);
	}
}

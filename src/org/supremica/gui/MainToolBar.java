
/***************** MainToolBar.java ********************/

// Free standing leaf class implementing Supremicas
// main toolbar. Prime reason for this is easy access
// The class instantiates itself with the toolbar stuff
package org.supremica.gui;

import java.awt.*;
import javax.swing.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.useractions.*;

public class MainToolBar
	extends JToolBar
{
	private static final long serialVersionUID = 1L;
	private static Supremica supremica;
	private static final OpenAction openAction = new OpenAction();
	private static final SaveAction saveAction = new SaveAction();
	private static final SaveAsAction saveAsAction = new SaveAsAction();
	private static final DeleteAction deleteAction = new DeleteAction();
	private static final CopyAction copyAction = new CopyAction();
	private static final ViewAction viewAction = new ViewAction();
	private static final StatusAction statusAction = new StatusAction();

	private static final MoveAutomataAction moveAutomataToTopAction = new MoveAutomataAction(true, true);
	private static final MoveAutomataAction moveAutomataUpAction = new MoveAutomataAction(true, false);
	private static final MoveAutomataAction moveAutomataDownAction = new MoveAutomataAction(false, false);
	private static final MoveAutomataAction moveAutomataToBottomAction = new MoveAutomataAction(false, true);

	private static final PreferencesAction preferencesAction = new PreferencesAction();
	private static final EditAction editAction = new EditAction(supremica);

	private static final Insets theInsets = new Insets(0, 0, 0, 0);

	public MainToolBar(Supremica supremica)
	{
		MainToolBar.supremica = supremica;

		initToolBar();
		setRollover(true);
	}

	private void initToolBar()
	{
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

		add(deleteAction);
		add(copyAction);
		addSeparator();
		add(viewAction);

		// add(statusAction);
		addSeparator();
		add(moveAutomataToTopAction);
		add(moveAutomataUpAction);
		add(moveAutomataDownAction);
		add(moveAutomataToBottomAction);
		addSeparator();

		if (SupremicaProperties.includeJGrafchart())
		{
			add(ActionMan.openJGrafchartAction);
			add(ActionMan.updateFromJGrafchartAction);
			addSeparator();
		}

		add(preferencesAction);
		addSeparator();

		// Tools.AutomataEditor
		if (SupremicaProperties.includeEditor())
		{
			add(editAction);
			addSeparator();
		}

		add(ActionMan.helpAction);
	}

	/**
	 * Set the button margin
	 */
	public JButton add(Action theAction)
	{
		JButton theButton = super.add(theAction);

		theButton.setMargin(theInsets);

		return theButton;
	}
}

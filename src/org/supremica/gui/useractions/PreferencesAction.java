
/******************* PropertiesAction.java *********************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class PreferencesAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public PreferencesAction()
	{
		super("Preferences...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Preferences16.gif")));

		putValue(SHORT_DESCRIPTION, "Edit preferences");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.configurePreferences_actionPerformed(ActionMan.getGui());
	}
}

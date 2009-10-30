
/******************** SynthesizeAction.java *****************/

// Encapuslates the synthesize action
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.ActionMan;

public class SynthesizeAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public SynthesizeAction()
	{
		super("Synthesize...", null);

		putValue(SHORT_DESCRIPTION, "Synthesize supervisor(s)");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.automataSynthesize_actionPerformed(ActionMan.getGui());
	}
}

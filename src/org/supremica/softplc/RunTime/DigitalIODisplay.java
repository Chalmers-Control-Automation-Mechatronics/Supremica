package org.supremica.softplc.RunTime;

import javax.swing.UIManager;

/**
 * Title:
 * Description:
 * @author Niclas Hamp
 * @version 1.0
 */
public class DigitalIODisplay
{
	boolean packFrame = false;

	/**Construct the application*/
	public DigitalIODisplay(String dynClass)
		throws Exception
	{
		DigitalIODisplayView frame = new DigitalIODisplayView(dynClass);

		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame)
		{
			frame.pack();
		}
		else
		{
			frame.validate();
		}

		frame.setVisible(true);
	}

	/**Main method*/
	public static void main(String[] args)
		throws Exception
	{
		/* Send BTSim or AdlinkPCI7432 as argument */
		String dynClass = new String();

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		if (args[0].length() == 0)
		{
			dynClass = "BTSim";
		}
		else
		{
			dynClass = args[0];
		}

		new DigitalIODisplay(dynClass);
	}
}

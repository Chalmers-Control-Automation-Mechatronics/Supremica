package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class SimulatorPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private String title;

	SimulatorPanel(IDE ide, String title)
	{
		this.ide = ide;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}
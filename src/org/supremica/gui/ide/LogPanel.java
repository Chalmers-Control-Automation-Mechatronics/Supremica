package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;
import java.awt.Dimension;
import org.supremica.log.*;

class LogPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private String title;

	LogPanel(IDE ide, String title)
	{
		super(LogDisplay.getInstance().getComponentWithoutScrollPane());
		this.ide = ide;
		this.title = title;
		setPreferredSize(IDEDimensions.loggerPreferredSize);
		setMinimumSize(IDEDimensions.loggerMinimumSize);
		updateUI();
	}

	public String getTitle()
	{
		return title;
	}

}
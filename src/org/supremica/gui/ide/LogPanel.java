package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;
import java.awt.Dimension;

class LogPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private String title;

	LogPanel(IDE ide, String title)
	{
		this.ide = ide;
		this.title = title;
		setPreferredSize(new Dimension(900, 100));
		setMinimumSize(new Dimension(100, 10));
	}

	public String getTitle()
	{
		return title;
	}

}
package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class AnalyzerPanel
	extends WhiteScrollPane
{
	private String title;

	AnalyzerPanel(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}
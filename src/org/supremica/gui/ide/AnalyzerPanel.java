package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class AnalyzerPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private String title;

	AnalyzerPanel(IDE ide, String title)
	{
		this.ide = ide;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}
package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class AnalyzerPanel
	extends WhiteScrollPane
{
	private ModuleContainer moduleContainer;
	private String name;

	AnalyzerPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

}
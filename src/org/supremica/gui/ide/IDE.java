
package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.Utility;
import org.supremica.gui.ide.actions.Actions;

public class IDE
    extends JFrame
{
	private Actions theActions;
	private IDEMenuBar menuBar;
	private IDEToolBar toolBar;
	private EditorPanel editorPanel;
	private AnalyzerPanel analyzerPanel;
	private LogPanel logPanel;

    public IDE()
    {
		Utility.setupFrame(this, 900, 700);
		setTitle("Supremica with Waters");

		theActions = new Actions(this);

    	menuBar = new IDEMenuBar(this);
    	toolBar = new IDEToolBar(this);

		editorPanel = new EditorPanel(this, "Editor");
		analyzerPanel = new AnalyzerPanel(this, "Analyzer");
		logPanel = new LogPanel(this, "Logger");
    }

	public Actions getActions()
	{
		return theActions;
	}

}

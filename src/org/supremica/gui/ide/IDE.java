
package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import org.supremica.gui.Utility;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.ide.actions.Actions;

public class IDE
    extends JFrame
{
	private final static InterfaceManager interfaceManager = InterfaceManager.getInstance();

	private Actions theActions;

	private JPanel contentPanel;
	private BorderLayout contentLayout;

	private IDEMenuBar menuBar;
	private IDEToolBar toolBar;
	private EditorPanel editorPanel;
	private AnalyzerPanel analyzerPanel;
	private LogPanel logPanel;

	private JTabbedPane tabPanel;
	private JSplitPane splitPanelVertical;

    public IDE()
    {
		Utility.setupFrame(this, 900, 700);
		setTitle("Supremica with Waters");

		contentPanel = (JPanel)getContentPane();
		contentLayout = new BorderLayout();
		contentPanel.setLayout(contentLayout);

		theActions = new Actions(this);

    	menuBar = new IDEMenuBar(this);
    	setJMenuBar(menuBar);

    	toolBar = new IDEToolBar(this);
    	contentPanel.add(toolBar, BorderLayout.NORTH);

		tabPanel = new JTabbedPane();

		editorPanel = new EditorPanel(this, "Editor");
		tabPanel.add(editorPanel.getTitle(), editorPanel);

		analyzerPanel = new AnalyzerPanel(this, "Analyzer");
		tabPanel.add(analyzerPanel.getTitle(), analyzerPanel);

		logPanel = new LogPanel(this, "Logger");

		splitPanelVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPanel, logPanel);
		splitPanelVertical.setContinuousLayout(false);
		splitPanelVertical.setOneTouchExpandable(false);

		contentPanel.add(splitPanelVertical, BorderLayout.CENTER);

    }

	public Actions getActions()
	{
		return theActions;
	}


	// Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			getActions().exitAction.doAction();
		}
	}

	public static void main(String args[])
	{
		IDE ide = new IDE();
		ide.setVisible(true);
	}

}


package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.util.*;
import org.supremica.gui.Utility;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.ide.actions.Actions;
import net.sourceforge.waters.model.module.ModuleProxy;

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
	private SimulatorPanel simulatorPanel;
	private LogPanel logPanel;

	private JTabbedPane tabPanel;
	private JSplitPane splitPanelVertical;

	private LinkedList modules = new LinkedList();

	private final String ideName = "Supremica with Waters";
	private int newModuleCounter = 1;

    public IDE()
    {
		Utility.setupFrame(this, 900, 700);
		setTitle(ideName);

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

		simulatorPanel = new SimulatorPanel(this, "Simulator");
		tabPanel.add(simulatorPanel.getTitle(), simulatorPanel);

		logPanel = new LogPanel(this, "Logger");

		splitPanelVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPanel, logPanel);
		splitPanelVertical.setContinuousLayout(false);
		splitPanelVertical.setOneTouchExpandable(false);
		splitPanelVertical.setDividerLocation(0.8);

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

	public String getNewModuleName(String prefix)
	{
		String nameSuggestion = prefix + newModuleCounter++;
		while (getModule(nameSuggestion) != null)
		{
			nameSuggestion = prefix + newModuleCounter++;
		}
		return nameSuggestion;

	}

	public ModuleProxy getModule(String name)
	{
		for(Iterator modIt = modules.iterator(); modIt.hasNext(); )
		{
			ModuleProxy currModule = (ModuleProxy)modIt.next();
			if (name.equals(currModule.getName()))
			{
				return currModule;
			}
		}
		return null;
	}

	public void add(ModuleProxy module)
	{
		modules.addFirst(module);
		setActiveModule(module);
	}

	public void remove(ModuleProxy module)
	{
		modules.remove(module);
	}

	public void setActiveModule(ModuleProxy module)
	{
		if (getActiveModule() != module)
		{
			remove(module);
			add(module);
		}
		setTitle(ideName + " [" + module.getName() + "]");
	}

	public ModuleProxy getActiveModule()
	{
		return (ModuleProxy)modules.getFirst();
	}

	public static void main(String args[])
	{
		IDE ide = new IDE();
		ide.setVisible(true);
	}

}


package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.util.*;
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

	private ModuleContainers moduleContainers;

	private LogPanel logPanel;

	private JTabbedPane tabPanel;
	private JSplitPane splitPanelVertical;

	private final String ideName = "Supremica with Waters";

    public IDE()
    {
		Utility.setupFrame(this, 900, 700);
		setTitle(ideName);
		moduleContainers = new ModuleContainers(this);

		contentPanel = (JPanel)getContentPane();
		contentLayout = new BorderLayout();
		contentPanel.setLayout(contentLayout);

		theActions = new Actions(this);

    	menuBar = new IDEMenuBar(this);
    	setJMenuBar(menuBar);

    	toolBar = new IDEToolBar(this);
    	contentPanel.add(toolBar, BorderLayout.NORTH);

		tabPanel = new JTabbedPane();

		ModuleContainer currModuleContainer = moduleContainers.getActiveModuleContainer();
		tabPanel.add(currModuleContainer.getEditorPanel());
		tabPanel.add(currModuleContainer.getAnalyzerPanel());
		tabPanel.add(currModuleContainer.getSimulatorPanel());

		logPanel = new LogPanel(this, "Logger");

		splitPanelVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPanel, logPanel);
		splitPanelVertical.setContinuousLayout(false);
		splitPanelVertical.setOneTouchExpandable(false);
		splitPanelVertical.setDividerLocation(0.8);

		contentPanel.add(splitPanelVertical, BorderLayout.CENTER);

//		pack();

    }

	public Actions getActions()
	{
		return theActions;
	}

	public Iterator moduleContainerIterator()
	{
		return moduleContainers.iterator();
	}

	public void add(ModuleContainer moduleContainer)
	{
		moduleContainers.add(moduleContainer);
	}

	public void remove(ModuleContainer moduleContainer)
	{
		if (moduleContainers.size() >= 2)
		{
			moduleContainers.remove(moduleContainer);
		}
	}

	public ModuleContainer getActiveModuleContainer()
	{
		return moduleContainers.getActiveModuleContainer();
	}

	public void setActive(ModuleContainer moduleContainer)
	{
		ModuleContainer oldModuleContainer = getActiveModuleContainer();
		if (moduleContainer != oldModuleContainer)
		{
			tabPanel.remove(oldModuleContainer.getEditorPanel());
			tabPanel.remove(oldModuleContainer.getAnalyzerPanel());
			tabPanel.remove(oldModuleContainer.getSimulatorPanel());

			moduleContainers.setActive(moduleContainer);

			tabPanel.add(moduleContainer.getEditorPanel());
			tabPanel.add(moduleContainer.getAnalyzerPanel());
			tabPanel.add(moduleContainer.getSimulatorPanel());
		}
	}

	public String getIDEName()
	{
		return ideName;
	}

	public ModuleContainer createNewModuleContainer()
	{
		return moduleContainers.createNewModuleContainer();
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

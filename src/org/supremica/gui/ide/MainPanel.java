package org.supremica.gui.ide;

import javax.swing.JToolBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JComponent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.actions.Actions;

abstract class MainPanel
	extends JPanel
{
	private IDEToolBar thisToolBar = null;
	private IDEToolBar currParentToolBar = null;

	private GridBagConstraints constraints = new GridBagConstraints();

	private EmptyRightPanel emptyRightPanel = new EmptyRightPanel();

	private ModuleContainer moduleContainer;
	private String name;

	protected JSplitPane splitPanelHorizontal;

	public MainPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		setPreferredSize(IDEDimensions.mainPanelPreferredSize);
		setMinimumSize(IDEDimensions.mainPanelMinimumSize);

		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);

		constraints.gridy = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
	}

	public String getName()
	{
		return name;
	}

	public Actions getActions()
	{
		return moduleContainer.getActions();
	}

	protected GridBagConstraints getGridBagConstraints()
	{
		return constraints;
	}

	public abstract void addToolBarEntries(IDEToolBar toolbar);

	public void setRightComponent(JComponent newComponent)
	{
		JComponent oldComponent = getRightComponent();
		if (oldComponent != newComponent)
		{
			JScrollPane emptyRightPanel = getEmptyRightPanel();
			Dimension oldSize = emptyRightPanel.getSize();

			if (oldComponent != null)
			{
				splitPanelHorizontal.remove(oldComponent);
				oldSize = oldComponent.getSize();
			}

			if (newComponent == null)
			{
//				emptyRightPanel.setPreferredScrollableViewportSize(oldSize);
				emptyRightPanel.setPreferredSize(oldSize);
				splitPanelHorizontal.setRightComponent(emptyRightPanel);
			}
			else
			{
//				newComponent.setPreferredScrollableViewportSize(oldSize);
				newComponent.setPreferredSize(oldSize);
				splitPanelHorizontal.setRightComponent(newComponent);
			}
		}
		validate();
	}

	public JComponent getRightComponent()
	{
		return (JComponent)splitPanelHorizontal.getRightComponent();
	}

	public JScrollPane getEmptyRightPanel()
	{
		return emptyRightPanel;
	}

	public JToolBar getToolBar(JToolBar parentToolBar)
	{
		if (parentToolBar instanceof IDEToolBar)
		{
			if (parentToolBar == currParentToolBar)
			{
				return thisToolBar;
			}
			thisToolBar = new IDEToolBar((IDEToolBar)parentToolBar);

			addToolBarEntries(thisToolBar);

			currParentToolBar = (IDEToolBar)parentToolBar;
			return thisToolBar;
		}
		return null;
	}


 	class EmptyRightPanel
 		extends WhiteScrollPane
 	{
		public EmptyRightPanel()
		{
			setPreferredSize(IDEDimensions.rightEmptyPreferredSize);
			setMinimumSize(IDEDimensions.rightEmptyMinimumSize);
		}
	}
}
/***************** MainToolBar.java ********************/
// Free standing leaf class implementing Supremicas
// main toolbar. Prime reason for this is easy access
// The class instantiates itself with the toolbar stuff

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

// import org.supremica.gui.help.ContentHelp;
import org.supremica.properties.SupremicaProperties;

// Prototypical implementations, should be in own files and shared between menus, but for now...
class OpenAction
	extends AbstractAction
{
	public OpenAction()
	{
		super("Open...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
		putValue(SHORT_DESCRIPTION, "Open a new project");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileOpen(ActionMan.getGui());
	}

}

class SaveAction
	extends AbstractAction
{
	public SaveAction()
	{
		super("Save...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
		putValue(SHORT_DESCRIPTION, "Save this project");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileSave(ActionMan.getGui());
	}

}

class SaveAsAction
	extends AbstractAction
{
	public SaveAsAction()
	{
		super("Save As...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/SaveAs16.gif")));
		putValue(SHORT_DESCRIPTION, "Save this project");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileSaveAs(ActionMan.getGui());
	}

}
class EditAction
	extends AbstractAction
{
	private Supremica supremica;
	
	public EditAction(Supremica supremica)
	{
		super("Edit...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")));
		putValue(SHORT_DESCRIPTION, "Open automata editor");
		this.supremica = supremica;
	}

	public void actionPerformed(ActionEvent e)
	{
		supremica.toolsAutomataEditor();
	}

}



public class MainToolBar
	extends JToolBar
{
	private static Supremica supremica;
// 	private ContentHelp help = null;
//	private CSH.DisplayHelpFromSource helpDisplayer = null;

	private static final OpenAction openAction = new OpenAction();
	private static final SaveAction saveAction = new SaveAction();
	private static final SaveAsAction saveAsAction = new SaveAsAction();
	private static final EditAction editAction = new EditAction(supremica);
	// private static final HelpAction helpAction = new HelpAction();	
	
	public MainToolBar(Supremica supremica)
	{
		this.supremica = supremica;
//		this.help = new ContentHelp();
//		this.helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());

		initToolBar();
		setRollover(true);
	}

	public void initToolBar()
	{
		Insets tmpInsets = new Insets(0, 0, 0, 0);

		if (SupremicaProperties.fileAllowOpen())
		{
			add(openAction);
			/*
			// Create buttons
			JButton openButton = new JButton();

			openButton.setToolTipText("Open");

			ImageIcon open16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Open16.gif"));

			openButton.setIcon(open16Img);
			openButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileOpen(ActionMan.getGui());
				}
			});
			openButton.setMargin(tmpInsets);
			add(openButton, "WEST");*/
		}

		if (SupremicaProperties.fileAllowSave())
		{
			add(saveAction);
			add(saveAsAction);
			/*
			JButton saveButton = new JButton();

			saveButton.setToolTipText("Save");

			ImageIcon save16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Save16.gif"));

			saveButton.setIcon(save16Img);
			saveButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSave(ActionMan.getGui());
				}
			});

			JButton saveAsButton = new JButton();

			saveAsButton.setToolTipText("Save As");

			ImageIcon saveAs16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/SaveAs16.gif"));

			saveAsButton.setIcon(saveAs16Img);
			saveAsButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSaveAs(ActionMan.getGui());
				}
			});
			saveButton.setMargin(tmpInsets);
			saveAsButton.setMargin(tmpInsets);
			add(saveButton, "WEST");
			add(saveAsButton, "WEST");*/
			addSeparator();
		}

		// Tools.AutomataEditor
		if (SupremicaProperties.includeEditor())
		{
			add(editAction);
			/*JButton editButton = new JButton();

			editButton.setToolTipText("Edit");

			ImageIcon edit16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Edit16.gif"));

			editButton.setIcon(edit16Img);
			editButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					supremica.toolsAutomataEditor();
				}
			});
			editButton.setMargin(tmpInsets);
			add(editButton, "WEST");*/
			addSeparator();
		}

		add(ActionMan.helpAction);
		/*JButton helpButton = new JButton();

		helpButton.setToolTipText("Help");

		ImageIcon help16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Help16.gif"));

		helpButton.setIcon(help16Img);
		helpButton.addActionListener(helpDisplayer);
		helpButton.setMargin(tmpInsets);
		add(helpButton, "EAST");*/
	}
}

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;

import org.supremica.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;

class MainPopupMenu extends JPopupMenu
{
	private MenuHandler menuHandler = null;

	// local utilities
	private Gui getGui() { return (Gui)getInvoker(); }

	// except for access, these are copied straight from gui.Supremica
	private void initPopups()
	{
		JMenuItem selectAllItem = new JMenuItem("Select all");
		menuHandler.add(selectAllItem, 0);

		menuHandler.addSeparator();

		JMenuItem statusItem = new JMenuItem("Status");
		menuHandler.add(statusItem, 0);

		JMenuItem exploreItem = new JMenuItem("Explore states");
		menuHandler.add(exploreItem, 1);

		JMenuItem alphabetItem = new JMenuItem("View alphabet");
		menuHandler.add(alphabetItem, 1);

		if (WorkbenchProperties.useDot())
		{
			JMenuItem viewItem = new JMenuItem("View graph");
			menuHandler.add(viewItem, 1);

			viewItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.automatonView_actionPerformed(getGui());
						getGui().repaint();
					}
				});
		}

		menuHandler.addSeparator();

		JMenuItem synchronizeItem = new JMenuItem("Synchronize");
		menuHandler.add(synchronizeItem, 2);

		JMenuItem verifyItem = new JMenuItem("Verify");
		menuHandler.add(verifyItem, 1);

		JMenuItem synthesizeItem = new JMenuItem("Synthesize");
		menuHandler.add(synthesizeItem, 1);

		menuHandler.addSeparator();

		JMenuItem purgeItem = new JMenuItem("Purge");
		menuHandler.add(purgeItem, 1);

		JMenuItem minimizeItem = new JMenuItem("Minimize");
		menuHandler.add(minimizeItem, 1);

		JMenuItem allAcceptingItem = new JMenuItem("Set all states as accepting");
		menuHandler.add(allAcceptingItem, 1);

		JMenuItem complementItem = new JMenuItem("Automaton complement");
		menuHandler.add(complementItem, 1);

		menuHandler.addSeparator();

		if (WorkbenchProperties.includeBoundedUnconTools())
		{
			JMenuItem extendItem = new JMenuItem("Extend");
			menuHandler.add(extendItem, 1);

			JMenuItem removePassItem = new JMenuItem("Remove pass events");
			menuHandler.add(removePassItem, 1);

			JMenuItem addSelfLoopArcsItem = new JMenuItem("Add self-loop arcs");
			menuHandler.add(addSelfLoopArcsItem, 1);

			JMenuItem removeSelfLoopArcsItem = new JMenuItem("Remove self-loop arcs");
			menuHandler.add(removeSelfLoopArcsItem, 1);

			JMenuItem normalizeAlphabetItem = new JMenuItem("Normalize alphabet");
			menuHandler.add(normalizeAlphabetItem, 1);

			menuHandler.addSeparator();

			extendItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.automataExtend_actionPerformed(getGui());
						getGui().repaint();
					}
				});

			removePassItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.automataRemovePass_actionPerformed(getGui());
						getGui().repaint();
					}
				});

			addSelfLoopArcsItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.automataAddSelfLoopArcs_actionPerformed(getGui());
						getGui().repaint();
					}
				});

			removeSelfLoopArcsItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.automataRemoveSelfLoopArcs_actionPerformed(getGui());
						getGui().repaint();
					}
				});

			normalizeAlphabetItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.normalizeAlphabet_actionPerformed(getGui());
						getGui().repaint();
					}
				});
		}

		JMenuItem alphabetAnalyzerItem = new JMenuItem("Analyze alphabets");
		menuHandler.add(alphabetAnalyzerItem, 2);

		alphabetAnalyzerItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.alphabetAnalyzer_actionPerformed(getGui());
					getGui().repaint();
				}
			});

	//** MF Find States **
		JMenuItem findStatesItem = new JMenuItem("Find States...");
		menuHandler.add(findStatesItem, 1);
		findStatesItem.addActionListener(new ActionListener()
		{	// anonymous class (is this a good thing?)
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.findStates_action(getGui());
				getGui().repaint();
			}
		});

		menuHandler.addSeparator(); //----------------------------------------------


		JMenuItem copyItem = new JMenuItem("Copy");
		menuHandler.add(copyItem, 1);

		JMenuItem deleteItem = new JMenuItem("Delete");
		menuHandler.add(deleteItem, 1);

		JMenuItem deleteAllItem = new JMenuItem("Delete all");
		menuHandler.add(deleteAllItem, 0);

		JMenuItem renameItem = new JMenuItem("Rename");
		menuHandler.add(renameItem, 1);

		menuHandler.addSeparator();


		// JMenuItem saveAsItem = new JMenuItem("Save As...");
		// menuHandler.add(saveAsItem, 1);

		if (WorkbenchProperties.fileAllowExport())
		{
			JMenuItem exportItem = new JMenuItem("Export...");
			menuHandler.add(exportItem, 1);
			exportItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.automataExport(getGui());
						getGui().repaint();
					}
				});
		}

		selectAllItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.selectAll_actionPerformed(getGui());
				}
			});

		statusItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automatonStatus_actionPerformed(getGui());
				}
			});

		exploreItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automatonExplore_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		alphabetItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automatonAlphabet_actionPerformed(getGui());
					getGui().repaint();
				}
			});


		synchronizeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataSynchronize_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		verifyItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataVerify_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		synthesizeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataSynthesize_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		purgeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataPurge_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		allAcceptingItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataAllAccepting_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		complementItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataComplement_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		minimizeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automatonMinimize_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		copyItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataCopy_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		deleteItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataDelete_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		deleteAllItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataDeleteAll_actionPerformed(getGui());
					getGui().repaint();
				}
			});

		renameItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataRename_actionPerformed(getGui());
					getGui().repaint();
				}
			});




// Here is revelaed that this one knows that the interface is built around a table. Not good!
// It should popup when ordered so by the gui, not de ide for itself when to
// It should have no notion of rows/cols, these thinsg it should get from the gui
/***
		theAutomatonTable.addMouseListener(new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
				{
					// This is needed for the Linux platform
					// where isPopupTrigger is true only on mousePressed.
					maybeShowPopup(e);
				}

				public void mouseReleased(MouseEvent e)
				{
					// This is for triggering the popup on Windows platforms
					maybeShowPopup(e);
				}

				private void maybeShowPopup(MouseEvent e)
				{
					if (e.isPopupTrigger())
					{
						int currRow = theAutomatonTable.rowAtPoint(new Point(e.getX(), e.getY()));
						if (currRow < 0)
						{
							return;
						}
						if (!theAutomatonTable.isRowSelected(currRow))
						{
							theAutomatonTable.clearSelection();
							theAutomatonTable.setRowSelectionInterval(currRow, currRow);
						}
						JPopupMenu regionPopup = menuHandler.getDisabledPopupMenu(theAutomatonTable);
						regionPopup.show(e.getComponent(),
										 e.getX(), e.getY());
					}
				}
			});
	**********/
	}
	public void show(int num_selected, Component c, int x, int y)
	{
		JPopupMenu regionPopup = menuHandler.getDisabledPopupMenu(num_selected);
		regionPopup.show(c, x, y);
	}

	public MainPopupMenu(Gui gui)
	{
		setInvoker(gui.getFrame());
		menuHandler = new MenuHandler();
		initPopups();
	}
}


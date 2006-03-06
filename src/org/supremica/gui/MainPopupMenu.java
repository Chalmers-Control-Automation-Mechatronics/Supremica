
/*********************** MainPopupMenu.java *****************/

// Free standing leaf class implementing Supremicas
// main popup menu. Prime reason for this is easy access
// The class instantiates itself with the menu stuff
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.useractions.*;
import org.supremica.util.VPopupMenu;
import org.supremica.util.SupremicaMenuItem;
import org.supremica.log.*;

class MainPopupMenu
	extends VPopupMenu
{
    private static Logger logger = LoggerFactory.createLogger(MainPopupMenu.class);

	private static final long serialVersionUID = 1L;
	private MenuHandler menuHandler = null;

	// local utilities
	private Gui getGui()
	{
		return (Gui) getInvoker();
	}

	// except for access, these are copied straight from gui.Supremica
	private void initPopups()
		throws Exception
	{
		JMenuItem selectAllItem = new JMenuItem("Select all");

		menuHandler.add(selectAllItem, 0);
		menuHandler.addSeparator();

		JMenuItem statusItem = new JMenuItem("Statistics");
		statusItem.setToolTipText("Displays some statistics of the selected automata");
		menuHandler.add(statusItem, 0);

		JMenuItem exploreItem = new JMenuItem("Explore states");
		exploreItem.setToolTipText("Explore states one by one interactively");
		menuHandler.add(exploreItem, 1);

		JMenu viewMenu = new JMenu("View");
		menuHandler.add(viewMenu, 1);

		if (SupremicaProperties.useDot())
		{
			JMenuItem viewItem = new JMenuItem("View automaton");
			viewItem.setToolTipText("Display graphical representation of the selected automata");
			//menuHandler.add(viewItem, 1);
			viewMenu.add(viewItem);

			viewItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automatonView_actionPerformed(getGui());
					getGui().repaint();
				}
			});
		}

		JMenuItem hierarchyItem = new JMenuItem("View modular structure");
		hierarchyItem.setToolTipText("Display graphically the connections between different modules");
		//menuHandler.add(hierarchyItem, 1);
		viewMenu.add(hierarchyItem);

		JMenuItem alphabetItem = new JMenuItem("View alphabet");
		alphabetItem.setToolTipText("Display information about the alphabets of the selected automata");
		//menuHandler.add(alphabetItem, 1);
		viewMenu.add(alphabetItem);

		JMenuItem statesItem = new JMenuItem("View states");
		statesItem.setToolTipText("Display information about the states of the selected automata");
		//menuHandler.add(statesItem, 1);
		viewMenu.add(statesItem);

		menuHandler.addSeparator();

		JMenuItem synchronizeItem = new JMenuItem("Synchronize...");
		synchronizeItem.setToolTipText("Calculate the synchronous composition of the selected automata");
		menuHandler.add(synchronizeItem, 2);
		synchronizeItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automataSynchronize_actionPerformed(getGui());
				getGui().repaint();
			}
		});

		JMenuItem verifyItem = new JMenuItem("Verify...");
		verifyItem.setToolTipText("Verify properties");
		verifyItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataVerify_actionPerformed(getGui());
					getGui().repaint();
				}
			});
		
		JMenuItem synthesizeItem = new SupremicaMenuItem(ActionMan.synthesizeAction);
		synthesizeItem.setToolTipText("Synthesize supervisor");
			
		if (SupremicaProperties.getStudentVersion())
		{
			verifyItem.setToolTipText("Verification is disabled--use the Workbench!");
			synthesizeItem.setToolTipText("Synthesis is disabled--use the Workbench!");
			menuHandler.add(verifyItem, menuHandler.DISABLED);
			menuHandler.add(synthesizeItem, menuHandler.DISABLED);
		}
		else
		{
			menuHandler.add(verifyItem, 1);
			menuHandler.add(synthesizeItem, 1);
		}

		JMenuItem minimizeItem = new JMenuItem("Minimize...");
		minimizeItem.setToolTipText("Minimize automata");
		menuHandler.add(minimizeItem, 1);
		minimizeItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automatonMinimize_actionPerformed(getGui());
				getGui().repaint();
			}
		});

		menuHandler.addSeparator();

		JMenuItem workbench = new SupremicaMenuItem(new WorkbenchAction());
		menuHandler.add(workbench, 1);
		// JMenuItem testbench = new SupremicaMenuItem(new TestBenchAction());
		// menuHandler.add(testbench, 1);

		menuHandler.addSeparator();

		JMenuItem purgeItem = new JMenuItem("Purge");
		purgeItem.setToolTipText("Remove all states marked as forbidden");
		menuHandler.add(purgeItem, 1);

		// These are the "standard" algorithms
		// Submenu stuff won't work here, the menuHandler concept has painted us into a corner
		// ** This has to be reworked ** Use the Action concept instead **
		// JMenu standardalgos = JMenu("Standard Algorithms");
		// menuHandler.add(standardalgos, 0);

		/* These are rarely if ever used... 
		JMenuItem allAcceptingItem = new JMenuItem("Set all states as accepting");
		allAcceptingItem.setToolTipText("Make all states accepting (marked)");
		menuHandler.add(allAcceptingItem, 1);
		allAcceptingItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automataAllAccepting_actionPerformed(getGui());
				getGui().repaint();
			}
		});

		JMenuItem stateEnumerator = new JMenuItem(ActionMan.stateEnumerator);
		menuHandler.add(stateEnumerator, 1);

		JMenuItem complementItem = new JMenuItem("Automaton complement");
		complementItem.setToolTipText("Generate an automaton with complementary marked language");
		menuHandler.add(complementItem, 1);
		complementItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automataComplement_actionPerformed(getGui());
				getGui().repaint();
			}
		});

		// Do this...
		JMenuItem languageRestrictor = new SupremicaMenuItem(ActionMan.languageRestrictor);
		menuHandler.add(languageRestrictor, 1);
		*/

		// Do this...
		JMenuItem eventHider = new SupremicaMenuItem(ActionMan.eventHider);
		menuHandler.add(eventHider, 1);

		/* ...and you can forget about this
		languageRestrictor.addActionListener(new ActionListener()
		{
				public void actionPerformed(ActionEvent e)
				{
						ActionMan.languageRestrictor_actionPerformed(getGui());
						getGui().repaint();
				}
		});*/

		/*
		JMenuItem interfaceItem = new JMenuItem("Interface Properties...");
		menuHandler.add(interfaceItem, 1);
		menuHandler.addSeparator();
		interfaceItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automatonUpdateInterface_actionPerformed(getGui());
				getGui().repaint();
			}
		});
		*/

		if (SupremicaProperties.includeBoundedUnconTools())
		{
			JMenuItem extendItem = new JMenuItem("Extend");
			menuHandler.add(extendItem, 1);
			extendItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataExtend_actionPerformed(getGui());
					getGui().repaint();
				}
			});

			JMenuItem liftingItem = new JMenuItem("Compute lifting automaton");
			menuHandler.add(liftingItem, 1);
			liftingItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataLifting_actionPerformed(getGui());
					getGui().repaint();
				}
			});

			JMenuItem removePassItem = new JMenuItem("Remove pass events");
			menuHandler.add(removePassItem, 1);
			removePassItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataRemovePass_actionPerformed(getGui());
					getGui().repaint();
				}
			});

			JMenuItem addSelfLoopArcsItem = new JMenuItem("Add self-loop arcs");
			addSelfLoopArcsItem.setToolTipText("Add self loops so that each state has the whole alphabet elabled");
			menuHandler.add(addSelfLoopArcsItem, 1);
			addSelfLoopArcsItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataAddSelfLoopArcs_actionPerformed(getGui());
					getGui().repaint();
				}
			});

			JMenuItem removeSelfLoopArcsItem = new JMenuItem("Remove self-loop arcs");
			removeSelfLoopArcsItem.setToolTipText("Remove all self-loops");
			menuHandler.add(removeSelfLoopArcsItem, 1);
			removeSelfLoopArcsItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.automataRemoveSelfLoopArcs_actionPerformed(getGui());
					getGui().repaint();
				}
			});

			JMenuItem normalizeAlphabetItem = new JMenuItem("Normalize alphabet");
			menuHandler.add(normalizeAlphabetItem, 1);
			menuHandler.addSeparator();
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

		//-- MF Find States --
		JMenuItem findStatesItem = new JMenuItem(ActionMan.findStates);

		menuHandler.add(findStatesItem, 1);

/*              findStatesItem.addActionListener(new ActionListener()
				{

						// anonymous class (is this a good thing?)
						public void actionPerformed(ActionEvent e)
						{
								ActionMan.findStates_action(getGui());
								getGui().repaint();
						}
				});
*/
		menuHandler.addSeparator();

		// ----------------------------------------------
		JMenuItem copyItem = new JMenuItem("Copy");

		menuHandler.add(copyItem, 1);

		JMenuItem deleteItem = new JMenuItem("Delete");

		menuHandler.add(deleteItem, 1);

		JMenuItem deleteAllItem = new JMenuItem("Delete all");

		menuHandler.add(deleteAllItem, 0);

		//JMenuItem cropItem = new JMenuItem("Crop to selection");
		JMenuItem cropItem = new JMenuItem("Delete unselected");

		menuHandler.add(cropItem, 0);

		JMenuItem invertItem = new JMenuItem("Invert selection");

		menuHandler.add(invertItem, 0);

		JMenuItem renameItem = new JMenuItem("Rename");

		menuHandler.add(renameItem, 1);
		menuHandler.addSeparator();

		// JMenuItem saveAsItem = new JMenuItem("Save As...");
		// menuHandler.add(saveAsItem, 1);
		if (SupremicaProperties.fileAllowExport())
		{
			// This is how it would be done with an export command object
			// JMenuItem exportItem = new SupremicaMenuItem(ActionMan.exportItem);
			// menuHandler.add(exportItem, 1);
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

		// --------------------------------------------------------------
		// ***************** UNDER DEVELOPMENT MENUES ARE ADDED HERE:
		if (SupremicaProperties.includeExperimentalAlgorithms())
		{
			JMenu expMenu = new JMenu("Experimental algorithms");
			menuHandler.add(expMenu, 1);

			expMenu.add(new SupremicaMenuItem(new SaturateAction()));
			expMenu.add(new SupremicaMenuItem(new BuildObserverAction()));
			expMenu.add(new SupremicaMenuItem(new SplitAction()));

			expMenu.addSeparator();

			expMenu.add(new SupremicaMenuItem(new ScheduleAction()));
			
			JMenuItem mMd, mMmc, predictCompositionSize;

			expMenu.addSeparator();
			expMenu.add(mMd = new JMenuItem("Select dependency set"));
			expMenu.add(mMmc = new JMenuItem("Select maximal component"));
			expMenu.add(predictCompositionSize = new JMenuItem("Predict composition size"));
			mMd.setToolTipText("Select the automata that share events with the currently selected automata");
			mMd.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.markDependencySet();
				}
			});
			mMmc.setToolTipText("Selects all automata that are directly or indirectly connected to the selected automata");
			mMmc.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.markMaximalComponent();
				}
			});
			predictCompositionSize.setToolTipText("Predicts the size of the composition of two selected automata");
			predictCompositionSize.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.evoCompPredictSize();
				}
			});

			// BDD crap, sorry for the compressed lines... /Arash
			JMenuItem miR, miCR, miXXX;
			
			expMenu.addSeparator();
			expMenu.add(miR = new JMenuItem("BDD/Reachability"));
			miR.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.DoBDDReachability();
				}
			});
			expMenu.add(miCR = new JMenuItem("BDD/CoReachability"));
			miCR.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.DoBDDCoReachability();
				}
			});
			/*
			expMenu.add(miXXX = new JMenuItem("BDD/UnderConstructionAlgo"));
			miXXX.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.DoBDDUnderConstruction();
					}
			});
			*/
			expMenu.addSeparator();
		}

		// ------------------------------------------------------------------
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
		hierarchyItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.hierarchyView_actionPerformed(getGui());
				getGui().repaint();
			}
		});
		alphabetItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// ActionMan.automatonAlphabet_actionPerformed(getGui());
				ActionMan.alphabetView_actionPerformed(getGui());
				getGui().repaint();
			}
		});
		statesItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.statesView_actionPerformed(getGui());
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
		cropItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automataCrop_actionPerformed(getGui());
				ActionMan.selectAll_actionPerformed(getGui());
				getGui().repaint();
			}
		});
		invertItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.automataInvert_actionPerformed(getGui());
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
	}

	public void show(int num_selected, Component c, int x, int y)
	{
		JPopupMenu regionPopup = menuHandler.getDisabledPopupMenu(num_selected);

		regionPopup.show(c, x, y);
	}

	public MainPopupMenu(Gui gui)
	{
		setInvoker(gui.getFrame());

		// Ugly fixx, temporary
		ActionMan.gui = gui;
		menuHandler = new MenuHandler();

		try
		{
			initPopups();
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}
}

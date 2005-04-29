
/*************************** Workbench.java ***********************/

// Gui for the manual synthesis procedure we impose on the students
// Owner: MF
package org.supremica.workbench;

import java.awt.event.*;
import javax.swing.*;
import java.util.Iterator;
import java.util.LinkedList;
import org.supremica.log.*;
import org.supremica.gui.*;
import org.supremica.automata.algorithms.*;
import org.supremica.util.VerticalFlowLayout;
import org.supremica.automata.*;
import org.supremica.automata.IO.AutomatonToDot;
import org.supremica.automata.IO.AutomataSerializer;
import org.supremica.properties.SupremicaProperties;
// For debug
import org.supremica.testcases.StickPickingGame;

//*** Finally! A valid use for inheritance -- inherit to be reused!!
// We want the colors to be different from the default so we inherit
// and make our own AutoatonToDot serializer
// Had to change access to AutomatonToDot::getColor(), made it protected
// instead of private, but hey, can't think of everything
class MyAutomatonToDot
	extends AutomatonToDot
{
	public MyAutomatonToDot(Automaton automaton)
	{
		// logger.info("MyAutomatonToDot::constructed");
		super(automaton);
	}

	protected String getStateColor(State state)
	{
		/*
		if (state.isForbidden() && state.isSelected())
		{
			return ", color = red, style=bold";
		}

		return super.getStateColor(state);
		*/
		if(state.isSelected())
		{
			return super.getStateColor(state) + ", style=bold";
		}
		else
		{
			return super.getStateColor(state);
		}
	}
}

// Must also override AutomatonViewer::getSerializer()
// And this required adding AutomataViewer::getAutomaton()
class MyAutomatonViewer
	extends AutomatonViewer
{
	private static final long serialVersionUID = 1L;

	public MyAutomatonViewer(Automaton theAutomaton)
		throws Exception
	{
		// logger.info("MyAutomatonViewer::constructed");
		super(theAutomaton);
	}

	public AutomataSerializer getSerializer()
	{
		MyAutomatonToDot serializer = new MyAutomatonToDot(getAutomaton());

		serializer.setLeftToRight(leftToRightCheckBox.isSelected());
		serializer.setWithLabels(withLabelsCheckBox.isSelected());
		serializer.setWithCircles(withCirclesCheckBox.isSelected());
		serializer.setUseStateColors(useStateColorsCheckBox.isSelected());
		serializer.setUseArcColors(useArcColorsCheckBox.isSelected());

		return serializer;
	}
}

class MyAutomatonViewerFactory
	implements AutomatonViewerFactory
{
	public AutomatonViewer createAutomatonViewer(Automaton automaton)
		throws Exception
	{
		return new MyAutomatonViewer(automaton);
	}
}

/**
 * Parent class for workbench buttons.
 */
class ButtonImpl
	extends JButton
{
	private static final long serialVersionUID = 1L;

	Workbench wb = null;

	ButtonImpl(String text, Workbench wb, String tooltip)
	{
		super(text);

		this.wb = wb;

		setToolTipText(tooltip);
	}
}

/**
 * Synch button. Composes the automata selected in the gui, generating the first candidate
 * for a supervisor, a "total" specification.
 */
class SynchButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(SynchButton.class);

	SynchButton(Workbench wb)
	{
		super("Synch", wb, "Synchronize the selected automata (make total specification)");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	// Synchronize all. Note that we do not mark new forbidden states
	// Probably this will change the look of the graph after comparison, but what can we do?
	void action(ActionEvent e)
	{
		SynchronizationOptions synch_ops = SynchronizationOptions.getDefaultSynchronizationOptions();
		synch_ops.setForbidUncontrollableStates(false);
		synch_ops.setExpandForbiddenStates(true); // (may be explicit ones)
		synch_ops.setExpandEventsUsingPriority(false);
		synch_ops.setBuildAutomaton(true);
		synch_ops.setRequireConsistentControllability(true);
		synch_ops.setRequireConsistentImmediate(false);
		synch_ops.setRememberDisabledEvents(false); // don't redirect disabled events to dump-state

		try
		{    // wb.syncher *should* be null here - we assume this!
			wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops);

			wb.syncher.execute();

			wb.automaton = wb.syncher.getAutomaton();

			/* Which behavior is the "correct" one?
			 * If we set the name, the user won't be asked, not even if asame-named automaton already exists
			 * If we don't set the name, the user will be prompted with a suggested name, and prompted again until a unique name is given
			 */
			wb.automaton.setName("sup(" + wb.automaton.getComment() + ")");
			wb.automaton.setType(AutomatonType.Supervisor);
			//ActionMan.getGui().addAutomaton(wb.automaton);

			// Note that there may be explicitly specified uc-states
			// These are not "new" in a direct sense, but...
			wb.showMessage(wb.automaton.nbrOfForbiddenStates() +
						   " forbidden state(s) (out of " + wb.automaton.nbrOfStates() + ")");
			wb.showGraph();

			wb.updateButtons();
		}
		catch (Exception excp)
		{

			// synchronizer and viewer may throw. what then?
			logger.error(excp + " in SynchButton::action()");
			logger.debug(excp.getStackTrace());
		}
	}
}

/**
 * "Compare" button, this button examines the controllablity status of the current 
 * supervisor with respect to the plant model that it is supposed to control, i.e. 
 * the plants (and all other automata) that were selected previously in the gui.
 */
class CompareButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(CompareButton.class);

	CompareButton(Workbench wb)
	{
		super("Compare", wb, "Compare supervisor wrt plant");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	// Compare the synched result with the plant
	//      If the automaton exists, calc new states (only) and manually set the forbiddenness
	void action(ActionEvent e)
	{
		int num_x_states = 0;

		// This should not be the case, first press the SynchButton...
		if (wb.automaton == null)
		{
			wb.showMessage("Press Synch to get started");
			return;

			/*
			SynchronizationOptions synch_ops = SynchronizationOptions.getDefaultSynchronizationOptions();
			synch_ops.setForbidUncontrollableStates(true);
			synch_ops.setExpandForbiddenStates(true); // (may be explicit ones)
			synch_ops.setBase class for ExpandEventsUsingPriority(false);
			synch_ops.setBuildAutomaton(true);
			synch_ops.setRequireConsistentControllability(true);
			synch_ops.setRequireConsistentImmediate(false);
			synch_ops.setRememberDisabledEvents(false); // don't redirect disabled events to dump-state

			try
			{
				wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops);

				wb.syncher.execute();

				wb.automaton = wb.syncher.getAutomaton();

				wb.automaton.setName(wb.automaton.getComment());
			}
			catch (Exception excp)
			{
				// synchronizer may throw. what then?
				logger.error(excp + " in CompareButton::action");
				logger.debug(excp.getStackTrace());

				return;
			}
			*/
		}

		// Do the thing!
		SynchronizationOptions synch_ops = SynchronizationOptions.getDefaultSynchronizationOptions();
		synch_ops.setForbidUncontrollableStates(true);
		synch_ops.setExpandForbiddenStates(true); // (may be explicit ones)
		synch_ops.setExpandEventsUsingPriority(false);
		synch_ops.setBuildAutomaton(true);
		synch_ops.setRequireConsistentControllability(true);
		synch_ops.setRequireConsistentImmediate(false);
		synch_ops.setRememberDisabledEvents(false); // don't redirect disabled events to dump-state

		try
		{
			num_x_states = wb.automaton.nbrOfForbiddenStates();

			wb.automaton.clearSelectedStates();

			//wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops);
			Automata plantsAndSupervisors = new Automata(wb.automata);
			Automaton candidateSupervisor = new Automaton(wb.automaton);
			// We need this ugly thing to be able to find the proper state name after composition...
			candidateSupervisor.normalizeStateIdentities();
			plantsAndSupervisors.addAutomaton(candidateSupervisor);
			wb.syncher = new AutomataSynchronizer(plantsAndSupervisors, synch_ops);

			wb.syncher.execute();

			Automaton aut = wb.syncher.getAutomaton();

			for (Iterator it = aut.stateIterator(); it.hasNext(); )
			{
				State state = (State) it.next();

				logger.debug("state " + state.getName() + " is " + (state.isForbidden()
																	? ""
																	: "not") + " forbidden");

				// Make this state forbidden in the supervisor candidate!
				if (state.isForbidden())
				{
					// Find the proper state name (we've composed so we must remove the final
					// part of the name - from the last stateseparator an onward (we "know"
					// there are no later stateseparators in the name (unless the stateseparator
					// coincides with the normalized name (omg that would be stupid)) since we've
					// normalized the names above!))
					String name = state.getName();
					name = name.substring(0, name.lastIndexOf(SupremicaProperties.getStateSeparator()));

					// Find this state in automaton
					State s = wb.automaton.getStateWithName(name);

					s.setForbidden(true);
					s.setSelected(true);
					logger.debug("Setting " + s.getName() + " forbidden");
				}
			}
		}
		catch (Exception excp)
		{

			// synchronizer may throw. what then?
			logger.error(excp + " in CompareButton::action");
			logger.debug(excp.getStackTrace());

			return;
		}

		// In any case we do this and let wb handle how...
		try
		{
			wb.showGraph();
			wb.showMessage(wb.automaton.nbrOfForbiddenStates()-num_x_states +
						   " new uncontrollable state(s)");
			wb.automaton.invalidate();

		}
		catch (Exception excp)
		{

			// viewer may throw, what then?
			logger.error(excp + " in CompareButton::action");
			logger.debug(excp.getStackTrace());

			return;
		}
	}
}

/**
 * Controllability button.
 */
class ContButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(ContButton.class);

	ContButton(Workbench wb)
	{
		super("Controllability", wb, "Calculate uncontrollable states - repeat until zero new states");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	// let's calc the new uc-states
	void action(ActionEvent e)
	{
		// Is there an automaton to work with?
		if (wb.automaton == null)
		{
			wb.showMessage("Press Synch to get started");
			return;
		}

		// Do the thing!
		try
		{
			int num_x_states = wb.automaton.nbrOfForbiddenStates();    // cache this value so we know the number of new ones
			StateSet state_set = new StateSet();    // store the newly forbidden states her, want to avoid duplicates
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());

			// Now, for each state that is forbidden now, calc the new uncontrollable ones
			for (Iterator it = wb.automaton.stateIterator(); it.hasNext(); )
			{
				State state = (State) it.next();

				if (state.isForbidden())
				{
					state.setSelected(false);
					state_set.add(synth.doControllable(state));
				}
			}

			// Traverse the new set of forbidden states and set the forbidden flag
			for (Iterator it = state_set.iterator(); it.hasNext(); )
			{
				State state = (State) it.next();

				state.setForbidden(true);
				state.setSelected(true);    // show the new ones in bold
			}

			wb.showGraph();
			wb.showMessage(wb.automaton.nbrOfForbiddenStates()-num_x_states +
						   " new uncontrollable state(s)");
			wb.automaton.invalidate();

		}
		catch (Exception excp)
		{
			logger.error(excp + " in ContButton::action");
			logger.debug(excp.getStackTrace());

			return;
		}
	}
}

/**
 * Coreachability button.
 */
class NonblockButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(NonblockButton.class);

	NonblockButton(Workbench wb)
	{
		super("Coreachability", wb, "Calculate blocking states - repeat until zero new states");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	// Here we are to forbid *non*coreachable states
	// So we have to calc the coreachable (and nonforbidden ones) and forbid the rest
	public void action(ActionEvent e)
	{
		// Is there an automaton to work with?
		if (wb.automaton == null)
		{
			wb.showMessage("Press Synch to get started");
			return;
		}

		// Do the thing!
		try
		{
			int num_x_states = wb.automaton.nbrOfForbiddenStates();    // cache this value so we know the number of new ones
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());

			synth.initializeAcceptingStates();

			LinkedList list = synth.doCoreachable();    // returns non-coreachable states

			wb.automaton.clearSelectedStates();

			for (Iterator it = list.iterator(); it.hasNext(); )
			{
				State state = (State) it.next();

				state.setForbidden(true);
				state.setSelected(true);
			}

			wb.showGraph();
			wb.showMessage(wb.automaton.nbrOfForbiddenStates()-num_x_states +
						   " new blocking state(s)");
			wb.automaton.invalidate();
		}
		catch (Exception excp)
		{
			logger.error(excp + " in NonblockButton::action");
			logger.debug(excp.getStackTrace());

			return;
		}
	}
}

/**
 * Reachability button.
 */
class ReachButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(ReachButton.class);

	ReachButton(Workbench wb)
	{
		super("Reachability", wb, "Calc the reachable states - need be done only once");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	void action(ActionEvent e)
	{
		// Is there an automaton to work with?
		if (wb.automaton == null)
		{
			wb.showMessage("Press Synch to get started");
			return;
		}

		// Do the thing!
		try
		{
			int num_x_states = wb.automaton.nbrOfForbiddenStates();    // cache this value so we know the number of new ones

			wb.automaton.clearSelectedStates();

			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());

			synth.doReachable();

			for (Iterator it = wb.automaton.stateIterator(); it.hasNext(); )
			{
				State state = (State) it.next();

				if (state.getCost() == State.MAX_COST)
				{
					state.setForbidden(true);
					state.setSelected(false);
				}
				else
				{
					state.setSelected(true);
				}
			}

			wb.showGraph();
			wb.showMessage(wb.automaton.nbrOfForbiddenStates()-num_x_states +
						   " new unreachable state(s)");
			wb.automaton.invalidate();

		}
		catch (Exception excp)
		{
			logger.error(excp + " in ReachButton::action");
			logger.debug(excp.getStackTrace());

			return;
		}
	}
}

/**
 * Purge button.
 */
class PurgeButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(ReachButton.class);

	PurgeButton(Workbench wb)
	{
		super("Purge", wb, "Remove forbidden states");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	void action(ActionEvent e)
	{
		// Is there an automaton to work with?
		if (wb.automaton == null)
		{
			wb.showMessage("Press Synch to get started");
			return;
		}

		int before = wb.automaton.nbrOfStates();

		// Do the thing!
		try
		{
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());

			synth.purge();
			wb.showGraph();
			wb.automaton.invalidate();
		}
		catch (Exception excp)
		{
			logger.error(excp + " in PurgeButton::action");
			logger.debug(excp.getStackTrace());

			return;
		}

		int after = wb.automaton.nbrOfStates();
		wb.showMessage(before-after + " state(s) removed");
	}
}

/**
 * Done button.
 */
class DoneButton
	extends ButtonImpl
{
	private static final long serialVersionUID = 1L;

	DoneButton(Workbench wb)
	{
		super("Done", wb, "Done already... close");

		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	void action(ActionEvent e)
	{
		// Add to gui?
		if (wb.automaton != null && wb.toAddIt())
		{
			ActionMan.getGui().addAutomaton(wb.automaton);
		}
		
		// Exit
		wb.close();
		wb.dispose();
	}
}

class ParamPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;

	Workbench wb = null;
	JCheckBox show_graph;
	JCheckBox list_uc;
	JCheckBox list_nb;
	JCheckBox add_it;

	public ParamPanel(Workbench wb)
	{
		this.wb = wb;

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Parameters"));
		setLayout(new VerticalFlowLayout(true, 0));
		add(show_graph = new JCheckBox("Show graph", true));
		//add(list_uc = new JCheckBox("List new uncontrollable states", true));
		//add(list_nb = new JCheckBox("List new non-blocking states", true));
		add(add_it = new JCheckBox("Add final result to the project", true));
		show_graph.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				toggle_show(e);
			}
		});
	}

	private void toggle_show(ActionEvent e)
	{
		try
		{
			if (show_graph.isSelected())
			{
				wb.showGraph();
				wb.automaton.invalidate();

			}
			else    // show_graph is not selected
			{
				wb.hideGraph();
			}
		}
		catch (Exception excp)
		{
			// what now?
		}
	}

	boolean toShowGraph()
	{
		return show_graph.isSelected();
	}

	boolean toListUC()
	{
		return list_uc.isSelected();
	}

	boolean toListNB()
	{
		return list_nb.isSelected();
	}

	boolean toAddIt()
	{
		return add_it.isSelected();
	}
}

class ButtonPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;

	// Buttons
	/*
	ButtonImpl synchButton = new SynchButton(wb);
	ButtonImpl compareButton = new CompareButton(wb);
	ButtonImpl contButton = new ContButton(wb);
	ButtonImpl nonblockButton = new NonblockButton(wb);
	ButtonImpl reachButton = new ReachButton(wb);
	ButtonImpl purgeButton = new PurgeButton(wb);
	ButtonImpl doneButton = new DoneButton(wb);
	*/
	ButtonImpl synchButton;
	ButtonImpl compareButton;
	ButtonImpl contButton;
	ButtonImpl nonblockButton;
	ButtonImpl reachButton;
	ButtonImpl purgeButton;
	ButtonImpl doneButton;

	public ButtonPanel(Workbench wb)
	{
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Synthesize"));

		JPanel panel = new JPanel();

		panel.setLayout(new VerticalFlowLayout(true, 5));
		/*
		panel.add(new SynchButton(wb));
		panel.add(new CompareButton(wb));
		panel.add(new ContButton(wb));
		panel.add(new NonblockButton(wb));
		panel.add(new ReachButton(wb));
		panel.add(new PurgeButton(wb));
		panel.add(new DoneButton(wb));
		*/
		panel.add(synchButton = new SynchButton(wb));
		panel.add(compareButton = new CompareButton(wb));
		panel.add(contButton = new ContButton(wb));
		panel.add(nonblockButton = new NonblockButton(wb));
		panel.add(reachButton = new ReachButton(wb));
		panel.add(purgeButton = new PurgeButton(wb));
		panel.add(doneButton = new DoneButton(wb));
		add(panel);
	}
	public void disable()
	{
		setEnabled(false);
	}
	public void enable()
	{
		setEnabled(true);
	}
}

class InfoPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;

	JTextField text = new JTextField("", 18);

	InfoPanel(Workbench wb)
	{
		text.setEditable(false);
		add(text);
	}

	void setText(String string)
	{
		text.setText(string);
	}
}

/**
 * The main frame for the workbench.
 */
public class Workbench
	extends CenteredFrame
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(Workbench.class);
	Automata automata = null;    // these are used "globally" in this file
	Automaton automaton = null;    // eventually the resulting supervisor
	VisualProject project = null;
//	AutomatonViewer viewer = null;	// Each workbench manages only a single viewer
	AutomataSynchronizer syncher = null;
	private ParamPanel params;
	private ButtonPanel buttons;
	private InfoPanel info;

	public Workbench(VisualProject project, Automata automata)
		throws Exception
	{
		//super(228, 432);
		super(235, 475);

		setTitle("Supervisor Workbench");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent we)
			{
				close();
			}
		});
		setResizable(false);

		JPanel panel = new JPanel();

		panel.setLayout(new VerticalFlowLayout(true, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		panel.add(params = new ParamPanel(this));
		panel.add(buttons = new ButtonPanel(this));
		panel.add(info = new InfoPanel(this));

		//getContentPane().add(panel, BorderLayout.CENTER);
		setContentPane(panel);

		this.project = project;
		this.automata = automata;

		showMessage("Press Synch to get started");

		updateButtons();
		pack();
	}

	void close()
	{
		hideGraph();
		/*
		if (automaton != null && toAddIt() == false)
		{
			project.removeAutomaton(automaton);
		}
		*/
	}

	void showGraph()
		throws Exception
	{
		if (toShowGraph() && automaton != null)    // we should show it and there's something to show
		{
			try
			{
				if(project.existsAutomatonViewer(automaton))
				{
					AutomatonViewer viewer = project.returnAutomatonViewer(automaton);
					if (viewer.isVisible() == false)
					{
						viewer.setVisible(true);
					}
				}
				else
				{
					if(project.showAutomatonViewer(automaton))
					{
						AutomatonViewer viewer = project.createAutomatonViewer(automaton, new MyAutomatonViewerFactory());
						viewer.setVisible(true);
					}
				}
			}
			catch(Exception excp)
			{
				System.out.println("Something bad occurred");
			}
		}
	}

	void hideGraph()
	{
		try
		{
			if (automaton != null && project.existsAutomatonViewer(automaton))
			{
				AutomatonViewer viewer = project.returnAutomatonViewer(automaton);
				if (viewer.isVisible())
				{
					viewer.setVisible(false);
				}
			}
		}
		catch(Exception excp)
		{
			logger.error("Error in hiding viewer", excp);
		}
	}

	void showMessage(String mess)
	{
		info.setText(mess);

		//if (toListUC()) {}
	}

	/**
	 * What is this one supposed to do?
	 */
	boolean toShowGraph()
	{
		return params.toShowGraph();
	}

	/**
	 * What is this one supposed to do?
	 */
	boolean toListUC()
	{
		return params.toListUC();
	}

	/**
	 * What is this one supposed to do?
	 */
	boolean toListNB()
	{
		return params.toListNB();
	}

	/**
	 * Returns true if "Add final result to the project" is selected.
	 */
	boolean toAddIt()
	{
		return params.toAddIt();
	}

	/**
	 * Updates the enabled status of the buttons.
	 */
	void updateButtons()
	{
		if (automaton == null)
		{
			buttons.synchButton.setEnabled(true);
			buttons.compareButton.setEnabled(false);
			buttons.contButton.setEnabled(false);
			buttons.nonblockButton.setEnabled(false);
			buttons.reachButton.setEnabled(false);
			buttons.purgeButton.setEnabled(false);
			buttons.doneButton.setEnabled(true);
		}
		else
		{
			buttons.synchButton.setEnabled(true);
			buttons.compareButton.setEnabled(true);
			buttons.contButton.setEnabled(true);
			buttons.nonblockButton.setEnabled(true);
			buttons.reachButton.setEnabled(true);
			buttons.purgeButton.setEnabled(true);
			buttons.doneButton.setEnabled(true);

			if (automata.isNoAutomataPlants())
			{
				buttons.compareButton.setEnabled(false);
			}
		}
	}

	// For debugging only
	public static void main(String args[])
		throws Exception
	{
		StickPickingGame game = new StickPickingGame(2, 7);
		Workbench wb = new Workbench(null, game.getProject());

		wb.setVisible(true);
	}
}

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
		super(automaton);
	}
	protected String getColor(State state)
	{
		if(state.isForbidden() && state.isSelected())
		{
			return ", color = red, style=bold";
		}
		return super.getColor(state);
	}
}
// Must also override AutomatonViewer::getSerializer()
// And this required adding AutomataViewer::getAutomaton()
class MyAutomatonViewer
	extends AutomatonViewer
{
	public MyAutomatonViewer(Automaton theAutomaton)
		throws Exception
	{
		super(theAutomaton);
	}
	
	public AutomataSerializer getSerializer()
	{
		MyAutomatonToDot serializer = new MyAutomatonToDot(getAutomaton());

		serializer.setLeftToRight(leftToRightCheckBox.isSelected());
		serializer.setWithLabels(withLabelsCheckBox.isSelected());
		serializer.setWithCircles(withCirclesCheckBox.isSelected());
		serializer.setUseColors(useColorsCheckBox.isSelected());

		return serializer;
	}
}
//
class ButtonImpl
	extends JButton
{
	Workbench wb = null;
	
	ButtonImpl(String text, Workbench wb, String tooltip)
	{
		super(text);
		this.wb = wb;
		
		setToolTipText(tooltip);
	}
}

class SynchButton
	extends ButtonImpl
{
	private static Logger logger = LoggerFactory.createLogger(SynchButton.class);

	SynchButton(Workbench wb)
	{
		super("Synch", wb, "Synchronize the selected automata");
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
		SynchronizationOptions synch_ops = 
			new SynchronizationOptions(SynchronizationType.Full, // full synch
										false,	// do *not* forbid uc-states
										true,	// expand forbidden states (may be explicit ones)
										false,	// do not terminate if one uc-state found
										false,	// expandEventsUsingPriority??
										true,	// build entire automaton
										false,	// no verbose mode
										true,	// require consistent controllability
										false,	// don't require consistent immediate
										false	// don't redirect disabled events to dump-state
										);
		try
		{	// wb.syncher *should* be null here - we assume this!
			wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops);
			wb.syncher.execute();
			wb.automaton = wb.syncher.getAutomaton();
			wb.automaton.setName(wb.automaton.getComment());
			// Note that there may be explicitly specified uc-states
			// These are not "new" in a direct sense, but...
			wb.listUcStates(wb.calcForbiddenStates());
			wb.showGraph();
			
		}
		catch(Exception excp)
		{
			// synchronizer and viewer may throw. what then?
			logger.error(excp + " in SynchButton::action()");
			logger.debug(excp.getStackTrace());
		}
	}

}

class CompareButton
	extends ButtonImpl
{
	private static Logger logger = LoggerFactory.createLogger(CompareButton.class);

	CompareButton(Workbench wb)
	{
		super("Compare", wb, "Compare synch result with plant");
		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				action(e);
			}
		});
	}

	// Compare the synched result with the plant
	//	If the automaton exists, calc new states (only) and manually set the forbiddenness
	//	Else synch up a new one
	void action(ActionEvent e)
	{
		int num_x_states = 0;

		// This should not be the case, first press the SynchButton...
		if(wb.automaton == null) // Then also the syncher does not exist
		{
			SynchronizationOptions synch_ops = 
				new SynchronizationOptions(SynchronizationType.Full, // full synch
										true,	// *do* forbid uc-states
										true,	// expand forbidden states (may be explicit ones)
										false,	// do not terminate if one uc-state found
										false,	// expandEventsUsingPriority??
										true,	// build entire automaton
										false,	// no verbose mode
										true,	// require consistent controllability
										false,	// don't require consistent immediate
										false);	// don't redirect disabled events to dump-state

			try
			{
				wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops);
				wb.syncher.execute();
				wb.automaton = wb.syncher.getAutomaton();
				wb.automaton.setName(wb.automaton.getComment());
			}
			catch(Exception excp)
			{
				// synchronizer may throw. what then?
				logger.error(excp + " in CompareButton::action");
				logger.debug(excp.getStackTrace());
				return;
			}
		}
		else // wb.automaton (and hence wb.syncher) exists
		{
			SynchronizationOptions synch_ops = 
				new SynchronizationOptions(SynchronizationType.Full, // full synch
										true,	// *do* forbid uc-states
										true,	// expand forbidden states (may be explicit ones)
										false,	// do not terminate if one uc-state found
										false,	// expandEventsUsingPriority??
										// false,	// do *not* build entire automaton
										true,	// *yes* do build full, otherwise we have to fiddle with index-formats etc (a class for that would be useful)
												// and in any case, the automata are (supposed to be) usefully small
										false,	// no verbose mode
										true,	// require consistent controllability
										false,	// don't require consistent immediate
										false);	// don't redirect disabled events to dump-state

			try
			{
				num_x_states = wb.calcForbiddenStates();
				
				wb.automaton.clearSelectedStates();
				
				wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops);
				wb.syncher.execute();
				Automaton aut = wb.syncher.getAutomaton();
				
				for(Iterator it = aut.stateIterator(); it.hasNext(); )
				{
					State state = (State)it.next();
					logger.debug("state " + state.getName() + " is " + (state.isForbidden() ? "" : "not") + " forbidden");
					if(state.isForbidden())
					{
						// find this state in automaton
						State s = wb.automaton.getStateWithName(state.getName());
						s.setForbidden(true);
						s.setSelected(true);
						logger.debug("Setting " + s.getName() + " forbidden");
					}
				}

			}
			catch(Exception excp)
			{
				// synchronizer may throw. what then?
				logger.error(excp + " in CompareButton::action");
				logger.debug(excp.getStackTrace());
				return;
			}

		}
		// In any case we do this and let wb handle how...
		try
		{
			wb.showGraph();
			wb.listUcStates(wb.calcForbiddenStates() - num_x_states);
		}
		catch(Exception excp)
		{
			// viewer may throw, what then?
			logger.error(excp + " in CompareButton::action");
			logger.debug(excp.getStackTrace());
			return;
		}
	}
}

class ContButton
	extends ButtonImpl
{
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
		try
		{
			int num_x_states = wb.calcForbiddenStates(); // cache this value so we know the number of new ones
			StateSet state_set = new StateSet();	// store the newly forbidden states her, want to avoid duplicates
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, new SynthesizerOptions());
			
			// Now, for each state that is forbidden now, calc the new uncontrollable ones
			for(Iterator it = wb.automaton.stateIterator(); it.hasNext(); )
			{
				State state = (State)it.next();
				if(state.isForbidden())
				{
					state.setSelected(false);
					state_set.add(synth.doControllable(state));
				}
			}
			// Traverse the new set of forbidden states and set the forbidden flag
			for(Iterator it = state_set.iterator(); it.hasNext(); )
			{
				State state = (State)it.next();
				state.setForbidden(true);
				state.setSelected(true); // show the new ones in bold
			}
			wb.showGraph();
			wb.listUcStates(wb.calcForbiddenStates() - num_x_states);
			
		}
		catch(Exception excp)
		{
			logger.error(excp + " in ContButton::action");
			logger.debug(excp.getStackTrace());
			return;
		}
		
	}
}

class NonblockButton
	extends ButtonImpl
{
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
		try
		{
			int num_x_states = wb.calcForbiddenStates(); // cache this value so we know the number of new ones
			
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, new SynthesizerOptions());
			synth.initializeAcceptingStates();
			LinkedList list = synth.doCoreachable(); // returns non-coreachable states
			
			wb.automaton.clearSelectedStates();
			for(Iterator it = list.iterator(); it.hasNext(); )
			{
				State state = (State)it.next();
				state.setForbidden(true);
				state.setSelected(true);
			}
			
			wb.showGraph();
			wb.listUcStates(wb.calcForbiddenStates() - num_x_states);
			
		}
		catch(Exception excp)
		{
			logger.error(excp + " in NonblockButton::action");
			logger.debug(excp.getStackTrace());
			return;
		}
		
	}
}

class ReachButton
	extends ButtonImpl
{
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
		try
		{
			int num_x_states = wb.calcForbiddenStates(); // cache this value so we know the number of new ones
			wb.automaton.clearSelectedStates();
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, new SynthesizerOptions());
			synth.doReachable();
			
			for(Iterator it = wb.automaton.stateIterator(); it.hasNext(); )
			{
				State state = (State)it.next();
				if(state.getCost() == State.MAX_COST)
				{
					state.setForbidden(true);
					state.setSelected(true);
				}
			}
			
			wb.showGraph();
			wb.listUcStates(wb.calcForbiddenStates() - num_x_states);
		}
		catch(Exception excp)
		{
			logger.error(excp + " in ReachButton::action");
			logger.debug(excp.getStackTrace());
			return;
		}
	}
}

class PurgeButton
	extends ButtonImpl
{
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
		try
		{
			AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, new SynthesizerOptions());
			synth.purge();

			wb.showGraph();
		}
		catch(Exception excp)
		{
			logger.error(excp + " in ReachButton::action");
			logger.debug(excp.getStackTrace());
			return;
		}
	}

}
class DoneButton
	extends ButtonImpl
{
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
		wb.close();
		wb.dispose();
	}

}


class ParamPanel
	extends JPanel
{
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
		add(list_uc = new JCheckBox("List new uncontrollable states", true));
		add(list_nb = new JCheckBox("List new non-blocking states", true));
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
			if(show_graph.isSelected())
			{
				wb.showGraph();
			}
			else // show_graph is not selected
			{
				wb.hideGraph();
			}
		}
		catch(Exception excp)
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
	public ButtonPanel(Workbench wb)
	{
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Synthesize"));

		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout(true, 5));
		
		panel.add(new SynchButton(wb));
		panel.add(new CompareButton(wb));
		panel.add(new ContButton(wb));
		panel.add(new NonblockButton(wb));
		panel.add(new ReachButton(wb));
		panel.add(new PurgeButton(wb));
		panel.add(new DoneButton(wb));
		
		add(panel);
	}

}

class InfoPanel
	extends JPanel
{
	JTextField text= new JTextField("Supervisor Workbench", 18);
	
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

public class Workbench
	extends CenteredFrame
{
	private static Logger logger = LoggerFactory.createLogger(Workbench.class);
	
	Automata automata = null;	// these are used "globally" in this file
	Automaton automaton = null; // eventually the resulting supervisor
	VisualProject project = null;
	AutomatonViewer viewer = null;
	AutomataSynchronizer syncher = null;

	private ParamPanel params;
	private ButtonPanel buttons;
	private InfoPanel info;
	
	public Workbench(VisualProject project, Automata automata)
		throws Exception
	{
		super(228, 416);
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
		if(automata.sanityCheck(null, 1, true, true, true) == false)
		{
			// dispose(); // cannot dispose itself! What then?
			logger.error("Sanity check not passed, see log window");
			throw new Exception("Sanity check not passed, see log window");
		}
	}
	
	void close()
	{
		if(viewer != null)
		{
			viewer.dispose();
			viewer = null;
		}
		if(toAddIt() && automaton != null)
		{
			project.addAutomaton(automaton);
			automaton = null;
		}
	}
	
	void showGraph()
		throws Exception
	{
		if(toShowGraph()) // we should show it
		{
			if(viewer == null) // if not already shown
			{
				viewer = new MyAutomatonViewer(automaton);
				viewer.run();
			}
			else // have the new automaton drawn in the old window
			{
				if(viewer.isVisible() == false)
				{
					viewer.show();
				}
				viewer.update();
				logger.debug("Viewer updated");
			}
		}

	}
	void hideGraph()
	{
		if(viewer != null)
		{
			if(viewer.isVisible())
			{
				viewer.hide();
			}
		}
	}
	
	void listUcStates(int num)
	{
		info.setText(num + " new forbidden states");
		
		if(toListUC())
		{
		}
	}
	
	boolean toShowGraph()
	{
		return params.toShowGraph();
	}
	boolean toListUC()
	{
		return params.toListUC();
	}
	boolean toListNB()
	{
		return params.toListNB();
	}
	boolean toAddIt()
	{
		return params.toAddIt();
	}
	
	int calcForbiddenStates()
		throws Exception
	{
		if(automaton == null)
		{
			throw new NullPointerException();
		}
		
		int num = 0;
		for(Iterator it = automaton.stateIterator(); it.hasNext(); )
		{
			State state = (State)it.next();
			if(state.isForbidden())
				++num;
		}
		
		return num;
	}
	
	public static void main(String args[])
		throws Exception
	{
		StickPickingGame game = new StickPickingGame(2, 7);		
		Workbench wb = new Workbench(null, game.getProject());
		wb.show();
	}
}

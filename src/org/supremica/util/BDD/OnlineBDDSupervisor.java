package org.supremica.util.BDD;

import org.supremica.automata.*;
import java.util.*;
import java.awt.*;

/**
 * Online BDD-supervisor:
 *
 * This supervisor object is NOT an automaton. It does not reject or accept any events/strings.
 * It does however tell you if a certain global state is "safe", that is, if you from
 * some safe state "x" do a transition to another safe state "y" you are guaranteed to
 * stay the NBC sub-space.
 *
 * This object is a bit complex, however, if you follow the instructions below,
 * everything should work just fine...
 *
 *
 * 1) this code will compute a BDD supNBC and create an online object from it:
 *
 * AutomataBDDSynthesizer bddSynthesizer = new AutomataBDDSynthesizer(theAutomata, true, true);
 * OnlineBDDSupervisor ob = bddSynthesizer.extractOnlineSupervisor();
 * bddSynthesizer.cleanup();
 *
 * 2) to request a state, you must set each component [sub-state] separately. For example
 *
 * for (Iterator autIt = theAutomata.iterator(); autIt.hasNext();) {
 *      Automaton currAutomaton = (Automaton) autIt.next();
 *      ob.setPartialState( currAutomaton, currAutomaton.getInitialState());
 * }
 *
 * 3) then you can ask the BDD engine if that state is OK:
 *
 *  boolean state_is_safe = ob.isStateSafe();
 *
 *
 * 4) IMPORTANT: when you are done, you __MUST__ cleanup the mess by this call
 *
 *    ob.cleanup();
 *
 *  before you do this, the BDD package is _locked_ and may [can ?] not be used!
 *
 */
public class OnlineBDDSupervisor
	extends Frame
{
	private BDDAutomata ba;
	private int safe_states;
	private HashMap amap, smap;
	private org.supremica.util.BDD.State[] state_vector;
	private Label state;
	private String helpmsg = "You can not close this window.\n" + "This windows will only dissapear when you call OnlineBDDSupervisor.cleanup()\n" + "Until you do that, no subsequent calls to the BDD package can be made!\n";

	public OnlineBDDSupervisor(BDDAutomata ba, int safe_states)
		throws Exception
	{
		super("OnlineBDDSupervisor");

		this.ba = ba;
		this.safe_states = safe_states;
		this.state_vector = new org.supremica.util.BDD.State[ba.getSize()];

		for (int i = 0; i < state_vector.length; i++)
		{
			state_vector[i] = null;
		}

		try
		{
			setup_mapping();
		}
		catch (Exception exx)
		{
			cleanup();

			throw exx;
		}

		// gui stuff:
		Panel p1 = new Panel(new FlowLayout(FlowLayout.LEFT));

		add(p1, BorderLayout.NORTH);
		p1.add(new Label("Online BDD supervisor module, safe_nodeCount = " + ba.nodeCount(safe_states)));

		TextArea ta = new TextArea(helpmsg, 4, 60, TextArea.SCROLLBARS_NONE);

		ta.setEditable(false);
		ta.setForeground(Color.yellow);
		add(ta, BorderLayout.CENTER);
		add(state = new Label(), BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public void cleanup()
	{
		if (ba != null)
		{
			ba.cleanup();

			ba = null;
		}

		setVisible(false);
		dispose();
	}

	// ---------------------------------------------------------------
	private void setup_mapping()
		throws Exception
	{
		amap = new HashMap();
		smap = new HashMap();

		BDDAutomaton[] as = ba.getAutomataVector();

		for (int i = 0; i < as.length; i++)
		{
			as[i].index = i;

			org.supremica.util.BDD.Automaton sb = as[i].getModel();
			org.supremica.automata.Automaton sm = (org.supremica.automata.Automaton) sb.getSupremicaModel();

			amap.put(sm, as[i]);

			org.supremica.automata.StateSet ss1 = sm.getStateSet();
			org.supremica.util.BDD.StateSet ss2 = sb.getStates();

			for (Iterator<org.supremica.automata.State> si = ss1.iterator(); si.hasNext(); )
			{
				org.supremica.automata.State state1 = si.next();
				org.supremica.util.BDD.State state2 = ss2.getByName(state1.getName());

				smap.put(state1, state2);
			}
		}
	}

	private int computeStateBDD()
	{
		int ret = ba.ref(ba.getOne());

		for (int i = 0; i < state_vector.length; i++)
		{
			if (state_vector[i] != null)
			{
				ret = ba.andTo(ret, state_vector[i].bdd_s);
			}
		}

		return ret;
	}

	private void show_state()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("<");

		for (int i = 0; i < state_vector.length; i++)
		{
			if (state_vector[i] != null)
			{
				sb.append(" ");
				sb.append(state_vector[i].name);
			}
		}

		sb.append(" >");
		state.setText(sb.toString());
	}

	// -------------------------------------------------------------------------------
	public void setPartialState(org.supremica.automata.Automaton owner, org.supremica.automata.State state)
	{
		org.supremica.util.BDD.BDDAutomaton a1 = (org.supremica.util.BDD.BDDAutomaton) amap.get(owner);

		if (a1 != null)
		{
			if (state != null)
			{
				org.supremica.util.BDD.State s1 = (org.supremica.util.BDD.State) smap.get(state);

				state_vector[a1.index] = s1;
			}
			else
			{
				state_vector[a1.index] = null;
			}
		}
	}

	/**
	 * THIS IS NOT EFFICIENT!!!!
	 *
	 * WE NEED A MORE EFFICIENT WAY TO  check if bdd_state \in sage_states
	 *
	 */
	public boolean isStateSafe()
	{
		int bdd_state = computeStateBDD();
		int tmp = ba.and(bdd_state, safe_states);
		boolean ret = (tmp != ba.getZero());

		ba.deref(tmp);
		ba.deref(bdd_state);

		if (ret)
		{
			show_state();
		}

		return ret;
	}
}

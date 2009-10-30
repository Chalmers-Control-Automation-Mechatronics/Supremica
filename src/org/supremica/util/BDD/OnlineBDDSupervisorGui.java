package org.supremica.util.BDD;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
/**
 * 
 * Comments by Arash:
 * 
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
public class OnlineBDDSupervisorGui extends Frame
{
    private static final long serialVersionUID = 1L;

	private OnlineBDDSupervisor supervisor;
	private Label state;
	private String helpmsg = "You can not close this window.\n"
		+ "This windows will only dissapear when you call " +
		"OnlineBDDSupervisor.cleanup()\n" + 
		"Until you do that, no subsequent calls to the " +
		"BDD package can be made!\n";
	
	public OnlineBDDSupervisorGui(BDDAutomata ba, int safe_states)
	throws Exception
	{
		super("OnlineBDDSupervisor");
		supervisor = new OnlineBDDSupervisor(ba, safe_states);
		
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
		supervisor.cleanup();
		
		setVisible(false);
		dispose();
	}
	
	private void show_state()
	{
		String stateString = supervisor.get_state_string();
		state.setText(stateString);
	}
	
	public boolean isStateSafe()
	{
		boolean returnValue = supervisor.isStateSafe();
		if (returnValue)
		{
			show_state();
		}
		return returnValue;
	}
}

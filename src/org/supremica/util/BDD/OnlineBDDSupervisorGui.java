package org.supremica.util.BDD;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;

public class OnlineBDDSupervisorGui extends Frame {
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

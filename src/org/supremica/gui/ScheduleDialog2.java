/********************** ScheduleDialog.java *****************/
package org.supremica.gui;

import java.awt.event.*;
import javax.swing.*;
import java.awt.GridLayout;
import java.util.Iterator;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.scheduling.*;

public class ScheduleDialog2
	extends JDialog
{
	private static final String[] optiMethodNames = new String[]{"Modified A*"};
	private static final String[] heuristicsNames = new String[]{"Default"}; 
	private static Logger logger = LoggerFactory.createLogger(ScheduleDialog2.class);
	
	private JComboBox optiMethodsBox;
	private JComboBox heuristicsBox;

	public ScheduleDialog2()
	{
		this(ActionMan.getGui().getFrame());
	}

	public ScheduleDialog2(JFrame frame)
	{
		super(frame, "Schedule Selected Automata", true);
	
		
		/******** Base components of the dialog ***********/
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					doit();
				}
			});
			
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					done();
				}
			});
			
		JLabel optiMethodsLabel = new JLabel("Optimization methods: \t \t");
		optiMethodsBox = new JComboBox(optiMethodNames);
		
		JLabel heuristicsLabel = new JLabel("Heuristics: \t \t");
		heuristicsBox = new JComboBox(heuristicsNames);
	
		
		/******** Base containers of the dialog ***********/
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		JPanel optiPanel = new JPanel();
		optiPanel.add(optiMethodsLabel);
		optiPanel.add(optiMethodsBox);
		
		JPanel heuristicsPanel = new JPanel();
		heuristicsPanel.add(heuristicsLabel);
		heuristicsPanel.add(heuristicsBox);
		
		
		/******** Layout of the dialog ***********/	
		getContentPane().setLayout(new GridLayout(3, 1));
		
		getContentPane().add(optiPanel);
		getContentPane().add(heuristicsPanel);		
		getContentPane().add(buttonPanel);
				
		Utility.setDefaultButton(this, okButton);
		Utility.setupDialog(this, 300, 150);
	}

	/**
	 *	Calls the selected scheduling algorithm. 
	 *	It is assumed that synchronization has been performed prior to scheduling. 
	 */
	void doit() {
		try {
			if (optiMethodsBox.getSelectedItem().equals("Modified A*")) {
				Automaton theAutomaton = ActionMan.getGui().getSelectedAutomata().getFirstAutomaton();

				ModifiedAstar2 mastar = new ModifiedAstar2(theAutomaton);
	
	/*			ModifiedAstar mastar = new ModifiedAstar(automata, weights.getCalculator(estimates.getEstimator(automata)));
				Element elem = mastar.walk();
				if(elem == null)
				{
					throw new RuntimeException("no marked state found");
				}
	
				// logger.info(mastar.trace(elem));
				logger.info(mastar.getInfo(elem).toString());
				Automaton automaton = mastar.getAutomaton(elem);
				ActionMan.getGui().addAutomaton(automaton);
	*/
				State acceptingState = mastar.walk();

				if(acceptingState == null)
				{
					throw new RuntimeException("no marked state found");
				}
				
				Automaton schedule = mastar.buildScheduleAutomaton(acceptingState);
				ActionMan.getGui().addAutomaton(schedule);
			}		
		}
		catch(Exception excp)
		{
			logger.error("ScheduleDialog::doit "  + excp);
			logger.debug(excp.getStackTrace());
		}
		
		done();
	}

	/**
	 *	Terminates the Schedule Dialog. 
	 */
	void done()
	{
		setVisible(false);
		dispose();
		getParent().repaint();
	}
}
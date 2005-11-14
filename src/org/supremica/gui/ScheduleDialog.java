
/********************** ScheduleDialog.java *****************/
package org.supremica.gui;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.BorderLayout;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.scheduling.*;

public class ScheduleDialog
	extends JDialog
{
    private static final long serialVersionUID = 1L;
    private static final String[] optiMethodNames = new String[]{"Modified A*", "Modified VGA*", "MILP", "Modified IDA*", "Modified SMA*"};
    private static final String[] heuristicsNames = new String[]{"1-product relax", "2-product relax", "brute force"};
    private static Logger logger = LoggerFactory.createLogger(ScheduleDialog.class);
    private JComboBox optiMethodsBox, heuristicsBox;
    private JCheckBox nodeExpander, buildAutomaton, vgDrawer;
    private int memoryCapacity;
    private JTextField memoryCapacityField;

    public ScheduleDialog()
    {
		this(ActionMan.getGui().getFrame());
    }
    
    public ScheduleDialog(JFrame frame)
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
	
		nodeExpander = new JCheckBox("use AK's node expander", true);
		buildAutomaton = new JCheckBox("build schedule", true); 
		vgDrawer = new JCheckBox("Draw Visibility Graph", false);

		memoryCapacityField = new JTextField("300", 10);
	
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
	
		JPanel smaPanel = new JPanel();
		smaPanel.add(new JLabel("Nr of nodes in memory (SMA*)"));
		smaPanel.add(memoryCapacityField);


		// 	JPanel expanderPanel = new JPanel();
		// 	expanderPanel.add(nodeExpander);

		/********* Composite containers *******************/
	
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setLayout(new GridLayout(2,1));
		algorithmPanel.add(optiPanel);
		algorithmPanel.add(heuristicsPanel);

		JPanel specPanel = new JPanel();
		specPanel.setLayout(new GridLayout(3, 1));
		specPanel.add(nodeExpander);
		specPanel.add(buildAutomaton);
		specPanel.add(vgDrawer);
		//	specPanel.add(smaPanel);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Algorithms", algorithmPanel);
		tabbedPane.addTab("Specifications", specPanel);
	
		// 	/******** Layout of the dialog ***********/
		// 	getContentPane().setLayout(new GridLayout(4, 1));
		// 	getContentPane().add(optiPanel);
		// 	getContentPane().add(heuristicsPanel);
		// 	getContentPane().add(expanderPanel);
		// 	getContentPane().add(buttonPanel);
	
		getContentPane().add("Center", tabbedPane);
		getContentPane().add("South", buttonPanel);

		Utility.setDefaultButton(this, okButton);
		Utility.setupDialog(this, 300, 250);
    }

    /**
     *      Calls the selected scheduling algorithm.
     *      It is assumed that synchronization has been performed prior to scheduling.
     */
    void doit()
    {
		try 
		{
			readMemoryCapacity();
			
			Scheduler sched;
			
			if (optiMethodsBox.getSelectedItem().equals("Modified A*"))
				sched = new ModifiedAstar(ActionMan.getGui().getSelectedAutomata(), (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), false);
			else if (optiMethodsBox.getSelectedItem().equals("Modified VGA*"))
				sched = new ModifiedVGAstar(ActionMan.getGui().getSelectedAutomata(), (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), vgDrawer.isSelected(), false);
			else if (optiMethodsBox.getSelectedItem().equals("MILP"))
				sched = new Milp(ActionMan.getGui().getSelectedAutomata());
			else if (optiMethodsBox.getSelectedItem().equals("Modified IDA*"))
				throw new Exception("IMA* not implemented yet...");
			// 		sched = new ModifiedAstar(ActionMan.getGui().getSelectedAutomata(), (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), true);	
			else if (optiMethodsBox.getSelectedItem().equals("Modified SMA*"))
				throw new Exception("SMA* not implemented yet...");
			else 
				throw new Exception("Unknown optimization method");


			int[] acceptingNode = sched.schedule();
			
			if (buildAutomaton.isSelected()) 
			{
				Automaton schedule = sched.buildScheduleAutomaton(acceptingNode);
				ActionMan.getGui().addAutomaton(schedule);	
			}
		}
		catch (Exception excp) 
		{
			logger.error("ScheduleDialog::doit " + excp);
			logger.debug(excp.getStackTrace());
		}
		
		done();
    }
    
    /**
     *      Terminates the Schedule Dialog.
     */
    void done()
    {
		setVisible(false);
		dispose();
		getParent().repaint();
    }

    void readMemoryCapacity() {
		memoryCapacity = (int) (new Integer(memoryCapacityField.getText()));
    }
}

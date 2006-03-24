
/********************** ScheduleDialog.java *****************/
package org.supremica.gui;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.scheduling.*;

public class ScheduleDialog
	extends JDialog
{
	private static final String MODIFIED_A_STAR = "Modified A*";
	private static final String MODIFIED_VGA_STAR = "Modified VGA*";
	private static final String MILP = "MILP";
	private static final String VIS_GRAPH = "Visibility Graph";

	private static final String ONE_PRODUCT_RELAXATION = "1-product relax";
	private static final String TWO_PRODUCT_RELAXATION = "2-product relax";
	private static final String VIS_GRAPH_TIME_RELAXATION = "visibility graph (time)";
	private static final String VIS_GRAPH_NODE_RELAXATION = "visibility graph (node)";

    private static final long serialVersionUID = 1L;
    private static final String[] optiMethodNames = new String[]{MODIFIED_A_STAR, MODIFIED_VGA_STAR, MILP, VIS_GRAPH}; //, "Modified IDA*", "Modified SMA*"};
    private static final String[] heuristicsNames = new String[]{ONE_PRODUCT_RELAXATION, TWO_PRODUCT_RELAXATION, VIS_GRAPH_TIME_RELAXATION, VIS_GRAPH_NODE_RELAXATION, "brute force"}; 
    private static Logger logger = LoggerFactory.createLogger(ScheduleDialog.class);
    private JComboBox optiMethodsBox, heuristicsBox;
    private JCheckBox nodeExpander, buildAutomaton, vgDrawer;
    private int memoryCapacity;
    private JTextField memoryCapacityField;
	private JButton okButton, cancelButton;
	JButton autoTestButton; //Tillf
	public ScheduleDialog dia = this; //Tillf
	private java.util.ArrayList filesToSchedule = new java.util.ArrayList();

	Scheduler sched = null;
	public Thread milpThread = null;

	private BufferedWriter writer = null; //Tillf
	

    public ScheduleDialog()
    {
		this(ActionMan.getGui().getFrame());
    }
    
    public ScheduleDialog(JFrame frame)
    {
		super(frame, "Schedule Selected Automata", true);
	
		/******** Base components of the dialog ***********/
		okButton = new JButton("Schedule");
		cancelButton = new JButton("Cancel");
	
		JLabel optiMethodsLabel = new JLabel("Optimization methods: \t \t");
		optiMethodsBox = new JComboBox(optiMethodNames);
	
		JLabel heuristicsLabel = new JLabel("Heuristics: \t \t");
		heuristicsBox = new JComboBox(heuristicsNames);
	
		nodeExpander = new JCheckBox("use AK's node expander", true);
		buildAutomaton = new JCheckBox("build schedule", true); 
		vgDrawer = new JCheckBox("Draw Visibility Graph", true);

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

		/************* Event Handlers ****************/
		okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doit();
				}
			});
	
		cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (sched != null)
					{
						sched.requestStop();
					}
					else
					{
						done();
					}
						
				}
			});

		optiMethodsBox.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (((String)optiMethodsBox.getSelectedItem()).contains("A*"))
					{
						heuristicsBox.setEnabled(true);

// 						heuristicsBox.removeAllItems();
// 						for (String heuristic : heuristicsNames)
// 						{
// 							heuristicsBox.addItem(heuristic);
// 						}
					}
					else
					{
						heuristicsBox.setEnabled(false);
					}
				}
			}); 

		// Is only used for automatic scheduling of several files (a whole directory)
		autoTestButton = new JButton("AutoTest");
		buttonPanel.add(autoTestButton);
		autoTestButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					File rootDir = new File(org.supremica.properties.SupremicaProperties.getFileOpenPath());
	 				File[] files = rootDir.listFiles();
					
					try {
						File resultFile = null;
						if (((String)optiMethodsBox.getSelectedItem()).equals(MODIFIED_A_STAR))
						{
							resultFile = new File(rootDir + File.separator + "_" + heuristicsBox.getSelectedItem() + ".txt");
						}
						else 
						{
							resultFile = new File(rootDir + File.separator + "_" + optiMethodsBox.getSelectedItem() + ".txt");
						}
						resultFile.createNewFile();

						writer =  new BufferedWriter(new FileWriter(resultFile));
					}
					catch (IOException ioe)
					{
						logger.error("Error at file creation : " + rootDir + File.separator + optiMethodsBox.getSelectedItem() + "_" + heuristicsBox.getSelectedItem() + ".txt");
					}

					for (int i=0; i<files.length; i++)
					{
						filesToSchedule.add(files[i]);
					}

					autoTestButton.setEnabled(false);
					done();
				}
			});
    }

    /**
     *      Calls the selected scheduling algorithm.
     *      It is assumed that synchronization has been performed prior to scheduling.
     */
    void doit()
    {
		try 
		{
			cancelButton.setText("Stop");
			okButton.setEnabled(false);

			readMemoryCapacity();
			
			if (optiMethodsBox.getSelectedItem().equals(MODIFIED_A_STAR))
			{
				String selectedHeuristic = (String) heuristicsBox.getSelectedItem();
				if (selectedHeuristic.equals(VIS_GRAPH_TIME_RELAXATION))
				{
					if (ActionMan.getGui().getSelectedAutomata().getPlantAutomata().size() == 2)
					{
						sched = new VisGraphScheduler(ActionMan.getGui().getSelectedAutomata(), vgDrawer.isSelected(), this);
					}
					else
					{
						sched = new ModifiedAstarUsingVisGraphRelaxation(ActionMan.getGui().getSelectedAutomata(), nodeExpander.isSelected(), buildAutomaton.isSelected(), this);
					}
				}
				// Tillf
				// This might be temporary (or the alternative just above) to determine which relaxation is best 
				else if (selectedHeuristic.equals(VIS_GRAPH_NODE_RELAXATION))
				{
					if (ActionMan.getGui().getSelectedAutomata().getPlantAutomata().size() == 2)
					{
						sched = new VisGraphScheduler(ActionMan.getGui().getSelectedAutomata(), vgDrawer.isSelected(), this);
					}
					else
					{
						sched = new ModifiedAstarUsingVisGraphRelaxation(ActionMan.getGui().getSelectedAutomata(), nodeExpander.isSelected(), buildAutomaton.isSelected(), true, this);
					}
				}
				else if (selectedHeuristic.equals(ONE_PRODUCT_RELAXATION))
				{
					sched = new ModifiedAstarUsingOneProdRelaxation(ActionMan.getGui().getSelectedAutomata(), nodeExpander.isSelected(), buildAutomaton.isSelected(), this);
				}
				else if (selectedHeuristic.equals(TWO_PRODUCT_RELAXATION))
				{
					sched = new ModifiedAstarUsingTwoProdRelaxation(ActionMan.getGui().getSelectedAutomata(), nodeExpander.isSelected(), buildAutomaton.isSelected(), this);
				}
				else
				{
					sched = new ModifiedAstar(ActionMan.getGui().getSelectedAutomata(), (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), false, buildAutomaton.isSelected(), this);
				}
			}
			else if (optiMethodsBox.getSelectedItem().equals(MODIFIED_VGA_STAR))
			{
// 				sched = new ModifiedVGAstar(ActionMan.getGui().getSelectedAutomata(), (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), vgDrawer.isSelected(), false, buildAutomaton.isSelected(), this);
			}
			else if (optiMethodsBox.getSelectedItem().equals(MILP))
			{
				sched = new Milp(ActionMan.getGui().getSelectedAutomata(), buildAutomaton.isSelected(), this);
			}
			else if (optiMethodsBox.getSelectedItem().equals(VIS_GRAPH))
			{
				sched = new VisGraphScheduler(ActionMan.getGui().getSelectedAutomata(), vgDrawer.isSelected(), this);
			}
// 			else if (optiMethodsBox.getSelectedItem().equals("Modified IDA*"))
// 				throw new Exception("IMA* not implemented yet...");
// 			// 		sched = new ModifiedAstar(ActionMan.getGui().getSelectedAutomata(), (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), true);	
// 			else if (optiMethodsBox.getSelectedItem().equals("Modified SMA*"))
// 				throw new Exception("SMA* not implemented yet...");
			else 
			{
				throw new Exception("Unknown optimization method");
			}
		}
		catch (Exception excp) 
		{
			logger.error("ScheduleDialog::doit " + excp);
			logger.debug(excp.getStackTrace());
		}
    }
    
    /**
     *      Terminates the Schedule Dialog.
     */
    public void done()
    {
		try 
		{
			if (autoTestButton.isEnabled())
			{
				setVisible(false);
				dispose();
				getParent().repaint();
			}
			else
			{
				// Write the results of previous scheduling operation
				if (sched != null && sched instanceof ModifiedAstar)
				{
					writer.write(((ModifiedAstar)sched).getOutputString());
					writer.newLine();
					writer.newLine();
				}

				if (filesToSchedule.size() > 0)
				{
					// 				getParent().repaint();

					File currFile = (File)filesToSchedule.remove(0);

					writer.write(currFile.getName());
					writer.newLine();

					if (currFile.getName().contains(".xml"))
					{
						ActionMan.automataDeleteAll_actionPerformed(ActionMan.getGui());
						ActionMan.openFile(ActionMan.getGui(), currFile);
						ActionMan.getGui().invertSelection();
						// 					getParent().repaint();
					
						doit();
					}
					else
					{
						done();
					}
				}
				else 
				{
					writer.flush();

					autoTestButton.setEnabled(true);
					done();
				}
			}
		}
		catch (IOException ioe)
		{
			logger.error("Error at writing the results of scheduling to file");
		}
    }

    void readMemoryCapacity() {
		memoryCapacity = (int) (new Integer(memoryCapacityField.getText()));
    }

	/**
	 * Resets the buttons and the scheduler-instance
	 */
	public void reset()
	{
		cancelButton.setText("Cancel");
		okButton.setEnabled(true);

		sched = null;
	}
}

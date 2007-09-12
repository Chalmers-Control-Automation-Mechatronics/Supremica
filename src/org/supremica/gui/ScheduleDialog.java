
/********************** ScheduleDialog.java *****************/
package org.supremica.gui;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.scheduling.*;
import org.supremica.gui.ide.actions.IDEActionInterface;

public class ScheduleDialog
    extends JDialog
{
    public static final String MODIFIED_A_STAR = "Modified A*";
    public static final String MILP = "MILP";
    public static final String VIS_GRAPH = "Visibility Graph";
    public static final String MULTITHREADED_A_STAR = "Multithreaded A*";
    
    public static final String ONE_PRODUCT_RELAXATION = "1-product relax";
    public static final String TWO_PRODUCT_RELAXATION = "2-product relax";
    public static final String VIS_GRAPH_TIME_RELAXATION = "visibility graph (time)";
    public static final String VIS_GRAPH_NODE_RELAXATION = "visibility graph (node)";
    public static final String BRUTE_FORCE_RELAXATION = "brute force";
    private static final String OPTIMAL = "optimal";
    private static final String SUBOPTIMAL = "suboptimal";
    
    private static final long serialVersionUID = 1L;
    private static final String[] optimizationMehtods = new String[]{MODIFIED_A_STAR, MILP, VIS_GRAPH, MULTITHREADED_A_STAR}; //, "Modified IDA*", "Modified SMA*"};
    private static final String[] astarHeuristics = new String[]{ONE_PRODUCT_RELAXATION, SUBOPTIMAL, TWO_PRODUCT_RELAXATION, VIS_GRAPH_TIME_RELAXATION, VIS_GRAPH_NODE_RELAXATION, BRUTE_FORCE_RELAXATION};
    private static final String[] milpHeuristics = new String[]{OPTIMAL, SUBOPTIMAL};
    private static Logger logger = LoggerFactory.createLogger(ScheduleDialog.class);
    private JComboBox optiMethodsBox, heuristicsBox;
    private JCheckBox nodeExpander, buildAutomaton, vgDrawer;
    private int memoryCapacity;
    private JTextField memoryCapacityField;
    private JButton okButton, cancelButton;
    JButton autoTestButton; //Tillf
    private java.util.ArrayList filesToSchedule = new java.util.ArrayList();
    
    private Automata selectedAutomata = null;
    // TODO: do something with this ugly implementation (search for "ugly" in this file)
    // Ugly implementation due to difference between the interfaces for Supremica.java and IDE.java
    private IDEActionInterface ide = null;
    
    Scheduler sched = null;
    public Thread milpThread = null;
    
    private BufferedWriter writer = null; //Tillf
    
    public ScheduleDialog(IDEActionInterface ide)
    {
        super(ide.getFrame(), "Schedule Selected Automata", true);
        
        this.ide = ide;
//        selectedAutomata = getSelectedAutomata();
        selectedAutomata = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();  

        /******** Base components of the dialog ***********/
        okButton = new JButton("Schedule");
        cancelButton = new JButton("Cancel");
        
        JLabel optiMethodsLabel = new JLabel("Optimization methods: \t \t");
        optiMethodsBox = new JComboBox(optimizationMehtods);
        
        JLabel heuristicsLabel = new JLabel("Heuristics: \t \t");
        heuristicsBox = new JComboBox(astarHeuristics);
        
        nodeExpander = new JCheckBox("use AK's node expander", false);
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
                    
                    heuristicsBox.removeAllItems();
                    for (String heuristic : astarHeuristics)
                    {
                            heuristicsBox.addItem(heuristic);
                    }
                }
                else if (((String)optiMethodsBox.getSelectedItem()).equals("MILP"))
                {
                    heuristicsBox.setEnabled(true);
                    
                    heuristicsBox.removeAllItems();
                    for (String heuristic : milpHeuristics)
                    {
                        heuristicsBox.addItem(heuristic);
                    }
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
                File rootDir = new File(org.supremica.properties.Config.FILE_OPEN_PATH.get());
                File[] files = rootDir.listFiles();
                
                try
                {
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
            
            String selectedHeuristic = (String) heuristicsBox.getSelectedItem();
            if (optiMethodsBox.getSelectedItem().equals(MODIFIED_A_STAR))
            {
                if ((selectedAutomata.getPlantAutomata().size() == 2) && (selectedHeuristic.equals(VIS_GRAPH_TIME_RELAXATION) || selectedHeuristic.equals(VIS_GRAPH_NODE_RELAXATION)))
                {
                    sched = new VisGraphScheduler(selectedAutomata, vgDrawer.isSelected(), this);
                }
                else if (heuristicsBox.getSelectedItem().equals(SUBOPTIMAL))
                {
                    //temp
                    sched = new ModifiedAstar(selectedAutomata, ONE_PRODUCT_RELAXATION, 
                            nodeExpander.isSelected(), buildAutomaton.isSelected(), this, 
                            new double[]{30, 40});
                    
                    
                    
                    
//                    Thread mainThread = Thread.currentThread();
                    
//                    logger.info("0");
//                    ApproxWeightsDialog approxWeightsDlg = new ApproxWeightsDialog(this);
//                    return;
//                    Thread approxDlgThread = new Thread(approxWeightsDlg);
//                    approxDlgThread.start();
//                    mainThread.join();
//                    
////                    while (! approxWeightsDlg.isDone())
////                    {
////                        mainThread.sleep(1000);
////                    }
////                    synchronized (approxWeightsDlg)
////                    {
////                        mainThread.wait();
////                    
//                    logger.info("1");
////                    Thread.currentThread().wait();
//                    logger.info("2");
//                    sched = new ModifiedAstar(selectedAutomata, (String) heuristicsBox.getSelectedItem(), 
//                            nodeExpander.isSelected(), buildAutomaton.isSelected(), this, 
//                            approxWeightsDlg.getWeights());
//                    logger.info("3");
////                    }
                }
                else
                {
                    sched = new ModifiedAstar(selectedAutomata, (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), buildAutomaton.isSelected(), this);
                }
            }
            else if (optiMethodsBox.getSelectedItem().equals(MILP))
            {
                if (selectedHeuristic.equals(OPTIMAL))
                {
                    sched = new Milp(selectedAutomata, buildAutomaton.isSelected(), this);
                }
                else if (selectedHeuristic.equals(SUBOPTIMAL))
                {
                    sched = new RandomPathUsingMilp(selectedAutomata, buildAutomaton.isSelected(), this);
                }
            }
            else if (optiMethodsBox.getSelectedItem().equals(VIS_GRAPH))
            {
                sched = new VisGraphScheduler(selectedAutomata, vgDrawer.isSelected(), this);
            }
            else if (optiMethodsBox.getSelectedItem().equals(MULTITHREADED_A_STAR))
            {
                sched = new MultithreadedAstar(selectedAutomata, (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), buildAutomaton.isSelected(), false, this);
            }
// 			else if (optiMethodsBox.getSelectedItem().equals("Modified IDA*"))
// 				throw new Exception("IMA* not implemented yet...");
// 			// 		sched = new ModifiedAstar(selectedAutomata, (String) heuristicsBox.getSelectedItem(), nodeExpander.isSelected(), true);
// 			else if (optiMethodsBox.getSelectedItem().equals("Modified SMA*"))
// 				throw new Exception("SMA* not implemented yet...");
            else
            {
                throw new Exception("Unknown optimization method");
            }
            
            sched.startSearchThread();
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
                if (sched != null)
                {
                    writer.write(sched.getOutputString());
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
                    writer.close();
                    
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
    
    void readMemoryCapacity()
    {
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
    
    public void close()
    {
        setVisible(false);
        dispose();
    }

    public IDEActionInterface getIde()
    {
        return ide;
    }
    
//    // Ugly implementation 
//    public Automata getSelectedAutomata()
//    {
//        if (ide instanceof Gui)
//        {
//            return ((Gui) ide).getSelectedAutomata();
//        }
//        else if (ide instanceof IDEActionInterface)
//        {
//            return ((IDEActionInterface) ide).getIDE().getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
//        }
//    }
}

    
class ApproxWeightsDialog
        extends JDialog implements Runnable, ActionListener, KeyListener
{
    private Scheduler sched = null;
    private ScheduleDialog parentDlg = null;
    private JPanel ioPanel = new JPanel();
    private JPanel btnPanel = new JPanel();
    private JTextField xField = new JTextField(5);
    private JTextField yField = new JTextField(5);
    private JButton okBtn = new JButton("OK");
    private JButton cancelBtn = new JButton("Cancel");
    private double xWeight = -1;
    private double yWeight = -1;
    private boolean threadDone = false;
    private final static Logger logger = LoggerFactory.createLogger(ApproxWeightsDialog.class);
        
    public ApproxWeightsDialog(ScheduleDialog scheduleDlg)
        throws Exception
    {
        super(scheduleDlg);
        parentDlg = scheduleDlg;
        
        openApproxWeightsDialog();
    }
    
    public void run()
    {
        logger.info("in run");
               
//        try 
//        {
//            while (xWeight < 0 || yWeight < 0)
//            {
//                repaint();
//            }
       
        for (int i = 0; i < 5; i++)
        {
            repaint();
            logger.warn("in_run_nr_" + i);
        } 

            
            logger.info("thread done");
            threadDone = true;
            notifyAll();
//        }
//        catch (InterruptedException ex)
//        {
//            ex.printStackTrace();
//        }
    }
    
    private void openApproxWeightsDialog()
            throws Exception
    {
        // Set the main layout of the dialog box
        java.awt.BorderLayout headLayout = new java.awt.BorderLayout();
        headLayout.setHgap(5);
        headLayout.setVgap(5);
        setLayout(headLayout);

        // Set the layout of the x-y-input-panel and populate it
        ioPanel.setLayout(new java.awt.GridLayout(1, 8));
        ioPanel.setSize(this.getWidth() + 10, 100);
        ioPanel.add(new JLabel(""));
        ioPanel.add(new JLabel("x-weight: "));
        ioPanel.add(xField);
        ioPanel.add(new JLabel(""));
        ioPanel.add(new JLabel(""));
        ioPanel.add(new JLabel("y-weight: "));
        ioPanel.add(yField);
        ioPanel.add(new JLabel(""));

        // Set the layout of the button-panel and populate it
        btnPanel.setLayout(new java.awt.GridLayout(1, 2));
        btnPanel.setSize(this.getWidth() + 10, okBtn.getHeight() + 10);
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        // Add the action listener to the ok-button, calling ModifiedA* if the weights are set
        okBtn.addActionListener(this);

        // Close the dialog box if cancel is called
        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ev)
            {
                parentDlg.done();
            }
        });

        // Listen for the "enter"-key and click the ok-button if it happens
        xField.addKeyListener(this);
        yField.addKeyListener(this);

        // Populate the dialog box: add title, header text, x-y-input-panel and button-panel
        setTitle("Approximation weights for the A*");
        add(new JLabel(" Set the x- and y-weights used by the A*" + 
                "to find an approximative solution...  "), java.awt.BorderLayout.NORTH);
        add(ioPanel, java.awt.BorderLayout.CENTER);
        add(btnPanel, java.awt.BorderLayout.SOUTH);

        // Pack and show the dialog box
        pack();
        setVisible(true);  
    }
    
    /**
     * Returns the weight-values if they are non-negative, i.e if they have 
     * been set by the user.
     */
    public double[] getWeights()
        throws Exception
    {
        return new double[]{xWeight, yWeight};
    }
    
    public boolean isDone()
    {
        return threadDone;
    }
    
    /**
     * Called at the click of the ok-button. If the x- and y-fields are non-empty and
     * non-negative, the weight-values are stored.
     */
    public void actionPerformed(ActionEvent ev)
    {
        try
        {
            xWeight = new Double(xField.getText()).doubleValue();
            yWeight = new Double(yField.getText()).doubleValue();

            if (xWeight < 0 || yWeight < 0)
            {
                throw new NumberFormatException("At least one of the approximation weights is negative.");
            }

        }
        catch (NumberFormatException ex)
        {
            logger.error("The weights have incorrect format (must be positive floating numbers).");
        }
        catch (Exception excp)
        {
            logger.error("ScheduleDialog::doit " + excp);
            logger.debug(excp.getStackTrace());
        }
    }
    
    /**
     * If return is pressed in the x- or y-field, click the ok-button
     */
    public void keyPressed(KeyEvent ev)
    {
        if (ev.getKeyCode() == ev.VK_ENTER)
        {
            okBtn.doClick();
        }
    }
    
    // Necessary methods to implement the KeyListener-interface
    public void keyReleased(KeyEvent ev){}
    public void keyTyped(KeyEvent ev){}
}
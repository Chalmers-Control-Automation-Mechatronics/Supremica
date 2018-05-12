//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.SelectionOwner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.scheduling.ModifiedAstar;
import org.supremica.automata.algorithms.scheduling.MultithreadedAstar;
import org.supremica.automata.algorithms.scheduling.Scheduler;
import org.supremica.automata.algorithms.scheduling.SchedulingConstants;
import org.supremica.automata.algorithms.scheduling.VelocityBalancer;
import org.supremica.automata.algorithms.scheduling.VisGraphScheduler;
import org.supremica.automata.algorithms.scheduling.milp.Milp;
import org.supremica.automata.algorithms.scheduling.milp.RandomPathUsingMilp;
import org.supremica.gui.ide.EditorPanel;
import org.supremica.gui.ide.actions.IDEActionInterface;


public class ScheduleDialog
    extends JDialog
{
    private static final long serialVersionUID = 1L;
    private static final String[] optimizationMethods = new String[] {
        SchedulingConstants.MODIFIED_A_STAR,
        SchedulingConstants.MILP_GLPK,
        SchedulingConstants.MILP_CBC,
        SchedulingConstants.MILP_CPLEX,
        SchedulingConstants.VIS_GRAPH,
        SchedulingConstants.MULTITHREADED_A_STAR,
        "Velocity Balancing"}; //, "Modified IDA*", "Modified SMA*"};
    private static final String[] astarHeuristics = new String[] {
        SchedulingConstants.ONE_PRODUCT_RELAXATION,
        SchedulingConstants.SUBOPTIMAL,
        SchedulingConstants.TWO_PRODUCT_RELAXATION,
        SchedulingConstants.VIS_GRAPH_TIME_RELAXATION,
        SchedulingConstants.VIS_GRAPH_NODE_RELAXATION,
        SchedulingConstants.BRUTE_FORCE_RELAXATION};
    private static final String[] milpHeuristics = new String[] {SchedulingConstants.OPTIMAL, SchedulingConstants.SUBOPTIMAL};
    private static Logger logger = LogManager.getLogger(ScheduleDialog.class);
    private final JComboBox<String> optiMethodsBox, heuristicsBox;
    private final JCheckBox nodeExpander, buildAutomaton, vgDrawer, balanceVelocities;
    @SuppressWarnings("unused")
	private int memoryCapacity;
    private final JTextField memoryCapacityField;
    private final JButton okButton, cancelButton;
    JButton autoTestButton; //Tillf
    private final java.util.ArrayList<File> filesToSchedule = new java.util.ArrayList<File>();

    private Automata selectedAutomata = null;
    // TODO: do something with this ugly implementation (search for "ugly" in this file)
    // Ugly implementation due to difference between the interfaces for Supremica.java and IDE.java
    private IDEActionInterface ide = null;

    Scheduler sched = null;
    public Thread milpThread = null;

    private BufferedWriter writer = null; //Tillf

    public ScheduleDialog(final IDEActionInterface ide)
    {
        super(ide.getFrame(), "Schedule Selected Automata", true);

        this.ide = ide;
//        selectedAutomata = getSelectedAutomata();
        selectedAutomata = ide.getIDE().getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();

        /******** Base components of the dialog ***********/
        okButton = new JButton("Schedule");
        cancelButton = new JButton("Cancel");

        final JLabel optiMethodsLabel = new JLabel("Optimization methods: \t \t");
        optiMethodsBox = new JComboBox<String>(optimizationMethods);

        final JLabel heuristicsLabel = new JLabel("Heuristics: \t \t");
        heuristicsBox = new JComboBox<String>(astarHeuristics);

        nodeExpander = new JCheckBox("use AK's node expander", false);
        buildAutomaton = new JCheckBox("build schedule", true);
        vgDrawer = new JCheckBox("draw visibility graph", true);
        balanceVelocities = new JCheckBox("balance velocities", false);

        memoryCapacityField = new JTextField("300", 10);

        /******** Base containers of the dialog ***********/
        final JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        final JPanel optiPanel = new JPanel();
        optiPanel.add(optiMethodsLabel);
        optiPanel.add(optiMethodsBox);

        final JPanel heuristicsPanel = new JPanel();
        heuristicsPanel.add(heuristicsLabel);
        heuristicsPanel.add(heuristicsBox);

        final JPanel smaPanel = new JPanel();
        smaPanel.add(new JLabel("Nr of nodes in memory (SMA*)"));
        smaPanel.add(memoryCapacityField);


        // 	JPanel expanderPanel = new JPanel();
        // 	expanderPanel.add(nodeExpander);

        /********* Composite containers *******************/

        final JPanel algorithmPanel = new JPanel();
        algorithmPanel.setLayout(new GridLayout(2,1));
        algorithmPanel.add(optiPanel);
        algorithmPanel.add(heuristicsPanel);

        final JPanel specPanel = new JPanel();
        specPanel.setLayout(new GridLayout(2, 2));
        specPanel.add(nodeExpander);
        specPanel.add(buildAutomaton);
        specPanel.add(vgDrawer);
        specPanel.add(balanceVelocities);
        //	specPanel.add(smaPanel);

        final JTabbedPane tabbedPane = new JTabbedPane();
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
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                doit();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                if (sched != null)
                {
                    sched.requestAbort();
                    reset();
                }
                else
                {
                    done();
                }

            }
        });

        optiMethodsBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                if (((String)optiMethodsBox.getSelectedItem()).contains("A*"))
                {
                    heuristicsBox.setEnabled(true);

                    heuristicsBox.removeAllItems();
                    for (final String heuristic : astarHeuristics)
                    {
                            heuristicsBox.addItem(heuristic);
                    }
                }
                else if (((String)optiMethodsBox.getSelectedItem()).contains(SchedulingConstants.MILP))
                {
                    heuristicsBox.setEnabled(true);

                    heuristicsBox.removeAllItems();
                    for (final String heuristic : milpHeuristics)
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
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                try
                {
                    prepareAutoTest();
                }
                catch (final Exception ex)
                {
                    logger.error("Exception while performing autotest. " + ex.getMessage());
                }
            }
        });
    }

    public void prepareAutoTest()
            throws Exception
    {
        final File rootDir = new File(org.supremica.properties.Config.FILE_OPEN_PATH.getAsString());
        final File[] files = rootDir.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(final File dir, final String name)
                {
                    return name.endsWith(".xml");
                }
            });

        try
        {
            File resultFile = null;
            if (((String)optiMethodsBox.getSelectedItem()).equals(SchedulingConstants.MODIFIED_A_STAR))
            {
                resultFile = new File(rootDir + File.separator + heuristicsBox.getSelectedItem() + "_stat.txt");
            }
            else
            {
                resultFile = new File(rootDir + File.separator + optiMethodsBox.getSelectedItem() + "_stat.txt");
            }
            resultFile.createNewFile();

            writer =  new BufferedWriter(new FileWriter(resultFile));
        }
        catch (final IOException ioe)
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

            //temp - only here to be able to test the velocity balancer rapidly
            if (optiMethodsBox.getSelectedItem().equals("Velocity Balancing"))
            {
                final VelocityBalancer vb = new VelocityBalancer(selectedAutomata);
                for (int i = 0; i < vb.getOptimalSubPlants().size(); i++)
                {
                    ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomaton(vb.getOptimalSubPlants().getAutomatonAt(i));
                }

                //temp
                final Automata subControllers = vb.getSubControllers();
                for (int i = 0; i < subControllers.size(); i++)
                {
                    ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomaton(subControllers.getAutomatonAt(i));
                }

                close();
                return;
            }

            final String selectedHeuristic = (String) heuristicsBox.getSelectedItem();
            if (optiMethodsBox.getSelectedItem().equals(SchedulingConstants.MODIFIED_A_STAR))
            {
                if ((selectedAutomata.getPlantAutomata().size() == 2) &&
                        (selectedHeuristic.equals(SchedulingConstants.VIS_GRAPH_TIME_RELAXATION) ||
                        selectedHeuristic.equals(SchedulingConstants.VIS_GRAPH_NODE_RELAXATION)))
                {
                    sched = new VisGraphScheduler(selectedAutomata, vgDrawer.isSelected());
                }
                else if (heuristicsBox.getSelectedItem().equals(SchedulingConstants.SUBOPTIMAL))
                {
                    sched = new ModifiedAstar(selectedAutomata, SchedulingConstants.ONE_PRODUCT_RELAXATION,
                            nodeExpander.isSelected(), buildAutomaton.isSelected(), balanceVelocities. isSelected(),
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
                    sched = new ModifiedAstar(selectedAutomata, (String) heuristicsBox.getSelectedItem(),
                            nodeExpander.isSelected(), buildAutomaton.isSelected(), balanceVelocities.isSelected());
                }
            }
            else if (((String)optiMethodsBox.getSelectedItem()).contains(SchedulingConstants.MILP))
            {
                if (selectedHeuristic.equals(SchedulingConstants.OPTIMAL))
                {
                    sched = new Milp(selectedAutomata, buildAutomaton.isSelected(),
                            (String)optiMethodsBox.getSelectedItem(), balanceVelocities.isSelected());
                }
                else if (selectedHeuristic.equals(SchedulingConstants.SUBOPTIMAL))
                {
                    sched = new RandomPathUsingMilp(selectedAutomata, buildAutomaton.isSelected(), balanceVelocities.isSelected());
                }
            }
            else if (optiMethodsBox.getSelectedItem().equals(SchedulingConstants.VIS_GRAPH))
            {
                sched = new VisGraphScheduler(selectedAutomata, vgDrawer.isSelected());
            }
            else if (optiMethodsBox.getSelectedItem().equals(SchedulingConstants.MULTITHREADED_A_STAR))
            {
                sched = new MultithreadedAstar(selectedAutomata, (String) heuristicsBox.getSelectedItem(),
                        nodeExpander.isSelected(), buildAutomaton.isSelected(), false);
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

            // If autotest is used, a print-out is done elsewhere
            if (autoTestButton.isEnabled())
            {
                logger.info("Scheduling started...");
            }

            // Start the scheduling thread
            sched.startSearchThread();

            // Wait for the Scheduler to become stopped...
            while (!sched.isAborting())
            {
                Thread.sleep(10);
            }

            // ... add the schedule automaton to the GUI...¨(unless autotest is running)
            // If autotest is running, only print error messages
            if (autoTestButton.isEnabled())
            {
                addAutomatonToGUI(sched.getSchedule());

                // ... Print the messages (if there are any) to the screen...
                if (! sched.getMessages(SchedulingConstants.MESSAGE_TYPE_INFO).equals(""))
                {
                        logger.info(sched.getMessages(SchedulingConstants.MESSAGE_TYPE_INFO));
                }
                if (! sched.getMessages(SchedulingConstants.MESSAGE_TYPE_WARN).equals(""))
                {
                        logger.warn(sched.getMessages(SchedulingConstants.MESSAGE_TYPE_WARN));
                }
            }
            if (! sched.getMessages(SchedulingConstants.MESSAGE_TYPE_ERROR).equals(""))
            {
                    logger.error(sched.getMessages(SchedulingConstants.MESSAGE_TYPE_ERROR));
            }
            logger.debug(sched.getDebugMessages());

            // ... and dispose of the schedule dialog if there were no errors
            if (sched.getDebugMessages().length == 0)
            {
                done();
            }
            else
            {
                reset();
            }
        }
        catch (final Exception excp)
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

                logger.info("Scheduling done");
            }
            else
            {
                // Write the results of previous scheduling operation
                if (sched != null)
                {
                    writer.write(sched.getMessages(SchedulingConstants.MESSAGE_TYPE_INFO));
                    writer.newLine();
                    writer.newLine();
                }

                if (filesToSchedule.size() > 0)
                {
                    // 				getParent().repaint();

                    final File currFile = filesToSchedule.remove(0);

                    logger.info("Scheduling " + currFile.getPath());

                    writer.write(currFile.getName());
                    writer.newLine();

//                    if (currFile.getName().contains(".xml"))
//                    {
//                        ActionMan.automataDeleteAll_actionPerformed(ActionMan.getGui());
//                        ActionMan.openFile(ActionMan.getGui(), currFile);
//                        ActionMan.getGui().invertSelection();
                        // 					getParent().repaint();

//                        ide.getActiveDocumentContainer().getAnalyzerPanel().getVisualProject().clear();
                        //ide.getActiveDocumentContainer().getAnalyzerPanel().addProject();
                        //ide.getIDE().getDocumentContainerManager().openContainer(currFile);

                        // Open the current file
                        ide.getActiveDocumentContainer().getIDE().getDocumentContainerManager().openContainer(currFile);
                        // Switch to the analyzer panel
                        ((JTabbedPane)ide.getActiveDocumentContainer().getPanel()).setSelectedComponent(
                                ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel());
                        // Select all the automata
                        selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getAllAutomata();

                        // Schedule using the chosen settings
                        doit();
//                    }
//                    else
//                    {
//                        done();
//                    }
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
        catch (final IOException ioe)
        {
            logger.error("Error at writing the results of scheduling to file");
        }
    }

    void readMemoryCapacity()
    {
        memoryCapacity = (new Integer(memoryCapacityField.getText()));
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

    private void addAutomatonToGUI(final Automaton scheduleAuto)
            throws Exception
    {
        if (scheduleAuto != null)
        {
            // Choose a name for the schedule automaton
            String scheduleName = "";
            while (scheduleName != null && scheduleName.trim().equals(""))
            {
                scheduleName = getIde().getActiveDocumentContainer().getSupremicaAnalyzerPanel().getNewAutomatonName(
                        "Enter a name for the schedule", "Schedule");
            }

            // If the name is non-null, add the schedule automaton to the GUI
            if (scheduleName != null)
            {
                scheduleAuto.setName(scheduleName);
                ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomaton(scheduleAuto);

                // The following part should be somewhere else, but unfortunately the communication between the editor
                // and the analyzer are not automatic in the IDE, tuff luck...
                if (ide.getActiveDocumentContainer().getEditorPanel() != null)
                {
                    // Compile into Waters module
                    final net.sourceforge.waters.model.marshaller.ProductDESImporter importer =
                            new net.sourceforge.waters.model.marshaller.ProductDESImporter(
                            net.sourceforge.waters.subject.module.ModuleSubjectFactory.getInstance());

                    // Adds the cost in the state to the name of the state for correct display in the editor
                    final Automaton scheduleAutoForEditor = scheduleAuto.clone();
                    for (final java.util.Iterator<org.supremica.automata.State> states = scheduleAutoForEditor.stateIterator(); states.hasNext(); )
                    {
                        final org.supremica.automata.State state = states.next();
                        state.setName(state.getName() + ", cost=" + state.getCost());
                    }

                    final net.sourceforge.waters.model.module.SimpleComponentProxy component =
                            importer.importComponent(scheduleAutoForEditor);
                    try
                    {
                        // Add to current module
                        final net.sourceforge.waters.model.module.IdentifierProxy ident = component.getIdentifier();
                        final net.sourceforge.waters.gui.ModuleContext context = ide.getActiveDocumentContainer().getEditorPanel().getModuleContext();
                        context.checkNewComponentName(ident);
                        final EditorPanel root = ide.getActiveDocumentContainer().getEditorPanel();
                        final SelectionOwner panel = root.getComponentsPanel();
                        final Command cmd = new InsertCommand(component, panel, null, false);
                        final UndoInterface iface = root.getUndoInterface();
                        iface.executeCommand(cmd);
                        // Add all (new) events to the module
                        final net.sourceforge.waters.subject.module.ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
                        boolean problem = false;
                        for (final org.supremica.automata.LabeledEvent event : scheduleAuto.getAlphabet())
                        {
                            final String name = event.getName();
                            if (!name.contains("["))
                            {
                                boolean found = false;
                                for (final net.sourceforge.waters.model.module.EventDeclProxy decl : module.getEventDeclList()) {
                                    if (decl.getName().equals(name))
                                    {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    final net.sourceforge.waters.subject.module.SimpleIdentifierSubject nameident = new net.sourceforge.waters.subject.module.SimpleIdentifierSubject(name);
                                    final net.sourceforge.waters.subject.module.EventDeclSubject decl =
                                            new net.sourceforge.waters.subject.module.EventDeclSubject(nameident,
                                                                                                       event.getKind(),
                                                                                                       event.isObservable(),
                                                                                                       net.sourceforge.waters.xsd.module.ScopeKind.LOCAL,
                                                                                                       null, null, null);
                                    module.getEventDeclListModifiable().add(decl);
                                }
                            }
                            else
                            {
                                problem = true;
                            }
                        }
                        if (problem)
                        {
                            javax.swing.JOptionPane.showMessageDialog(ide.getFrame(), "There is a problem in the back-translation of parametrised events.", "Alert", javax.swing.JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    catch (final Exception ex)
                    {
                        logger.error("Could not add " + scheduleAuto + " to editor." + ex);
                    }
                }
                else
                {
                    javax.swing.JOptionPane.showMessageDialog(ide.getFrame(), "The editor is unknown. The schedule was not added.", "Editor null", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                logger.error("The schedule automaton was not added (schedule.name = null)");
            }
        }
    }
}


class ApproxWeightsDialog
        extends JDialog implements Runnable, ActionListener, KeyListener
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final Scheduler sched = null;
    private ScheduleDialog parentDlg = null;
    private final JPanel ioPanel = new JPanel();
    private final JPanel btnPanel = new JPanel();
    private final JTextField xField = new JTextField(5);
    private final JTextField yField = new JTextField(5);
    private final JButton okBtn = new JButton("OK");
    private final JButton cancelBtn = new JButton("Cancel");
    private double xWeight = -1;
    private double yWeight = -1;
    private boolean threadDone = false;
    private final static Logger logger = LogManager.getLogger(ApproxWeightsDialog.class);

    public ApproxWeightsDialog(final ScheduleDialog scheduleDlg)
        throws Exception
    {
        super(scheduleDlg);
        parentDlg = scheduleDlg;

        openApproxWeightsDialog();
    }

    @Override
    public void run()
    {
//        try
//        {
//            while (xWeight < 0 || yWeight < 0)
//            {
//                repaint();
//            }

        for (int i = 0; i < 5; i++)
        {
            repaint();
        }



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
        final java.awt.BorderLayout headLayout = new java.awt.BorderLayout();
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
            @Override
            public void actionPerformed(final ActionEvent ev)
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
    @Override
    public void actionPerformed(final ActionEvent ev)
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
        catch (final NumberFormatException ex)
        {
            logger.error("The weights have incorrect format (must be positive floating numbers).");
        }
        catch (final Exception excp)
        {
            logger.error("ScheduleDialog::doit " + excp);
            logger.debug(excp.getStackTrace());
        }
    }

    /**
     * If return is pressed in the x- or y-field, click the ok-button
     */
    @Override
    public void keyPressed(final KeyEvent ev)
    {
        if (ev.getKeyCode() == KeyEvent.VK_ENTER)
        {
            okBtn.doClick();
        }
    }

    // Necessary methods to implement the KeyListener-interface
    @Override
    public void keyReleased(final KeyEvent ev){}
    @Override
    public void keyTyped(final KeyEvent ev){}
}

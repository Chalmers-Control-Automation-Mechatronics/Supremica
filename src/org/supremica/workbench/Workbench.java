package org.supremica.workbench;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import net.sourceforge.waters.gui.about.AboutPanel;
import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.State;
import org.supremica.automata.StateSet;
import org.supremica.automata.IO.AutomataSerializer;
import org.supremica.automata.IO.AutomatonToDot;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.AutomatonSynthesizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.gui.AutomatonViewer;
import org.supremica.gui.AutomatonViewerFactory;
import org.supremica.gui.CenteredFrame;
import org.supremica.gui.VisualProject;
import org.supremica.properties.Config;
import org.supremica.testcases.StickPickingGame;


/**
 * The main frame for the workbench. It contains the GUI for the manual
 * synthesis procedure we impose on the students.
 *
 * @author Martin Fabian
 */

public class Workbench
    extends CenteredFrame
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(Workbench.class);
    Automata automata = null;    // these are used "globally" in this file
    Automaton automaton = null;    // eventually the resulting supervisor
    VisualProject project = null;
//	AutomatonViewer viewer = null;	// Each workbench manages only a single viewer
    AutomataSynchronizer syncher = null;
    private ParamPanel params;
    private ButtonPanel buttons;
    private InfoPanel info;
    public Workbench(final VisualProject project, final Automata automata)
    throws Exception
    {
        //super(228, 432);
        super(235, 475);
        setTitle("Supervisor Workbench");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(final WindowEvent we)
            {
                close();
            }
        });
        setResizable(false);
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(params = new ParamPanel(this), constraints);
        panel.add(buttons = new ButtonPanel(this), constraints);
        panel.add(info = new InfoPanel(this), constraints);
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
        // Should we show it and is there something to show?
        if (toShowGraph() && automaton != null)
        {
            try
            {
                if(project.existsAutomatonViewer(automaton))
                {
                    final AutomatonViewer viewer = project.returnAutomatonViewer(automaton);
                    if (viewer.isVisible() == false)
                    {
                        viewer.setVisible(true);
                    }
                }
                else
                {
                    if(project.showAutomatonViewer(automaton))
                    {
                        final AutomatonViewer viewer = project.createAutomatonViewer(automaton, new MyAutomatonViewerFactory());
                        viewer.setVisible(true);
                    }
                }
            }
            catch(final Exception excp)
            {
				logger.error(excp);
                //System.out.println("Something bad occurred");
            }
        }
    }

    void hideGraph()
    {
        try
        {
            if (automaton != null && project.existsAutomatonViewer(automaton))
            {
                final AutomatonViewer viewer = project.returnAutomatonViewer(automaton);
                if (viewer.isVisible())
                {
                    viewer.setVisible(false);
                }
            }
        }
        catch(final Exception excp)
        {
            logger.error("Error in hiding viewer", excp);
        }
    }

    void showMessage(final String mess)
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
     * Add current automaton to the project
     */
    void addAutomaton()
    {
        project.addAutomaton(automaton);
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
            if (automata.hasNoPlants())
            {
                buttons.compareButton.setEnabled(false);
            }
        }
    }

    // For debugging only
    public static void main(final String args[])
    throws Exception
    {
        final StickPickingGame game = new StickPickingGame(2, 7);
        final Workbench wb = new Workbench(null, game.getProject());
        wb.setVisible(true);
    }
}

//*** Finally! A valid use for inheritance -- inherit to be reused!!
// We want the colors to be different from the default so we inherit
// and make our own AutoatonToDot serializer
// Had to change access to AutomatonToDot::getColor(), made it protected
// instead of private, but hey, can't think of everything
class MyAutomatonToDot
    extends AutomatonToDot
{
    public MyAutomatonToDot(final Automaton automaton)
    {
        // logger.info("MyAutomatonToDot::constructed");
        super(automaton);
    }

    @Override
    protected String getStateColor(final State state)
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
    public MyAutomatonViewer(final Automaton theAutomaton)
    throws Exception
    {
        // logger.info("MyAutomatonViewer::constructed");
        super(theAutomaton);
    }

    @Override
    public AutomataSerializer getSerializer()
    {
        final MyAutomatonToDot serializer = new MyAutomatonToDot(getAutomaton());
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
    @Override
    public AutomatonViewer createAutomatonViewer(final Automaton automaton)
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

    ButtonImpl(final String text, final Workbench wb, final String tooltip)
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
    private static Logger logger = LogManager.getLogger(SynchButton.class);
    SynchButton(final Workbench wb)
    {
        super("Synch", wb, "Synchronize the selected automata (make total specification)");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }

    /**
     * Synchronize all. Note that we do not mark new forbidden states
     * Probably this will change the look of the graph after
     * comparison, but what can we do?
     */
    void action(final ActionEvent e)
    {
        final SynchronizationOptions synch_ops = SynchronizationOptions.getDefaultSynchronizationOptions();
        synch_ops.setForbidUncontrollableStates(false);
        synch_ops.setExpandForbiddenStates(true); // (may be explicit ones)
        synch_ops.setExpandEventsUsingPriority(false);
        synch_ops.setBuildAutomaton(true);
        synch_ops.setRequireConsistentControllability(true);
        synch_ops.setRequireConsistentImmediate(false);
        synch_ops.setRememberDisabledEvents(false); // don't redirect disabled events to dump-state
        try
        {    // wb.syncher *should* be null here - we assume this!
            wb.syncher = new AutomataSynchronizer(wb.automata, synch_ops, Config.SYNTHESIS_SUP_AS_PLANT.getValue());
            wb.syncher.execute();
            wb.automaton = wb.syncher.getAutomaton();
            // Which behavior is the "correct" one?  If we set the
            // name, the user won't be asked, not even if asame-named
            // automaton already exists If we don't set the name, the
            // user will be prompted with a suggested name, and
            // prompted again until a unique name is given
            wb.automaton.setName("sup(" + wb.automaton.getComment() + ")");
            wb.automaton.setType(AutomatonType.SUPERVISOR);
            // Note that there may be explicitly specified uc-states
            // These are not "new" in a direct sense, but...
            wb.showMessage(wb.automaton.nbrOfForbiddenStates() +
                " forbidden state(s) (out of " + wb.automaton.nbrOfStates() + ")");
            wb.showGraph();
            wb.updateButtons();
        }
        catch (final Exception excp)
        {
            // synchronizer and viewer may throw. what then?
            logger.error(excp + " in SynchButton::action()");
            logger.debug(excp.getStackTrace());
        }
    }
}

/**
 * "Compare" button, this button examines the controllablity status of
 * the current supervisor with respect to the plant model that it is
 * supposed to control, i.e.  the plants (and all other automata) that
 * were selected previously in the gui.
 */
class CompareButton
    extends ButtonImpl
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(CompareButton.class);
    CompareButton(final Workbench wb)
    {
        super("Compare", wb, "Compare supervisor candidate vs. plant for controllability problems");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }

    /**
     * Compare the synched result with the plant If the automaton
     * exists, calc new states (only) and manually set the
     * forbiddenness
     */
    void action(final ActionEvent e)
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
        final SynchronizationOptions synch_ops = SynchronizationOptions.getDefaultSynchronizationOptions();
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
            final Automata plantsAndSupervisors = new Automata(wb.automata);
            final Automaton candidateSupervisor = new Automaton(wb.automaton);
            // We need this ugly thing to be able to find the proper state name after composition...
            candidateSupervisor.normalizeStateIdentities();
            plantsAndSupervisors.addAutomaton(candidateSupervisor);
            wb.syncher = new AutomataSynchronizer(plantsAndSupervisors, synch_ops, Config.SYNTHESIS_SUP_AS_PLANT.getValue());
            wb.syncher.execute();
            final Automaton aut = wb.syncher.getAutomaton();
            for (final Iterator<State> it = aut.stateIterator(); it.hasNext(); )
            {
                final State state = it.next();
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
                    name = name.substring(0, name.lastIndexOf(Config.GENERAL_STATE_SEPARATOR.getValue()));
                    // Find this state in automaton
                    final State s = wb.automaton.getStateWithName(name);
                    s.setForbidden(true);
                    s.setSelected(true);
                    logger.debug("Setting " + s.getName() + " forbidden");
                }
            }
        }
        catch (final Exception excp)
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
        catch (final Exception excp)
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
    private static Logger logger = LogManager.getLogger(ContButton.class);
    ContButton(final Workbench wb)
    {
        super("Controllability", wb, "Forbid states that uncontrollably can reach forbidden states");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }

    // let's calc the new uc-states
    void action(final ActionEvent e)
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
            final int num_x_states = wb.automaton.nbrOfForbiddenStates();    // cache this value so we know the number of new ones
            final StateSet state_set = new StateSet();    // store the newly forbidden states her, want to avoid duplicates
            final AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());
            // Now, for each state that is forbidden now, calc the new uncontrollable ones
            for (final Iterator<State> it = wb.automaton.stateIterator(); it.hasNext(); )
            {
                final State state = it.next();
                if (state.isForbidden())
                {
                    state.setSelected(false);
                    state_set.addAll(synth.doControllable(state));
                }
            }
            // Traverse the new set of forbidden states and set the forbidden flag
            for (final Iterator<State> it = state_set.iterator(); it.hasNext(); )
            {
                final State state = it.next();
                state.setForbidden(true);
                state.setSelected(true);    // show the new ones in bold
            }
            wb.showGraph();
            wb.showMessage(wb.automaton.nbrOfForbiddenStates()-num_x_states +
                " new uncontrollable state(s)");
            wb.automaton.invalidate();
        }
        catch (final Exception excp)
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
    private static Logger logger = LogManager.getLogger(NonblockButton.class);
    NonblockButton(final Workbench wb)
    {
        super("Coreachability", wb, "Forbid states that can not reach marked states");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }

    // Here we are to forbid *non*coreachable states
    // So we have to calc the coreachable (and nonforbidden ones) and forbid the rest
    public void action(final ActionEvent e)
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
            final int num_x_states = wb.automaton.nbrOfForbiddenStates();    // cache this value so we know the number of new ones
            final AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());
            synth.initializeAcceptingStates();
            final StateSet states = synth.doCoreachable(); // returns non-coreachable states
            wb.automaton.clearSelectedStates();
            for (final Iterator<State> it = states.iterator(); it.hasNext(); )
            {
                final State state = it.next();
                state.setForbidden(true);
                state.setSelected(true);
            }
            wb.showGraph();
            wb.showMessage(wb.automaton.nbrOfForbiddenStates()-num_x_states +
                " new blocking state(s)");
            wb.automaton.invalidate();
        }
        catch (final Exception excp)
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
    private static Logger logger = LogManager.getLogger(ReachButton.class);
    ReachButton(final Workbench wb)
    {
        super("Reachability", wb, "Forbid non-reachable states");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }
    void action(final ActionEvent e)
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
            final int num_x_states = wb.automaton.nbrOfForbiddenStates();    // cache this value so we know the number of new ones
            wb.automaton.clearSelectedStates();
            final AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());
            synth.doReachable();
            for (final Iterator<State> it = wb.automaton.stateIterator(); it.hasNext(); )
            {
                final State state = it.next();
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
        catch (final Exception excp)
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
    private static Logger logger = LogManager.getLogger(ReachButton.class);
    PurgeButton(final Workbench wb)
    {
        super("Purge", wb, "Remove all forbidden states");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }
    void action(final ActionEvent e)
    {
        // Is there an automaton to work with?
        if (wb.automaton == null)
        {
            wb.showMessage("Press Synch to get started");
            return;
        }
        final int before = wb.automaton.nbrOfStates();
        // Do the thing!
        try
        {
            final AutomatonSynthesizer synth = new AutomatonSynthesizer(wb.automaton, SynthesizerOptions.getDefaultSynthesizerOptions());
            synth.purge();
            wb.showGraph();
            wb.automaton.invalidate();
        }
        catch (final Exception excp)
        {
            logger.error(excp + " in PurgeButton::action");
            logger.debug(excp.getStackTrace());
            return;
        }
        final int after = wb.automaton.nbrOfStates();
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
    DoneButton(final Workbench wb)
    {
        super("Done", wb, "Exit workbench (return supervisor)");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                action(e);
            }
        });
    }
    void action(final ActionEvent e)
    {
        // Add to gui?
        if (wb.automaton != null && wb.toAddIt())
        {
            wb.addAutomaton();
                        /*
                        Gui gui = ActionMan.getGui();
                        if (gui != null)
                        {
                                gui.addAutomaton(wb.automaton);
                        }
                         */
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
    public ParamPanel(final Workbench wb)
    {
        this.wb = wb;
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Parameters"));
        setLayout(new GridLayout(0, 1));
        final boolean dotAvailable = AboutPanel.isDotAvailable();
        add(show_graph = new JCheckBox("Show graph", dotAvailable));
        if (!dotAvailable) {
          show_graph.setEnabled(false);
          final StringBuilder builder = new StringBuilder();
          builder.append("<HTML><BODY><P STYLE=\"font-size: ");
          builder.append(IconAndFontLoader.HTML_FONT_SIZE);
          builder.append("px; width: ");
          builder.append((int) Math.ceil(320 * IconAndFontLoader.GLOBAL_SCALE_FACTOR));
          builder.append("px;\">");
          builder.append("GraphViz/Dot not available. ");
          builder.append("To show the graph, please install GraphViz and ");
          builder.append("configure it using menu <I>Configure</I>, ");
          builder.append("<I>Options</I>, tab <I>Supremica Analyzer</I>, ");
          builder.append("and sub-tab <I>Dot</I>.");
          builder.append("</P></BODY></HTML>");
          show_graph.setToolTipText(builder.toString());
        }
        // These are not used?
        // add(list_uc = new JCheckBox("List new uncontrollable states", true));
        // add(list_nb = new JCheckBox("List new non-blocking states", true));
        add(add_it = new JCheckBox("Add final result to the project", true));
        show_graph.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                toggle_show(e);
            }
        });
    }
    private void toggle_show(final ActionEvent e)
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
        catch (final Exception excp)
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
    ButtonImpl synchButton;
    ButtonImpl compareButton;
    ButtonImpl contButton;
    ButtonImpl nonblockButton;
    ButtonImpl reachButton;
    ButtonImpl purgeButton;
    ButtonImpl doneButton;
    public ButtonPanel(final Workbench wb)
    {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Synthesize"));
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 0, 4));
        panel.add(synchButton = new SynchButton(wb));
        panel.add(compareButton = new CompareButton(wb));
        panel.add(contButton = new ContButton(wb));
        panel.add(nonblockButton = new NonblockButton(wb));
        panel.add(reachButton = new ReachButton(wb));
        panel.add(purgeButton = new PurgeButton(wb));
        panel.add(doneButton = new DoneButton(wb));
        add(panel);
    }
    @Override
    public void disable()
    {
        setEnabled(false);
    }
    @Override
    public void enable()
    {
        setEnabled(true);
    }
}

class InfoPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    //JTextField text = new JTextField("", 18);
    //JTextArea text = new JTextArea(2,22);
    JTextPane text = new JTextPane();

    InfoPanel(final Workbench wb)
    {
        //text.setFont(new JTextField().getFont()); // Didn't know how to find the right font...
        text.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        text.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        text.setEditable(false);
        text.setBackground(super.getBackground());
        add(text);
    }

    void setText(final String string)
    {
        text.setText(string);
    }
}

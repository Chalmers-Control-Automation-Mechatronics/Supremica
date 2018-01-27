
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.net.URL;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.execution.Action;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.Command;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Controls;
import org.supremica.automata.execution.EventTimer;
import org.supremica.automata.execution.Signal;
import org.supremica.automata.execution.Signals;
import org.supremica.automata.execution.Timers;


/**
 * A project is a automata object together with actions and controls.
 * A Project might be encapsulated in a gui.VisualProject object
 * if this is a graphical application.
 */

public class Project
    extends Automata
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(Project.class);

	private Actions theActions = null;
    private Controls theControls = null;
    private Signals theInputSignals = null;
    private Signals theOutputSignals = null;
    private Timers theTimers = null;
    private URL animationURL = null;
    private URL userInterfaceURL = null;

    public Project()
    {
        theActions = new Actions();
        theControls = new Controls();
        theInputSignals = new Signals();
        theOutputSignals = new Signals();
        theTimers = new Timers();
    }

    public Project(final String name)
    {
        this();

        setName(name);
    }

    public Project(final Project otherProject)
    {
        super(otherProject);

        theActions = new Actions(otherProject.theActions);
        theControls = new Controls(otherProject.theControls);
        theInputSignals = new Signals(otherProject.theInputSignals);
        theOutputSignals = new Signals(otherProject.theOutputSignals);
        theTimers = new Timers(otherProject.theTimers);

        setName(otherProject.getName());
    }

    public Project(final Project otherProject, final boolean shallowCopy)
    {
        super(otherProject, shallowCopy);

        theActions = new Actions(otherProject.theActions);
        theControls = new Controls(otherProject.theControls);
        theInputSignals = new Signals(otherProject.theInputSignals);
        theOutputSignals = new Signals(otherProject.theOutputSignals);
        theTimers = new Timers(otherProject.theTimers);

        setName(otherProject.getName());
    }
    public Actions getActions()
    {
        return theActions;
    }

    public Iterator<Action> actionIterator()
    {
        return theActions.iterator();
    }

    public Controls getControls()
    {
        return theControls;
    }

    public Iterator<Control> controlIterator()
    {
        return theControls.iterator();
    }

    public Signals getInputSignals()
    {
        return theInputSignals;
    }

    public Iterator<Signal> inputSignalsIterator()
    {
        return theInputSignals.iterator();
    }

    public Signals getOutputSignals()
    {
        return theOutputSignals;
    }

    public Iterator<Signal> outputSignalsIterator()
    {
        return theOutputSignals.iterator();
    }

    public Timers getTimers()
    {
        return theTimers;
    }

    public Iterator<EventTimer> timerIterator()
    {
        return theTimers.iterator();
    }

    public boolean hasAnimation()
    {
        return animationURL != null;
    }

    public URL getAnimationURL()
    {
        return animationURL;
    }

    /**
     * Set an absolute path
     **/
    public void setAnimationURL(final URL url)
    {
        animationURL = url;
    }

    public boolean hasUserInterface()
    {
        return userInterfaceURL != null;

    }

    public URL getUserInterfaceURL()
    {
        return userInterfaceURL;
    }

    /**
     * Set an absolute path
     **/
    public void setUserInterfaceURL(final URL url)
    {
        userInterfaceURL = url;
    }

/*
                public InputProtocol getInputProtocol()
                {
                                return inputProtocol;
                }

                public void setInputProtocol(InputProtocol theProtocol)
                {
                                this.inputProtocol = theProtocol;
                }
 */
    public void addAttributes(final Project otherProject)
    {
        addInputSignals(otherProject.getInputSignals());
        addOutputSignals(otherProject.getOutputSignals());
        addActions(otherProject.getActions());
        addControls(otherProject.getControls());
        addTimers(otherProject.getTimers());
        setAnimationURL(otherProject.getAnimationURL());
        setUserInterfaceURL(otherProject.getUserInterfaceURL());
        setName(otherProject.getName());
        setComment(otherProject.getComment());
    }

    private void addInputSignals(final Signals otherSignals)
    {
        if (theInputSignals == null)
        {
            theInputSignals = new Signals();
        }

        theInputSignals.addSignals(otherSignals);
        notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
    }

    private void addOutputSignals(final Signals otherSignals)
    {
        if (theOutputSignals == null)
        {
            theOutputSignals = new Signals();
        }

        theOutputSignals.addSignals(otherSignals);
        notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
    }

    private void addActions(final Actions otherActions)
    {
        if (theActions == null)
        {
            theActions = new Actions();
        }

        theActions.addActions(otherActions);
        notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
    }

    private void addControls(final Controls otherControls)
    {
        if (theControls == null)
        {
            theControls = new Controls();
        }

        theControls.addControls(otherControls);
        notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
    }

    private void addTimers(final Timers otherTimers)
    {
        if (theTimers == null)
        {
            theTimers = new Timers();
        }

        theTimers.addTimers(otherTimers);
        notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
    }

    @SuppressWarnings("unused")
	private void clearActions()
    {
        if (theActions != null)
        {
            theActions.clear();
            notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
        }
    }

    @SuppressWarnings("unused")
	private void clearControls()
    {
        if (theControls != null)
        {
            theControls.clear();
            notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
        }
    }

    @SuppressWarnings("unused")
	private void clearTimers()
    {
        if (theTimers != null)
        {
            theTimers.clear();
            notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
        }
    }

    public void clearExecutionParameters()
    {
        theActions.clear();
        theControls.clear();
        theTimers.clear();
    }

    public boolean validExecutionParameters()
    {
        Alphabet theAlphabet;

        try
        {
            theAlphabet = AlphabetHelpers.getUnionAlphabet(this, false, false);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }

        boolean valid = true;

        for (final Iterator<Action> theIt = actionIterator(); theIt.hasNext(); )
        {
            final Action currAction = theIt.next();
            final String currLabel = currAction.getLabel();

            if (!theAlphabet.contains(currLabel))
            {
                valid = false;

                logger.error("The action " + currLabel + " is not a valid event");
            }

            for (final Iterator<Command> theCmdIt = currAction.commandIterator();
            theCmdIt.hasNext(); )
            {
                final Command currCommand = theCmdIt.next();
                final String currCommandLabel = currCommand.getLabel();

                if (!theOutputSignals.hasSignal(currCommandLabel))
                {
                    valid = false;

                    logger.error("The command " + currCommandLabel + " is not a valid output signal");
                }
            }
        }

        for (final Iterator<Control> theIt = controlIterator(); theIt.hasNext(); )
        {
            final Control currControl = theIt.next();
            final String currLabel = currControl.getLabel();

            if (!theAlphabet.contains(currLabel))
            {
                valid = false;

                logger.error("The control " + currLabel + " is not a valid event");
            }

            for (final Iterator<Condition> theCondIt = currControl.conditionIterator();
            theCondIt.hasNext(); )
            {
                final Condition currCondition = theCondIt.next();
                final String currConditionLabel = currCondition.getLabel();

                if (!theInputSignals.hasSignal(currConditionLabel))
                {
                    valid = false;

                    logger.error("The condition " + currConditionLabel + " is not a valid output signal");
                }
            }
        }

        for (final Iterator<EventTimer> theIt = timerIterator(); theIt.hasNext(); )
        {
            final EventTimer currTimer = theIt.next();
            final String currStartEvent = currTimer.getStartEvent();

            if (!theAlphabet.contains(currStartEvent))
            {
                valid = false;

                logger.error("The start event, " + currStartEvent + ", in timer " + currTimer.getName() + " is not a valid event");
            }

            final String currTimeoutEvent = currTimer.getTimeoutEvent();

            if (!theAlphabet.contains(currTimeoutEvent))
            {
                valid = false;

                logger.error("The timeout event, " + currStartEvent + ", in timer " + currTimer.getName() + " is not a valid event");
            }
        }

        return valid;
    }

    /**
     * Set the synchronization indices. The returned alphabet is the union alphabet
     * and contains the synchronization index of all the events in this automata.
     */
    @Override
    public Alphabet setIndices()
    {
        theTimers.setIndices();

        return super.setIndices();
    }

    @Override
    public void clear()
    {
        clearExecutionParameters();
        super.clear();
    }

    public boolean equalProject(final Project other)
    {
        if (!equalAutomata(other))
        {
            return false;
        }

        // Add more checks here
        return true;
    }

    public boolean hasExecutionParameters()
    {
        return hasAnimation() || (getTimers().size() > 0) || (getInputSignals().size() > 0) || (getOutputSignals().size() > 0) || (getActions().size() > 0) || (getControls().size() > 0);
    }

    public boolean hasNonPrioritizedEvents()
    {
      for (final Automaton aut : this) {
        for (final LabeledEvent event : aut.getAlphabet()) {
          if (!event.isPrioritized()) {
            return true;
          }
        }
      }
      return false;
    }
}

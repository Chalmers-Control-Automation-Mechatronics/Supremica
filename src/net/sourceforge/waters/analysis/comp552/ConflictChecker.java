//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * <P>A dummy implementation of a conflict checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is nonconflicting.</P>
 *
 * <P>You are welcome to edit this file as much as you like,
 * but please <STRONG>do not change</STRONG> the public interface.
 * Do not change the signature of the two constructors,
 * or of the {@link #run()} or {@link #getCounterExample()} methods.
 * You should expect a single constructor call, followed by several calls
 * to {@link #run()} and {@link #getCounterExample()}, so your code needs
 * to be reentrant.</P>
 *
 * <P><STRONG>WARNING:</STRONG> If you do not comply with these rules, the
 * automatic tester may fail to run your program, resulting in 0 marks for
 * your assignment.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class ConflictChecker extends ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   * @param  model      The model to be checked by this conflict checker.
   * @param  desFactory Factory used for trace construction.
   */
  public ConflictChecker(final ProductDESProxy model,
                         final ProductDESProxyFactory desFactory)
  {
    this(model, getDefaultMarkingProposition(model), desFactory);
  }


  /**
   * Creates a new conflict checker to check a particular model.
   * @param  model      The model to be checked by this conflict checker.
   * @param  marking    The proposition event that defines which states
   *                    are marked. Every state has a list of propositions
   *                    attached to it; the conflict checker considers only
   *                    those states as marked that are labelled by
   *                    <CODE>marking</CODE>, i.e., their list of
   *                    propositions must contain this event (exactly the
   *                    same object).
   * @param  desFactory Factory used for trace construction.
   */
  public ConflictChecker(final ProductDESProxy model,
                         final EventProxy marking,
                         final ProductDESProxyFactory desFactory)
  {
    super(model, desFactory);
    mMarking = marking;
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this conflict checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * if the result is false, a counterexample can be queried using the
   * {@link #getCounterExample()} method.
   * Presently, this is a dummy implementation that does nothing but always
   * returns <CODE>true</CODE>.
   * @return <CODE>true</CODE> if the model is nonconflicting, or
   *         <CODE>false</CODE> if it is not.
   */
  @Override
  public boolean run()
  {
    // The following code determines and prints the number of marked
    // states in the model (before synchronous composition).
    // This is not very helpful for a conflict check, but it demonstrates
    // the use of the interfaces.

    int count = 0;

    // First get the model
    final ProductDESProxy model = getModel();

    // For each automaton ...
    for (final AutomatonProxy aut : model.getAutomata()) {
      // Is the marking proposition in the alphabet?
      final Collection<EventProxy> events = aut.getEvents();
      if (events.contains(mMarking)) {
        // Yes, this automaton has explicit markings.
        // For each state ...
        for (final StateProxy state : aut.getStates()) {
          // Does the list of propositions contain the marking proposition?
          final Collection<EventProxy> props = state.getPropositions();
          if (props.contains(mMarking)) {
            // Yes --- this is a marked state.
            count++;
          }
        }
      } else {
        // Marking proposition not in alphabet: all states marked implicitly.
        count += aut.getStates().size();
      }
    }
    // Print the number of marked states that we have counted.
    System.out.print(count + " ");

    // Try to compute a counterexample ...
    // This is not yet implemented and should only be done of the model is
    // conflicting, but never mind ...
    mCounterExample = computeCounterExample();

    // This all was no good as far as conflict checking was concerned.
    // Let us just leave.
    return true;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets a counterexample if the model was found to be conflicting,
   * representing a conflict error trace. A conflict error trace is a
   * sequence of events that takes the model to a state that is not
   * coreachable. That is, after executing the counterexample, the automata
   * are in a state from where it is no longer possible to reach a state
   * where all automata are marked at the same time.
   * @return A conflict trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this conflict checker and shares its automata and
   *         event objects.
   */
  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    // Just return a stored counterexample. This is the recommended way
    // of doing this, because we may no longer be able to use the
    // data structures used by the algorithm once the run() method has
    // finished. The counterexample can be computed by a method similar to
    // computeCounterExample() below or otherwise.
    return mCounterExample;
  }

  /**
   * Computes a counterexample.
   * This method is to be called from {@link #run()} after the model was
   * found to be conflicting, and while any data structures from the
   * conflict check that may be needed to compute the counterexample are
   * still available.
   * @return The computed counterexample.
   */
  private ConflictCounterExampleProxy computeCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT COUNTEREXAMPLE!

    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy model = getModel();
    final String modelName = model.getName();
    final String traceName = modelName + "-conflicting";
    final Collection<EventProxy> events = model.getEvents();
    final List<EventProxy> eventList = new LinkedList<>();
    for (final EventProxy event : events) {
      eventList.add(event);
    }
    // Note. The conflict kind field of the trace is optional for
    // this assignment---it will not be tested.
    return desFactory.createConflictCounterExampleProxy
      (traceName, model, eventList, ConflictKind.CONFLICT);
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Searches the given model for a proposition event with the default
   * marking name and returns this event.
   * @throws IllegalArgumentException to indicate that the given model
   *         does not contain any proposition with the default marking
   *         name.
   */
  private static EventProxy getDefaultMarkingProposition
    (final ProductDESProxy model)
  {
    for (final EventProxy event : model.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION &&
          event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        return event;
      }
    }
    throw new IllegalArgumentException
      ("ProductDESProxy '" + model.getName() +
       "' does not contain any proposition called '" +
       EventDeclProxy.DEFAULT_MARKING_NAME + "'!");
  }


  //#########################################################################
  //# Data Members
  /**
   * The proposition chosen by the user to identify the marked states
   * for conflict checking.
   */
  private final EventProxy mMarking;
  /**
   * The computed counterexample or null if the model is nonblocking.
   */
  private ConflictCounterExampleProxy mCounterExample;

}

//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A dummy implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is controllable.</P>
 *
 * <P>You are welcome to edit this file as much as you like,
 * but please <STRONG>do not change</STRONG> the public interface.
 * Do not change the signature of the constructor,
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

public class ControllabilityChecker extends ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new controllability checker to check a particular model.
   * @param  model      The model to be checked by this controllability
   *                    checker.
   * @param  desFactory Factory used for trace construction.
   */
  public ControllabilityChecker(final ProductDESProxy model,
                                final ProductDESProxyFactory desFactory)
  {
    super(model, desFactory);
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this controllability checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * if the result is false, a counterexample can be queried using the
   * {@link #getCounterExample()} method.
   * Presently, this is a dummy implementation that does nothing but always
   * returns <CODE>true</CODE>.
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   */
  @Override
  public boolean run()
  {
    // The following code determines and prints the number of uncontrollable
    // events in each finite-state machine in the model.
    // This is not very helpful for a controllability check,
    // but it demonstrates the use of the interfaces.

    // Start new line (for tidier output with ControllabilityMain)
    System.out.println();

    // First get the model
    final ProductDESProxy model = getModel();

    // For each automaton ...
    for (final AutomatonProxy aut : model.getAutomata()) {

      // Is it a plant or specification?
      final String plantOrSpec;
      switch (aut.getKind()) {
      case PLANT:
        plantOrSpec = "Plant";
        break;
      case SPEC:
        plantOrSpec = "Spec";
        break;
      default:
        // Don't bother if it is something else
        continue;
      }

      // Reset counter for uncontrollable events
      int count = 0;
      // For each event in the automaton alphabet ...
      for (final EventProxy event : aut.getEvents()) {
        // count if uncontrollable
        if (event.getKind() == EventKind.UNCONTROLLABLE) {
          count++;
        }
      }

      // Print what we have found
      System.out.println(plantOrSpec + " " + aut.getName() + " has " +
                         count + " uncontrollable events.");
    }

    // Try to compute a counterexample ...
    // This is not yet implemented and should only be done of the model is
    // not controllable, but never mind ...
    mCounterExample = computeCounterExample();

    // This all was no good as far as controllability checking is concerned.
    // Let us just leave.
    return true;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets a counterexample if the model was found to be not controllable.
   * representing a controllability error trace. A controllability error
   * trace is a nonempty sequence of events such that all except the last
   * event in the list can be executed by the model. The last event in the list
   * is an uncontrollable event that is possible in all plant automata, but
   * not in all specification automata present in the model. Thus, the last
   * step demonstrates why the model is not controllable.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this controllability checker and shares its automata and
   *         event objects.
   */
  @Override
  public SafetyCounterExampleProxy getCounterExample()
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
   * found to be not controllable, and while any data structures from
   * the controllability check that may be needed to compute the
   * counterexample are still available.
   * @return The computed counterexample.
   */
  private SafetyCounterExampleProxy computeCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT TRACE!

    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy des = getModel();
    final String desName = des.getName();
    final String traceName = desName + ":uncontrollable";
    final Collection<EventProxy> events = des.getEvents();
    final List<EventProxy> traceList = new LinkedList<>();
    for (final EventProxy event : events) {
      traceList.add(event);
    }
    return
      desFactory.createSafetyCounterExampleProxy(traceName, des, traceList);
  }


  //#########################################################################
  //# Data Members
  /**
   * The computed counterexample or null if the model is controllable.
   */
  private SafetyCounterExampleProxy mCounterExample;

}

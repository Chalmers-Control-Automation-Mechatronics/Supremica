//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * <P>A dummy implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is controllable.</P>
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
   * the result of checking the property is known and can be queried
   * using the {@link #getResult()} and {@link #getCounterExample()}
   * methods.
   * Presently, this is a dummy implementation that does nothing but always
   * returns <CODE>true</CODE>.
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   *         The same value can be queried using the {@link #getResult()}
   *         method.
   */
  public boolean run()
  {
    // Let us try to open a BDD factory. The "java" BDD factory is much
    // easier to debug than the faster "buddy" factory, but let us try
    // "buddy" first to see whether the native library can be loaded. 
    // Another fast alternative is "cudd".
    final BDDFactory bddFactory = BDDFactory.init("buddy", 10000, 5000);

    try {

      // The following code demonstrates how to create some BDDs.
      // It has nothing to do with controllability checking.

      // Allocate five Boolean variables.
      bddFactory.setVarNum(5);

      // Get the first and second variable.
      final BDD x0 = bddFactory.ithVar(0);
      final BDD x1 = bddFactory.ithVar(1);

      // Calculate their logical AND ...
      @SuppressWarnings("unused")
      final BDD and01 = x0.and(x1);

      // The BDD factory has its own memory management and garbage
      // collection. If we do not want to use a BDD any longer, we
      // should inform the factory that it can now be reclaimed.
      x0.free();
      x1.free();
      // It causes an exception if we use x0 or x1 again from now on.

      // Since this is going to be done very often, there is a shorter
      // alternative. The andWith() (and similar methods) automatically
      // consume the two input BDDs.
      final BDD x2 = bddFactory.ithVar(2);
      final BDD x3 = bddFactory.ithVar(3);
      x2.andWith(x3);
      // Now variable x2 contains the conjunction x2 && x3.
      // x3 has been freed and must not be accessed again.

      // This all was no good at all as far as controllability of
      // the input model is concerned. Let us just leave ...
      return true;

    } finally {

      // Before we leave, we _must_ close the BDD factory, otherwise
      // it cannot be used a second time. This is done in a finally
      // block, to make sure it happens even in case of errors.
      bddFactory.done();

    }      
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the result of controllability checking.
   * @return <CODE>true</CODE> if the model was found to be controllable,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException in all cases, because this method is
   *         not yet implemented.
   */
  public boolean getResult()
  {
    throw new IllegalStateException
      ("Controllability checking not yet implemented!");
  }

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
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public SafetyTraceProxy getCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT TRACE!

    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + ":uncontrollable";
    final Collection<EventProxy> events = des.getEvents();
    final List<EventProxy> tracelist = new LinkedList<EventProxy>();
    for (final EventProxy event : events) {
      tracelist.add(event);
    }
    final SafetyTraceProxy trace =
      desFactory.createSafetyTraceProxy(tracename, des, tracelist);
    return trace;
  }

}

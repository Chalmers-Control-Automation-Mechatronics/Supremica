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

import java.util.LinkedList;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * <P>A dummy implementation of a conflict checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is nonconflicting.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class BDDConflictChecker extends ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   * @param  model      The model to be checked by this conflict checker.
   * @param  desFactory Factory used for trace construction.
   */
  public BDDConflictChecker(final ProductDESProxy model,
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
  public BDDConflictChecker(final ProductDESProxy model,
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
    // Let us try to open a BDD factory. The "java" BDD factory is much
    // easier to debug than the faster "buddy" factory, but let us try
    // "buddy" first to see whether the native library can be loaded.
    // Another (faster?) alternative is "cudd".
    mBDDFactory = BDDFactory.init("buddy", 10000, 5000);
    // You can try to increase performance by increasing the cache sizes,
    // but please be aware of the memory limits.

    // Uncomment the following try-catch block to disable disconcerting
    // debug output. This also requires two imports:
    // import java.lang.reflect.Method;
    // import net.sourceforge.waters.model.base.WatersRuntimeException;
    /*
    try {
      final Class<?>[] parameterTypes =
        new Class<?>[] {Object.class, Object.class};
      final Method method =
        getClass().getMethod("silentBDDHandler", parameterTypes);
      mBDDFactory.registerGCCallback(this, method);
      mBDDFactory.registerReorderCallback(this, method);
      mBDDFactory.registerResizeCallback(this, method);
    } catch (final SecurityException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final NoSuchMethodException exception) {
      throw new WatersRuntimeException(exception);
    }
    */

    // Comment out the following try-catch block if you want dynamic variable
    // reordering (not supported by all BDD packages).
    try {
      mBDDFactory.disableReorder();
    } catch (final UnsupportedOperationException exception) {
      // No auto reorder? --- Never mind!
    }

    try {

      // The following code demonstrates how to create some BDDs.
      // It has nothing to do with conflict checking.

      // Allocate five Boolean variables ...
      mBDDFactory.setVarNum(5);

      // Get the first and second variable ...
      final BDD x0 = mBDDFactory.ithVar(0);
      final BDD x1 = mBDDFactory.ithVar(1);

      // Calculate their logical AND ...
      final BDD and01 = x0.and(x1);

      // The BDD factory has its own memory management and garbage
      // collection. If we do not want to use a BDD any longer, we
      // should inform the factory that it can now be reclaimed.
      x0.free();
      x1.free();
      // It causes an exception if we use x0 or x1 again from now on.
      and01.free();

      // Since such operations are used very often, there is a shorter
      // alternative. The andWith() (and similar methods) automatically
      // consume the two input BDDs.
      final BDD x2 = mBDDFactory.ithVar(2);
      final BDD x3 = mBDDFactory.ithVar(3);
      x2.andWith(x3);
      // Now variable x2 contains the conjunction x2 && x3.
      // The old value of x2 and x3 have been freed and must not be accessed
      // again.
      x2.free();

      // This all was no good at all as far as nonblocking of the input model
      // is concerned. Let us try something more serious ...

      // First get the model.
      final ProductDESProxy model = getModel();
      // Create an encoding for the events and automata.
      mBDDEncoding = new BDDEncoding(mBDDFactory, model);
      // Get the initial states BDD ...
      final BDD init = mBDDEncoding.computeInitialStatesBDD();
      // ... and the marked states BDD.
      final EventProxy marking = getConfiguredMarkingProposition();
      final BDD marked = mBDDEncoding.computeMarkedStatesBDD(marking);
      // Which states are initial and not marked?
      final BDD notMarked = marked.not();
      marked.free();
      final BDD initNotMarked = init.andWith(notMarked);
      // Use this BDD for something (well, sort of) ...
      initNotMarked.free();

      // Try to compute a counterexample ...
      // This is not yet implemented and should only be done of the model is
      // conflicting, but never mind ...
      mCounterExample = computeCounterExample();

      // Still no real progress towards conflict checking.
      // Let us just leave ...
      return true;

    } finally {

      // Before we leave, we _must_ close the BDD factory, otherwise
      // it cannot be used a second time. This is done in a finally
      // block, to make sure it happens even in case of errors.
      mBDDFactory.done();

    }
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
    // of doing this, because we can no longer use the BDD factory after
    // the run() method has finished. The counterexample can be computed
    // by a method similar to computeCounterExample() below or otherwise.
    return mCounterExample;
  }


  /**
   * Computes a counterexample.
   * This method is to be called from {@link #run()} after the model was
   * found to be conflicting, before the BDD factory has been closed. It
   * uses BDD operations to build the counterexample.
   * @return The computed counterexample.
   */
  private ConflictCounterExampleProxy computeCounterExample()
  {
    // The following creates a trace that consists of the first three events
    // in the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT TRACE!
    final ProductDESProxy model = getModel();
    final String modelName = model.getName();
    final String traceName = modelName + "-conflicting";
    final List<EventProxy> traceList = new LinkedList<EventProxy>();
    for (int e = 0; e < 3; e++) {
      if (e < mBDDEncoding.getNumberOfProperEvents()) {
        // Get event from encoding.
        final EventProxy event = mBDDEncoding.getEvent(e);
        // Construct the BDD for this event.
        final BDD eventBDD = mBDDEncoding.computeEventBDD(event);
        // Decode the BDD back to an event.
        final EventProxy bddEvent = mBDDEncoding.findEvent(eventBDD);
        // This should give back the event.
        assert event == bddEvent : "Unexpected event found";
        eventBDD.free();
        traceList.add(event);
      }
    }
    // Note. The conflict kind field of the trace is optional for
    // this assignment---it will not be tested.
    final ProductDESProxyFactory desFactory = getFactory();
    return desFactory.createConflictCounterExampleProxy
      (traceName, model, traceList, ConflictKind.CONFLICT);
  }


  //#########################################################################
  //# Debug Output
  /**
   * A dummy callback. Used to suppress debug output of BDD packages.
   */
  public void silentBDDHandler(final Object dummy1, final Object dummy2)
  {
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets the marking proposition configured for the current conflict check.
   * This method checks if a marking proposition has been set by the user,
   * and if so, returns it. Otherwise the model's default marking proposition
   * is found and returned.
   */
  private EventProxy getConfiguredMarkingProposition()
  {
    if (mMarking == null) {
      final ProductDESProxy model = getModel();
      return getDefaultMarkingProposition(model);
    } else {
      return mMarking;
    }
  }

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
   * The proposition event that identifies states as marked states for the
   * conflict check.
   */
  private final EventProxy mMarking;
  /**
   * The BDD factory to be used. Stored in this instance variable so that
   * all methods have access to it.
   */
  private BDDFactory mBDDFactory;
  /**
   * The BDD encoding constructed for the current model. Stored in this
   * instance variable so that in can be used from all methods without the
   * expensive re-computation.
   */
  private BDDEncoding mBDDEncoding;
  /**
   * The computed counterexample or null if the model is nonblocking.
   */
  private ConflictCounterExampleProxy mCounterExample;

}

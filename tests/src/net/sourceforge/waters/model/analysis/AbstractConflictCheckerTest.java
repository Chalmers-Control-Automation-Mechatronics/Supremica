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

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.analysis.monolithic.MonolithicLanguageInclusionChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


public abstract class AbstractConflictCheckerTest extends
    AbstractModelVerifierTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public AbstractConflictCheckerTest()
  {
  }

  public AbstractConflictCheckerTest(final String name)
  {
    super(name);
  }

  // #########################################################################
  // # Simple Access
  @Override
  protected ConflictChecker getModelVerifier()
  {
    return (ConflictChecker) super.getModelVerifier();
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {

    final Collection<String> empty = Collections.emptyList();
    compiler.setEnabledPropertyNames(empty);
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.AbstractModelVerifierTest
  /**
   * Checks the correctness of a conflict counterexample. A conflict
   * counterexample has to be a {@link ConflictCounterExampleProxy}, its event
   * sequence has to be accepted by all automata in the given model, and it
   * must take the model to a blocking state. The latter condition is checked
   * by means of a language inclusion check. Furthermore, when a state has a
   * nondeterministic choice it is verified whether the counter example
   * includes correct state information.
   *
   * @see AbstractModelVerifierTest#checkCounterExample(ProductDESProxy,CounterExampleProxy)
   * @see #createLanguageInclusionChecker(ProductDESProxy,ProductDESProxyFactory)
   */
  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws Exception
  {
    super.checkCounterExample(des, counter);
    final ConflictCounterExampleProxy castTest =
      (ConflictCounterExampleProxy) counter;
    final TraceProxy trace = castTest.getTrace();
    assertTrue("Conflict counterexample trace includes a loop!",
               trace.getLoopIndex() < 0);

    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int size = automata.size();
    final Map<AutomatonProxy,StateProxy> tuple =
        new HashMap<AutomatonProxy,StateProxy>(size);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = checkTrace(aut, trace);
      tuple.put(aut, state);
    }
    final ProductDESProxy ldes = createLanguageInclusionModel(des, tuple);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final LanguageInclusionChecker lchecker =
        createLanguageInclusionChecker(ldes, factory);
    final boolean blocking = lchecker.run();
    if (!blocking) {
      final CounterExampleProxy ltrace = lchecker.getCounterExample();
      final File filename = saveCounterExample(ltrace);
      fail("Counterexample does not lead to blocking state (trace written to" +
           filename + ")!");
    }
  }

  //#########################################################################
  //# May be Overridden by Subclasses
  /**
   * <P>
   * Creates a language inclusion checker for counterexample verification.
   * </P>
   * <P>
   * To check whether a counterexample indeed leads to a blocking state, a
   * language inclusion check is performed to determine whether a marked state
   * is reachable from the end state of the counterexample.
   * </P>
   * <P>
   * Depending on the size of the model, this language inclusion check may be a
   * difficult problem on its own. This default implementation returns a
   * {@link NativeLanguageInclusionChecker} if available, otherwise resorts to a
   * {@link MonolithicLanguageInclusionChecker}. This should be enough for the
   * test cases contained in this class. Subclasses that involve more advanced
   * conflict checkers with larger tests may have to override this method.
   * </P>
   *
   * @param des
   *          A language inclusion model to be verified.
   * @param factory
   *          The factory to be used for trace construction.
   * @return A language inclusion checker to verify the given model.
   */
  protected LanguageInclusionChecker createLanguageInclusionChecker(
                                                                    final ProductDESProxy des,
                                                                    final ProductDESProxyFactory factory)
  {
    if (mLanguageInclusionChecker == null) {
      try {
        mLanguageInclusionChecker =
            new NativeLanguageInclusionChecker(des, factory);
      } catch (final NoClassDefFoundError exception) {
        mLanguageInclusionChecker =
            new MonolithicLanguageInclusionChecker(des, factory);
      } catch (final UnsatisfiedLinkError exception) {
        mLanguageInclusionChecker =
            new MonolithicLanguageInclusionChecker(des, factory);
      }
    }
    mLanguageInclusionChecker.setModel(des);
    return mLanguageInclusionChecker;
  }

  //#########################################################################
  //# Coreachability Model
  protected ProductDESProxy createLanguageInclusionModel
    (final ProductDESProxy des, final Map<AutomatonProxy,StateProxy> inittuple)
  {
    return createLanguageInclusionModel(des, inittuple, null);
  }

  protected ProductDESProxy createLanguageInclusionModel
    (final ProductDESProxy des,
     final Map<AutomatonProxy,StateProxy> inittuple,
     final Collection<EventProxy> disabledEvents)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final Collection<EventProxy> oldevents = des.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
        new ArrayList<EventProxy>(numevents);
    EventProxy oldmarking = null;
    EventProxy newmarking = null;
    for (final EventProxy oldevent : oldevents) {
      if (oldevent.getKind() == EventKind.PROPOSITION) {
        final String eventname = oldevent.getName();
        if (eventname.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          oldmarking = oldevent;
          newmarking =
              factory.createEventProxy(eventname, EventKind.UNCONTROLLABLE);
          newevents.add(newmarking);
        }
      } else {
        newevents.add(oldevent);
      }
    }
    if (oldmarking == null) {
      throw new IllegalArgumentException
        ("Default marking proposition not found in model!");
    }
    final Collection<AutomatonProxy> oldautomata = des.getAutomata();
    final int numaut = oldautomata.size();
    final Collection<AutomatonProxy> newautomata =
        new ArrayList<AutomatonProxy>(numaut + 1);
    for (final AutomatonProxy oldaut : oldautomata) {
      final StateProxy init = inittuple.get(oldaut);
      final AutomatonProxy newaut =
        createLanguageInclusionAutomaton(oldaut, init, oldmarking, newmarking);
      newautomata.add(newaut);
    }
    if (disabledEvents != null) {
      final AutomatonProxy disable = createDisablingAutomaton
        (":disable", ComponentKind.PLANT, disabledEvents);
      newautomata.add(disable);
    }
    final AutomatonProxy prop = createPropertyAutomaton(newmarking);
    newautomata.add(prop);
    final String name = des.getName() + "-coreachability";
    return factory.createProductDESProxy(name, newevents, newautomata);
  }

  protected AutomatonProxy createLanguageInclusionAutomaton
    (final AutomatonProxy aut,
     final StateProxy newinit,
     final EventProxy oldmarking,
     final EventProxy newmarking)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final Collection<EventProxy> oldevents = aut.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
        new ArrayList<EventProxy>(numevents);
    for (final EventProxy oldevent : oldevents) {
      if (oldevent == oldmarking) {
        newevents.add(newmarking);
      } else if (oldevent.getKind() != EventKind.PROPOSITION) {
        newevents.add(oldevent);
      }
    }
    final Collection<StateProxy> oldstates = aut.getStates();
    final int numstates = oldstates.size();
    final Collection<StateProxy> newstates =
        new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> statemap =
        new HashMap<StateProxy,StateProxy>(numstates);
    final Collection<TransitionProxy> oldtransitions = aut.getTransitions();
    final int numtrans = oldtransitions.size();
    final Collection<TransitionProxy> newtransitions =
        new ArrayList<TransitionProxy>(numstates + numtrans);
    for (final StateProxy oldstate : oldstates) {
      final String statename = oldstate.getName();
      final StateProxy newstate =
          factory.createStateProxy(statename, oldstate == newinit, null);
      newstates.add(newstate);
      statemap.put(oldstate, newstate);
      if (oldstate.getPropositions().contains(oldmarking)) {
        final TransitionProxy trans =
            factory.createTransitionProxy(newstate, newmarking, newstate);
        newtransitions.add(trans);
      }
    }
    for (final TransitionProxy oldtrans : oldtransitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final StateProxy newsource = statemap.get(oldsource);
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateProxy newtarget = statemap.get(oldtarget);
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
          factory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    return factory.createAutomatonProxy(autname, kind, newevents, newstates,
                                        newtransitions);
  }

  protected AutomatonProxy createPropertyAutomaton(final EventProxy newmarking)
  {
    final String name = ":never:" + newmarking.getName();
    final Collection<EventProxy> events = Collections.singletonList(newmarking);
    return createDisablingAutomaton(name, ComponentKind.PROPERTY, events);
  }

  protected AutomatonProxy createDisablingAutomaton
    (final String name,
     final ComponentKind kind,
     final Collection<EventProxy> events)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final StateProxy state = factory.createStateProxy("s0", true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return factory.createAutomatonProxy(name, kind, events, states, null);
  }


  //#########################################################################
  //# Data Members
  private LanguageInclusionChecker mLanguageInclusionChecker;

}

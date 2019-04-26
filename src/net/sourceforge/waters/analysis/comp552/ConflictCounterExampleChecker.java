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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A tool to check whether a conflict counterexample is a correct
 * counterexample to show that a given product DES is blocking.</P>
 *
 * <P>To use this class, it must be initialised with a
 * {@link ProductDESProxyFactory}. Afterwards, {@link
 * #checkCounterExample(ProductDESProxy,CounterExampleProxy)
 * checkCounterExample()} can be called repeatedly. If a check fails,
 * {@link #getDiagnostics()} can be called to retrieve an explanation.</P>
 *
 * <P>
 * <CODE>{@link ProductDESProxyFactory} factory =
 *   {@link net.sourceforge.waters.plain.des.ProductDESElementFactory}.{@link
 *   net.sourceforge.waters.plain.des.ProductDESElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>ConflictCounterExampleChecker checker =
 *   new {@link #ConflictCounterExampleChecker(ProductDESProxyFactory)
 *   ConflictCounterExampleChecker}(factory);</CODE><BR>
 * <CODE>if (checker.{@link #checkCounterExample(ProductDESProxy,CounterExampleProxy)
 *   checkCounterExample}(</CODE><I>des</I><CODE>, </CODE><I>trace</I><CODE>))
 *   {</CODE><BR>
 * <CODE>&nbsp;&nbsp;System.out.println(&quot;OK&quot;);</CODE><BR>
 * <CODE>} else {</CODE><BR>
 * <CODE>&nbsp;&nbsp;String msg = checker.{@link #getDiagnostics()
 *   getDiagnostics}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;System.out.println(msg);</CODE><BR>
 * <CODE>}</CODE>
 * </P>
 *
 * <P>To verify a counterexample, the counterexample checker first determines
 * whether all components in a given model accept all events in the
 * counterexample. If this is the case, it furthermore checks whether the
 * counterexample's end state is indeed blocking. This is done using an
 * language inclusion check, which explicitly enumerates the states of the
 * synchronous product. This may run for several seconds or even minutes,
 * and depending on the amount of memory available, it will only be able
 * to handle models with 50-100 million reachable states.</P>
 *
 * @author Robi Malik
 */

public class ConflictCounterExampleChecker
  extends AbstractCounterExampleChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict counterexample checker.
   * @param  factory   Factory to be used to construct the language inclusion
   *                   model and secondary counterexample if needed.
   */
  public ConflictCounterExampleChecker(final ProductDESProxyFactory factory)
  {
    this(factory, true);
  }

  /**
   * Creates a new counterexample checker.
   * Creates a new conflict counterexample checker.
   * @param  factory   Factory to be used to construct the language inclusion
   *                   model and secondary counterexample if needed.
   * @param  fullDiag  Whether the diagnostic text should include the
   *                   name of the trace. The default is <CODE>true</CODE>.
   */
  public ConflictCounterExampleChecker(final ProductDESProxyFactory factory,
                                       final boolean fullDiag)
  {
    super(fullDiag);
    mFactory = factory;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks a conflict error trace.
   * @param  des       The product DES that was verified.
   * @param  counter   The counterexample to be checked.
   * @return <CODE>true</CODE> if the given counterexample demonstrates that
   *         the given product DES is blocking, <CODE>false</CODE> otherwise.
   * @throws AnalysisException to indicate a problem while attempting to
   *                   verify the counterexample.
   */
  @Override
  public boolean checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws AnalysisException
  {
    if (!super.checkCounterExample(des, counter)) {
      return false;
    } else if (!(counter instanceof ConflictCounterExampleProxy)) {
      reportMalformedCounterExample
        (counter, "is not a ConflictCounterExampleProxy", null);
      return false;
    }
    final ConflictCounterExampleProxy confCounter =
      (ConflictCounterExampleProxy) counter;
    final TraceProxy trace = confCounter.getTrace();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int size = automata.size();
    final Map<AutomatonProxy,StateProxy> tuple =
      new HashMap<AutomatonProxy,StateProxy>(size);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = checkCounterExample(aut, trace);
      if (state == null) {
        reportMalformedCounterExample
          (confCounter, "is not accepted by component", aut);
        return false;
      }
      tuple.put(aut, state);
    }
    final ProductDESProxy lDES = createLanguageInclusionModel(des, tuple);
    final LanguageInclusionChecker checker =
      new NativeLanguageInclusionChecker(lDES, mFactory);
    final boolean blocking = checker.run();
    if (!blocking) {
      final SafetyCounterExampleProxy unsafe = checker.getCounterExample();
      reportMalformedCounterExample(confCounter, unsafe);
    } else {
      reportCorrectCounterExample();
    }
    return blocking;
  }


  //#########################################################################
  //# Hooks
  @Override
  String getTraceLabel()
  {
    return "Conflict";
  }


  //#########################################################################
  //# Auxiliary Methods
  private StateProxy checkCounterExample(final AutomatonProxy aut,
                                         final TraceProxy trace)
  {
    final Collection<EventProxy> events = aut.getEvents();
    StateProxy current = AutomatonTools.getFirstInitialState(aut);
    if (current == null) {
      return null;
    }
    final List<EventProxy> traceevents = trace.getEvents();
    for (final EventProxy event : traceevents) {
      if (events.contains(event) && event.getKind() != EventKind.PROPOSITION) {
        current = AutomatonTools.getFirstSuccessorState(aut, current, event);
        if (current == null) {
          return null;
        }
      }
    }
    return current;
  }


  //#########################################################################
  //# Coreachability Model
  private ProductDESProxy createLanguageInclusionModel
    (final ProductDESProxy des, final Map<AutomatonProxy,StateProxy> inittuple)
  {
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
            mFactory.createEventProxy(eventname, EventKind.UNCONTROLLABLE);
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
    final AutomatonProxy prop = createPropertyAutomaton(newmarking);
    newautomata.add(prop);
    final String name = des.getName() + ":coreachability";
    return mFactory.createProductDESProxy(name, newevents, newautomata);
  }

  private AutomatonProxy createLanguageInclusionAutomaton
    (final AutomatonProxy aut,
     final StateProxy newinit,
     final EventProxy oldmarking,
     final EventProxy newmarking)
  {
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
        mFactory.createStateProxy(statename, oldstate == newinit, null);
      newstates.add(newstate);
      statemap.put(oldstate, newstate);
      if (oldstate.getPropositions().contains(oldmarking)) {
        final TransitionProxy trans =
          mFactory.createTransitionProxy(newstate, newmarking, newstate);
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
        mFactory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    return mFactory.createAutomatonProxy
      (autname, kind, newevents, newstates, newtransitions, null);
  }

  private AutomatonProxy createPropertyAutomaton(final EventProxy newmarking)
  {
    final String name = ":never:" + newmarking.getName();
    final Collection<EventProxy> events =
      Collections.singletonList(newmarking);
    final StateProxy state = mFactory.createStateProxy("s0", true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return mFactory.createAutomatonProxy
      (name, ComponentKind.PROPERTY, events, states, null, null);
  }

  private void reportMalformedCounterExample
    (final ConflictCounterExampleProxy confCounter,
     final SafetyCounterExampleProxy langCounter)
  {
    reportMalformedCounterExample
      (confCounter, "does not lead to blocking state", null);
    reportCounterCounterExample
      ("A marked state can be reached as follows:", langCounter);
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

}

//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractConflictCheckerTest
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


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
  protected ConflictChecker getModelVerifier()
  {
    return (ConflictChecker) super.getModelVerifier();
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.AbstractAnalysisTest
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
   * counterexample has to be a {@link ConflictTraceProxy}, its event sequence
   * has to be accepted by all automata in the given model, and it must take the
   * model to a blocking state. The latter condition is checked by means of a
   * language inclusion check.
   *
   * @see AbstractModelVerifierTest#checkCounterExample(ProductDESProxy,TraceProxy)
   * @see #createLanguageInclusionChecker(ProductDESProxy,ProductDESProxyFactory)
   */
  protected void checkCounterExample(final ProductDESProxy des,
      final TraceProxy trace) throws Exception
  {
    super.checkCounterExample(des, trace);
    final ConflictTraceProxy counterexample = (ConflictTraceProxy) trace;
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int size = automata.size();
    final Map<AutomatonProxy,StateProxy> tuple =
        new HashMap<AutomatonProxy,StateProxy>(size);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = checkCounterExample(aut, counterexample);
      assertNotNull("Counterexample not accepted by automaton " + aut.getName()
          + "!", state);
      tuple.put(aut, state);
    }
    final ProductDESProxy ldes = createLanguageInclusionModel(des, tuple);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final LanguageInclusionChecker lchecker =
        createLanguageInclusionChecker(ldes, factory);
    final boolean blocking = lchecker.run();
    if (!blocking) {
      final TraceProxy ltrace = lchecker.getCounterExample();
      final File filename = saveCounterExample(ltrace);
      fail("Counterexample does not lead to blocking state (trace written to"
          + filename + ")!");
    }
  }

  // #########################################################################
  // # May be Overridden by Subclasses
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
   * {@link MonolithicLanguageInclusionChecker.} This should be enough for the
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
      final ProductDESProxy des, final ProductDESProxyFactory factory)
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

  // #########################################################################
  // # Auxiliary Methods
  protected StateProxy checkCounterExample(final AutomatonProxy aut,
      final ConflictTraceProxy trace)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    StateProxy current = null;
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        current = state;
        break;
      }
    }
    if (current == null) {
      return null;
    }
    for (final EventProxy event : trace.getEvents()) {
      if (events.contains(event)) {
        boolean found = false;
        for (final TransitionProxy trans : transitions) {
          if (trans.getSource() == current && trans.getEvent() == event) {
            current = trans.getTarget();
            found = true;
            break;
          }
        }
        if (!found) {
          return null;
        }
      }
    }
    // returns the end state of the counterexample trace
    return current;
  }

  // #########################################################################
  // # Coreachability Model
  private ProductDESProxy createLanguageInclusionModel(
      final ProductDESProxy des, final Map<AutomatonProxy,StateProxy> inittuple)
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
      throw new IllegalArgumentException(
          "Default marking proposition not found in model!");
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
    return factory.createProductDESProxy(name, newevents, newautomata);
  }

  private AutomatonProxy createLanguageInclusionAutomaton(
      final AutomatonProxy aut, final StateProxy newinit,
      final EventProxy oldmarking, final EventProxy newmarking)
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

  private AutomatonProxy createPropertyAutomaton(final EventProxy newmarking)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final String name = ":never:" + newmarking.getName();
    final Collection<EventProxy> events = Collections.singletonList(newmarking);
    final StateProxy state = factory.createStateProxy("s0", true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return factory.createAutomatonProxy(name, ComponentKind.PROPERTY, events,
        states, null);
  }

  // #########################################################################
  // # Data Members
  private LanguageInclusionChecker mLanguageInclusionChecker;

}

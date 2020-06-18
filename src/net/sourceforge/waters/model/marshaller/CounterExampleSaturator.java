//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.marshaller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DefaultProductDESProxyVisitor;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>A tool to check whether a {@link CounterExampleProxy} is accepted by a
 * {@link ProductDESProxy}, and to add recover suppressed state information.</P>
 *
 * <P>The traces in a {@link CounterExampleProxy} defines a path through
 * the automata ({@link AutomatonProxy}) of a {@link ProductDESProxy},
 * which normally consists of a sequence of states and events. But for
 * the common case of deterministic automata, the state information can be
 * recovered from the automaton. Therefore the state information is optional
 * an disk space or computation effort can be saved by not including it.
 * On the other hand, it is convenient for many algorithms to have access
 * to full state information in a counterexample.</P>
 *
 * <P>The CounterExampleSaturator can <I>saturate</I> a counterexample to
 * ensure that state information is available at every step, or it can
 * <I>desaturate</I> it to remove all state information except where needed
 * to resolve nondeterminism.</P>
 *
 * <P>In addition, the CounterExampleSaturator can be used to check whether
 * a counterexample is accepted by a {@link ProductDESProxy}. The check
 * determines whether all automata can perform all the traces in the
 * counterexample (except for possible failure at the end of a {@link
 * SafetyCounterExampleProxy}). It is <I>not</I> a debugging tool to
 * determine whether a counterexample computed by verification algorithm
 * refutes the property that was verified&mdash;this check is performed by
 * class {@link TraceChecker}.
 *
 * @author Robi Malik
 */

public class CounterExampleSaturator
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a counterexample saturator.
   * @param  factory  Factory to create saturated or desaturated
   *                  counterexamples.
   */
  public CounterExampleSaturator(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks whether a counterexample is accepted by all automata listed in
   * the counterexample.
   * @param  cex  The counterexample to be checked.
   * @throws CounterExampleValidationException to indicate that the
   *              counterexample is not accepted by the product DES.
   */
  public void check(final CounterExampleProxy cex)
  {
    process(Mode.CHECK, cex);
  }

  /**
   * Saturates a counterexample. This method constructs a new counterexample
   * that has state information for all automata listed in the counterexample.
   * @param  cex  The counterexample to be saturated.
   * @return The saturated counterexample, or the argument <CODE>cex</CODE>
   *         if it is already saturated.
   * @throws CounterExampleValidationException to indicate that the
   *              counterexample is not accepted by the product DES.
   */
  public CounterExampleProxy saturate(final CounterExampleProxy cex)
  {
    return process(Mode.SATURATE, cex);
  }

  /**
   * Desaturates a counterexample. This method constructs a new counterexample
   * with minimal state information. State information is only included when
   * necessary to avoid ambiguity due to nondeterminism.
   * @param  cex  The counterexample to be desaturated.
   * @return The desaturated counterexample, or the argument <CODE>cex</CODE>
   *         if it is already desaturated.
   * @throws CounterExampleValidationException to indicate that the
   *              counterexample is not accepted by its product DES.
   */
  public CounterExampleProxy desaturate(final CounterExampleProxy cex)
  {
    return process(Mode.DESATURATE, cex);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CounterExampleProxy process(final Mode mode,
                                      final CounterExampleProxy cex)
  {
    mMode = mode;
    final Collection<AutomatonProxy> automata = cex.getAutomata();
    final List<TraceProxy> traces = cex.getTraces();
    final List<TraceProxy> newTraces =
      mode == Mode.CHECK ? null : new ArrayList<TraceProxy>(traces.size());
    boolean hasNewTrace = false;
    mCurrentTraceIndex = 0;
    for (final TraceProxy trace : traces) {
      if (traces.size() > 1) {
        mCurrentTrace = trace;
        mCurrentTraceIndex++;
      }
      final TraceProxy newTrace = process(trace, automata);
      if (newTraces != null) {
        newTraces.add(newTrace);
        hasNewTrace |= trace != newTrace;
      }
    }
    if (hasNewTrace) {
      final CounterExampleCopyVisitor visitor = new CounterExampleCopyVisitor();
      return visitor.createNewCounterExample(cex, newTraces);
    } else {
      return cex;
    }
  }

  private TraceProxy process(final TraceProxy trace,
                             final Collection<AutomatonProxy> automata)
  {
    final int numAutomata = automata.size();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final List<TraceStepProxy> newSteps =
      mMode == Mode.CHECK ? null : new ArrayList<>(steps.size());
    final int loopIndex = trace.getLoopIndex();
    final Iterator<TraceStepProxy> iter = steps.iterator();
    Map<AutomatonProxy,StateProxy> current = new HashMap<>(numAutomata);
    Map<AutomatonProxy,StateProxy> loopMap = null;
    int index = 0;
    boolean hasNewStep = false;
    TraceStepProxy step = iter.next();
    TraceStepProxy newStep = getInitialState(step, current, automata);
    if (newSteps != null) {
      newSteps.add(newStep);
      hasNewStep |= step != newStep;
    }
    if (loopIndex == 0) {
      loopMap = current;
    }
    while (iter.hasNext()) {
      step = iter.next();
      final Map<AutomatonProxy,StateProxy> succ = new HashMap<>(numAutomata);
      newStep = getSuccessorState(current, step, succ, automata);
      if (newSteps != null) {
        newSteps.add(newStep);
        hasNewStep |= step != newStep;
      }
      current = succ;
      if (loopIndex == ++index) {
        loopMap = current;
      }
    }
    if (loopMap != null) {
      for (final AutomatonProxy aut : automata) {
        final StateProxy loopState = loopMap.get(aut);
        final StateProxy currentState = current.get(aut);
        if (!ProxyTools.equals(loopState, currentState)) {
          final StringBuilder builder = prepareStringBuilder();
          if (currentState == null) {
            builder.append("ends with an undefined state");
          } else {
            builder.append("ends at state '");
            builder.append(currentState.getName());
            builder.append('\'');
          }
          builder.append(" of automaton '");
          builder.append(aut.getName());
          builder.append("', but this is not the same as at the loop entry " +
                         "point, where it is '");
          builder.append(loopState.getName());
          builder.append("'.");
          throw new CounterExampleValidationException(builder);
        }
      }
    } else if (loopIndex >= 0) {
      final StringBuilder builder = prepareStringBuilder();
      builder.append("mentions a loop index ");
      builder.append(loopIndex);
      builder.append(", which is beyond the end of the trace.");
      throw new CounterExampleValidationException(builder);
    }
    if (hasNewStep) {
      return mFactory.createTraceProxy(newSteps, loopIndex);
    } else {
      return trace;
    }
  }

  private TraceStepProxy getInitialState
    (final TraceStepProxy step,
     final Map<AutomatonProxy,StateProxy> current,
     final Collection<AutomatonProxy> automata)
  {
    final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
    for (final AutomatonProxy aut : automata) {
      StateProxy state = stepMap.get(aut);
      if (state != null) {
        if (!state.isInitial()) {
          final StringBuilder builder = prepareStringBuilder();
          builder.append("starts at state '");
          builder.append(state.getName());
          builder.append("' of automaton '");
          builder.append(aut.getName());
          builder.append("', which is not an initial state.");
          throw new CounterExampleValidationException(builder);
        } else {
          current.put(aut, state);
        }
      } else {
        for (final StateProxy altState : aut.getStates()) {
          if (altState.isInitial()) {
            if (state == null) {
              state = altState;
              current.put(aut, state);
            } else {
              final StringBuilder builder = prepareStringBuilder();
              builder.append("specifies no initial state for automaton '");
              builder.append(aut.getName());
              builder.append("', which has more than one initial state.");
              throw new CounterExampleValidationException(builder);
            }
          }
        }
      }
    }
    switch (mMode) {
    case SATURATE:
      if (current.size() > stepMap.size()) {
        return mFactory.createTraceStepProxy(null, current);
      }
      break;
    case DESATURATE:
      final Map<AutomatonProxy,StateProxy> desatMap =
        new HashMap<>(stepMap.size());
      for (final Map.Entry<AutomatonProxy,StateProxy> entry : stepMap.entrySet()) {
        final AutomatonProxy aut = entry.getKey();
        final StateProxy state = entry.getValue();
        for (final StateProxy altState : aut.getStates()) {
          if (altState.isInitial() && altState != state) {
            desatMap.put(aut, state);
            break;
          }
        }
      }
      if (desatMap.size() < stepMap.size()) {
        return mFactory.createTraceStepProxy(null, desatMap);
      }
      break;
    default:
      break;
    }
    return step;
  }

  private TraceStepProxy getSuccessorState
    (final Map<AutomatonProxy,StateProxy> current,
     final TraceStepProxy step,
     final Map<AutomatonProxy,StateProxy> succ,
     final Collection<AutomatonProxy> automata)
  {
    final EventProxy event = step.getEvent();
    final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
    for (final AutomatonProxy aut : automata) {
      final StateProxy source = current.get(aut);
      StateProxy target = stepMap.get(aut);
      if (source == null) {
        // skip
      } else if (aut.getEvents().contains(event)) {
        if (target != null) {
          boolean found = false;
          for (final TransitionProxy trans : aut.getTransitions()) {
            if (trans.getSource() == source && trans.getEvent() == event) {
              found |= trans.getTarget() == target;
            }
          }
          if (!found) {
            final StringBuilder builder = prepareStringBuilder();
            builder.append("specifies a transition from state '");
            builder.append(source.getName());
            builder.append("' to state '");
            builder.append(target.getName());
            builder.append("' on event '");
            builder.append(event.getName());
            builder.append("' in automaton '");
            builder.append(aut.getName());
            builder.append("', which has no such transition.");
            throw new CounterExampleValidationException(builder);
          } else {
            succ.put(aut, target);
          }
        } else {
          for (final TransitionProxy trans : aut.getTransitions()) {
            if (trans.getSource() == source && trans.getEvent() == event) {
              if (target == null) {
                target = trans.getTarget();
                succ.put(aut, target);
              } else {
                final StringBuilder builder = prepareStringBuilder();
                builder.append("specifies no transition from state '");
                builder.append(source.getName());
                builder.append("' on event '");
                builder.append(event.getName());
                builder.append("' in automaton '");
                builder.append(aut.getName());
                builder.append("', but the automaton has more than one " +
                               "such transition.");
                throw new CounterExampleValidationException(builder);
              }
            }
          }
        }
      } else if (source == target || target == null) {
        succ.put(aut, source);
      } else {
        final StringBuilder builder = prepareStringBuilder();
        builder.append("specifies a transition from state '");
        builder.append(source.getName());
        builder.append("' to state '");
        builder.append(target.getName());
        builder.append("' on event '");
        builder.append(event.getName());
        builder.append("' in automaton '");
        builder.append(aut.getName());
        builder.append("', but the event is not in the automaton alphabet.");
        throw new CounterExampleValidationException(builder);
      }
    }
    switch (mMode) {
    case SATURATE:
      if (succ.size() > stepMap.size()) {
        return mFactory.createTraceStepProxy(event, succ);
      }
      break;
    case DESATURATE:
      final Map<AutomatonProxy,StateProxy> desatMap =
      new HashMap<>(stepMap.size());
      for (final Map.Entry<AutomatonProxy,StateProxy> entry :
           stepMap.entrySet()) {
        final AutomatonProxy aut = entry.getKey();
        final StateProxy source = current.get(aut);
        if (aut.getEvents().contains(event) && source != null) {
          final StateProxy target = entry.getValue();
          for (final TransitionProxy trans : aut.getTransitions()) {
            if (trans.getSource() == source &&
                trans.getEvent() == event &&
                trans.getTarget() != target) {
              desatMap.put(aut, target);
              break;
            }
          }
        }
      }
      if (desatMap.size() < stepMap.size()) {
        return mFactory.createTraceStepProxy(event, desatMap);
      }
      break;
    default:
      break;
    }
    return step;
  }

  private StringBuilder prepareStringBuilder()
  {
    final StringBuilder builder = new StringBuilder();
    if (mCurrentTrace == null) {
      builder.append("The counterexample ");
    } else if (mCurrentTrace.getName().equals("")) {
      builder.append("Trace #");
      builder.append(mCurrentTraceIndex);
      builder.append(' ');
    } else {
      builder.append("Trace '");
      builder.append(mCurrentTrace.getName());
      builder.append("' ");
    }
    return builder;
  }


  //#########################################################################
  //# Inner Enumeration Mode
  private enum Mode {
    CHECK,
    SATURATE,
    DESATURATE
  }


  //#########################################################################
  //# Inner Class CounterExampleCopyVisitor
  private class CounterExampleCopyVisitor
    extends DefaultProductDESProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private CounterExampleProxy createNewCounterExample
      (final CounterExampleProxy cex, final List<TraceProxy> traces)
    {
      try {
        mTraces = traces;
        return (CounterExampleProxy) cex.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mTraces = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProductDESProxyVisitor
    @Override
    public Object visitCounterExampleProxy(final CounterExampleProxy cex)
      throws VisitorException
    {
      throw new VisitorException("Unknown counterexample type " +
                                 ProxyTools.getShortClassName(cex) + "!");
    }

    @Override
    public ConflictCounterExampleProxy visitConflictCounterExampleProxy
      (final ConflictCounterExampleProxy cex)
    {
      final String name = cex.getName();
      final String comment = cex.getComment();
      final ProductDESProxy des = cex.getProductDES();
      final Collection<AutomatonProxy> automata = cex.getAutomata();
      final TraceProxy trace = mTraces.get(0);
      final ConflictKind kind = cex.getKind();
      return mFactory.createConflictCounterExampleProxy
        (name, comment, null, des, automata, trace, kind);
    }

    @Override
    public DualCounterExampleProxy visitDualCounterExampleProxy
      (final DualCounterExampleProxy cex)
    {
      final String name = cex.getName();
      final String comment = cex.getComment();
      final ProductDESProxy des = cex.getProductDES();
      final Collection<AutomatonProxy> automata = cex.getAutomata();
      final TraceProxy trace0 = mTraces.get(0);
      final TraceProxy trace1 = mTraces.get(1);
      return mFactory.createDualCounterExampleProxy
        (name, comment, null, des, automata, trace0, trace1);
    }

    @Override
    public LoopCounterExampleProxy visitLoopCounterExampleProxy
      (final LoopCounterExampleProxy cex)
    {
      final String name = cex.getName();
      final String comment = cex.getComment();
      final ProductDESProxy des = cex.getProductDES();
      final Collection<AutomatonProxy> automata = cex.getAutomata();
      final TraceProxy trace = mTraces.get(0);
      return mFactory.createLoopCounterExampleProxy
        (name, comment, null, des, automata, trace);
    }

    @Override
    public SafetyCounterExampleProxy visitSafetyCounterExampleProxy
      (final SafetyCounterExampleProxy cex)
    {
      final String name = cex.getName();
      final String comment = cex.getComment();
      final ProductDESProxy des = cex.getProductDES();
      final Collection<AutomatonProxy> automata = cex.getAutomata();
      final TraceProxy trace = mTraces.get(0);
      return mFactory.createSafetyCounterExampleProxy
        (name, comment, null, des, automata, trace);
    }

    //#######################################################################
    //# Data Members
    private List<TraceProxy> mTraces;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private Mode mMode;
  private TraceProxy mCurrentTrace;
  private int mCurrentTraceIndex;

}

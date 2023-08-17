//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.des;

import gnu.trove.strategy.HashingStrategy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.AbstractEqualityVisitor;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;


public class ProductDESEqualityVisitor
  extends AbstractEqualityVisitor
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  public static ProductDESEqualityVisitor getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final ProductDESEqualityVisitor INSTANCE =
        new ProductDESEqualityVisitor();
  }


  //#########################################################################
  //# Constructors
  public ProductDESEqualityVisitor(final boolean diag)
  {
    super(diag, false);
  }

  private ProductDESEqualityVisitor()
  {
    this(false);
  }


  //#########################################################################
  //# Specific Access
  /**
   * Gets a GNU Trove hashing strategy for transitions in deterministic
   * automata. Unlike the standard {@link #getTObjectHashingStrategy()},
   * this method returns a strategy that considers transitions as equal if
   * they have the same source states and events, where states and events
   * are compared by object identity.
   */
  public static HashingStrategy<TransitionProxy>
    getDeterminsiticTransitionHashingStrategy()
  {
    return DeterministicTransitionHashingStrategy.getInstance();
  }

  /**
   * Gets a GNU Trove hashing strategy for transitions in nondeterministic
   * automata. Unlike the standard {@link #getTObjectHashingStrategy()},
   * this method returns a strategy that considers transitions as equal if
   * they have the same source and target states and events, where states
   * and events are compared by object identity.
   */
  public static HashingStrategy<TransitionProxy>
    getNonDeterminsiticTransitionHashingStrategy()
  {
    return NonDeterministicTransitionHashingStrategy.getInstance();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.base.AbstractEqualityVisitor
  @Override
  public ProductDESHashCodeVisitor getHashCodeVisitor()
  {
    return ProductDESHashCodeVisitor.getInstance();
  }

  @Override
  public ProductDESEqualityVisitor getNonReportingEqualityVisitor()
  {
    return getInstance();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public Boolean visitAutomatonProxy(final AutomatonProxy aut)
      throws VisitorException
  {
    if (visitNamedProxy(aut)) {
      final AutomatonProxy expected = (AutomatonProxy) getSecondProxy();
      final ComponentKind kind1 = aut.getKind();
      final ComponentKind kind2 = expected.getKind();
      if (kind1 != kind2) {
        return reportAttributeMismatch("kind", kind1, kind2);
      }
      final Set<EventProxy> events1 = aut.getEvents();
      final Set<EventProxy> events2 = expected.getEvents();
      if (!compareRefSets(events1, events2)) {
        return false;
      }
      final Set<StateProxy> states1 = aut.getStates();
      final Set<StateProxy> states2 = expected.getStates();
      if (!compareSets(states1, states2)) {
        return false;
      }
      final Collection<TransitionProxy> trans1 = aut.getTransitions();
      final Collection<TransitionProxy> trans2 = expected.getTransitions();
      if (!compareSets(trans1, trans2)) {
        return false;
      }
      final Map<String,String> attribs1 = aut.getAttributes();
      final Map<String,String> attribs2 = expected.getAttributes();
      if (!compareAttributeMaps(attribs1, attribs2)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Object visitConflictCounterExampleProxy
    (final ConflictCounterExampleProxy counter)
    throws VisitorException
  {
    if (visitCounterExampleProxy(counter)) {
      final ConflictCounterExampleProxy expected =
        (ConflictCounterExampleProxy) getSecondProxy();
      final ConflictKind kind1 = counter.getKind();
      final ConflictKind kind2 = expected.getKind();
      if (kind1 != kind2) {
        return reportAttributeMismatch("kind", kind1, kind2);
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean visitCounterExampleProxy(final CounterExampleProxy counter)
    throws VisitorException
  {
    if (visitDocumentProxy(counter)) {
      final CounterExampleProxy expected = (CounterExampleProxy) getSecondProxy();
      final ProductDESProxy des1 = counter.getProductDES();
      final ProductDESProxy des2 = expected.getProductDES();
      if (!des1.refequals(des2)) {
        return reportAttributeMismatch
            ("product DES", des1.getName(), des2.getName());
      }
      final Set<AutomatonProxy> automata1 = counter.getAutomata();
      final Set<AutomatonProxy> automata2 = expected.getAutomata();
      if (!compareRefSets(automata1, automata2)) {
        return false;
      }
      final List<TraceProxy> traces1 = counter.getTraces();
      final List<TraceProxy> traces2 = expected.getTraces();
      if (!compareLists(traces1, traces2)) {
        return false;
      }
      setSecondProxy(expected);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean visitDualCounterExampleProxy
    (final DualCounterExampleProxy proxy)
    throws VisitorException
  {
    return visitCounterExampleProxy(proxy);
  }

  @Override
  public Boolean visitEventProxy(final EventProxy event)
      throws VisitorException
  {
    if (visitNamedProxy(event)) {
      final EventProxy expected = (EventProxy) getSecondProxy();
      final EventKind kind1 = event.getKind();
      final EventKind kind2 = expected.getKind();
      if (kind1 != kind2) {
        return reportAttributeMismatch("kind", kind1, kind2);
      }
      final boolean obs1 = event.isObservable();
      final boolean obs2 = expected.isObservable();
      if (obs1 != obs2) {
        return reportAttributeMismatch("observable", obs1, obs2);
      }
      final Map<String,String> attribs1 = event.getAttributes();
      final Map<String,String> attribs2 = expected.getAttributes();
      if (!compareAttributeMaps(attribs1, attribs2)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean visitLoopCounterExampleProxy
    (final LoopCounterExampleProxy counter)
    throws VisitorException
  {
    return visitCounterExampleProxy(counter);
  }

  @Override
  public Boolean visitProductDESProxy(final ProductDESProxy des)
      throws VisitorException
  {
    if (visitDocumentProxy(des)) {
      final ProductDESProxy expected = (ProductDESProxy) getSecondProxy();
      final Set<EventProxy> events1 = des.getEvents();
      final Set<EventProxy> events2 = expected.getEvents();
      if (!compareNamedSets(events1, events2)) {
        return false;
      }
      final Set<AutomatonProxy> aut1 = des.getAutomata();
      final Set<AutomatonProxy> aut2 = expected.getAutomata();
      if (!compareNamedSets(aut1, aut2)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Object visitMultipleCounterExampleProxy
    (final MultipleCounterExampleProxy proxy)
    throws VisitorException
  {
    return visitCounterExampleProxy(proxy);
  }

  @Override
  public Boolean visitSafetyCounterExampleProxy
    (final SafetyCounterExampleProxy counter)
    throws VisitorException
  {
    return visitCounterExampleProxy(counter);
  }

  @Override
  public Boolean visitStateProxy(final StateProxy state)
      throws VisitorException
  {
    if (visitNamedProxy(state)) {
      final StateProxy expected = (StateProxy) getSecondProxy();
      final boolean init1 = state.isInitial();
      final boolean init2 = expected.isInitial();
      if (init1 != init2) {
        return reportAttributeMismatch("initial", init1, init2);
      }
      final Collection<EventProxy> props1 = state.getPropositions();
      final Collection<EventProxy> props2 = expected.getPropositions();
      if (!compareRefCollections(props1, props2)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean visitTraceProxy(final TraceProxy trace)
      throws VisitorException
  {
    if (visitProxy(trace)) {
      final TraceProxy expected = (TraceProxy) getSecondProxy();
      final String name1 = trace.getName();
      final String name2 = expected.getName();
      if (!ProxyTools.equals(name1, name2)) {
        return reportAttributeMismatch("trace name", name1, name2);
      }
      final List<TraceStepProxy> steps1 = trace.getTraceSteps();
      final List<TraceStepProxy> steps2 = expected.getTraceSteps();
      if (!compareLists(steps1, steps2)) {
        return false;
      }
      final int loop1 = trace.getLoopIndex();
      final int loop2 = expected.getLoopIndex();
      if (loop1 != loop2 && (loop1 >= 0 || loop2 >= 0)) {
        return reportAttributeMismatch("loop index", loop1, loop2);
      }
      setSecondProxy(expected);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean visitTraceStepProxy(final TraceStepProxy step)
      throws VisitorException
  {
    if (visitProxy(step)) {
      final TraceStepProxy expected = (TraceStepProxy) getSecondProxy();
      final EventProxy event1 = step.getEvent();
      final EventProxy event2 = expected.getEvent();
      if (!compareReferences(event1, event2)) {
        return reportAttributeMismatch("event", event1, event2);
      }
      final Map<AutomatonProxy,StateProxy> statemap1 = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> statemap2 = expected.getStateMap();
      if (!compareRefMaps(statemap1, statemap2)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean visitTransitionProxy(final TransitionProxy trans)
      throws VisitorException
  {
    if (visitProxy(trans)) {
      final TransitionProxy expected = (TransitionProxy) getSecondProxy();
      final StateProxy source1 = trans.getSource();
      final StateProxy source2 = expected.getSource();
      if (!source1.refequals(source2)) {
        return reportAttributeMismatch
            ("source", source1.getName(), source2.getName());
      }
      final EventProxy event1 = trans.getEvent();
      final EventProxy event2 = expected.getEvent();
      if (!event1.refequals(event2)) {
        return reportAttributeMismatch
            ("event", event1.getName(), event2.getName());
      }
      final StateProxy target1 = trans.getTarget();
      final StateProxy target2 = expected.getTarget();
      if (!target1.refequals(target2)) {
        return reportAttributeMismatch
            ("target", target1.getName(), target2.getName());
      }
      return true;
    } else {
      return false;
    }
  }

}

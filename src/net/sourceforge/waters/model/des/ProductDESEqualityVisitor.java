//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESEqualityVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.AbstractEqualityVisitor;
import net.sourceforge.waters.model.base.VisitorException;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


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
  //# Overrides for net.sourceforge.waters.model.base.AbstractEqualityVisitor
  public ProductDESHashCodeVisitor getHashCodeVisitor()
  {
    return ProductDESHashCodeVisitor.getInstance();
  }

  public ProductDESEqualityVisitor getNonReportingEqualityVisitor()
  {
    return getInstance();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  public Boolean visitConflictTraceProxy(final ConflictTraceProxy trace)
      throws VisitorException
  {
    if (visitTraceProxy(trace)) {
      final ConflictTraceProxy expected = (ConflictTraceProxy) getSecondProxy();
      final ConflictKind kind1 = trace.getKind();
      final ConflictKind kind2 = expected.getKind();
      if (kind1 != kind2) {
        return reportAttributeMismatch("kind", kind1, kind2);
      }
      return true;
    } else {
      return false;
    }
  }

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

  public Boolean visitLoopTraceProxy(final LoopTraceProxy trace)
      throws VisitorException
  {
    if (visitTraceProxy(trace)) {
      final LoopTraceProxy expected = (LoopTraceProxy) getSecondProxy();
      final int loop1 = trace.getLoopIndex();
      final int loop2 = expected.getLoopIndex();
      if (loop1 != loop2) {
        return reportAttributeMismatch("loop index", loop1, loop2);
      }
      return true;
    } else {
      return false;
    }
  }

  public Boolean visitProductDESProxy(final ProductDESProxy des)
      throws VisitorException
  {
    if (visitDocumentProxy(des)) {
      final ProductDESProxy expected = (ProductDESProxy) getSecondProxy();
      final Set<EventProxy> events1 = des.getEvents();
      final Set<EventProxy> events2 = expected.getEvents();
      if (!compareSets(events1, events2)) {
        return false;
      }
      final Set<AutomatonProxy> aut1 = des.getAutomata();
      final Set<AutomatonProxy> aut2 = expected.getAutomata();
      if (!compareSets(aut1, aut2)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  public Boolean visitSafetyTraceProxy(final SafetyTraceProxy trace)
      throws VisitorException
  {
    return visitTraceProxy(trace);
  }

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

  public Boolean visitTraceProxy(final TraceProxy trace)
      throws VisitorException
  {
    if (visitDocumentProxy(trace)) {
      final TraceProxy expected = (TraceProxy) getSecondProxy();
      final ProductDESProxy des1 = trace.getProductDES();
      final ProductDESProxy des2 = expected.getProductDES();
      if (!des1.refequals(des2)) {
        return reportAttributeMismatch
            ("product DES", des1.getName(), des2.getName());
      }
      final Set<AutomatonProxy> automata1 = trace.getAutomata();
      final Set<AutomatonProxy> automata2 = expected.getAutomata();
      if (!compareRefSets(automata1, automata2)) {
        return false;
      }
      final List<TraceStepProxy> steps1 = trace.getTraceSteps();
      final List<TraceStepProxy> steps2 = expected.getTraceSteps();
      if (!compareLists(steps1, steps2)) {
        return false;
      }
      setSecondProxy(expected);
      return true;
    } else {
      return false;
    }
  }

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

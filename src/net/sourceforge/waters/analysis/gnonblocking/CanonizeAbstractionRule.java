//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.xsd.base.EventKind;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntArrayList;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * @author Rachel Francis
 */

public class CanonizeAbstractionRule
  extends AbstractionRule
{
  //#######################################################################
  //# Constructor
  public CanonizeAbstractionRule(final ProductDESProxyFactory factory,
                                 final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  public CanonizeAbstractionRule(final ProductDESProxyFactory factory,
                                 final KindTranslator translator,
                                 final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions);
    mCont = factory.createEventProxy(":cont",
                                      EventKind.CONTROLLABLE);
  }
  
  void setOmegaMarking(final EventProxy omegaMarking)
  {
    mOmegaMarking = omegaMarking;
  }
  
  EventProxy getOmegaMarking()
  {
    return mOmegaMarking;
  }
  
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tauproxy)
      throws AnalysisException
  {
    EventEncoding ee = new EventEncoding(autToAbstract, getKindTranslator(), tauproxy);
    if (!autToAbstract.getEvents().contains(mCont)) {
      ee.addEvent(mCont, getKindTranslator(), false);
    }
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      ee.addEvent(mAlphaMarking, getKindTranslator(), true);
    }
    if (!autToAbstract.getEvents().contains(mOmegaMarking)) {
      ee.addEvent(mOmegaMarking, getKindTranslator(), true);
    }
    ListBufferTransitionRelation tr = 
      new ListBufferTransitionRelation(autToAbstract, ee,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      int alpha = ee.getEventCode(mAlphaMarking);
      for (int s = 0; s < tr.getNumberOfStates(); s++) {
        tr.setMarked(s, alpha, true);
      }
    }
    if (!autToAbstract.getEvents().contains(mOmegaMarking)) {
      int omega = ee.getEventCode(mAlphaMarking);
      for (int s = 0; s < tr.getNumberOfStates(); s++) {
        tr.setMarked(s, omega, true);
      }
    }
    int tau = EventEncoding.TAU;
    int marking = ee.getEventCode(mOmegaMarking);
    int alpha = ee.getEventCode(mAlphaMarking);
    int cont = ee.getEventCode(mCont);
    if (autToAbstract.getEvents().contains(mCont)) {
      tr.replaceEvent(cont, tau);
    }
    try {
      Canonize canonizer = new Canonize(tr, ee, marking, alpha, cont);
      ListBufferTransitionRelation canon = canonizer.run(getFactory());
      canon.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      TIntHashSet visited = new TIntHashSet();
      TIntArrayList tovisit = new TIntArrayList();
      for (int i = 0; i < canon.getNumberOfStates(); i++) {
        if (canon.isInitial(i)) {
          tovisit.add(i);
          visited.add(i);
        }
      }
      while (!tovisit.isEmpty()) {
        int state = tovisit.remove(tovisit.size() -1);
        canon.setReachable(state, true);
        TransitionIterator it = canon.createSuccessorsReadOnlyIterator(state);
        while (it.advance()) {
          int suc = it.getCurrentTargetState();
          if (visited.add(suc)) {tovisit.add(suc);}
        }
      }
      for (int i = 0; i < canon.getNumberOfStates(); i++) {
        if (!visited.contains(i))  {canon.setReachable(i, false);}
      }
      canon.removeUnreachableTransitions();
      System.out.println(autToAbstract.getName());
      System.out.println("Canon: " + canon.getNumberOfReachableStates() + " Comp: " + autToAbstract.getStates().size());
      //if (canon.getNumberOfReachableStates() <= autToAbstract.getStates().size() * 2) {
        return canon.createAutomaton(getFactory(), ee);
      //}
    } catch (AnalysisException analysis) {
      System.out.println("overflow");
    }
    return autToAbstract;
  }

  CompositionalGeneralisedConflictChecker.Step createStep(final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return null;
  }
  
  public void requestAbort()
  {
    mIsAborting = true;
  }

  public boolean isAborting()
  {
    return mIsAborting;
  }

  public void cleanup()
  {
    return;
  }


  //########################################################################
  //# Logging
  Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#######################################################################
  //# Data Members
  private EventProxy mOmegaMarking;
  private EventProxy mAlphaMarking;
  private EventProxy mCont;

  
  private boolean mIsAborting = false;
}

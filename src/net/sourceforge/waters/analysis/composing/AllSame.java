//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id: ProjectingControllabilityChecker.java 4468 2008-11-01 21:54:58Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.composing;

import net.sourceforge.waters.model.des.AutomatonProxy;
import java.io.PrintStream;
import net.sourceforge.waters.model.des.EventProxy;
import gnu.trove.TObjectIntHashMap;
import net.sourceforge.waters.model.des.TransitionProxy;
import java.util.Set;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Collection;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import gnu.trove.THashSet;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import java.util.Map;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import java.util.List;
import java.util.ArrayList;
import net.sourceforge.waters.model.analysis.OverflowException;
import java.util.TreeSet;
import java.util.SortedSet;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.KindTranslator;

/**
 * The projecting controllability check algorithm.
 *
 * @author Simon Ware
 */

public class AllSame
{
  private final TObjectIntHashMap<EventProxy> mEventNumber;
  private final EventProxy[] mNumberEvent;
  private int mNumberOfEvents;
  private AutomatonProxy mResult;
  private SortedSet<AutomatonProxy> mNotComposed;
  
  // #########################################################################
  // # Constructors
  public AllSame(ProductDESProxy proxy)
  {
    mNumberOfEvents = proxy.getEvents().size();
    mEventNumber = new TObjectIntHashMap<EventProxy>();
    mNumberEvent = new EventProxy[mNumberOfEvents];
    int e = 0; 
    for (EventProxy event : proxy.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {continue;}
      mNumberEvent[e] = event;
      mEventNumber.put(event, e);
      e++;
    }
  }
  
  private boolean[][] calculateAllSame(AutomatonProxy aut, KindTranslator kt) throws OverflowException
  {
    boolean[][] eventssame = new boolean[mNumberOfEvents][mNumberOfEvents];
    // mark all non local events as being not same of local
    for (int e1 = 0; e1 < mNumberOfEvents; e1++) {
      for (int e2 = 0; e2 < mNumberOfEvents; e2++) {
        if (aut.getEvents().contains(mNumberEvent[e1]) != 
            aut.getEvents().contains(mNumberEvent[e2])) {
          eventssame[e1][e2] = false;
        } else {
          eventssame[e1][e2] = true;
        }
      }
    }
    EventEncoding ee = new EventEncoding(aut, kt);
    ListBufferTransitionRelation lbtr =
      new ListBufferTransitionRelation(aut, ee,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    for (int s = 0; s < lbtr.getNumberOfStates(); s++) {
      for (int e1 = 0; e1 < ee.getNumberOfProperEvents(); e1++) {
        if (!mEventNumber.containsKey(ee.getProperEvent(e1))) {
          continue;
        }
        TIntHashSet targets1 = new TIntHashSet();
        TransitionIterator ti1 = lbtr.createSuccessorsReadOnlyIterator(s, e1);
        int eventnumberorig1 = mEventNumber.get(ee.getProperEvent(e1)); 
        while (ti1.advance()) {
          targets1.add(ti1.getCurrentTargetState());
        }
        for (int e2 = 0; e2 < ee.getNumberOfProperEvents(); e2++) {
          if (e1 == e2) {continue;}
          if (!mEventNumber.containsKey(ee.getProperEvent(e2))) {
            continue;
          }
          int eventnumberorig2 = mEventNumber.get(ee.getProperEvent(e2));
          if (eventssame[eventnumberorig1][eventnumberorig2] == false) {continue;}
          TIntHashSet targets2 = new TIntHashSet();
          TransitionIterator ti2 = lbtr.createSuccessorsReadOnlyIterator(s, e2);
          while (ti2.advance()) {
            targets2.add(ti2.getCurrentTargetState());
          }
          eventssame[eventnumberorig1][eventnumberorig2] = !targets2.equals(targets1);
        }
      }
    }
    return eventssame;
  }
  
  public boolean[][] intersect(boolean[][][] eventssame)
  {
    for (int e1 = 0; e1 < eventssame[0].length; e1++) {
      for (int e2 = 0; e2 < eventssame[0][e1].length; e2++) {
        for (int i = 1; i < eventssame.length; i++) {
          eventssame[0][e1][e2] = eventssame[0][e1][e2] && eventssame[i][e1][e2];
          if (!eventssame[0][e1][e2]) {break;}
        }
      }
    }
    return eventssame[0];
  }
  
  public void update(Set<AutomatonProxy> composed, Set<AutomatonProxy> notcomposed,
                     AutomatonProxy result, ProductDESProxyFactory factory,
                     Set<EventProxy> hidden, KindTranslator kt)
  {
    Map<EventProxy, EventProxy> replacementmap =
      new THashMap<EventProxy, EventProxy>();
    if (notcomposed.isEmpty()) {return;}
    boolean[][][] eventssamearr = new boolean[notcomposed.size()][][];
    int a = 0;
    for (AutomatonProxy aut : notcomposed) {
      try {
        eventssamearr[a] = calculateAllSame(aut, kt);
        a++;
      } catch (OverflowException oe) {
        oe.printStackTrace();
        return;
      }
    }
    boolean[][] eventssame = intersect(eventssamearr);
    for (EventProxy e1 : result.getEvents()) {
      if (hidden.contains(e1)) {continue;}
      if (!mEventNumber.containsKey(e1)) {continue;}
      int evnum1 = mEventNumber.get(e1);
      for (EventProxy e2 : result.getEvents()) {
        if (hidden.contains(e2)) {continue;}
        if (e1 == e2) {continue;}
        if (!mEventNumber.containsKey(e2)) {continue;}
        int evnum2 = mEventNumber.get(e2);
        if (eventssame[evnum1][evnum2]) {
          EventProxy replacement = replacementmap.get(e1);
          if (replacement == null) {
            replacement = e1;
            replacementmap.put(e1, replacement);
          }
          replacementmap.put(e2, replacement);
        }
      }
    }
    System.out.println("map");
    System.out.println(replacementmap);
    if (replacementmap.isEmpty()) {return;}
    replace(result, notcomposed, replacementmap, factory);
  }
  
  public AutomatonProxy getResult()
  {
    return mResult;
  }
  
  public SortedSet<AutomatonProxy> getNotComposed()
  {
    return mNotComposed;
  }
  
  public void replace(AutomatonProxy result, Set<AutomatonProxy> notcomposed,
                      Map<EventProxy, EventProxy> replacementmap,
                      ProductDESProxyFactory factory)
  {
    System.out.println("replacing");
    //System.out.println(result);
    List<TransitionProxy> newTrans = new ArrayList<TransitionProxy>();
    Set<EventProxy> removed = new THashSet<EventProxy>();
    for (TransitionProxy tran : result.getTransitions()) {
      if (replacementmap.containsKey(tran.getEvent())) {
        TransitionProxy t = factory.createTransitionProxy(tran.getSource(),
                                                          replacementmap.get(tran.getEvent()),
                                                          tran.getTarget());
        newTrans.add(t);
        removed.add(tran.getEvent());
        removed.remove(replacementmap.get(tran.getEvent()));
      } else {
        newTrans.add(tran);
      }
    }
    mResult = factory.createAutomatonProxy(result.getName(), result.getKind(),
                                           result.getEvents(),
                                           result.getStates(), newTrans);
    //System.out.println(mResult);
    //System.exit(1);
    // make automaton
    mNotComposed = new TreeSet<AutomatonProxy>();
    for (AutomatonProxy aut : notcomposed) {
      List<TransitionProxy> trans = new ArrayList<TransitionProxy>();
      for (TransitionProxy tran : result.getTransitions()) {
        if (!removed.contains(tran.getEvent())) {
          newTrans.add(tran);
        }
      }
      mNotComposed.add(factory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                                    aut.getEvents(), aut.getStates(),
                                                    trans));
    }
  }
}

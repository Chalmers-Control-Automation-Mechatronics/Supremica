//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCandidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class TRCandidate
  implements Comparable<TRCandidate>
{

  //#########################################################################
  //# Constructors
  TRCandidate(final List<TRAutomatonProxy> automata,
              final EventEncoding enc)
  {
    mAutomata = automata;
    mEventEncoding = enc;
  }

  TRCandidate(final List<TRAutomatonProxy> automata,
              final TRSubsystemInfo subsys)
    throws OverflowException
  {
    mAutomata = automata;
    mEventEncoding = new EventEncoding();
    final Set<TRAutomatonProxy> set = new THashSet<>(automata);
    for (final TRAutomatonProxy aut : automata) {
      final EventEncoding localEnc = aut.getEventEncoding();
      final int numEvents = localEnc.getNumberOfProperEvents();
      for (int local = EventEncoding.NONTAU; local < numEvents; local++) {
        byte status = localEnc.getProperEventStatus(local);
        if (EventStatus.isUsedEvent(status)) {
          final EventProxy event = localEnc.getProperEvent(local);
          if (mEventEncoding.getEventCode(event) < 0) {
            final TREventInfo info = subsys.getEventInfo(event);
            status = info.getEventStatus(set);
            mEventEncoding.addProperEvent(event, status);
          }
        }
      }
    }
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return AutomatonTools.getCompositionName(mAutomata).replaceAll(":", "-");
  }

  List<TRAutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#########################################################################
  //# Advanced Access
  ProductDESProxy createProductDESProxy(final ProductDESProxyFactory factory)
  {
    final String name = getName();
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    final List<EventProxy> events =
      new ArrayList<>(numEvents + mAutomata.size());
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = mEventEncoding.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = mEventEncoding.getProperEvent(e);
        events.add(event);
      }
    }
    for (final TRAutomatonProxy aut : mAutomata) {
      final EventEncoding enc = aut.getEventEncoding();
      final byte status = enc.getProperEventStatus(EventEncoding.TAU);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy tau = enc.getProperEvent(EventEncoding.TAU);
        if (tau != null) {
          events.add(tau);
        }
      }
    }
    return factory.createProductDESProxy(name, events, mAutomata);
  }

  EventEncoding createSyncEventEncoding()
    throws OverflowException
  {
    final String name = getName();
    final EventProxy tau = mEventEncoding.provideTauEvent(name);
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    final EventEncoding syncEncoding = new EventEncoding();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = mEventEncoding.getProperEvent(e);
      final byte status = mEventEncoding.getProperEventStatus(e);
      if (EventStatus.isLocalEvent(status)) {
        syncEncoding.addSilentEvent(tau);
        syncEncoding.setProperEventStatus(EventEncoding.TAU, status);
        syncEncoding.addSilentEvent(event);
      } else {
        syncEncoding.addProperEvent(event, status);
      }
    }
    final int numProps = mEventEncoding.getNumberOfPropositions();
    for (int p = 0; p < numProps; p++) {
      final EventProxy prop = mEventEncoding.getProposition(p);
      final boolean used = mEventEncoding.isPropositionUsed(p);
      syncEncoding.addProposition(prop, used);
    }
    for (final TRAutomatonProxy aut : mAutomata) {
      final EventEncoding enc = aut.getEventEncoding();
      final byte status = enc.getProperEventStatus(EventEncoding.TAU);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = enc.getProperEvent(EventEncoding.TAU);
        if (event != null) {
          syncEncoding.addSilentEvent(tau);
          syncEncoding.addSilentEvent(event);
        }
      }
    }
    syncEncoding.setTauEvent(tau);
    return syncEncoding;
  }

  boolean hasSameEventStatus(final TRCandidate candidate)
  {
    final EventEncoding enc1 = mEventEncoding;
    final EventEncoding enc2 = candidate.mEventEncoding;
    final int numEvents1 = enc1.getNumberOfProperEvents();
    final int numEvents2 = enc2.getNumberOfProperEvents();
    if (numEvents1 != numEvents2) {
      return false;
    }
    for (int e = EventEncoding.NONTAU; e < numEvents1; e++) {
      final byte status1 = enc1.getProperEventStatus(e);
      final byte status2 = enc2.getProperEventStatus(e);
      if (status1 != status2) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Interface java.util.Comparable<Candidate>
  /**
   * Implements default candidate ordering. If both candidates have different
   * numbers of automata, the candidate with fewer automata is considered
   * smaller. If the number of automata is equal, the lists are compared
   * lexicographically by automaton names.
   */
  @Override
  public int compareTo(final TRCandidate candidate)
  {
    final List<TRAutomatonProxy> automata1 = mAutomata;
    final List<TRAutomatonProxy> automata2 = candidate.mAutomata;
    final int size1 = automata1.size();
    final int size2 = automata2.size();
    if (size1 != size2) {
      return size1 - size2;
    }
    final Iterator<TRAutomatonProxy> iter1 = automata1.iterator();
    final Iterator<TRAutomatonProxy> iter2 = automata2.iterator();
    while (iter1.hasNext()) {
      final AutomatonProxy aut1 = iter1.next();
      final AutomatonProxy aut2 = iter2.next();
      final int result = aut1.compareTo(aut2);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return getName();
  }


  //#########################################################################
  //# Data Members
  private final List<TRAutomatonProxy> mAutomata;
  private final EventEncoding mEventEncoding;

}

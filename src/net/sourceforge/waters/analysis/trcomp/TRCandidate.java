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

  void setComposedSuccessfully()
  {
    mComposedSuccessfully = true;
  }

  boolean isComposedSuccessfully()
  {
    return mComposedSuccessfully;
  }


  //#########################################################################
  //# Advanced Access
  ProductDESProxy createProductDESProxy(final ProductDESProxyFactory factory)
  {
    final String name = getName();
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    final int numProps = mEventEncoding.getNumberOfPropositions();
    final List<EventProxy> events =
      new ArrayList<>(numEvents + numProps + mAutomata.size());
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
    for (int p = 0; p < numProps; p++) {
      final EventProxy prop = mEventEncoding.getProposition(p);
      events.add(prop);
    }
    return factory.createProductDESProxy(name, events, mAutomata);
  }

  EventEncoding createSyncEventEncoding()
    throws OverflowException
  {
    final String name = getName();
    final EventProxy tau = mEventEncoding.provideTauEvent(name);
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    final byte pattern =
      EventStatus.STATUS_FULLY_LOCAL | EventStatus.STATUS_CONTROLLABLE;
    final EventEncoding syncEncoding = new EventEncoding();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = mEventEncoding.getProperEvent(e);
      final byte status = mEventEncoding.getProperEventStatus(e);
      if (EventStatus.isLocalEvent(status)) {
        syncEncoding.addSilentEvent(tau);
        syncEncoding.setProperEventStatus(EventEncoding.TAU, status & pattern);
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
  private boolean mComposedSuccessfully = false;

}

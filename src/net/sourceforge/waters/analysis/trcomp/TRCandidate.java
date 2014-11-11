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

import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

public class TRCandidate
{

  //#########################################################################
  //# Constructors
  TRCandidate(final List<TRAutomatonProxy> automata,
              final SubsystemInfo subsys)
    throws OverflowException
  {
    mAutomata = automata;
    mEventEncoding = new EventEncoding();
    final Set<TRAutomatonProxy> set = new THashSet<>(automata);
    for (final TRAutomatonProxy aut : automata) {
      final EventEncoding localEnc = aut.getEventEncoding();
      final int numEvents = localEnc.getNumberOfProperEvents();
      for (int local = EventEncoding.NONTAU; local < numEvents; local++) {
        final EventProxy event = localEnc.getProperEvent(local);
        if (mEventEncoding.getEventCode(event) < 0) {
          final TREventInfo info = subsys.getEventInfo(event);
          final byte status = info.getEventStatus(set);
          mEventEncoding.addProperEvent(event, status);
        }
      }
      final int numProps = localEnc.getNumberOfPropositions();
      for (int local = 0; local < numProps; local++) {
        final EventProxy prop = localEnc.getProperEvent(local);
        final boolean used = localEnc.isPropositionUsed(local);
        mEventEncoding.addProposition(prop, used);
      }
    }
  }


  //#########################################################################
  //# Simple Access
  List<TRAutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#########################################################################
  //# Data Members
  private final List<TRAutomatonProxy> mAutomata;
  private final EventEncoding mEventEncoding;

}

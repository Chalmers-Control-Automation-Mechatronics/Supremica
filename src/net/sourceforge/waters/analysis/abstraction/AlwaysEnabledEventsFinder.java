//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AlwaysEnabledEventsFinder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to find always enabled events.</P>
 *
 * <P>Although implemented as a transition relation simplifier, this
 * abstraction never changes its automaton. Its sole purpose is to
 * produce as output a collection of always enabled events, where
 * an event is considered as always enabled if it is enabled in
 * every state without an outgoing tau-transitions, except for
 * deadlock states. This algorithm is based on the assumption that
 * the input automaton is tau-loop free.</P>
 *
 * @author Robi Malik
 */

public class AlwaysEnabledEventsFinder
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public AlwaysEnabledEventsFinder()
  {
  }

  public AlwaysEnabledEventsFinder(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Simple Access
  public TIntArrayList getAlwaysEnabledEvents()
  {
    return mAlwaysEnabledEvents;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numEvents = rel.getNumberOfProperEvents();
    final int defaultID = getDefaultMarkingID();
    if ((rel.getConfiguration() &
         ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
      // Algorithm for forwards transitions ...
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final boolean[] disabled = new boolean[numEvents];
      states:
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) && !rel.isDeadlockState(s, defaultID)) {
          int lastEvent = EventEncoding.TAU;
          iter.resetState(s);
          while (iter.advance()) {
            final int e = iter.getCurrentEvent();
            if (e == EventEncoding.TAU) {
              continue states;
            } else if (e > lastEvent) {
              for (int d = lastEvent + 1; d < e; d++) {
                disabled[d] = true;
              }
              lastEvent = e;
            }
          }
          for (int d = lastEvent + 1; d < numEvents; d++) {
            disabled[d] = true;
          }
        }
      }
      mAlwaysEnabledEvents = new TIntArrayList(numEvents - 1);
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status) && !disabled[e]) {
          mAlwaysEnabledEvents.add(e);
        }
      }
    } else {
      // Algorithm for backwards transitions ...
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator();
      final byte[] stateInfo = new byte[numStates];
      while (iter.advance()) {
        final int s = iter.getCurrentSourceState();
        stateInfo[s] |= NONDUMP;
        if (iter.getCurrentEvent() == EventEncoding.TAU) {
          stateInfo[s] |= HASTAU;
        }
      }
      int numStatesChecked = 0;
      for (int s = 0; s < numStates; s++) {
        if (!rel.isReachable(s)) {
          stateInfo[s] = DUMP;
        } else if (stateInfo[s] == NONDUMP) {
          numStatesChecked++;
        } else if (stateInfo[s] == DUMP && rel.isMarked(s, defaultID)) {
          mAlwaysEnabledEvents = new TIntArrayList(0);
          return false;
        }
      }
      mAlwaysEnabledEvents = new TIntArrayList(numEvents - 1);
      final boolean[] found = new boolean[numStates];
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status)) {
          Arrays.fill(found, false);
          int numStatesFound = 0;
          iter.resetEvent(e);
          while (iter.advance()) {
            final int s = iter.getCurrentSourceState();
            if (!found[s] && stateInfo[s] == NONDUMP) {
              found[s] = true;
              numStatesFound++;
            }
          }
          if (numStatesFound == numStatesChecked) {
            mAlwaysEnabledEvents.add(e);
          }
        }
      }
    }
    return false;
  }


  //#########################################################################
  //# Data Members
  private TIntArrayList mAlwaysEnabledEvents;


  //#########################################################################
  //# Class Constants
  private static final byte DUMP = 0x00;
  private static final byte NONDUMP = 0x01;
  private static final byte HASTAU = 0x02;

}

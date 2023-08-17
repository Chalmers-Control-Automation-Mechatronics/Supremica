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

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.TransitionListBuffer;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A synchronous product algorithm that produces a reduced synchronous product
 * based on conflict equivalence.
 *
 * @author Robi Malik
 */

public class TRReducingSynchronousProductBuilder
  extends AbstractTRSynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public TRReducingSynchronousProductBuilder()
  {
  }

  public TRReducingSynchronousProductBuilder(final ProductDESProxy model)
  {
    super(model);
  }

  public TRReducingSynchronousProductBuilder
    (final ProductDESProxy model,
     final KindTranslator translator)
  {
    super(model, translator);
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();

    final TRAutomatonProxy[] inputAutomata = getTRAutomata();
    final int numAutomata = inputAutomata.length;
    int limit = getTransitionLimit();
    if (limit < Integer.MAX_VALUE) {
      limit /= numAutomata;
    }

    final Collection<EventInfo> eventInfo = getEventInfo();
    mStronglyLocalEvents = new ArrayList<>(numAutomata);
    mStronglyLocalCode = -1;
    int numStronglyForbidden = 0;
    int numWeaklyLocal = 0;
    int numShared = 0;
    final TIntHashSet localAutomata = new TIntHashSet(numAutomata);
    for (final EventInfo event : eventInfo) {
      if (event.isStronglyLocal()) {
        final int code = event.getOutputCode();
        if (mStronglyLocalCode < 0) {
          mStronglyLocalCode = code;
        } else {
          assert mStronglyLocalCode == code;
        }
        final int a = event.getLocalAutomatonIndex();
        assert localAutomata.add(a);
        final TRAutomatonProxy aut = inputAutomata[a];
        final StronglyLocalEventInfo info =
          new StronglyLocalEventInfo(aut, event, limit);
        mStronglyLocalEvents.add(info);
      } else if (event.isLocal()) {
        numWeaklyLocal++;
      } else if (event.isStronglyForbidden()) {
        numStronglyForbidden++;
      } else {
        numShared++;
      }
    }
    if (mStronglyLocalEvents.size() <= 1) {
      mStronglyLocalEvents = null;
    } else {
      final StateTupleEncoding stateEnc = getStateTupleEncoding();
      mIntermediateEncoded = new int[stateEnc.getNumberOfWords()];
      mIntermediateDecoded = new int[numAutomata];
      mStronglyForbiddenEvents = new ArrayList<>(numStronglyForbidden);
      mWeaklyLocalEvents = new ArrayList<>(numWeaklyLocal);
      mSharedEvents = new ArrayList<>(numShared);
      for (final EventInfo event : eventInfo) {
        if (!event.isLocal()) {
          mSharedEvents.add(event);
        } else if (event.isStronglyForbidden()) {
          mStronglyForbiddenEvents.add(event);
        } else if (!event.isStronglyLocal()) {
          mWeaklyLocalEvents.add(event);
        }
      }
      mEnabledLocalEvents = new ArrayList<>(mStronglyLocalEvents.size());
      final EventEncoding eventEnc = getOutputEventEncoding();
      final int numProps = eventEnc.getNumberOfPropositions();
      mOnTheFlyMarkingInfo = new MarkingInfo[numProps];
      mStoredMarkingInfo = new StoredMarkingInfo[numProps];
      for (int p = 0; p < numProps; p++) {
        if (eventEnc.isPropositionUsed(p)) {
          final EventProxy prop = eventEnc.getProposition(p);
          mOnTheFlyMarkingInfo[p] = new OnTheFlyMarkingInfo(prop);
          mStoredMarkingInfo[p] = new StoredMarkingInfo();
        }
      }
    }
    final TRSynchronousProductResult result = getAnalysisResult();
    result.setReducedDiamondsCount(0);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mStronglyLocalEvents = null;
    mIntermediateEncoded = null;
    mIntermediateDecoded = null;
    mStronglyForbiddenEvents = null;
    mWeaklyLocalEvents = null;
    mSharedEvents = null;
    mEnabledLocalEvents = null;
    mOnTheFlyMarkingInfo = null;
    mStoredMarkingInfo = null;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.monolithic.TRAbstractSynchronousProductBuilder
  @Override
  protected void expandState(final int[] encoded, final int[] decoded)
    throws AnalysisException
  {
    if (mStronglyLocalEvents == null) {
      super.expandState(encoded, decoded);
      return;
    }
    for (final StronglyLocalEventInfo info : mStronglyLocalEvents) {
      if (info.isTauEnabled(decoded)) {
        mEnabledLocalEvents.add(info);
      }
    }
    try {
      if (mEnabledLocalEvents.size() <= 1) {
        super.expandState(encoded, decoded);
        for (int p = 0; p < mOnTheFlyMarkingInfo.length; p++) {
          final MarkingInfo info = mOnTheFlyMarkingInfo[p];
          if (info != null && info.isMarkedStateDecoded(decoded)) {
            final int source = getCurrentSource();
            mStoredMarkingInfo[p].setMarked(source);
          }
        }
      } else {
        final TRSynchronousProductResult result = getAnalysisResult();
        result.addReducedDiamond();
        System.arraycopy(encoded, 0, mIntermediateEncoded, 0, encoded.length);
        System.arraycopy(decoded, 0, mIntermediateDecoded, 0, decoded.length);
        // If this is a dump state, then stop
        for (final EventInfo event : mStronglyForbiddenEvents) {
          if (!expandIntermediateStates(decoded, 0, event, true)) {
            return;
          }
        }
        // Otherwise first expand tau transitions
        expandIntermediateStates(decoded, 0, null, true);
        for (final EventInfo event : mWeaklyLocalEvents) {
          expandIntermediateStates(decoded, 0, event, true);
        }
        // Then regular transitions, event by event
        for (final EventInfo event : mSharedEvents) {
          expandIntermediateStates(decoded, 0, event, true);
        }
      }
    } finally {
      mEnabledLocalEvents.clear();
    }
  }

  @Override
  public MarkingInfo getMarkingInfo(final EventProxy prop)
  {
    if (mStoredMarkingInfo == null) {
      return super.getMarkingInfo(prop);
    } else {
      final EventEncoding enc = getOutputEventEncoding();
      final int p = enc.getEventCode(prop);
      return mStoredMarkingInfo[p];
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean expandIntermediateStates(final int[] decodedSource,
                                           final int localIndex,
                                           final EventInfo event,
                                           final boolean terminal)
    throws AnalysisException
  {
    if (localIndex < mEnabledLocalEvents.size()) {
      final StronglyLocalEventInfo info = mEnabledLocalEvents.get(localIndex);
      final int a = info.getAutomatonIndex();
      final int s = decodedSource[a];
      final TransitionIterator iter = info.getClosureIterator();
      iter.resetState(s);
      while (iter.advance()) {
        final int t = iter.getCurrentTargetState();
        mIntermediateDecoded[a] = t;
        final StateTupleEncoding enc = getStateTupleEncoding();
        enc.set(mIntermediateEncoded, a, t);
        final boolean hasTau = info.isTauEnabled(t);
        if (!expandIntermediateStates(decodedSource, localIndex + 1,
                                      event, terminal && !hasTau)) {
          return false;
        }
      }
    } else if (!terminal) {
      if (event == null) {
        for (int p = 0; p < mOnTheFlyMarkingInfo.length; p++) {
          final MarkingInfo info = mOnTheFlyMarkingInfo[p];
          if (info != null &&
              info.isMarkedStateDecoded(mIntermediateDecoded)) {
            final int source = getCurrentSource();
            mStoredMarkingInfo[p].setMarked(source);
          }
        }
      } else {
        return expandState(mIntermediateEncoded, mIntermediateDecoded, event);
      }
    } else if (event == null) {
      final int target =
        createNewStateEncoded(mIntermediateDecoded, mIntermediateEncoded);
      createTransition(mStronglyLocalCode, target);
    }
    return true;
  }


  //#########################################################################
  //# Inner Class StronglyLocalEventInfo
  private static class StronglyLocalEventInfo
  {
    //#######################################################################
    //# Constructor
    private StronglyLocalEventInfo(final TRAutomatonProxy aut,
                                   final EventInfo event,
                                   final int limit)
    {
      final AutomatonEventInfo autInfo = event.getLocalAutomatonInfo();
      mAutomatonIndex = event.getLocalAutomatonIndex();
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final TransitionListBuffer buffer = rel.getSuccessorBuffer();
      mTauIterator = autInfo.getTransitionIterator();
      final TauClosure closure = new TauClosure(buffer, mTauIterator, limit);
      mClosureIterator = closure.createIterator();
    }

    //#######################################################################
    //# Simple Access
    private int getAutomatonIndex()
    {
      return mAutomatonIndex;
    }

    private TransitionIterator getClosureIterator()
    {
      return mClosureIterator;
    }

    //#######################################################################
    //# State Expansion
    private boolean isTauEnabled(final int state)
    {
      mTauIterator.resetState(state);
      return mTauIterator.advance();
    }

    private boolean isTauEnabled(final int[] decoded)
    {
      mTauIterator.resetState(decoded[mAutomatonIndex]);
      return mTauIterator.advance();
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonIndex;
    private final TransitionIterator mTauIterator;
    private final TransitionIterator mClosureIterator;
  }


  //#########################################################################
  //# Data Members
  private List<StronglyLocalEventInfo> mStronglyLocalEvents;
  private List<StronglyLocalEventInfo> mEnabledLocalEvents;
  private int mStronglyLocalCode;
  private List<EventInfo> mStronglyForbiddenEvents;
  private List<EventInfo> mWeaklyLocalEvents;
  private List<EventInfo> mSharedEvents;
  private int[] mIntermediateEncoded;
  private int[] mIntermediateDecoded;
  private MarkingInfo[] mOnTheFlyMarkingInfo;
  private StoredMarkingInfo[] mStoredMarkingInfo;

}

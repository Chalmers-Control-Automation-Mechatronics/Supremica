//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import gnu.trove.list.array.TIntArrayList;

import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.TarjanControlStack;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * Monolithic conflict checker implementation based on Tarjan's algorithm.
 *
 * Incomplete: does not yet support counterexamples and generalised
 * nonblocking.
 *
 * @author Benjamin Wheeler
 */

public class TRMonolithicConflictChecker
  extends AbstractTRMonolithicModelAnalyzer implements ConflictChecker
{

  public TRMonolithicConflictChecker()
  {
    this(null);
  }

  public TRMonolithicConflictChecker(final ProductDESProxy model)
  {
    super(model, ConflictKindTranslator.getInstanceControllable());
  }

  public TRMonolithicConflictChecker(final ProductDESProxy model,
                                     final KindTranslator translator)
  {
    super(model, translator);
  }


  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();

    final EventProxy markingEvent;
    if (mConfiguredMarking == null) {
      final ProductDESProxy model = getModel();
      markingEvent = AbstractConflictChecker.findMarkingProposition(model);
    } else {
      markingEvent = mConfiguredMarking;
    }
    mUsedMarking = getMarkingInfo(markingEvent);
  }

  @Override
  public boolean run() throws AnalysisException
  {

    try {
      setUp();

      mCompStack = new TIntArrayList();
      mControlStack = new ExtendedTarjanControlStack();

      final VerificationResult result = getAnalysisResult();

      final int numInitialStates = storeInitialStates();
      for (int s = 0; s < numInitialStates; s++) {
        mControlStack.push(s, s);
      }

      while (!mControlStack.isEmpty()) {
        final int i = mControlStack.getTopIndex();
        final int p = mControlStack.getTopParent();
        if (!(mControlStack.isTopExpanded())) {
          expand(i);
        }
        else {
          mControlStack.pop();
          close(i, p);
          if (result.isFinished()) return result.isSatisfied();
        }
      }
      result.setSatisfied(true);

    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }

    return getAnalysisResult().isSatisfied();
  }

  private void expand(final int i) throws AnalysisException {

    final StateTupleEncoding ste = getStateTupleEncoding();
    final IntArrayBuffer stateSpace = getStateSpace();

    final int dfsi = mCompStack.size() | EXPANDED;
    mControlStack.setLink(i, dfsi);
    mControlStack.setTopIndex(dfsi);

    mCompStack.add(i);

    final int[] x = new int[ste.getNumberOfWords()];
    final int[] xDec = new int[ste.getNumberOfAutomata()];
    stateSpace.getContents(i, x);
    ste.decode(x, xDec);
    mKnownNumberOfStates = getStateSpace().size();

    expandState(i);

  }


  private void close(final int dfsi, final int p) throws AnalysisException {

    final int i = mCompStack.get(dfsi);
    final int link = mControlStack.getLink(i);
    int j = 0;
    int k;

    if ((link & ~EXPANDED) == dfsi) {

      boolean nonblocking = false;
      k = mCompStack.size() - 1;
      do {
        j = mCompStack.get(k);
        if (mUsedMarking.isMarkedState(j)) {
          nonblocking = true;
          break;
        }
        else {
          mTransitionMode = SEARCHING;
          expandState(j);
          if (mTransitionMode == FOUND) {
            mTransitionMode = EXPANDING;
            nonblocking = true;
            break;
          }
          else mTransitionMode = EXPANDING;
        }

        k--;
      }
      while (i != j);

      if (nonblocking) {
        k = mCompStack.size() - 1;
        do {
          j = mCompStack.get(k);
          mControlStack.setLink(j, CLOSED);
          k--;
        }
        while (i != j);
        //Set k back to last value
        k++;
        mCompStack.remove(k, mCompStack.size() - k);
      }
      else {
        getAnalysisResult().setSatisfied(false);
        return;
      }
    }
    else {
      mControlStack.setLowlink(p, Math.min(mControlStack.getLowlink(p), mControlStack.getLowlink(i)));
    }

  }


  @Override
  protected void createTransition(final int event, final int j)
    throws OverflowException
  {

    if (mTransitionMode == EXPANDING) {

      final int i = getCurrentSource();

      final int numberOfStates = getStateSpace().size();
      if (numberOfStates > mKnownNumberOfStates) {
        mKnownNumberOfStates = numberOfStates;
        mControlStack.push(j, i);
      }
      else if (mControlStack.getMode(j) == OPEN) {
        mControlStack.moveToTop(mControlStack.getLink(j), i);
      }
      else if (mControlStack.getMode(j) == EXPANDED) {
        mControlStack.setLowlink(i, Math.min(mControlStack.getLowlink(i), mControlStack.getLowlink(j)));
      }

    }
    else if (mTransitionMode == SEARCHING) {
      if (mControlStack.getMode(j) == CLOSED) {
        mTransitionMode = FOUND;
      }
    }

  }



  @Override
  public boolean isSatisfied()
  {
    return getAnalysisResult().isSatisfied();
  }

  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }

  @Override
  public VerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult(this);
  }


  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCompStack = null;
    mControlStack = null;
    mUsedMarking = null;
    mConfiguredMarking = null;
  }

  private class ExtendedTarjanControlStack extends TarjanControlStack {

    private int getLowlink(final int index) {
      if (getLink(index) == CLOSED) throw new RuntimeException("CLOSED");
      return getLink(index) & ~EXPANDED;
    }

    private void setLowlink(final int index, final int value) {
      if (value == CLOSED) throw new RuntimeException("CLOSED");
      setLink(index, value | EXPANDED);
    }

    private int getMode(final int index) {
      final int link = getLink(index);
      return link == CLOSED ? CLOSED : link & EXPANDED;
    }

//    private void setMode(final int index, final int mode) {
//      if (mode == EXPANDED) setTopExpanded();
//      setLink(index, mode == CLOSED ? CLOSED : (getLink(index) & ~EXPANDED) | mode);
//    }

  }


  private TIntArrayList mCompStack;
  private ExtendedTarjanControlStack mControlStack;
  private int mKnownNumberOfStates;
  private int mTransitionMode;

  private MarkingInfo mUsedMarking;
  private EventProxy mConfiguredMarking;

  private static final int EXPANDING = 0;
  private static final int SEARCHING = 1;
  private static final int FOUND = 2;

  private static final int OPEN = 0;
  private static final int EXPANDED = 0x80000000;
  private static final int CLOSED = -1;

}

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

import gnu.trove.list.array.TIntArrayList;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateTupleBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TarjanControlStack;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An alternative implementation of the monolithic synthesis algorithm
 * that uses ideas of Tarjan's algorithm to detect blocking states early.
 *
 * @author Benjamin Wheeler
 */

public class TRMonolithicSynthesizer
  extends AbstractTRMonolithicModelAnalyzer implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public TRMonolithicSynthesizer(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
  }

  public TRMonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model);
    mFactory = factory;
  }

  public TRMonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, translator);
    mFactory = factory;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
  }

  @Override
  public boolean isNonblockingSynthesis()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setNondeterminismEnabled(final boolean enable)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void setSupervisorReductionFactory(final SupervisorReductionFactory factory)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public SupervisorReductionFactory getSupervisorReductionFactory()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isSupervisorLocalizationEnabled()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setNonblockingSynthesis(final boolean nonblocking)
  {
    // TODO Auto-generated method stub
  }


  @Override
  public TRSynthesisResult createAnalysisResult()
  {
    return new TRSynthesisResult(this);
  }

  @Override
  public TRSynthesisResult getAnalysisResult()
  {
    return (TRSynthesisResult) super.getAnalysisResult();
  }

  @Override
  public ProductDESProxy getComputedProxy()
  {
    return getComputedProductDES();
  }

  @Override
  public ProductDESProxy getComputedProductDES()
  {
    final TRSynthesisResult result = getAnalysisResult();
    return result.getComputedProductDES();
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();

    mTransitionMode = EXPANDING;
    setUpUsedDefaultMarking();
    mMarkingInfo = getMarkingInfo(mUsedMarking);
    mCompStack = new TIntArrayList();
    mControlStack = new ExtendedTarjanControlStack();
  }

  @Override
  public boolean run() throws AnalysisException
  {

    try {

      setUp();

      storeInitialStates();
      mControlStack.push(0, 0);

      while (!mControlStack.isEmpty()) {
        final int i = mControlStack.getTopIndex();
        final int p = mControlStack.getTopParent();
        if (!(mControlStack.isTopExpanded())) {
          expand(i);
        }
        else {
          mControlStack.pop();
          close(i, p);
        }
      }

      produceResult();

    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }

    return getAnalysisResult().isSatisfied();
  }

  private void expand(final int i) throws AnalysisException {

    if (mTransitionMode == EXPANDING) {
      final StateTupleEncoding ste = getStateTupleEncoding();
      final StateTupleBuffer stateSpace = getStateSpace();

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

        if (mMarkingInfo.isMarkedState(j)) {
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
          mControlStack.setLink(j, CLOSED_GOOD);
          k--;
        }
        while (i != j);
        //Set k back to last value
        k++;
        mCompStack.remove(k, mCompStack.size() - k);
      }
      else {
        k = mCompStack.size() - 1;
        do {
          j = mCompStack.get(k);
          mControlStack.setLink(j, CLOSED_BAD);
          k--;
        }
        while (i != j);
        //Set k back to last value
        k++;
        mCompStack.remove(k, mCompStack.size() - k);
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
      if (mControlStack.getMode(j) == CLOSED_GOOD) {
        mTransitionMode = FOUND;
      }
    }
    else if (mTransitionMode == BUILDING) {
      final int i = getCurrentSource();
      if (mControlStack.getMode(j) == CLOSED_GOOD) {
        mPreTransitionBuffer.addTransition(i, event, j);
      }

    }

  }


  private void produceResult() throws AnalysisException {

    mTransitionMode = BUILDING;

    if (mControlStack.getMode(0) == CLOSED_GOOD) {

      final ProductDESProxyFactory desFactory = mFactory;

      final EventEncoding enc = getOutputEventEncoding();
      final int numEvents = enc.getNumberOfProperEvents();
      mPreTransitionBuffer = new PreTransitionBuffer(numEvents);
      final int markedEvent = enc.getEventCode(mUsedMarking);

      final int numStates = getStateSpace().size();
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(mOutputName,
                                         ComponentKind.SUPERVISOR,
                                         enc,
                                         numStates,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);

      for (int i = 0; i < numStates; i++) {
        if (mControlStack.getMode(i) == CLOSED_GOOD) {
          expandState(i);
          rel.setReachable(i, true);
          if (markedEvent != -1 && mMarkingInfo.isMarkedState(i)) {
            rel.setMarked(i, markedEvent, true);
          }
        }
        else {
          rel.setReachable(i, false);
        }
      }

      mPreTransitionBuffer.addOutgoingTransitions(rel);
      rel.setInitial(0, true);
      rel.removeRedundantPropositions();
      rel.checkReachability();

      final TRAutomatonProxy aut = new TRAutomatonProxy(enc, rel);
      final ProductDESProxy des = AutomatonTools.createProductDESProxy(aut, desFactory);

      final TRSynthesisResult result = getAnalysisResult();
      result.setComputedProductDES(des);
      setAnalysisResult(result);
    }
    else {
      //Whole model is bad
      setBooleanResult(false);
    }
  }




  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCompStack = null;
    mControlStack = null;
    mUsedMarking = null;
  }

  private class ExtendedTarjanControlStack extends TarjanControlStack {

    private int getLowlink(final int index) {
      if (isClosed(getLink(index))) throw new RuntimeException("CLOSED");
      return getLink(index) & ~EXPANDED;
    }

    private void setLowlink(final int index, final int value) {
      if (isClosed(value)) throw new RuntimeException("CLOSED");
      setLink(index, value | EXPANDED);
    }

    private int getMode(final int index) {
      final int link = getLink(index);
      return isClosed(link) ? link : link & EXPANDED;
    }

//    private void setMode(final int index, final int mode) {
//      if (mode == EXPANDED) setTopExpanded();
//      setLink(index, mode == CLOSED ? CLOSED : (getLink(index) & ~EXPANDED) | mode);
//    }

    private boolean isClosed(final int value) {
      return value == CLOSED_GOOD || value == CLOSED_BAD;
    }

  }




  /**
   * Determines the marking proposition to be used.
   * This method returns the marking proposition specified by the {@link
   * #setConfiguredDefaultMarking(EventProxy) setMarkingProposition()} method,
   * if non-null, or the default marking proposition of the input model.
   * @throws EventNotFoundException to indicate that the a
   *         <CODE>null</CODE> marking was specified, but input model does
   *         not contain any proposition with the default marking name.
   */
  protected EventProxy setUpUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mConfiguredMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.findMarkingProposition(model);
      } else {
        mUsedMarking = mConfiguredMarking;
      }
    }
    return mUsedMarking;
  }

  private String mOutputName;

  private TIntArrayList mCompStack;
  private ExtendedTarjanControlStack mControlStack;
  private int mKnownNumberOfStates;
  private int mTransitionMode;

  private EventProxy mUsedMarking;
  private EventProxy mConfiguredMarking;


  private PreTransitionBuffer mPreTransitionBuffer;
  private final ProductDESProxyFactory mFactory;

  private MarkingInfo mMarkingInfo;

  private static final int EXPANDING = 0;
  private static final int SEARCHING = 1;
  private static final int FOUND = 2;
  private static final int BUILDING = 3;

  private static final int OPEN = 0;
  private static final int EXPANDED = 0x80000000;
  private static final int CLOSED_GOOD = -1;
  private static final int CLOSED_BAD = -2;

}

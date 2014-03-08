package org.supremica.automata.algorithms.LBSC;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAState;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * A transition iterator for EFAs.
 * <p>
 * @author Mohammad Reza Shoaei
 */
public class EFAIterator implements TransitionIterator
{

  private final ListBufferTransitionRelation mRel;
  private final TransitionIterator mIterator;
  private final SimpleEFATransitionLabelEncoding mLabelEncoding;
  private final TIntObjectHashMap<SimpleEFAEventDecl> mLabelEventMap;
  private final THashSet<SimpleEFAEventDecl> mAlphabet;
  private final int mInitialState;
  private Iterator<int[]> mInnerIter;
  private int mCurrState;
  private ArrayList<int[]> mCurrTrans;
  private int[] mCurrTran;
  private final SimpleEFAComponent mEFA;

  /**
   * Constructor of the iterator
   * <p>
   * @param efa An EFA to iterate on
   */
  public EFAIterator(final SimpleEFAComponent efa)
  {
    mEFA = efa;
    mAlphabet = new THashSet<>(efa.getAlphabet());
    mRel = efa.getTransitionRelation();
    mRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mIterator = mRel.createSuccessorsReadOnlyIterator();
    mLabelEncoding = efa.getTransitionLabelEncoding();
    mLabelEventMap = mLabelEncoding.getTranLabelToEventMap();
    mInitialState = getInitState();
    mCurrState = mInitialState;
    mCurrTrans = null;
    mCurrTran = null;
    mInnerIter = null;
  }

  @Override
  public void reset(){
    mIterator.resetState(mCurrState);
    mCurrTrans = getCurrTrans();
    mInnerIter = mCurrTrans.iterator();
  }

  private ArrayList<int[]> getCurrTrans() {
    final ArrayList<int[]> trs = new ArrayList<>();
    while (mIterator.advance()){
      final int[] tr = {mIterator.getCurrentSourceState(),
              mIterator.getCurrentEvent(),
              mIterator.getCurrentTargetState()};
      trs.add(tr);
    }
    return trs;
  }

  @Override
  public void resetEvent(final int label) {
    mIterator.resetEvent(label);
    mCurrTrans = getCurrTrans();
    mInnerIter = mCurrTrans.iterator();
  }

  public void resetEvent(final SimpleEFAEventDecl event) {
    final ArrayList<int[]> trs = new ArrayList<>();
    if (mAlphabet.contains(event)) {
      final int[] lbs = mLabelEncoding.getTransitionLabelIdByEvent(event);
      for (final int lb : lbs) {
        mIterator.resetEvent(lb);
        trs.addAll(getCurrTrans());
      }
    }
    mCurrTrans = trs;
    mInnerIter = mCurrTrans.iterator();
  }

  @Override
  public void resetEvents(final int first, final int last) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public void resetEventsByStatus(final int... flags) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public void resetState(final int from) {
    mCurrState = from;
    reset();
  }

  @Override
  public void reset(final int from, final int label) {
    mCurrState = from;
    mIterator.reset(from, label);
    mCurrTrans = getCurrTrans();
    mInnerIter = mCurrTrans.iterator();
  }

  public void reset(final int from, final SimpleEFAEventDecl event)
  {
    final ArrayList<int[]> trs = new ArrayList<>();
    if (mAlphabet.contains(event)) {
      resetState(from);
      for (final int[] tr : mCurrTrans) {
        final SimpleEFAEventDecl currEvent = mLabelEventMap.get(tr[1]);
        if (currEvent.equals(event)) {
          trs.add(tr);
        }
      }
    }
    mCurrTrans = trs;
    mInnerIter = mCurrTrans.iterator();

  }

  @Override
  public void resume(final int from) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public boolean advance() {
    if (!mCurrTrans.isEmpty() && mInnerIter.hasNext()) {
      mCurrTran = mInnerIter.next();
      return true;
    }
    return false;
  }

  @Override
  public int getCurrentSourceState() {
    return mCurrTran[0];
  }

  @Override
  public int getCurrentFromState() {
    return mCurrTran[0];
  }

  @Override
  public int getCurrentEvent() {
    return mCurrTran[1];
  }

  public SimpleEFAEventDecl getCurrentSimpleDeclEvent() {
    return mLabelEventMap.get(mCurrTran[1]);
  }

  public ConstraintList getCurrentConstraints() {
    final int currLabel = getCurrentEvent();
    return mLabelEncoding.getTransitionLabel(currLabel).getConstraint();
  }

  @Override
  public int getCurrentTargetState() {
    return mCurrTran[2];
  }

  @Override
  public int getCurrentToState() {
    return mCurrTran[2];
  }

  @Override
  public void setCurrentToState(final int state) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public boolean isEnabled(final int label)
  {
    for (final int[] tr : mCurrTrans){
      if (tr[1] == label){
        return true;
      }
    }
    return false;
  }

  public boolean isEnabled(final int source, final SimpleEFAEventDecl event)
  {
    if (!mAlphabet.contains(event)) {
      return true;
    }
    resetState(source);
    final TIntArrayList labels = new TIntArrayList(mLabelEncoding.getTransitionLabelIdByEvent(event));
    for (final int[] tr : mCurrTrans){
      if (labels.contains(tr[1])){
        return true;
      }
    }
    return false;
  }

  private int getInitState() {
    for (int s = 0; s < mRel.getNumberOfStates(); s++) {
      if (mRel.isInitial(s)) {
        return s;
      }
    }
    return -1;
  }

  public int getInitialState() {
    return mInitialState;
  }

  public HashSet<SimpleEFAEventDecl> getEnabledEvents(){
    final HashSet<SimpleEFAEventDecl> events = new HashSet<>();
    for (final int[] tr : mCurrTrans){
      events.add(mLabelEventMap.get(tr[1]));
    }
    return events;
  }

  public SimpleEFAState getSimpleState(final int stateId)
  {
    return mEFA.getStateEncoding().getSimpleState(stateId);
  }

}

package org.supremica.automata.algorithms.LBSC;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAState;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

/**
 * A transition iterator for EFAs.
 * <p>
 * @author Mohammad Reza Shoaei
 */
public class EFAInterpreter implements TransitionIterator
{

  public EFAInterpreter(final SimpleEFAComponent efa,
                        final SimpleEFAVariableContext varContext)
  {
    mEFA = efa;
    mAlphabet = new THashSet<>(efa.getAlphabet());
    mRel = efa.getTransitionRelation();
    mRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mIterator = mRel.createSuccessorsReadOnlyIterator();
    mLabelEncoding = efa.getTransitionLabelEncoding();
    mLabelEventMap = mLabelEncoding.getTranLabelToEventMap();
    mPropagator = new ConstraintPropagator(ModuleElementFactory.getInstance(),
                                           CompilerOperatorTable.getInstance(),
                                           varContext);
    mCurrState = getInitState();
    mCurrValue = getInitialValue(efa.getVariables());
    mCurrTrans = null;
    mCurrTran = null;
    mInnerIter = null;
  }

  @Override
  public void reset(){
    mIterator.resetState(mCurrState);
    mCurrTrans = getCurrTrans();
    mCurrValue = ConstraintList.TRUE;
    mPropagator.reset();
    mInnerIter = mCurrTrans.iterator();
  }

  public void reset(final ConstraintList value)
  {
    mIterator.resetState(mCurrState);
    mCurrValue = value;
    mPropagator.reset();
    mPropagator.addConstraints(value);
    mCurrTrans = getCurrTrans();
    mInnerIter = mCurrTrans.iterator();
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

  public void resetState(final int from, final ConstraintList value)
  {
    mCurrState = from;
    reset(value);
  }

  @Override
  public void reset(final int from, final int label) {
    mCurrState = from;
    mPropagator.reset();
    mIterator.reset(from, label);
    mCurrTrans = getCurrTrans();
    mInnerIter = mCurrTrans.iterator();
  }

  public void reset(final int from, final int label, final ConstraintList value)
  {
    mCurrState = from;
    mCurrValue = value;
    mPropagator.reset();
    mPropagator.addConstraints(value);
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
    if (mInnerIter.hasNext()) {
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

  public ConstraintList getNextValue()
  {
    mPropagator.reset();
    mPropagator.addConstraints(mCurrValue);
    mPropagator.addConstraints(getCurrentConstraints());
    ConstraintList result = null;
    try {
      mPropagator.propagate();
      result = mPropagator.getAllConstraints();
    } catch (final EvalException ex) {
    }
    return result;
  }

  public SimpleEFAEventDecl getCurrentSimpleDeclEvent() {
    return mLabelEventMap.get(mCurrTran[1]);
  }

  public ConstraintList getCurrentConstraints() {
    return mLabelEncoding.getTransitionLabel(getCurrentEvent()).getConstraint();
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

  public THashSet<SimpleEFAEventDecl> getEnabledEvents()
  {
    final THashSet<SimpleEFAEventDecl> events = new THashSet<>();
    for (final int[] tr : mCurrTrans){
      events.add(mLabelEventMap.get(tr[1]));
    }
    return events;
  }

  public SimpleEFAState getSimpleState(final int stateId)
  {
    return mEFA.getStateEncoding().getSimpleState(stateId);
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

  private int getInitState() {
    for (int s = 0; s < mRel.getNumberOfStates(); s++) {
      if (mRel.isInitial(s)) {
        return s;
      }
    }
    return -1;
  }
  private ConstraintList getInitialValue(
   final Collection<SimpleEFAVariable> vars)
  {
    final List<SimpleExpressionProxy> inits = new ArrayList<>(vars.size());
    for (final SimpleEFAVariable var : vars) {
      final SimpleExpressionProxy exp = var.getInitialStatePredicate();
      inits.add(exp);
    }
    return new ConstraintList(inits);
  }

  private final ListBufferTransitionRelation mRel;
  private final TransitionIterator mIterator;
  private final SimpleEFATransitionLabelEncoding mLabelEncoding;
  private final TIntObjectHashMap<SimpleEFAEventDecl> mLabelEventMap;
  private final THashSet<SimpleEFAEventDecl> mAlphabet;
  private Iterator<int[]> mInnerIter;
  private int mCurrState;
  private ArrayList<int[]> mCurrTrans;
  private int[] mCurrTran;
  private final SimpleEFAComponent mEFA;
  private ConstraintList mCurrValue;
  private final ConstraintPropagator mPropagator;

}

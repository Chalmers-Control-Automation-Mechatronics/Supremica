
package org.supremica.automata.algorithms.LBSC;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAState;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFATransitionLabel;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * A simple but not efficient implementation of synchronous composition for EFAs
 * <p>
 * @author Mohammad Reza Shoaei
 */
public class EFASynchronizer
{

  public EFASynchronizer(final List<SimpleEFAComponent> components)
  {
    mComponents = components;
    mSize = components != null ? components.size() : 0;
    mTR = new ArrayList<>();
    mStateSpace = new TObjectIntHashMap<>(50, 0.75f, -1);
    mStateEncoding = new SimpleEFAStateEncoding();
    mLabelEncoding = new SimpleEFATransitionLabelEncoding();
    mEFAIterators = new ArrayList<>(mSize);
    mInitialState = new TIntArrayList(mSize, -1);
    mSharedEvents = new THashSet<>();
    mPrimedVars = new THashSet<>();
    mUnprimedVars = new THashSet<>();
    mStateVars = new THashSet<>();
    mBlockedEvents = new THashSet<>();
    mAlphabet = new THashSet<>();
    mSynchEFA = null;
  }

  /**
   * Run the synchronizer
   * <p>
   * @return true if the synchronization is successfully finished, false
   *         otherwise.
   */
  public boolean run()
  {
    if (mSize < 2) {
      return false;
    }
    initialize();
    final Stack<TIntArrayList> stack = new Stack<>();
    stack.push(mInitialState);
    mStateSpace.put(mInitialState, getStateId(mInitialState));
    System.err.println("Start synching ... ");
    while (!stack.isEmpty()) {
      final TIntArrayList source = stack.pop();
      for (final SimpleEFAEventDecl event : mAlphabet) {
        final TIntArrayList target = step(source, event);
        if (target != null) {
          stack.push(target);
        }
      }
    }
    System.err.println("Finish synching ... ");
    return true;
  }

  private TIntArrayList step(final TIntArrayList source, final SimpleEFAEventDecl event)
  {
    // If the event is shared but disabled by one of the components then we stay at current state
    final boolean enabled = isEnabled(source, event);
    if (mSharedEvents.contains(event) && !enabled) {
      return null;
    }

    final TIntArrayList target = new TIntArrayList(source);
    ConstraintList newCon = ConstraintList.TRUE;
    boolean isFired = false;
    for (int id = 0; id < mSize; id++) {
      final EFAIterator iter = mEFAIterators.get(id);
      iter.reset(source.get(id), event);
      // Assuming deterministic EFA, i.e., there are no two transitions with the same source and event
      // but two different target locations
      if (iter.advance()) {
        isFired = true;
        target.set(id, iter.getCurrentTargetState());
        // The new condition is a list consists of each component's condition
        newCon = SimpleEFAHelper.merge(newCon, iter.getCurrentConstraints());
      }
    }

    if (!isFired) {
      return null;
    }

    boolean isNewState = false;
    int targetId = mStateSpace.get(target);
    if (targetId < 0) {
      targetId = getStateId(target);
      mStateSpace.put(target, targetId);
      isNewState = true;
    }

    final int[] tr = {mStateSpace.get(source),
            mLabelEncoding.createTransitionLabelId(
                    new SimpleEFATransitionLabel(event, newCon)),
                         targetId};

    mTR.add(tr);

    return isNewState ? target : null;
  }

  private int getStateId(final TIntArrayList state)
  {

    boolean isForbidden = false;
    boolean isMarked = true;
    boolean isInitial = true;
    String name = "";
    THashMap<String, String> attribute = new THashMap<>();
    String stateValues = "";
    for (int s = 0; s < mSize; s++) {
      final EFAIterator iter = mEFAIterators.get(s);
      final SimpleEFAState simpleState = iter.getSimpleState(state.get(s));
      attribute = SimpleEFAHelper.merge(attribute, simpleState.getAttributes(),
                                        SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
      stateValues += SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR + simpleState
       .getStateValue();
      if (simpleState.isForbidden()) {
        isForbidden = true;
        mUsesForbidden = true;
      }
      if (!simpleState.isInitial()) {
        isInitial = false;
      }
      if (!simpleState.isMarked()) {
        isMarked = false;
      }
      name += simpleState.getName() + '.';
    }
    //String name = "S" + mStateSpace.size();
    name = name.substring(0, name.length() - 1);
    final SimpleEFAState st = new SimpleEFAState(name, isInitial, isMarked,
                                                 isForbidden, attribute);
    st.setStateValue(stateValues.substring(1));
    return mStateEncoding.createSimpleStateId(st);
  }

  private boolean isEnabled(final TIntArrayList source, final SimpleEFAEventDecl event)
  {
    for (int i = 0; i < mSize; i++) {
      final EFAIterator iter = mEFAIterators.get(i);
      if (!iter.isEnabled(source.get(i), event)) {
        return false;
      }
    }
    return true;
  }

  private void initialize()
  {
    int sp = 1;
    for (final SimpleEFAComponent efa : mComponents) {
      sp *= efa.getStateSet().size();
      final EFAIterator iter = new EFAIterator(efa);

      iter.reset();
      mEFAIterators.add(iter);
      mInitialState.add(iter.getInitialState());
      final THashSet<SimpleEFAEventDecl> currEvents = new THashSet<>(efa
       .getAlphabet());
      final THashSet<SimpleEFAEventDecl> otherEvents = new THashSet<>();
      for (final SimpleEFAComponent other : mComponents) {
        if (!efa.equals(other)) {
          otherEvents.addAll(other.getAlphabet());
        }
      }
      currEvents.retainAll(otherEvents);
      mSharedEvents.addAll(currEvents);
      if (efa.getMarkedStates().isEmpty()) {
        mUsesMarking = false;
      }
      mPrimedVars.addAll(efa.getPrimeVariables());
      mUnprimedVars.addAll(efa.getUnprimeVariables());
      mStateVars.addAll(efa.getStateVariables());
      mBlockedEvents.addAll(efa.getBlockedEvents());
      mAlphabet.addAll(efa.getAlphabet());
      mName += efa.getName() + "||";
    }
    mStateSpace.setUp(Math.round(sp * 0.25f));
    mName = mName.substring(0, mName.length() - 2);
  }

  @SuppressWarnings("unused")
  private THashSet<SimpleEFAEventDecl> getEnabledEvents(
   final TIntArrayList state)
  {
    final THashSet<SimpleEFAEventDecl> events = new THashSet<>();
    for (int i = 0; i < mSize; i++) {
      final EFAIterator iter = mEFAIterators.get(i);
      iter.resetState(state.get(i));
      events.addAll(iter.getEnabledEvents());
    }
    return events;
  }

  /**
   * Constructing the synchronized EFA
   * <p>
   * @return A synch EFA
   * <p>
   * @throws OverflowException
   * @throws AnalysisException
   */
  public SimpleEFAComponent getSynchronizedEFA()
   throws OverflowException, AnalysisException
  {
    return getSynchronizedEFA(mName, ComponentKind.PLANT);
  }

  /**
   * Constructing the synchronized EFA
   * <p>
   * @param name
   * @param kind
   * <p>
   * @return
   * <p>
   * @throws OverflowException
   * @throws AnalysisException
   */
  public SimpleEFAComponent getSynchronizedEFA(final String name, final ComponentKind kind)
   throws OverflowException, AnalysisException
  {
    final int sNumPropositions = (mUsesMarking ? 1 : 0) + (mUsesForbidden ? 1 : 0);
    final ListBufferTransitionRelation sRel = createTransitionRelation(name,
                                                                 kind,
            mLabelEncoding.size(),
                                sNumPropositions,
            mStateEncoding.size());

    // Creating a residual EFA.
    final THashSet<SimpleEFAVariable> vars = new THashSet<>(mPrimedVars);
    vars.addAll(mUnprimedVars);
    mSynchEFA = new SimpleEFAComponent(name, vars,
                                       mStateEncoding,
                                       mLabelEncoding,
                                       mBlockedEvents,
                                       sRel, kind, null);

    mSynchEFA.setStructurallyDeterministic(true);
    // Setting the visitor / modifiers of the variables
    mSynchEFA.setPrimeVariables(new ArrayList<>(mPrimedVars));
    mSynchEFA.setUnprimeVariables(new ArrayList<>(mUnprimedVars));
    mSynchEFA.setStateVariables(new ArrayList<>(mStateVars));
    mSynchEFA.setIsEFA(!vars.isEmpty());
    mSynchEFA.register();
    return mSynchEFA;
  }

  private ListBufferTransitionRelation createTransitionRelation(
   final String sComponentName,
   final ComponentKind kind,
   final int nbrLabels,
   final int nbrPropositions,
   final int nbrStates)
   throws OverflowException
  {
    final ListBufferTransitionRelation sRel
     = new ListBufferTransitionRelation(sComponentName, kind, nbrLabels,
                                        nbrPropositions, nbrStates,
                                        ListBufferTransitionRelation.CONFIG_SUCCESSORS);

    for (final SimpleEFAState state : mStateEncoding.getSimpleStates()) {
      final int code = mStateEncoding.getStateId(state);
      if (state.isInitial()) {
        sRel.setInitial(code, true);
      }
      if (mUsesMarking && state.isMarked()) {
        sRel.setMarked(code, SimpleEFAHelper.DEFAULT_MARKING_ID, true);
      }
      if (mUsesForbidden && state.isForbidden()) {
        sRel.setMarked(code, SimpleEFAHelper.DEFAULT_FORBIDDEN_ID, true);
      }
    }

    for (final int[] tr : mTR) {
      sRel.addTransition(tr[0], tr[1], tr[2]);
    }
    return sRel;
  }


  private final List<SimpleEFAComponent> mComponents;
  private SimpleEFAComponent mSynchEFA;
  private final TObjectIntHashMap<TIntArrayList> mStateSpace;
  private final TIntArrayList mInitialState;
  private final ArrayList<int[]> mTR;
  private final ArrayList<EFAIterator> mEFAIterators;
  private final int mSize;
  private final THashSet<SimpleEFAEventDecl> mSharedEvents;
  private final SimpleEFAStateEncoding mStateEncoding;
  private boolean mUsesMarking = true;
  private boolean mUsesForbidden = false;
  private final SimpleEFATransitionLabelEncoding mLabelEncoding;
  private final Collection<SimpleEFAVariable> mPrimedVars;
  private final Collection<SimpleEFAVariable> mUnprimedVars;
  private final Collection<SimpleEFAVariable> mStateVars;
  private final Collection<SimpleEFAEventDecl> mBlockedEvents;
  private final Collection<SimpleEFAEventDecl> mAlphabet;
  private String mName = "";

}


package org.supremica.automata.algorithms.LBSC;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAState;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * A simple but not efficient implementation of synchronous composition for EFAs
 * <p>
 * @author Mohammad Reza Shoaei
 */
public class EFASynchronizer
{

  public EFASynchronizer()
  {
    mHelper = new SimpleEFAHelper();
    mComponents = new ArrayList<>();
  }

  public void init(final List<SimpleEFAComponent> components)
  {
    reset();
    mComponents.addAll(components);
  }

  public void init(final SimpleEFAComponent component)
  {
    reset();
    mComponents.add(component);
  }

  public void addComponent(final SimpleEFAComponent component)
  {
    mComponents.add(component);
  }

  public void reset()
  {
    mComponents.clear();
    mSynchEFA = null;
  }

  public void setEvaluationEnabled(final boolean enableEvaluation)
  {
    mIsEvaluationEnabled = enableEvaluation;
  }

  /**
   * Run the synchroniser
   * <p>
   * @return true if the synchronisation is successfully finished, false otherwise.
   * @throws net.sourceforge.waters.model.analysis.OverflowException
   */
  public boolean synch() throws AnalysisException
  {
    mSize = mComponents.size();
    if (mSize < 1) {
      return false;
    } else if (mSize < 2) {
      return true;
    }
    initialize();
    final Stack<TIntArrayList> stack = new Stack<>();
    stack.add(mInitialStates);
    mStateSpace.put(mInitialStates, getStateId(mInitialStates));
    System.err.println("Start synching ... ");
    while (!stack.isEmpty()) {
      final TIntArrayList source = stack.pop();
      final int[] toArray = mAlphabet.toArray();
      for (final int event : toArray) {
        final TIntArrayList target = step(source, event);
        if (target != null) {
          stack.push(target);
        }
      }
    }
    constructSynchEFA();
    mComponents.clear();
    mComponents.add(mSynchEFA);
    System.err.println("Finish synching ... ");
    return true;
  }

  /**
   * Returns the synchronised EFA
   * <p>
   * @return A synch EFA
   */
  public SimpleEFAComponent getSynchronizedEFA()
  {
    return mSynchEFA;
  }

  public void enableRegisterSynchronizedEFA(final boolean enable)
  {
    mRegister = enable;
  }

  /**
   * Returns the synchronised EFA
   * <p>
   * @param name
   * @param kind
   * <p>
   * @return
   */
  public SimpleEFAComponent getSynchronizedEFA(final String name, final ComponentKind kind)
  {
    mSynchEFA.setName(name);
    mSynchEFA.getTransitionRelation().setName(name);
    mSynchEFA.setKind(kind);
    mSynchEFA.getTransitionRelation().setKind(kind);
    return mSynchEFA;
  }

  @SuppressWarnings("unchecked")
  private ListBufferTransitionRelation createTransitionRelation() throws OverflowException
  {
    final int nbrPropositions = (mUsesMarking ? 1 : 0) + (mUsesForbidden ? 1 : 0);
    final ListBufferTransitionRelation sRel = new ListBufferTransitionRelation(mName,
                                                                               ComponentKind.PLANT,
                                                                               mLabelEncoding.size(),
                                                                               nbrPropositions,
                                                                               mStateEncoding.size(),
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
    for (final Object[] tr : mTR) {
      sRel.addTransition(mStateSpace.get(tr[0]),
                         (int) tr[1],
                         mStateSpace.get(tr[2]));
    }
    return sRel;
  }

  private void constructSynchEFA()
   throws AnalysisException
  {
    if (mSize < 1) {
      mSynchEFA = null;
      return;
    } else if (mSize < 2) {
      mSynchEFA = mComponents.get(0);
      return;
    }
    final ListBufferTransitionRelation sRel = createTransitionRelation();
    
    // Creating a residual EFA.
    final TIntHashSet vars = new TIntHashSet(mPrimedVars);
    vars.addAll(mUnprimedVars);
    mSynchEFA = new SimpleEFAComponent(mName, new TIntArrayList(vars), mVarContext, mStateEncoding,
                                       mLabelEncoding, sRel, new TIntArrayList(mBlockedEvents),
                                       ComponentKind.PLANT);
    
    mSynchEFA.setStructurallyDeterministic(true);
    // Setting the visitor / modifiers of the variables
    mSynchEFA.setPrimeVariables(new TIntArrayList(mPrimedVars));
    mSynchEFA.setUnprimeVariables(new TIntArrayList(mUnprimedVars));
    mSynchEFA.setStateVariables(new TIntArrayList(mStateVars));
    mSynchEFA.setIsEFA(!vars.isEmpty());
    if (mRegister) {
      mSynchEFA.register();
    }
  }

  private TIntArrayList step(final TIntArrayList source, final int event) throws AnalysisException
  {
    final TIntArrayList target = new TIntArrayList(source);
    final List<SimpleExpressionProxy> constraints = new ArrayList<>();
    final int nb = mEventComponent.get(event);
    int fired = 0;
    Outer:
    for (int id = 0; id < mSize; id++) {
      final List<Integer> lbs = mEventToLabelIds.get(id).get(event);
      if (lbs != null) {
        final SimpleEFATransitionLabelEncoding enc = mEFATrLbEns.get(id);
        final TransitionIterator iter = mEFAIters.get(id);
        for (final int lb : lbs) {
          iter.reset(source.get(id), lb);
          /**
           * It is assumed that given EFAs are deterministic, i.e., there are no two transitions
           * with the same source and event but two different target locations
           */
          if (iter.advance()) {
            fired++;
            final int currTS = iter.getCurrentTargetState();
            target.set(id, currTS);
            // The new condition is a list consists of each component's condition
            if (enc != null) {
              constraints.addAll(enc.getConstraintByLabelId(lb).getConstraints());
            }
            // Continue outer loop by the deterministic assumption
            continue Outer;
          }
        }
        // If it is fired by all the components who has this event
        if (fired == nb) {
          break;
        }
      }
      // If this is a shared events and did not fire by this component
      if (nb == mSize) {
        return null;
      }
    }
    // If this is a shared events but did not fire by one of the components
    if (fired < nb) {
      return null;
    }

    boolean isNewState = false;
    if (!mStateSpace.containsKey(target)) {
      mStateSpace.put(target, getStateId(target));
      isNewState = true;
    }
    if (mIsEvaluationEnabled && !constraints.isEmpty()) {
      final boolean sat = execute(mStateEncoding.getSimpleState(mStateSpace.get(source)),
                            mStateEncoding.getSimpleState(mStateSpace.get(target)),
                            constraints);
      if (!sat) {
        return null;
      }
    }

    final int labelId = mLabelEncoding.createTransitionLabelId(event, new ConstraintList(constraints));
    mTR.add(new Object[]{source, labelId, target});
    return isNewState ? target : null;
  }

  private int getStateId(final TIntArrayList state)
  {
    boolean isForbidden = false;
    boolean isMarked = true;
    boolean isInitial = true;
    String name = "";
    String stateValues = "";
    THashMap<String, String> attribute = null;
    for (int s = 0; s < mSize; s++) {
      final SimpleEFAStateEncoding stEnc = mEFAStEns.get(s);
      final SimpleEFAState simpleState = stEnc.getSimpleState(state.get(s));
      attribute = SimpleEFAHelper.merge(attribute, simpleState.getAttributes(),
                                        SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
      final String sv = simpleState.getStateValue();
      stateValues += sv.isEmpty() ? "" : SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR + sv;
      if (simpleState.isForbidden()) {
        isForbidden = true;
      }
      if (!simpleState.isInitial()) {
        isInitial = false;
      }
      if (!simpleState.isMarked()) {
        isMarked = false;
      }
      name += simpleState.getName() + '.';
    }
    name = name.substring(0, name.length() - 1);
    final SimpleEFAState st = new SimpleEFAState(name, isInitial, isMarked, isForbidden, attribute);
    if (!stateValues.isEmpty()) {
      st.setStateValue(stateValues.substring(1));
    }
    return mStateEncoding.createSimpleStateId(st);
  }

  private void initialize()
  {
    mVarContext = mComponents.get(0).getVariableContext();
    mEFAIters = new ArrayList<>(mSize);
    mEFATrLbEns = new ArrayList<>(mSize);
    mEFAStEns = new ArrayList<>(mSize);
    mEventComponent = new TIntIntHashMap(mSize * 2, 0.6f, 0, 0);
    mInitialStates = new TIntArrayList(mSize);
    mPrimedVars = new TIntHashSet();
    mUnprimedVars = new TIntHashSet();
    mStateVars = new TIntHashSet();
    mBlockedEvents = new TIntHashSet();
    mAlphabet = new TIntHashSet();
    mEventToLabelIds = new ArrayList<>(mSize);
    mUsesMarking = true;
    mUsesForbidden = false;
    int sp = 1;
    for (int i = 0; i < mSize; i++) {
      final SimpleEFAComponent efa = mComponents.get(i);
      sp *= efa.getNumberOfStates();
      final ListBufferTransitionRelation rel = efa.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mEFAStEns.add(efa.getStateEncoding());
      mEFAIters.add(rel.createSuccessorsReadOnlyIterator());
      if (efa.isEFA()) {
        mEFATrLbEns.add(efa.getTransitionLabelEncoding());
      } else {
        mEFATrLbEns.add(null);
      }
      mInitialStates.add(efa.getStateEncoding().getInitialStateId());
      final TIntArrayList events = efa.getEvents();
      mAlphabet.addAll(events);
      final TIntObjectHashMap<List<Integer>> map = new TIntObjectHashMap<>(events.size());
      for (final int event : events.toArray()) {
        map.put(event, efa.getTransitionLabelEncoding().getTransitionLabelIdsByEventId(event));
        final int value = mEventComponent.get(event) + 1;
        mEventComponent.put(event, value);
      }
      mEventToLabelIds.add(map);
      if (!efa.hasMarkedState()) {
        mUsesMarking = false;
      }
      if (efa.hasForbiddenState()) {
        mUsesForbidden = true;
      }
      mPrimedVars.addAll(efa.getPrimeVariables());
      mUnprimedVars.addAll(efa.getUnprimeVariables());
      mStateVars.addAll(efa.getStateVariables());
      final TIntArrayList blockedEvents = efa.getBlockedEvents();
      if (blockedEvents != null & !blockedEvents.isEmpty()) {
        mBlockedEvents.addAll(blockedEvents);
      }
      
      mName += efa.getName() + "||";
    }
    mName = mName.substring(0, mName.length() - 2);
    if (sp > Short.MAX_VALUE) {
      sp = Short.MAX_VALUE;
    }
    mTR = new ArrayList<>(sp);
    mStateSpace = new TObjectIntHashMap<>(sp);
    mLabelEncoding = new SimpleEFATransitionLabelEncoding(mComponents.get(0).getEventEncoding(), sp * 2);
    mStateEncoding = new SimpleEFAStateEncoding(sp);
    if (mIsEvaluationEnabled) {
      mPropagator = new ConstraintPropagator(SimpleEFAHelper.FACTORY, SimpleEFAHelper.OPTABLE,
                                             mVarContext);
    } else {
      mPropagator = null;
    }
  }

  private boolean execute(final SimpleEFAState source, final SimpleEFAState target,
                          final List<SimpleExpressionProxy> constraints)
   throws AnalysisException
  {
    final String AND = mHelper.getOperatorTable().getAndOperator().getName();
    final String NEXT = mHelper.getOperatorTable().getNextOperator().getName();
    String exp1 = source.getStateValue();
    if (!exp1.isEmpty()) {
      exp1 = exp1.replace(SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR, AND);
    }
    final String exp2 = mHelper.printer(constraints, "", AND, "");
    String exp3 = target.getStateValue();
    if (!exp3.isEmpty()) {
      for (final SimpleEFAVariable var : mVarContext.getVariables()) {
        exp3 = exp3.replace(var.getName(), var.getName() + NEXT);
      }
    } else if (exp1.isEmpty()) {
      return true;
    }
    final String exp = exp1 + AND + exp2 + (exp3.isEmpty() ? "" : AND + exp3);
    final List<SimpleExpressionProxy> parse = mHelper.parse(exp);
    mPropagator.reset();
    mPropagator.addConstraints(parse);
    return !mPropagator.isUnsatisfiable();
  }

  private List<SimpleEFAComponent> mComponents;
  private SimpleEFAComponent mSynchEFA;
  private TObjectIntHashMap<TIntArrayList> mStateSpace;
  private TIntArrayList mInitialStates;
  private ArrayList<Object[]> mTR;
  private ArrayList<TransitionIterator> mEFAIters;
  private int mSize;
  private SimpleEFAStateEncoding mStateEncoding;
  private boolean mUsesMarking;
  private boolean mUsesForbidden;
  private SimpleEFATransitionLabelEncoding mLabelEncoding;
  private TIntHashSet mPrimedVars;
  private TIntHashSet mUnprimedVars;
  private TIntHashSet mStateVars;
  private TIntHashSet mBlockedEvents;
  private TIntHashSet mAlphabet;
  private String mName = "";
  private boolean mRegister = true;
  private ArrayList<SimpleEFATransitionLabelEncoding> mEFATrLbEns;
  private ArrayList<TIntObjectHashMap<List<Integer>>> mEventToLabelIds;
  private ArrayList<SimpleEFAStateEncoding> mEFAStEns;
  private SimpleEFAVariableContext mVarContext;
  private TIntIntHashMap mEventComponent;
  private boolean mIsEvaluationEnabled;
  private final SimpleEFAHelper mHelper;
  private ConstraintPropagator mPropagator;
}

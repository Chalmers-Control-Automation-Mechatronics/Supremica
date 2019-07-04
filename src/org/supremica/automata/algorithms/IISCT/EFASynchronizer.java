
package org.supremica.automata.algorithms.IISCT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFALabelEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.AttributeMapSubject;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

/**
 * A simple but not efficient implementation of synchronous composition for EFAs
 * <p>
 *
 * @author Mohammad Reza Shoaei
 */
public class EFASynchronizer {

	public EFASynchronizer(final int mode) {
		mHelper = new SimpleEFAHelper();
		mComponents = new ArrayList<>();
		mMode = mode;
	}

	public EFASynchronizer() {
		this(MODE_NORMAL);
	}

	public void init(final List<SimpleEFAComponent> components) {
		reset();
		mComponents.addAll(components);
	}

	public void init(final SimpleEFAComponent component) {
		reset();
		if (component != null) {
			mComponents.add(component);
		}
	}

	public void addComponent(final SimpleEFAComponent component) {
		if (component != null) {
			mComponents.add(component);
		}
	}

	public void setEvaluationEnabled(final boolean enableEvaluation) {
		mIsEvaluationEnabled = enableEvaluation;
	}

	/**
	 * Setting the synchronization mode. The default mode is 0. Mode 0: Normal
	 * synchronization. Plants and specifications are not distinguish. Mode 1:
	 * IPL mode. Mode 2: SCT mode. (TODO)
	 * <p>
	 *
	 * @param mode
	 */
	public void setMode(final int mode) {
		mMode = mode;
	}

	/**
	 * Set to attach specification info to locations
	 * <p>
	 *
	 * @param attach
	 */
	public void setAttachSpecToLocations(final boolean attach) {
		mAttachSpecToLoc = attach;
	}

	/**
	 * Run the synchronizer and construct the synchronized model.
	 * <p>
	 *
	 * @return true if the synchronization is successfully finished, false
	 *         otherwise.
	 *         <p>
	 * @throws net.sourceforge.waters.model.analysis.OverflowException
	 */
	public boolean synchronize() throws AnalysisException {
		lowSynch();
		if (mSize < 1) {
			return false;
		} else if (mSize < 2) {
			return true;
		}
		constructSynchEFA();
		mComponents.clear();
		mComponents.add(mSynchEFA);
		return true;
	}

	/**
	 * Run the synchronizer but does not construct the synchronized model.
	 * <p>
	 *
	 * @return true if the synchronization is successfully finished, false
	 *         otherwise.
	 *         <p>
	 * @throws AnalysisException
	 */
	public boolean lowSynch() throws AnalysisException {
		mSize = mComponents.size();
		if (mSize < 1) {
			mSynchEFA = null;
			return false;
		} else if (mSize < 2) {
			mSynchEFA = mComponents.get(0);
			mStateEncoding = mSynchEFA.getStateEncoding();
			mVarContext = mSynchEFA.getVariableContext();
			mLabelEncoding = mSynchEFA.getTransitionLabelEncoding();
			mRel = mSynchEFA.getTransitionRelation();
			mPrimedVars = new TIntHashSet(mSynchEFA.getPrimeVariables());
			mUnprimedVars = new TIntHashSet(mSynchEFA.getUnprimeVariables());
			return true;
		}
		System.err.println("Start synching ... ");
		initialize();
		switch (mMode) {
		case MODE_NORMAL:
			// case 0 is the same as default
		case MODE_IISCT:
			// setAttachSpecToLocations(true);
			synch(mInitialStates);
			break;
		default:
			mContEvents.addAll(mUnContEvents);
			mUnContEvents.clear();
			synch(mInitialStates);
		}
		createTransitionRelation();
		System.err.println("Finish synching ... ");
		return true;
	}

	public ListBufferTransitionRelation getLowTransitionRelation() {
		return mRel;
	}

	public int[] getLowVars() {
		final TIntHashSet vars = new TIntHashSet(mPrimedVars);
		vars.addAll(mUnprimedVars);
		return vars.toArray();
	}

	public List<SimpleEFAVariable> getLowVarContext() {
		return mVarContext.getVariables(getLowVars());
	}

	public SimpleEFAStateEncoding getLowStateEncoding() {
		return mStateEncoding;
	}

	public HashMap<Integer, SimpleExpressionProxy> getLowLbToSpecMap() {
		return mLbToSpecMap;
	}

	public SimpleEFALabelEncoding getLowLabelEncoding() {
		return mLabelEncoding;
	}

	public SimpleEFAVariableContext getVarContext() {
		return mVarContext;
	}

	/**
	 * Returns the components.
	 */
	public List<SimpleEFAComponent> getComponents() {
		return mComponents;
	}

	/**
	 * Returns the synchronized EFA.
	 */
	public SimpleEFAComponent getSynchronizedEFA() {
		return getSynchronizedEFA(null, null);
	}

	public void enableRegisterSynchronizedEFA(final boolean enable) {
		mRegister = enable;
	}

	/**
	 * Returns the synchronized EFA.
	 */
	public SimpleEFAComponent getSynchronizedEFA(final String name, final ComponentKind kind) {
		if (name != null && !name.isEmpty()) {
			mSynchEFA.setName(name);
			mSynchEFA.getTransitionRelation().setName(name);
		}
		if (kind != null) {
			mSynchEFA.setKind(kind);
			mSynchEFA.getTransitionRelation().setKind(kind);
		}
		return mSynchEFA;
	}

	private void createTransitionRelation() throws OverflowException {
		final int nbrPropositions = (mUsesMarking ? 1 : 0) + (mUsesForbidden ? 1 : 0);
		mRel = new ListBufferTransitionRelation(mName, mKind, mLabelEncoding.size(),
				nbrPropositions, mStateEncoding.size(),
				ListBufferTransitionRelation.CONFIG_SUCCESSORS);

		mStateEncoding.getSimpleStates().stream().sequential().forEach((state) -> {
			final int stateId = mStateEncoding.getStateId(state);
			final boolean isMarked = mStateEncoding.isMarked(stateId);
			final boolean isForbidden = mStateEncoding.isForbidden(stateId);
			if (state.isInitial()) {
				mRel.setInitial(stateId, true);
			}
			if (mUsesMarking && isMarked) {
				mRel.setMarked(stateId, SimpleEFAHelper.DEFAULT_MARKING_ID, true);
			}
			if (mUsesForbidden && isForbidden) {
				mRel.setMarked(stateId, SimpleEFAHelper.DEFAULT_FORBIDDEN_ID, true);
			}
		});
		mTR.stream().sequential().forEach((tr) -> {
			mRel.addTransition(mStateSpace.get(tr[0]), (int) tr[1], mStateSpace.get(tr[2]));
		});
	}

	private void constructSynchEFA() throws AnalysisException {
		if (mSize < 1) {
			mSynchEFA = null;
			return;
		} else if (mSize < 2) {
			mSynchEFA = mComponents.get(0);
			return;
		}

		createTransitionRelation();

		// Creating a residual EFA.
		final TIntHashSet vars = new TIntHashSet(mPrimedVars);
		vars.addAll(mUnprimedVars);
		mSynchEFA = new SimpleEFAComponent(mName, vars.toArray(), mVarContext, mStateEncoding,
				mLabelEncoding, mRel, mBlockedEvents.toArray(), mKind);

		mSynchEFA.setStructurallyDeterministic(true);
		// Setting the visitor / modifiers of the variables
		mSynchEFA.setPrimeVariables(mPrimedVars.toArray());
		mSynchEFA.setUnprimeVariables(mUnprimedVars.toArray());
		mSynchEFA.setIsEFA(!vars.isEmpty());
		if (mRegister) {
			mSynchEFA.register();
		}
	}

	/**
	 * Performing IPL synchronization (mode 1).
	 * <p>
	 *
	 * @param initialSet
	 *            <p>
	 * @throws AnalysisException
	 */
	private void synch(final TIntArrayList initialSet) throws AnalysisException {
		final Stack<TIntArrayList> stack = new Stack<>();
		stack.add(initialSet);
		mStateSpace.put(initialSet, createSimpleEFAState(initialSet));
		while (!stack.isEmpty()) {
			final TIntArrayList source = stack.pop();
			final ArrayList<Object[]> targets = new ArrayList<>();
			for (final int event : mUnContEvents.toArray()) {
				targets.add(uStep(source, event));
			}
			for (final int event : mContEvents.toArray()) {
				targets.add(cStep(source, event));
			}
			final List<TIntArrayList> mkTargets = mkTargets(targets);
			mkTargets.stream().forEach(target -> stack.push(target));
		}
	}

	// Synchronization of transitions with controllable events
	private Object[] cStep(final TIntArrayList source, final int event) throws AnalysisException {
		final TIntArrayList target = new TIntArrayList(source);
		final List<SimpleExpressionProxy> constraints = new ArrayList<>();
		final int nb = mEventPlant.get(event) + mEventSpec.get(event);
		int fired = 0;
		Outer: for (int id = 0; id < mSize; id++) {
			final TIntArrayList lbs = mEventToLabelIds.get(id).get(event);
			if (lbs != null) {
				final SimpleEFALabelEncoding enc = mEFATrLbEns.get(id);
				final TransitionIterator iter = mEFAIters.get(id);
				for (final int lb : lbs.toArray()) {
					iter.reset(source.get(id), lb);
					/**
					 * It is assumed that given EFAs are 'structurally' deterministic, i.e.,
					 * there are no two transitions with the same source and
					 * event but two different target locations even if the conjunction of
					 * the guards are logically false.
					 */
					if (iter.advance()) {
						fired++;
						target.set(id, iter.getCurrentTargetState());
						// The new condition is a list consists of each
						// component's condition
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
		// If either this is a shared events but did not fire by one of the
		// components
		// or no one fired this event
		if (fired < nb || fired == 0) {
			return null;
		}
		return new Object[] { source, target, event, constraints, null };
	}

	// Synchronization of transitions with uncontrollable events
	private Object[] uStep(final TIntArrayList source, final int event) throws AnalysisException {
		final TIntArrayList target = new TIntArrayList(source);
		final int nbp = mEventPlant.get(event);
		final int nbs = mEventSpec.get(event);
		final List<SimpleExpressionProxy> plantCons = new ArrayList<>();
		List<SimpleExpressionProxy> specCons = new ArrayList<>();
		int firedBySpec = 0;
		int firedByPlant = 0;
		Outer: for (int id = 0; id < mSize; id++) {
			final TIntArrayList lbs = mEventToLabelIds.get(id).get(event);
			final int kind = mComponentToKind.get(id);
			if (lbs != null) {
				final SimpleEFALabelEncoding enc = mEFATrLbEns.get(id);
				final TransitionIterator iter = mEFAIters.get(id);
				for (final int lb : lbs.toArray()) {
					iter.reset(source.get(id), lb);
					/**
					 * It is assumed that given EFAs are deterministic, i.e.,
					 * there are no two transitions with the same source and
					 * event but two different target locations
					 */
					if (iter.advance()) {
						switch (kind) {
						case PLANT_CODE:
							firedByPlant++;
							if (enc != null) {
								plantCons.addAll(enc.getConstraintByLabelId(lb).getConstraints());
							}
							break;
						case SPEC_CODE:
							firedBySpec++;
							if (enc != null) {
								specCons.addAll(enc.getConstraintByLabelId(lb).getConstraints());
							}
							break;
						default:
							throw new AnalysisException("Unsupported component kind: " + kind);
						}
						final int currTS = iter.getCurrentTargetState();
						target.set(id, currTS);
						// Continue outer loop by the deterministic assumption
						continue Outer;
					}
				}
				// If it is fired by all the components who has this event
				if (firedByPlant + firedBySpec == nbp + nbs) {
					break;
				}
			}
		}
		// It is assumed that all components have the same uncontrollable
		// events!
		// Either it is disabled by plant or no one fired this event
		if (firedByPlant < nbp || firedByPlant + firedBySpec == 0) {
			return null;
		}
		if (firedBySpec < nbs) {
			specCons = FALSE;
		}
		return new Object[] { source, target, event, plantCons, specCons };
	}

	private List<TIntArrayList> mkTargets(final ArrayList<Object[]> targets) {
		return targets.stream().sequential().filter(info -> info != null)
				.map(info -> mkInternalState(info)).filter(target -> target != null)
				.collect(Collectors.toList());
	}

	/**
	 * info[]: 0 source, 1 target, 2 event, 3 plantCons, 4 specCons, 5 allFired
	 */
	@SuppressWarnings("unchecked")
	private TIntArrayList mkInternalState(final Object[] info) {
		final TIntArrayList source = (TIntArrayList) info[0];
		final TIntArrayList target = (TIntArrayList) info[1];
		final int event = (int) info[2];
		final List<SimpleExpressionProxy> plantCons = (List<SimpleExpressionProxy>) info[3];
		final List<SimpleExpressionProxy> specCons = (List<SimpleExpressionProxy>) info[4];
		boolean isNewState = false;
		if (!mStateSpace.containsKey(target)) {
			mStateSpace.put(target, createSimpleEFAState(target));
			isNewState = true;
		}
		if (mIsEvaluationEnabled && !plantCons.isEmpty()) {
			if (!execute(mStateSpace.get(source), mStateSpace.get(target), plantCons)) {
				return null;
			}
		}
		final int labelId = mLabelEncoding.createTransitionLabelId(event,
				new ConstraintList(plantCons));
		if (specCons != null && !specCons.isEmpty()) {
			mLbToSpecMap.put(labelId, mHelper.createExpression(specCons, mOp.getAndOperator()));
			if (mAttachSpecToLoc) {
				setStateLabel(mStateSpace.get(source), labelId, event, specCons);
			}
		}
		mTR.add(new Object[] { source, labelId, target });
		return (isNewState ? target : null);
	}

	private void setStateLabel(final int stateId, final int labelId, final int eventId,
			final List<SimpleExpressionProxy> exp) {
		mStateEncoding.mergeToAttribute(stateId, SimpleEFAHelper.DEFAULT_SPEC_KEY,
				labelId + SimpleEFAHelper.DEFAULT_SPEC_TO
						+ mEventEncoding.getEventDecl(eventId).getName()
						+ SimpleEFAHelper.DEFAULT_SPEC_TO
						+ SimpleEFAHelper.printer(exp, mOp.getAndOperator().getName()));
	}

	private int createSimpleEFAState(final TIntArrayList state) {
		boolean isForbidden = false;
		boolean isMarked = true;
		boolean isInitial = true;
		String name = "";
		final AttributeMapSubject attribute = new AttributeMapSubject();
		for (int s = 0; s < mSize; s++) {
			final SimpleEFAStateEncoding stEnc = mEFAStEns.get(s);
			final int stateId = state.get(s);
			SimpleEFAHelper.merge(attribute, stEnc.getAttributes(stateId),
					SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
			if (stEnc.isForbidden(stateId)) {
				isForbidden = true;
			}
			if (!stEnc.isInitial(stateId)) {
				isInitial = false;
			}
			if (!stEnc.isMarked(stateId)) {
				isMarked = false;
			}
			name += stEnc.getSimpleState(stateId).getName() + '.';
		}
		name = name.substring(0, name.length() - 1);
		final SimpleNodeProxy node = mHelper.getSimpleNodeSubject(name, isInitial, isMarked,
				isForbidden, attribute);
		return mStateEncoding.createSimpleStateId(node);
	}

	private void initialize() {
		Collections.sort(mComponents, SimpleEFAComponent.KindComparator);
		mVarContext = mComponents.get(0).getVariableContext();
		mEventEncoding = mComponents.get(0).getEventEncoding();
		mRel = null;
		mEFAIters = new ArrayList<>(mSize);
		mEFATrLbEns = new ArrayList<>(mSize);
		mEFAStEns = new ArrayList<>(mSize);
		mEventPlant = new TIntIntHashMap(mSize * 2, 0.6f, 0, 0);
		mEventSpec = new TIntIntHashMap(mSize * 2, 0.6f, 0, 0);
		mInitialStates = new TIntArrayList(mSize);
		mPrimedVars = new TIntHashSet();
		mUnprimedVars = new TIntHashSet();
		mBlockedEvents = new TIntHashSet();
		mContEvents = new TIntHashSet();
		mUnContEvents = new TIntHashSet();
		mEventToLabelIds = new ArrayList<>(mSize);
		mComponentToKind = new TIntIntHashMap(mSize, 0.8f, 0, 0);
		mLbToSpecMap = new HashMap<>();
		mUsesMarking = true;
		mUsesForbidden = false;
		int plantSize = 0;
		int specSize = 0;
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
			if (efa.getKind() == ComponentKind.SPEC) {
				mComponentToKind.put(i, SPEC_CODE);
				specSize++;
			} else {
				mComponentToKind.put(i, PLANT_CODE);
				plantSize++;
			}
			mInitialStates.add(efa.getStateEncoding().getInitialStateId());
			final TIntArrayList events = new TIntArrayList(efa.getEvents());
			for (final int event : events.toArray()) {
				if (mEventEncoding.isControllable(event)) {
					mContEvents.add(event);
				} else {
					mUnContEvents.add(event);
				}
			}
			final TIntObjectHashMap<TIntArrayList> map = new TIntObjectHashMap<>(events.size());
			final TIntArrayList eBlocked = new TIntArrayList(efa.getBlockedEvents());
			if (mMode != MODE_IISCT || efa.getKind() == ComponentKind.PLANT) {
				mBlockedEvents.addAll(eBlocked);
			}
			events.addAll(eBlocked);
			for (final int event : events.toArray()) {
				if (!eBlocked.contains(event)) {
					map.put(event,
							efa.getTransitionLabelEncoding().getTransitionLabelIdsByEventId(event));
				}
				final int value = mEventSpec.get(event) + 1;
				if (efa.getKind() == ComponentKind.SPEC) {
					mEventSpec.put(event, value);
				} else {
					mEventPlant.put(event, value);
				}
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
			mName += efa.getName() + "||";
		}
		mName = mName.substring(0, mName.length() - 2);
		if (sp > Short.MAX_VALUE) {
			sp = Short.MAX_VALUE;
		}
		mTR = new ArrayList<>(sp);
		mStateSpace = new TObjectIntHashMap<>(sp, 0.6f, -1);
		mLabelEncoding = new SimpleEFALabelEncoding(mEventEncoding, sp * 2);
		mStateEncoding = new SimpleEFAStateEncoding(sp);
		if (mIsEvaluationEnabled) {
			mPropagator = new ConstraintPropagator(SimpleEFAHelper.FACTORY, SimpleEFAHelper.OPTABLE,
					mVarContext);
		} else {
			mPropagator = null;
		}
		if (mMode == MODE_IISCT && (specSize < 1 || plantSize < 1)) {
			mMode = MODE_NORMAL;
		}
		mKind = ComponentKind.PLANT;
		if (plantSize < 1) {
			mKind = ComponentKind.SPEC;
		}
	}

	private boolean execute(final int sourceId, final int targetId,
			final List<SimpleExpressionProxy> constraints) {
		final String AND = mHelper.getOperatorTable().getAndOperator().getName();
		final String NEXT = mHelper.getOperatorTable().getNextOperator().getName();
		String exp1 = mStateEncoding.getAttribute(sourceId, SimpleEFAHelper.DEFAULT_STATEVALUE_KEY);
		if (!exp1.isEmpty()) {
			exp1 = exp1.replace(SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR, AND);
		}
		final String exp2 = SimpleEFAHelper.printer(constraints, AND);
		String exp3 = mStateEncoding.getAttribute(targetId, SimpleEFAHelper.DEFAULT_STATEVALUE_KEY);
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

	public void reset() {
		mComponents.clear();
		mSynchEFA = null;
		mStateSpace = null;
		mInitialStates = null;
		mTR = null;
		mEFAIters = null;
		mSize = 0;
		mStateEncoding = null;
		mUsesMarking = false;
		mUsesForbidden = false;
		mLabelEncoding = null;
		mPrimedVars = null;
		mUnprimedVars = null;
		mBlockedEvents = null;
		mContEvents = null;
		mUnContEvents = null;
		mName = "";
		mRegister = true;
		mEFATrLbEns = null;
		mEventToLabelIds = null;
		mEFAStEns = null;
		mLbToSpecMap = null;
		mVarContext = null;
		mEventPlant = null;
		mEventSpec = null;
		mPropagator = null;
		mEventEncoding = null;
		mComponentToKind = null;
		mRel = null;
		mKind = null;
	}

	private static final int PLANT_CODE = 1;
	private static final int SPEC_CODE = 2;
	private static final List<SimpleExpressionProxy> FALSE = SimpleEFAHelper.getFalseExpression();
	public static final int MODE_NORMAL = 0;
	public static final int MODE_IISCT = 1;
	private final List<SimpleEFAComponent> mComponents;
	private final SimpleEFAHelper mHelper;
	private final CompilerOperatorTable mOp = CompilerOperatorTable.getInstance();
	private SimpleEFAComponent mSynchEFA;
	private TObjectIntHashMap<TIntArrayList> mStateSpace;
	private TIntArrayList mInitialStates;
	private ArrayList<Object[]> mTR;
	private ArrayList<TransitionIterator> mEFAIters;
	private int mSize;
	private SimpleEFAStateEncoding mStateEncoding;
	private boolean mUsesMarking;
	private boolean mUsesForbidden;
	private SimpleEFALabelEncoding mLabelEncoding;
	private TIntHashSet mPrimedVars;
	private TIntHashSet mUnprimedVars;
	private TIntHashSet mBlockedEvents;
	private String mName = "";
	private boolean mRegister = true;
	private ArrayList<SimpleEFALabelEncoding> mEFATrLbEns;
	private ArrayList<TIntObjectHashMap<TIntArrayList>> mEventToLabelIds;
	private ArrayList<SimpleEFAStateEncoding> mEFAStEns;
	private SimpleEFAVariableContext mVarContext;
	private TIntIntHashMap mEventPlant;
	private TIntIntHashMap mEventSpec;
	private HashMap<Integer, SimpleExpressionProxy> mLbToSpecMap;
	private boolean mIsEvaluationEnabled;
	private ConstraintPropagator mPropagator;
	private SimpleEFAEventEncoding mEventEncoding;
	private TIntIntHashMap mComponentToKind;
	private int mMode;
	private ListBufferTransitionRelation mRel;
	private ComponentKind mKind;
	private TIntHashSet mContEvents;
	private TIntHashSet mUnContEvents;
	private boolean mAttachSpecToLoc;
}

//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAComponent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * An implementation of the {@link AbstractEFATransitionRelation}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAComponent
 extends AbstractEFATransitionRelation<SimpleEFATransitionLabel>
{

  public SimpleEFAComponent(final String name,
                            final Collection<SimpleEFAVariable> variables,
                            final SimpleEFAStateEncoding stateEncoding,
                            final SimpleEFATransitionLabelEncoding labels,
                            final Collection<SimpleEFAEventDecl> blockedEvents,
                            final ListBufferTransitionRelation rel,
                            final ComponentKind kind,
                            final ModuleProxyFactory factory)
  {
    super(rel, labels, null);
    super.setName(name);
    mFactory = factory != null ? factory : ModuleSubjectFactory.getInstance();
    mHelper = new SimpleEFAHelper(mFactory);
    mStateEncoding = stateEncoding != null ? stateEncoding
                     : mHelper.getStateEncoding(rel);
    mVariables = variables;
    mUnprimeVars = new ArrayList<>();
    mPrimeVars = new ArrayList<>();
    mStateVars = new ArrayList<>();
    mBlockedEvents = blockedEvents;
    mCloner = mFactory.getCloner();
    mAlphabet = initAlphabet();
    mIdentifier = mFactory.createSimpleIdentifierProxy(name);
    mKind = kind != null ? kind : ComponentKind.PLANT;
    rel.setKind(mKind);
    mIsStructurallyDeterministic = rel.isDeterministic();
  }

  public SimpleEFAComponent(final String name,
                            final Collection<SimpleEFAVariable> variables,
                            final SimpleEFATransitionLabelEncoding labels,
                            final ListBufferTransitionRelation rel)
  {
    this(name, variables, null, labels, null, rel, null, null);
  }

  @Override
  public SimpleEFATransitionLabelEncoding getTransitionLabelEncoding()
  {
    return (SimpleEFATransitionLabelEncoding) super.getTransitionLabelEncoding();
  }

  public void addVariable(final SimpleEFAVariable variable)
  {
    mVariables.add(variable);
    variable.addTransitionRelation(this);
  }

  public void removeVariable(final SimpleEFAVariable variable)
  {
    mVariables.remove(variable);
    variable.removeTransitionRelation(this);
  }

  public Collection<SimpleEFAVariable> getVariables()
  {
    return mVariables;
  }

  /**
   * Registers this transition relation by adding its reference to all its
   * variables and events.
   */
  public void register() throws AnalysisException
  {
    final HashSet<SimpleEFAVariable> vars = new HashSet<>(mUnprimeVars);
    vars.addAll(mPrimeVars);
    if (mVariables.size() != vars.size()) {
      vars.removeAll(mVariables);
      throw new AnalysisException("Inconsistency in variable set is detected: "
       + vars);
    }

    for (final SimpleEFAVariable var : mUnprimeVars) {
      var.addTransitionRelation(this);
      var.addVisitor(this);
    }

    for (final SimpleEFAVariable var : mPrimeVars) {
      var.addTransitionRelation(this);
      var.addModifier(this);
    }

    for (final SimpleEFAVariable var : mStateVars) {
      var.addTransitionRelation(this);
      var.addModifier(this);
    }

    for (final SimpleEFAEventDecl event : mAlphabet) {
      event.addComponent(this);
    }

  }

  /**
   * Deregisters this transition relation by removing its reference from all its
   * variables and events.
   */
  public void dispose()
  {
    for (final SimpleEFAVariable var : mUnprimeVars) {
      var.removeTransitionRelation(this);
      var.removeVisitor(this);
    }

    for (final SimpleEFAVariable var : mPrimeVars) {
      var.removeTransitionRelation(this);
      var.removeModifier(this);
    }

    for (final SimpleEFAVariable var : mStateVars) {
      var.removeTransitionRelation(this);
      var.removeModifier(this);
    }
    for (final SimpleEFAEventDecl event : mAlphabet) {
      event.removeComponent(this);
    }
  }

  public Collection<SimpleEFAEventDecl> getAlphabet()
  {
    return Collections.unmodifiableCollection(mAlphabet);
  }

  public Collection<SimpleEFAEventDecl> getBlockedEvents()
  {
    return Collections.unmodifiableCollection(mBlockedEvents);
  }

  public void setBlockedEvents(final Collection<SimpleEFAEventDecl> blocked)
  {
    mBlockedEvents = blocked;
  }

  public Collection<ConstraintList> getConstrainSet()
  {
    final Collection<ConstraintList> constraints =
      new THashSet<>(getTransitionLabelEncoding().size());
    for (final SimpleEFATransitionLabel label :
         getTransitionLabelEncoding().getTransitionLabelsIncludingTau()) {
      constraints.add(label.getConstraint());
    }
    return Collections.unmodifiableCollection(constraints);
  }

  public SimpleEFAStateEncoding getStateEncoding()
  {
    return mStateEncoding;
  }

  public void setStateEncoding(final SimpleEFAStateEncoding encoding)
  {
    mStateEncoding = encoding;
  }

  public List<SimpleEFAState> getStateSet()
  {
    return mStateEncoding.getSimpleStates();
  }

  public List<SimpleEFAState> getMarkedStates()
  {
    final List<SimpleEFAState> markedState = new ArrayList<>();
    if (mHasMarking) {
      final List<SimpleEFAState> states = getStateSet();
      for (final SimpleEFAState state : states) {
        if (state.isMarked()) {
          markedState.add(state);
        }
      }
    }
    return markedState;
  }

  public SimpleEFAState getInitialState()
  {
    for (final SimpleEFAState state : getStateSet()) {
      if (state.isInitial()) {
        return state;
      }
    }
    return null;
  }

  public List<EdgeProxy> getTransitionSet(final boolean reset)
  {
    if (mEdges == null || reset) {
      mEdges = computeTransitionSet();
    }
    return mEdges;
  }

  private List<EdgeProxy> computeTransitionSet()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final SimpleEFATransitionLabelEncoding efaEvent =
     getTransitionLabelEncoding();
    final List<EdgeProxy> edgeList =
     new ArrayList<>(rel.getNumberOfTransitions());
    final TransitionIterator iter =
     rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int eventId = iter.getCurrentEvent();
      final int source = iter.getCurrentSourceState();
      final int target = iter.getCurrentTargetState();
      final SimpleEFATransitionLabel label =
       efaEvent.getTransitionLabel(eventId);
      final ConstraintList condition = label.getConstraint();
      final List<SimpleIdentifierProxy> identList =
       new ArrayList<>();
      final SimpleIdentifierProxy ident =
              mFactory.createSimpleIdentifierProxy(label.getEvent().getName());
      identList.add(ident);
      final CompilerOperatorTable op = CompilerOperatorTable.getInstance();
      final GuardActionBlockProxy guardActionBlock =
              mHelper.createGuardActionBlock(condition, op);
      final LabelBlockProxy block =
              mFactory.createLabelBlockProxy(identList, null);
      final SimpleNodeProxy sourceNode = mStateEncoding.getSimpleNode(source);
      final SimpleNodeProxy targetNode = mStateEncoding.getSimpleNode(target);
      final EdgeProxy edge =
              mFactory.createEdgeProxy((NodeProxy) mCloner.getClone(sourceNode),
                      (NodeProxy) mCloner.getClone(targetNode),
                      block, guardActionBlock, null, null, null);
      edgeList.add(edge);
    }
    return edgeList;
  }

  public SimpleComponentProxy getSimpleComponent()
  {
    final String name = getName();
    final List<SimpleEFAState> stateSet = getStateSet();
    final List<SimpleNodeProxy> nodes = new ArrayList<>(stateSet.size());
    for (final SimpleEFAState state : stateSet) {
      nodes.add((SimpleNodeProxy) mCloner.getClone(state.getSimpleNode()));
    }
    final List<EdgeProxy> edgeList = computeTransitionSet();
    final boolean isMarkingIsUsed =
     getTransitionRelation()
     .isUsedProposition(SimpleEFAHelper.DEFAULT_MARKING_ID);
    final int numOfMarkingState = getMarkedStates().size();
    LabelBlockProxy markingBlock = null;
    final List<SimpleIdentifierProxy> identList = new ArrayList<>();
    final Collection<SimpleEFAEventDecl> blockedEvents = getBlockedEvents();
    if (blockedEvents != null && !blockedEvents.isEmpty()) {
      for (final SimpleEFAEventDecl e : blockedEvents) {
        identList.add(mFactory.createSimpleIdentifierProxy(e.getName()));
      }
    }
    if (isMarkingIsUsed && numOfMarkingState < 1) {
      final SimpleIdentifierProxy marking =
              mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
      identList.add(marking);
    }
    if (!identList.isEmpty()) {
      markingBlock = mFactory.createLabelBlockProxy(identList, null);
    }
    final GraphProxy graph =
            mFactory.createGraphProxy(false, markingBlock, nodes, edgeList);
    final SimpleIdentifierProxy ident =
            mFactory.createSimpleIdentifierProxy(name);

    return mFactory.createSimpleComponentProxy(ident, getKind(), graph);
  }

  public void setStructurallyDeterministic(final boolean deterministic)
  {
    mIsStructurallyDeterministic = deterministic;
  }

  public boolean isStructurallyDeterministic()
  {
    return mIsStructurallyDeterministic;
  }

  public boolean isStructurallyNonBlocking()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (int source = 0; source < rel.getNumberOfStates(); source++) {
      if (rel.isReachable(source)
       && rel.isDeadlockState(source, SimpleEFAHelper.DEFAULT_MARKING_ID)) {
        return false;
      }
    }
    return true;
  }

  public void setIsEFA(final boolean isEFA)
  {
    mIsEFA = isEFA;
  }

  public boolean isEFA()
  {
    return mIsEFA;
  }

  public void addSystem(final SimpleEFASystem system)
  {
    if (mSystems == null) {
      mSystems = new ArrayList<>();
    }
    mSystems.add(system);
  }

  public void removeSystem(final SimpleEFASystem system)
  {
    if (mSystems != null) {
      mSystems.remove(system);
    }
  }

  public List<SimpleEFASystem> getSystems()
  {
    if (mSystems != null) {
      return mSystems;
    } else {
      return Collections.emptyList();
    }
  }

  public ModuleProxyFactory getFactory()
  {
    return mFactory;
  }

  public void setFactory(final ModuleProxyFactory factory)
  {
    mFactory = factory;
  }

  public ComponentKind getKind()
  {
    return mKind;
  }

  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
    getTransitionRelation().setKind(kind);
  }

  public SimpleIdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }

  @Override
  public String getName()
  {
    return mIdentifier.getName();
  }

  public boolean hasMarkedState()
  {
    return mHasMarking;
  }

  public boolean hasForbiddenState()
  {
    return mHasForbidden;
  }

  public int getNumberOfAlphabet()
  {
    final int extras = (mHasMarking ? 1 : 0) + (mHasForbidden ? 1 : 0);
    return mAlphabet.size() - extras;
  }

  public int getNumberOfTransitions()
  {
    return getTransitionSet(false).size();
  }

  public int getNumberOfStates()
  {
    return mStateEncoding.size();
  }

  public void setUnprimeVariables(final List<SimpleEFAVariable> uvars)
  {
    mUnprimeVars = uvars;
  }

  public void setPrimeVariables(final List<SimpleEFAVariable> pvars)
  {
    mPrimeVars = pvars;
  }

  public List<SimpleEFAVariable> getUnprimeVariables()
  {
    return mUnprimeVars;
  }

  public List<SimpleEFAVariable> getPrimeVariables()
  {
    return mPrimeVars;
  }

  public void setStateVariables(final List<SimpleEFAVariable> svars)
  {
    mStateVars = svars;
  }

  public List<SimpleEFAVariable> getStateVariables()
  {
    return mStateVars;
  }
  private Collection<SimpleEFAEventDecl> initAlphabet()
  {
    final SimpleEFATransitionLabelEncoding labels = getTransitionLabelEncoding();
    final Collection<SimpleEFAEventDecl> blockedEvents = getBlockedEvents();
    final Collection<SimpleEFAEventDecl> alphabet = new THashSet<>();
    for (int i = EventEncoding.NONTAU; i < labels.size(); i++) {
      alphabet.add(labels.getTransitionLabel(i).getEvent());
    }
    if (blockedEvents != null && !blockedEvents.isEmpty()) {
      alphabet.addAll(blockedEvents);
    }
    final long prop = getTransitionRelation().getUsedPropositions();
    if ((prop & 1) != 0) {
      final SimpleEFAEventDecl marking =
       new SimpleEFAEventDecl(mHelper.getMarkingDecl());
      alphabet.add(marking);
      mHasMarking = true;
    }
    if ((prop & 1 << SimpleEFAHelper.DEFAULT_FORBIDDEN_ID) != 0) {
      final SimpleEFAEventDecl forbidden =
       new SimpleEFAEventDecl(mHelper.getForbiddenDecl());
      alphabet.add(forbidden);
      mHasForbidden = true;
    }
    return alphabet;
  }

  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;
  private SimpleEFAStateEncoding mStateEncoding;
  private final Collection<SimpleEFAVariable> mVariables;
  private Collection<SimpleEFAEventDecl> mBlockedEvents;
  private List<SimpleEFASystem> mSystems;
  private List<EdgeProxy> mEdges;
  private List<SimpleEFAVariable> mUnprimeVars;
  private List<SimpleEFAVariable> mPrimeVars;
  private List<SimpleEFAVariable> mStateVars;
  private final ModuleProxyCloner mCloner;
  private final SimpleEFAHelper mHelper;
  private final Collection<SimpleEFAEventDecl> mAlphabet;
  private boolean mIsStructurallyDeterministic;
  private boolean mIsEFA = true;
  private boolean mHasMarking;
  private boolean mHasForbidden;
  private final SimpleIdentifierProxy mIdentifier;
  private ComponentKind mKind;
}

//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAComponent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
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
                            final TIntObjectHashMap<String> stateEncoding,
                            final SimpleEFATransitionLabelEncoding labels,
                            final Collection<SimpleEFAEventDecl> blockedEvents,
                            final ListBufferTransitionRelation rel,
                            final ModuleProxyFactory factory)
  {
    super(rel, labels, variables, null);
    super.setName(name);
    mFactory = factory != null ? factory : ModuleSubjectFactory.getInstance();
    mStateNameEncoding = stateEncoding;
    mBlockedEvents = blockedEvents;
    mSystems = new ArrayList<>();
    mCloner = mFactory.getCloner();
  }

  public SimpleEFAComponent(final String name,
                            final Collection<SimpleEFAVariable> variables,
                            final SimpleEFATransitionLabelEncoding labels,
                            final ListBufferTransitionRelation rel)
  {
    this(name, variables, null, labels, null, rel, null);
  }

  @Override
  public SimpleEFATransitionLabelEncoding getTransitionLabelEncoding()
  {
    return (SimpleEFATransitionLabelEncoding) super.getTransitionLabelEncoding();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<SimpleEFAVariable> getVariables()
  {
    return (Collection<SimpleEFAVariable>) super.getVariables();
  }

  public void addVariable(final SimpleEFAVariable variable)
  {
    super.addVariable(variable);
  }
  
  public void removeVariable(final SimpleEFAVariable variable)
  {
    super.removeVariable(variable);
  }
  
  @Override
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return super.getTransitionRelation();
  }

  public Collection<SimpleEFAEventDecl> getAlphabet()
  {
    SimpleEFATransitionLabelEncoding encoding = getTransitionLabelEncoding();
    Collection<SimpleEFAEventDecl> alphabet = new THashSet<>(
     encoding.size());
    for (int i = EventEncoding.NONTAU; i < encoding.size(); i++) {
      SimpleEFAEventDecl[] events = encoding.getTransitionLabel(i).getEvents();
      alphabet.addAll(Arrays.asList(events));
    }
    return alphabet;
  }

  public Collection<SimpleEFAEventDecl> getBlockedEvents(){
    return mBlockedEvents;
  }
  
  public Collection<ConstraintList> getConstrainSet()
  {
    Collection<ConstraintList> constrains = new THashSet<>(
     getTransitionLabelEncoding().size());
    for (SimpleEFATransitionLabel label : getTransitionLabelEncoding()) {
      constrains.add(label.getConstraint());
    }
    return constrains;
  }

  public List<SimpleNodeProxy> getLocationSet()
  {
    return new ArrayList<>(getStateEncoding().valueCollection());
  }

  public List<SimpleNodeProxy> getMarkedLocationSet()
  {
    List<SimpleNodeProxy> locations = getLocationSet();
    List<SimpleNodeProxy> markedlocations = new ArrayList<>();
    for (SimpleNodeProxy location : locations) {
      if (containsMarkingProposition(location.getPropositions(), getMarking())) {
        markedlocations.add(location);
      }
    }
    return markedlocations;
  }

  public List<EdgeProxy> getEdges()
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
      for (final SimpleEFAEventDecl e : label.getEvents()) {
        final SimpleIdentifierProxy ident =
         mFactory.createSimpleIdentifierProxy(e.getName());
        identList.add(ident);
      }
      CompilerOperatorTable op = CompilerOperatorTable.getInstance();
      final GuardActionBlockProxy guardActionBlock = createGuard(condition, op);
      final LabelBlockProxy block =
       mFactory.createLabelBlockProxy(identList, null);
      TIntObjectHashMap<SimpleNodeProxy> idStateMap = getStateEncoding();
      final SimpleNodeProxy sourceNode = idStateMap.get(source);
      final SimpleNodeProxy targetNode = idStateMap.get(target);
      final EdgeProxy edge =
       mFactory.createEdgeProxy((NodeProxy) mCloner.getClone(sourceNode),
                                (NodeProxy) mCloner.getClone(targetNode),
                                block, guardActionBlock, null, null, null);
      edgeList.add(edge);
    }
    return edgeList;
  }

  public GuardActionBlockProxy createGuard(final ConstraintList constraints,
                                           final CompilerOperatorTable op)
  {
    if (constraints.isTrue()) {
      return null;
    } else {
      final BinaryOperator bop = op.getAndOperator();
      SimpleExpressionProxy guard = null;
      for (final SimpleExpressionProxy constraint : constraints.getConstraints()) {
        SimpleExpressionSubject subjectConstraint =
         (SimpleExpressionSubject) mCloner.getClone(constraint);
        if (guard == null) {
          guard = subjectConstraint;
        } else {
          guard =
           mFactory.createBinaryExpressionProxy(bop, guard, subjectConstraint);
        }
      }
      final Collection<SimpleExpressionProxy> guards =
       Collections.singletonList(guard);
      return mFactory.createGuardActionBlockProxy(guards, null, null);
    }
  }
  /**
   * Constructing component proxy
   * <p/>
   * @return A simple component proxy {@SimpleComponentProxy}
   */
  public SimpleComponentProxy getSimpleComponent()
  {
    final String name = getName();
    final List<SimpleNodeProxy> nodes = getLocationSet();
    final List<EdgeProxy> edgeList = getEdges();
    final boolean isMarkingIsUsed =
     getTransitionRelation()
     .isUsedProposition(SimpleEFAComponent.DEFAULT_MARKING_ID);
    int numOfMarkingState = getMarkedLocationSet().size();
    LabelBlockProxy markingBlock = null;
    final List<SimpleIdentifierProxy> identList = new ArrayList<>();
    Collection<SimpleEFAEventDecl> blockedEvents = getBlockedEvents();
    if (!blockedEvents.isEmpty()) {
      for (SimpleEFAEventDecl e : blockedEvents) {
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

  public void setDeterministic(boolean deterministic)
  {
    mIsDeterministic = deterministic;
  }

  public boolean isDeterministic()
  {
    return mIsDeterministic;
  }
  
  public boolean addSystem(SimpleEFASystem system)
  {
    return mSystems.add(system);
  }
  
  public ArrayList<SimpleEFASystem> getSystems(){
    return mSystems;
  }
  
  public ModuleProxyFactory getFactory(){
    return mFactory;
  }

  public void setFactory(ModuleProxyFactory factory)
  {
    mFactory = factory;
  }

  public ComponentKind getKind()
  {
    return getTransitionRelation().getKind();
  }

  public void setKind(ComponentKind kind)
  {
    getTransitionRelation().setKind(kind);
  }

  private boolean containsMarkingProposition(final EventListExpressionProxy list,
                                             final IdentifierProxy marking)
  {
    final ModuleEqualityVisitor eq =
     ModuleEqualityVisitor.getInstance(false);
    return eq.contains(list.getEventIdentifierList(), marking);
  }

  private IdentifierProxy getMarking()
  {
    ModuleProxyFactory factory = getFactory();
    return factory.createSimpleIdentifierProxy(
     EventDeclProxy.DEFAULT_MARKING_NAME);
  }

  private TIntObjectHashMap<SimpleNodeProxy> getStateEncoding()
  {
    ListBufferTransitionRelation rel = getTransitionRelation();
    TIntObjectHashMap<SimpleNodeProxy> encoding =
      new TIntObjectHashMap<>(rel.getNumberOfStates());
    final boolean isMarkingIsUsed =
     rel.isUsedProposition(DEFAULT_MARKING_ID);
    final boolean isForbiddenIsUsed =
     rel.isUsedProposition(DEFAULT_FORBIDDEN_ID);
    final int numStates = rel.getNumberOfStates();
    for (int i = 0; i < numStates; i++) {
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked =
       rel.isMarked(i, DEFAULT_MARKING_ID);
      final boolean isForbidden =
       rel.isMarked(i, DEFAULT_FORBIDDEN_ID);
      final List<SimpleIdentifierProxy> identList =
       new ArrayList<>();
      if (isMarkingIsUsed && isMarked) {
        final SimpleIdentifierProxy ident =
         mFactory.createSimpleIdentifierProxy(
         EventDeclProxy.DEFAULT_MARKING_NAME);
        identList.add(ident);
      }
      if (isForbiddenIsUsed && isForbidden) {
        final SimpleIdentifierProxy ident =
         mFactory.createSimpleIdentifierProxy(
         EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
        identList.add(ident);
      }
      final PlainEventListProxy props =
       identList.isEmpty() ? null : mFactory.createPlainEventListProxy(
       identList);
      final String nodeName;
      if (mStateNameEncoding == null){
        nodeName = "S" + i;
      } else {
         nodeName = mStateNameEncoding.get(i);
      }
      final SimpleNodeProxy node =
       mFactory.createSimpleNodeProxy(nodeName, props, null,
                                      isInitial, null, null, null);
      encoding.put(i, node);
    }
    return encoding;
  }
    
  //#########################################################################
  //# Data Members  
  public final static int DEFAULT_MARKING_ID = 0;
  public final static int DEFAULT_FORBIDDEN_ID = 1;
  private ModuleProxyFactory mFactory;
  private final TIntObjectHashMap<String> mStateNameEncoding;
  private final Collection<SimpleEFAEventDecl> mBlockedEvents;
  private final ArrayList<SimpleEFASystem> mSystems;
  private boolean mIsDeterministic = true;
  private final ModuleProxyCloner mCloner;
}

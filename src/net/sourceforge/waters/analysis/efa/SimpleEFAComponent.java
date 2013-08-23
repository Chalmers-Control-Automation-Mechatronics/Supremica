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
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
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
      SimpleEFASystemImporter importer = new SimpleEFASystemImporter(mFactory);
      final GuardActionBlockProxy guardActionBlock =
       importer.createGuard(condition);
      final LabelBlockProxy block =
       mFactory.createLabelBlockProxy(identList, null);
      TIntObjectHashMap<SimpleNodeProxy> idStateMap = getStateEncoding();
      final SimpleNodeProxy sourceNode = idStateMap.get(source);
      final SimpleNodeProxy targetNode = idStateMap.get(target);
      final EdgeProxy edge =
       mFactory.createEdgeProxy((NodeProxy) mFactory.getCloner().getClone(sourceNode), 
                                (NodeProxy) mFactory.getCloner().getClone(targetNode), 
                                block, guardActionBlock, null, null, null);
      edgeList.add(edge);
    }
    return edgeList;
  }

  public void setDeterministic(boolean deterministic)
  {
    mIsDeterministic = deterministic;
  }

  public boolean isDeterministic()
  {
    return mIsDeterministic;
  }
  
  public boolean addSystem(
   SimpleEFASystem system)
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

  private boolean containsMarkingProposition(final EventListExpressionProxy list, final IdentifierProxy marking)
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
     rel.isUsedProposition(SimpleEFACompiler.DEFAULT_MARKING_ID);
    final boolean isForbiddenIsUsed =
     rel.isUsedProposition(SimpleEFACompiler.DEFAULT_FORBIDDEN_ID);
    final int numStates = rel.getNumberOfStates();
    for (int i = 0; i < numStates; i++) {
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked =
       rel.isMarked(i, SimpleEFACompiler.DEFAULT_MARKING_ID);
      final boolean isForbidden =
       rel.isMarked(i, SimpleEFACompiler.DEFAULT_FORBIDDEN_ID);
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
  private ModuleProxyFactory mFactory;
  private final TIntObjectHashMap<String> mStateNameEncoding;
  private final Collection<SimpleEFAEventDecl> mBlockedEvents;
  private final ArrayList<SimpleEFASystem> mSystems;
  private boolean mIsDeterministic = true;
}

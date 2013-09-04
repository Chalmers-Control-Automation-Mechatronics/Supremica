//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAComponent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
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
    super(rel, labels, variables, null);
    super.setName(name);
    mFactory = factory != null ? factory : ModuleSubjectFactory.getInstance();
    mHelper = new EFAHelper(mFactory);
    mStateEncoding = stateEncoding != null ? stateEncoding
                     : mHelper.getStateEncoding(rel);
    mBlockedEvents = blockedEvents;
    mSystems = new ArrayList<>();
    mCloner = mFactory.getCloner();
    mAlphabet = initAlphabet();
    mIdentifier = mFactory.createSimpleIdentifierProxy(name);
    mKind = kind != null ? kind : ComponentKind.PLANT;
    rel.setKind(mKind);
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
    return mAlphabet;
  }

  public Collection<SimpleEFAEventDecl> getBlockedEvents()
  {
    return mBlockedEvents != null ? mBlockedEvents
           : new THashSet<SimpleEFAEventDecl>();
  }

  public void setBlockedEvents(final Collection<SimpleEFAEventDecl> blocked)
  {
    mBlockedEvents = blocked;
  }
  
  public Collection<ConstraintList> getConstrainSet()
  {
    final Collection<ConstraintList> constrains = new THashSet<>(
     getTransitionLabelEncoding().size());
    for (final SimpleEFATransitionLabel label : getTransitionLabelEncoding()) {
      constrains.add(label.getConstraint());
    }
    return constrains;
  }

  public SimpleEFAStateEncoding getStateEncoding()
  {
    return mStateEncoding;
  }

  public void setStateEncoding(SimpleEFAStateEncoding encoding)
  {
    mStateEncoding = encoding;
  }

  public List<SimpleEFAState> getStateSet()
  {
    return mStateEncoding.getSimpleStates();
  }

  public List<SimpleEFAState> getMarkedStates()
  {
    final List<SimpleEFAState> states = getStateSet();
    final List<SimpleEFAState> markedState = new ArrayList<>();
    for (final SimpleEFAState state : states) {
      if (state.isMarked()) {
        markedState.add(state);
      }
    }
    return markedState;
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

  /**
   * Constructing component proxy
   * <p/>
   * @return A simple component proxy {@SimpleComponentProxy}
   */
  public SimpleComponentProxy getSimpleComponent()
  {
    final String name = getName();
    final List<SimpleEFAState> stateSet = getStateSet();
    final List<SimpleNodeProxy> nodes = new ArrayList<>(stateSet.size());
    for (SimpleEFAState state : stateSet) {
      nodes.add((SimpleNodeProxy) mCloner.getClone(state.getSimpleNode()));
    }
    final List<EdgeProxy> edgeList = getEdges();
    final boolean isMarkingIsUsed =
     getTransitionRelation()
     .isUsedProposition(SimpleEFAComponent.DEFAULT_MARKING_ID);
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

  public void setDeterministic(final boolean deterministic)
  {
    mIsDeterministic = deterministic;
  }

  public boolean isDeterministic()
  {
    return mIsDeterministic;
  }
  
  public void setIsEFA(final boolean isEFA)
  {
    mIsEFA = isEFA;
  }

  public boolean isEFA()
  {
    return mIsEFA;
  }

  public boolean addSystem(final SimpleEFASystem system)
  {
    return mSystems.add(system);
  }
  
  public ArrayList<SimpleEFASystem> getSystems()
  {
    return mSystems;
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

    private Collection<SimpleEFAEventDecl> initAlphabet()
  {
    final SimpleEFATransitionLabelEncoding labels = getTransitionLabelEncoding();
    final Collection<SimpleEFAEventDecl> blockedEvents = getBlockedEvents();
    final Collection<SimpleEFAEventDecl> alphabet = new THashSet<>();
    for (int i = EventEncoding.NONTAU; i < labels.size(); i++) {
      alphabet.add(labels.getTransitionLabel(i).getEvent());
    }
    if (!blockedEvents.isEmpty()) {
      alphabet.addAll(blockedEvents);
    }
    final long prop = getTransitionRelation().getUsedPropositions();
    if ((prop & 1 << DEFAULT_MARKING_ID) != 0) {
      final SimpleEFAEventDecl marking =
       new SimpleEFAEventDecl(mHelper.getMarkingDecl());
      alphabet.add(marking);
    }
    if ((prop & 1 << DEFAULT_FORBIDDEN_ID) != 0) {
      final SimpleEFAEventDecl forbidden =
       new SimpleEFAEventDecl(mHelper.getForbiddenDecl());
      alphabet.add(forbidden);
    }
    return alphabet;
  }

  //#########################################################################
  //# Data Members  
  public final static int DEFAULT_MARKING_ID = 0;
  public final static int DEFAULT_FORBIDDEN_ID = 1;
  private ModuleProxyFactory mFactory;
  private SimpleEFAStateEncoding mStateEncoding;
  private Collection<SimpleEFAEventDecl> mBlockedEvents;
  private final ArrayList<SimpleEFASystem> mSystems;
  private final ModuleProxyCloner mCloner;
  private final EFAHelper mHelper;
  private final Collection<SimpleEFAEventDecl> mAlphabet;
  private boolean mIsDeterministic = true;
  private boolean mIsEFA = true;
  private final SimpleIdentifierProxy mIdentifier;
  private ComponentKind mKind;
}

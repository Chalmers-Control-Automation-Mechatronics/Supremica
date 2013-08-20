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
import java.util.*;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

/**
 * An implementation of the {@link AbstractEFATransitionRelation}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAComponent
 extends AbstractEFATransitionRelation<SimpleEFATransitionLabel>
{

  public SimpleEFAComponent(final String name,
                            final ListBufferTransitionRelation rel,
                            final SimpleEFATransitionLabelEncoding labels,
                            final Collection<SimpleEFAVariable> variables,
                            final List<SimpleNodeProxy> nodes,
                            final ModuleProxyFactory factory)
  {
    super(rel, labels, variables, nodes);
    super.setName(name);
    mFactory = factory;
  }

  public SimpleEFAComponent(final String name,
                            final ListBufferTransitionRelation rel,
                            final SimpleEFATransitionLabelEncoding labels,
                            final Collection<SimpleEFAVariable> variables,
                            final List<SimpleNodeProxy> nodes)
  {
    super(rel, labels, variables, nodes);
    super.setName(name);
    mFactory = null;
  }

  public SimpleEFAComponent(final String name,
                            final ListBufferTransitionRelation rel,
                            final SimpleEFATransitionLabelEncoding labels,
                            final Collection<SimpleEFAVariable> variables)
  {
    super(rel, labels, variables);
    super.setName(name);
    mFactory = null;
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

  @Override
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return super.getTransitionRelation();
  }

  public Collection<SimpleEFAEventDecl> getAlphabet()
  {
    Collection<SimpleEFAEventDecl> alphabet = new THashSet<SimpleEFAEventDecl>(
     getTransitionLabelEncoding().size());
    for (SimpleEFATransitionLabel label : getTransitionLabelEncoding()) {
      alphabet.addAll(Arrays.asList(label.getEvents()));
    }
    return alphabet;
  }

  public Collection<ConstraintList> getConstrainSet()
  {
    Collection<ConstraintList> constrains = new THashSet<ConstraintList>(
     getTransitionLabelEncoding().size());
    for (SimpleEFATransitionLabel label : getTransitionLabelEncoding()) {
      constrains.add(label.getConstraint());
    }
    return constrains;
  }

  public List<SimpleNodeProxy> getLocationSet()
  {
    ModuleProxyFactory factory = getFactory();
    final List<SimpleNodeProxy> nodes = getNodeList();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final boolean isMarkingIsUsed = rel.isUsedProposition(0);
    final int numStates = rel.getNumberOfStates();
    final List<SimpleNodeProxy> nodeList =
     new ArrayList<SimpleNodeProxy>(numStates);
    for (int i = 0; i < numStates; i++) {
      final boolean isInitial = rel.isInitial(i);
      final boolean isMarked = rel.isMarked(i, 0);
      PlainEventListProxy props = null;
      if (isMarked && isMarkingIsUsed) {
        final List<SimpleIdentifierProxy> identList =
         Collections.singletonList((SimpleIdentifierProxy) getMarking());
        props = factory.createPlainEventListProxy(identList);
      }
      final String nodeName;
      if (nodes == null) {
        nodeName = "S" + i;
      } else {
        final SimpleNodeProxy nodeFromEFA = nodes.get(i);
        nodeName = nodeFromEFA.getName();
      }
      final SimpleNodeProxy node =
       factory.createSimpleNodeProxy(nodeName, props, null,
                                     isInitial, null, null, null);
      nodeList.add(node);
    }
    return nodeList;
  }

  public List<SimpleNodeProxy> getMarkedLocationSet()
  {
    List<SimpleNodeProxy> locations = getLocationSet();
    List<SimpleNodeProxy> markedlocations = new ArrayList<SimpleNodeProxy>();
    for (SimpleNodeProxy location : locations) {
      if (containsMarkingProposition(location.getPropositions(), getMarking())) {
        markedlocations.add(location);
      }
    }
    return markedlocations;
  }

  private boolean containsMarkingProposition(
   final EventListExpressionProxy list, final IdentifierProxy marking)
  {
    final ModuleEqualityVisitor eq =
     ModuleEqualityVisitor.getInstance(false);
    return eq.contains(list.getEventIdentifierList(), marking);
  }

  private ModuleProxyFactory getFactory()
  {
    if (mFactory != null) {
      return mFactory;
    } else {
      return ModuleElementFactory.getInstance();
    }
  }

  private IdentifierProxy getMarking()
  {
    ModuleProxyFactory factory = getFactory();
    return factory.createSimpleIdentifierProxy(
     EventDeclProxy.DEFAULT_MARKING_NAME);
  }
  
  //#########################################################################
  //# Data Members  
  private final ModuleProxyFactory mFactory;
  
}

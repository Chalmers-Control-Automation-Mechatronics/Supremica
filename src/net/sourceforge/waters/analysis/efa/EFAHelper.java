//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFAHelper
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static net.sourceforge.waters.analysis.efa.SimpleEFAComponent.DEFAULT_FORBIDDEN_ID;
import static net.sourceforge.waters.analysis.efa.SimpleEFAComponent.DEFAULT_MARKING_ID;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class EFAHelper {

  public EFAHelper(final ModuleProxyFactory factory,
                   final CompilerOperatorTable optable)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mCloner = ModuleSubjectFactory.getCloningInstance();
  }

  /**
   * Using operation table {(
   * <p/>
   * @CompilerOperatorTable)} instances
   * @param factory Factory to be used for components construction
   */
  public EFAHelper(final ModuleProxyFactory factory)
  {
    this(factory, CompilerOperatorTable.getInstance());
  }

  /**
   * Using subject factory {(
   * <p/>
   * @ModuleSubjectFactory)} and operation table {(
   * @CompilerOperatorTable)} instances
   */
  public EFAHelper()
  {
    this(ModuleSubjectFactory.getInstance(),
         CompilerOperatorTable.getInstance());
  }

  public Collection<EventDeclProxy> getEventDeclProxy(
   final Collection<SimpleEFAEventDecl> list)
  {
    final Collection<EventDeclProxy> decls =
     new THashSet<>(list.size());
    for (final SimpleEFAEventDecl e : list) {
      final IdentifierProxy identifier =
       mFactory.createSimpleIdentifierProxy(e.getName());
      final EventDeclProxy event =
       mFactory.createEventDeclProxy(identifier,
                                     e.getKind(),
                                     e.isObservable(),
                                     ScopeKind.LOCAL,
                                     e.getRanges(),
                                     null,
                                     null);
      decls.add(event);
    }
    return decls;
  }

  public GuardActionBlockProxy createGuardActionBlock(
   final ConstraintList constraints,
   final CompilerOperatorTable op)
  {
    if (constraints.isTrue()) {
      return null;
    } else {
      final BinaryOperator bop = op.getAndOperator();
      SimpleExpressionProxy guard = null;
      for (final SimpleExpressionProxy constraint : constraints.getConstraints()) {
        final SimpleExpressionSubject subjectConstraint =
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

  public TIntObjectHashMap<SimpleNodeProxy> getStateEncoding(
   final TIntObjectHashMap<String> stateNameEncoding,
   final ListBufferTransitionRelation rel) throws AnalysisException
  {
    final TIntObjectHashMap<SimpleNodeProxy> encoding =
     new TIntObjectHashMap<>(rel.getNumberOfStates());
    final boolean isMarkingIsUsed =
     rel.isUsedProposition(DEFAULT_MARKING_ID);
    final boolean isForbiddenIsUsed =
     rel.isUsedProposition(DEFAULT_FORBIDDEN_ID);
    final int numStates = rel.getNumberOfStates();
    for (int i = 0; i < numStates; i++) {
      if (!rel.isReachable(i)) {
        continue;
      }
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
      if (stateNameEncoding == null) {
        nodeName = "S" + i;
      } else {
        nodeName = stateNameEncoding.get(i);
      }
      if (nodeName == null) {
        throw new AnalysisException(
         "EFAHelpre > getStateEncoding: Name for node " + i + " is null.");
      }
      final SimpleNodeProxy node =
       mFactory.createSimpleNodeProxy(nodeName, props, null,
                                      isInitial, null, null, null);
      encoding.put(i, node);
    }
    return encoding;
  }

  public boolean containsMarkingProposition(final EventListExpressionProxy list)
  {
    final ModuleEqualityVisitor eq =
     ModuleEqualityVisitor.getInstance(false);
    return eq.contains(list.getEventIdentifierList(), getMarkingIdentifier());
  }

  public boolean containsForbiddenProposition(
   final EventListExpressionProxy list)
  {
    final ModuleEqualityVisitor eq =
     ModuleEqualityVisitor.getInstance(false);
    return eq.contains(list.getEventIdentifierList(), getForbiddenIdentifier());
  }

  public SimpleIdentifierProxy getMarkingIdentifier()
  {
    return mFactory
     .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
  }

  public SimpleIdentifierProxy getForbiddenIdentifier()
  {
    return mFactory
     .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
  }

  public EventDeclProxy getTAUDecl()
  {
    final String name = "tau:";
    final SimpleIdentifierProxy iden =
     mFactory.createSimpleIdentifierProxy(name);
    return mFactory.createEventDeclProxy(iden, EventKind.CONTROLLABLE, false,
     ScopeKind.LOCAL, null, null, null);
  }

  public EventDeclProxy getMarkingDecl()
  {
    final SimpleIdentifierProxy iden =
     mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    return mFactory.createEventDeclProxy(iden, EventKind.PROPOSITION);
  }

  public EventDeclProxy getForbiddenDecl()
  {
    final SimpleIdentifierProxy iden =
     mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
    return mFactory.createEventDeclProxy(iden, EventKind.PROPOSITION);
  }

  public SimpleEFAVariable getSimpleEFAVariable(
   final VariableComponentProxy comp,
   final CompiledRange range)
  {
    final VariableComponentProxy cloneVar =
     (VariableComponentProxy) mCloner.getClone(comp);
    return new SimpleEFAVariable(cloneVar, range, mFactory, mOperatorTable);
  }

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleProxyCloner mCloner;
}

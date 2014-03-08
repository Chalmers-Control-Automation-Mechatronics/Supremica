//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAHelper
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.set.hash.THashSet;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * @author Mohammad Reza Shoaei
 */

public class SimpleEFAHelper {

  /**
   * Creates an EFA helper instance based on the given factory and
   * operator table.
   */
  public SimpleEFAHelper(final ModuleProxyFactory factory,
                         final CompilerOperatorTable optable) {
    super();
    mFactory = factory;
    mOperatorTable = optable;
    mCloner = ModuleSubjectFactory.getCloningInstance();
  }

  /**
   * Creates an EFA helper instance based on the given factory and
   * the standard {@link CompilerOperatorTable}.
   */
  public SimpleEFAHelper(final ModuleProxyFactory factory)
  {
    this(factory, CompilerOperatorTable.getInstance());
  }

  /**
   * Creates an EFA helper instance based on the standard
   * {@link ModuleSubjectFactory} and {@link CompilerOperatorTable}.
   */
  public SimpleEFAHelper()
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

  public SimpleEFAStateEncoding getStateEncoding(
   final ListBufferTransitionRelation rel)
  {
    final SimpleEFAStateEncoding encoding =
     new SimpleEFAStateEncoding(rel.getNumberOfStates());
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
      final String nodeName = "S" + i;
      final SimpleEFAState state = new SimpleEFAState(nodeName, isInitial,
                                                isMarkingIsUsed && isMarked,
                                                isForbiddenIsUsed && isForbidden,
                                                null, mFactory);
      encoding.createSimpleStateId(state);
    }
    return encoding;
  }

  public boolean containsMarkingProposition(final EventListExpressionProxy list)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    return eq.contains(list.getEventIdentifierList(), getMarkingIdentifier());
  }

  public boolean containsForbiddenProposition(
   final EventListExpressionProxy list)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
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

  public String printer(final ConstraintList constraints,
                        final String opening,
                        final String separator,
                        final String closing)
  {
    final StringBuilder result = new StringBuilder();
    result.append(opening);
    for (final SimpleExpressionProxy exp : constraints.getConstraints()) {
      result.append(exp.toString());
      result.append(separator);
    }
    result.delete(result.length() - separator.length(), result.length());
    result.append(closing);
    return result.toString();
  }

  public String printer(final List<SimpleExpressionProxy> exps,
                        final String opening,
                        final String separator,
                        final String closing)
  {
    return printer(new ConstraintList(exps), opening, separator, closing);
  }

  public List<SimpleExpressionProxy> parseString(final String str,
                                                 final String opening,
                                                 final String closing)
  {
    final List<SimpleExpressionProxy> exps = new ArrayList<>();
    final Pattern pattern = Pattern.compile(opening + "(.*?)" + closing);
    final Matcher matcher = pattern.matcher(str);
    while (matcher.find()) {
      matcher.start();
      final String exp = matcher.group(1);
      exps.addAll(parse(exp));
    }
    return exps;
  }

  public List<SimpleExpressionProxy> parse(final String... str)
  {
    final ExpressionParser parser =
     new ExpressionParser(mFactory, mOperatorTable);
    final List<SimpleExpressionProxy> exps = new ArrayList<>();
    for (final String s : str) {
      try {
        exps.add(parser.parse(s));
      } catch (final ParseException ignored) {
      }
    }
    return exps;
  }

  public static HashMap<String, String> merge(
   final HashMap<String, String> attribute1,
   final HashMap<String, String> attribute2,
   final String separator)
  {
    if (attribute1 == null || attribute1.isEmpty()) {
      return attribute2;
    }
    if (attribute2 == null || attribute2.isEmpty()) {
      return attribute1;
    }

    final HashMap<String, String> result = new HashMap<>(attribute1);
    for (final String att2 : attribute2.keySet()) {
      final String val2 = attribute2.get(att2);
      String val = result.get(att2);
      if (val != null) {
        val += separator + val2;
      } else {
        val = val2;
      }
      result.put(att2, val);
    }
    return result;
  }

  public static ConstraintList merge(final ConstraintList con1, final ConstraintList con2)
  {
    final List<SimpleExpressionProxy> con = new ArrayList<>(con1.getConstraints());
    con.addAll(con2.getConstraints());
    return new ConstraintList(con);
  }

  public ConstraintList conjunct(final ConstraintList con1, final ConstraintList con2){
    final ConstraintList merge = SimpleEFAHelper.merge(con1, con2);
    final SimpleExpressionProxy con = merge.createExpression(mFactory, mOperatorTable.getAndOperator());
    return new ConstraintList(Collections.singletonList(con));
  }

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleProxyCloner mCloner;

  public static final String DEFAULT_STATEVALUE_STRING = "PE:";
  public static final String DEFAULT_VALUE_OPENING = "<";
  public static final String DEFAULT_VALUE_CLOSING = ">";
  public static final String DEFAULT_VALUE_SEPARATOR = ",";

  public static final int DEFAULT_MARKING_ID = 0;
  public static final int DEFAULT_FORBIDDEN_ID = 1;

}

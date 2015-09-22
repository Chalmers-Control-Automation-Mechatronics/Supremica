//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.expr;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simple implementation of the {@link Comparator} interface, used to
 * compare Waters expressions.</P>
 *
 * <P>This comparator imposes an ordering on objects of all subtypes of
 * {@link SimpleExpressionProxy} and can be used to ensure deterministic
 * compiler output. The ordering is first based on proxy types; if the type
 * is the same, the contents are examined. Identifiers are compared based
 * on their names. Unary and binary expressions are compared first by their
 * operators, followed by the subterms.</P>
 *
 * <P>The ordering of the proxy types and the operators can be customised,
 * where the default for the class type ordering is given by the following
 * list:</P>
 * <OL>
 * <LI>{@link IntConstantProxy}</LI>
 * <LI>{@link SimpleIdentifierProxy}</LI>
 * <LI>{@link IndexedIdentifierProxy}</LI>
 * <LI>{@link QualifiedIdentifierProxy}</LI>
 * <LI>{@link EnumSetExpressionProxy}</LI>
 * <LI>{@link BinaryExpressionProxy}</LI>
 * <LI>{@link UnaryExpressionProxy}</LI>
 * </OL>
 * <P>The operator ordering can be given using an {@link OperatorTable}, or
 * the alphabetic ordering by the operator names will be used as default.</P>
 *
 * @author Robi Malik
 */

public class ExpressionComparator
  extends DefaultModuleProxyVisitor
  implements Comparator<SimpleExpressionProxy>
{

  //#########################################################################
  //# Singleton Pattern
  public static Comparator<SimpleExpressionProxy> getInstance()
  {
    return SingletonHolder.INSTANCE;
  }


  //#########################################################################
  //# Constructors
  public ExpressionComparator
    (final TObjectIntHashMap<Class<? extends Proxy>> ifacevalues)
  {
    this(ifacevalues, null);
  }

  public ExpressionComparator
    (final OperatorTable optable)
  {
    this(SingletonHolder.DEFAULTMAP, optable);
  }

  public ExpressionComparator
    (final TObjectIntHashMap<Class<? extends Proxy>> ifacevalues,
     final OperatorTable optable)
  {
    mInterfaceValues = ifacevalues;
    mOperatorTable = optable;
  }


  //##########################################################################
  //# Interface java.util.Comparator
  @Override
  public int compare(final SimpleExpressionProxy expr1,
                     final SimpleExpressionProxy expr2)
  {
    if (expr1 == null) {
      return expr2 == null ? 0 : -1;
    } else if (expr2 == null) {
      return 1;
    }
    final Class<? extends Proxy> clazz1 = expr1.getProxyInterface();
    final Class<? extends Proxy> clazz2 = expr2.getProxyInterface();
    if (clazz1 != clazz2) {
      final int classval1 = getInterfaceValue(clazz1);
      final int classval2 = getInterfaceValue(clazz2);
      if (classval1 != classval2) {
        return classval1 - classval2;
      }
    }
    final SimpleExpressionProxy old2 = mExpr2;
    try {
      mExpr2 = expr2;
      return (Integer) expr1.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mExpr2 = old2;
    }
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Integer visitBinaryExpressionProxy(final BinaryExpressionProxy expr1)
  {
    final BinaryExpressionProxy expr2 = (BinaryExpressionProxy) mExpr2;
    final BinaryOperator op1 = expr1.getOperator();
    final BinaryOperator op2 = expr2.getOperator();
    int result = compareOperators(op1, op2);
    if (result != 0) {
      return result;
    }
    final SimpleExpressionProxy lhs1 = expr1.getLeft();
    final SimpleExpressionProxy lhs2 = expr2.getLeft();
    result = compare(lhs1, lhs2);
    if (result != 0) {
      return result;
    }
    final SimpleExpressionProxy rhs1 = expr1.getRight();
    final SimpleExpressionProxy rhs2 = expr2.getRight();
    return compare(rhs1, rhs2);
  }

  @Override
  public Integer visitEnumSetExpressionProxy
    (final EnumSetExpressionProxy enum1)
  {
    final EnumSetExpressionProxy enum2 = (EnumSetExpressionProxy) mExpr2;
    final List<SimpleIdentifierProxy> items1 = enum1.getItems();
    final List<SimpleIdentifierProxy> items2 = enum2.getItems();
    final Iterator<SimpleIdentifierProxy> iter1 = items1.iterator();
    final Iterator<SimpleIdentifierProxy> iter2 = items2.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final SimpleIdentifierProxy item1 = iter1.next();
      final SimpleIdentifierProxy item2 = iter2.next();
      final String name1 = item1.getName();
      final String name2 = item2.getName();
      final int result = name1.compareTo(name2);
      if (result != 0) {
        return result;
      }
    }
    final int more1 = iter1.hasNext() ? 1 : 0;
    final int more2 = iter2.hasNext() ? 1 : 0;
    return more1 - more2;
  }

  @Override
  public Integer visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy expr1)
  {
    final FunctionCallExpressionProxy expr2 =
      (FunctionCallExpressionProxy) mExpr2;
    final String name1 = expr1.getFunctionName();
    final String name2 = expr2.getFunctionName();
    int result = name1.compareTo(name2);
    if (result != 0) {
      return result;
    }
    final List<SimpleExpressionProxy> args1 = expr1.getArguments();
    final List<SimpleExpressionProxy> args2 = expr2.getArguments();
    final Iterator<SimpleExpressionProxy> iter1 = args1.iterator();
    final Iterator<SimpleExpressionProxy> iter2 = args2.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final SimpleExpressionProxy arg1 = iter1.next();
      final SimpleExpressionProxy arg2 = iter2.next();
      result = compare(arg1, arg2);
      if (result != 0) {
        return result;
      }
   }
    final int more1 = iter1.hasNext() ? 1 : 0;
    final int more2 = iter2.hasNext() ? 1 : 0;
    return more1 - more2;
  }

  @Override
  public Integer visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident1)
  {
    final IndexedIdentifierProxy ident2 = (IndexedIdentifierProxy) mExpr2;
    final String name1 = ident1.getName();
    final String name2 = ident2.getName();
    int result = name1.compareTo(name2);
    if (result != 0) {
      return result;
    }
    final List<SimpleExpressionProxy> indexes1 = ident1.getIndexes();
    final List<SimpleExpressionProxy> indexes2 = ident2.getIndexes();
    final Iterator<SimpleExpressionProxy> iter1 = indexes1.iterator();
    final Iterator<SimpleExpressionProxy> iter2 = indexes2.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final SimpleExpressionProxy subterm1 = iter1.next();
      final SimpleExpressionProxy subterm2 = iter2.next();
      result = compare(subterm1, subterm2);
      if (result != 0) {
        return result;
      }
    }
    final int more1 = iter1.hasNext() ? 1 : 0;
    final int more2 = iter2.hasNext() ? 1 : 0;
    return more1 - more2;
  }

  @Override
  public Integer visitIntConstantProxy(final IntConstantProxy intconst1)
  {
    final IntConstantProxy intconst2 = (IntConstantProxy) mExpr2;
    final int value1 = intconst1.getValue();
    final int value2 = intconst2.getValue();
    if (value1 < value2) {
      return -1;
    } else if (value1 > value2) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public Integer visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident1)
  {
    final QualifiedIdentifierProxy ident2 = (QualifiedIdentifierProxy) mExpr2;
    final IdentifierProxy base1 = ident1.getBaseIdentifier();
    final IdentifierProxy base2 = ident2.getBaseIdentifier();
    final int result = compare(base1, base2);
    if (result != 0) {
      return result;
    }
    final IdentifierProxy comp1 = ident1.getComponentIdentifier();
    final IdentifierProxy comp2 = ident2.getComponentIdentifier();
    return compare(comp1, comp2);
  }

  @Override
  public Integer visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident1)
  {
    final SimpleIdentifierProxy ident2 = (SimpleIdentifierProxy) mExpr2;
    final String name1 = ident1.getName();
    final String name2 = ident2.getName();
    return name1.compareTo(name2);
  }

  @Override
  public Integer visitUnaryExpressionProxy(final UnaryExpressionProxy expr1)
  {
    final UnaryExpressionProxy expr2 = (UnaryExpressionProxy) mExpr2;
    final UnaryOperator op1 = expr1.getOperator();
    final UnaryOperator op2 = expr2.getOperator();
    final int result = compareOperators(op1, op2);
    if (result != 0) {
      return result;
    }
    final SimpleExpressionProxy subterm1 = expr1.getSubTerm();
    final SimpleExpressionProxy subterm2 = expr2.getSubTerm();
    return compare(subterm1, subterm2);
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getInterfaceValue(final Class<? extends Proxy> clazz)
  {
    final Integer result = mInterfaceValues.get(clazz);
    if (result != null) {
      return result;
    } else {
      throw new ClassCastException
        ("ExpressionComparator does not support " + clazz.getName() + "!");
    }
  }

  private int compareOperators(final Operator op1, final Operator op2)
  {
    if (mOperatorTable != null) {
      final int value1 = mOperatorTable.getOperatorValue(op1);
      final int value2 = mOperatorTable.getOperatorValue(op2);
      if (value1 != value2) {
        return value1 - value2;
      }
    }
    final String opname1 = op1.getName();
    final String opname2 = op2.getName();
    return opname1.compareTo(opname2);
  }


  //#########################################################################
  //# Data Members
  private final TObjectIntHashMap<Class<? extends Proxy>> mInterfaceValues;
  private final OperatorTable mOperatorTable;
  private SimpleExpressionProxy mExpr2;


  //#########################################################################
  //# Static Class Constants
  private static class SingletonHolder {

    private static final Comparator<SimpleExpressionProxy> INSTANCE;
    private static final TObjectIntHashMap<Class<? extends Proxy>> DEFAULTMAP;

    static {
      DEFAULTMAP = new TObjectIntHashMap<Class<? extends Proxy>>(32);
      DEFAULTMAP.put(IntConstantProxy.class, 0);
      DEFAULTMAP.put(SimpleIdentifierProxy.class, 1);
      DEFAULTMAP.put(IndexedIdentifierProxy.class, 2);
      DEFAULTMAP.put(QualifiedIdentifierProxy.class, 3);
      DEFAULTMAP.put(EnumSetExpressionProxy.class, 4);
      DEFAULTMAP.put(UnaryExpressionProxy.class, 5);
      DEFAULTMAP.put(BinaryExpressionProxy.class, 6);
      DEFAULTMAP.put(FunctionCallExpressionProxy.class, 7);
      INSTANCE = new ExpressionComparator(DEFAULTMAP, null);
    }

  }

}









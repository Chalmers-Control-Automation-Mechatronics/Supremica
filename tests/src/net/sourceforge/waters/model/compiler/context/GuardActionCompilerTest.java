//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.compiler.context;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class GuardActionCompilerTest
  extends AbstractExpressionCompilerTest
{

  //#########################################################################
  //# Test Cases
  public void testSimplify_error_assign()
    throws EvalException, ParseException
  {
    try {
      final String input = "(a = 0) | (b = a)";
      testSimplify(input, input);
      fail("Expected ActionSyntaxException not caught!");
    } catch (final ActionSyntaxException exception) {
      // OK
    }
  }


  public void testSimplify_keep_alone()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String input = "a' == a'";
    final String[] guards = {input};
    testGuardActionBlock(guards, null, input);
  }

  public void testSimplify_keep_assign()
    throws EvalException, ParseException
  {
    final String input = "order.x += 1";
    testSimplify(input, input);
  }

  public void testSimplify_keep_left()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String input = "a' == a' & b > 0";
    testSimplify(input, input);
  }

  public void testSimplify_keep_nested_or()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String input = "a' == a' | b' == b'";
    final String expected = "a' == a' & b' == b'";
    testSimplify(input, expected);
  }

  public void testSimplify_keep_right()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String input = "b > 0 & a' == a'";
    testSimplify(input, input);
  }

  public void testSimplify_keep_some()
    throws EvalException, ParseException
  {
    final String input = "a' == a' & (b = a) & b' == b'";
    final String expected = "a' == a' & (b = a)";
    testSimplify(input, expected);
  }


  public void testSimplify_null()
    throws EvalException, ParseException
  {
    testSimplify(null, null);
  }

  public void testSimplify_redundant_assign()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String input = "(a = 0) & a' == a'";
    final String expected = "a = 0";
    testSimplify(input, expected);
  }

  public void testSimplify_redundant_dup()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String input = "a' == a' & a' == a'";
    final String expected = "a' == a'";
    testSimplify(input, expected);
  }

  public void testSimplify_redundant_dup_assign()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String input = "a' == a' & (b += a) & a' == a' & b' == b'";
    final String expected = "a' == a' & (b += a)";
    testSimplify(input, expected);
  }

  public void testSimplify_redundant_eq()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String input = "a' == 0 & a' == a'";
    final String expected = "a' == 0";
    testSimplify(input, expected);
  }


  public void testSimplify_rewrite_leq()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 10);
    addVariable("x", range);
    addVariable("x'", range);
    final String input = "x' >= 0";
    final String expected = "x' == x'";
    testSimplify(input, expected);
  }

  public void testSimplify_rewrite_neq()
    throws EvalException, ParseException
  {
    final String input = "x' != x' & a > b";
    final String expected = "0";
    testSimplify(input, expected);
  }

  public void testSimplify_rewrite_true()
    throws EvalException, ParseException
  {
    final String input = "1 < 2";
    final String expected = null;
    testSimplify(input, expected);
  }


  public void testSimplify_simplify_ga()
    throws EvalException, ParseException
  {
    final String input = "(a = 0) & (b > 1 + 1) & (b += a)";
    final String expected = "(a = 0) & (b > 2) & (b += a)";
    testSimplify(input, expected);
  }


  public void testSimplify_unchanged_assign()
    throws EvalException, ParseException
  {
    final String input = "a = 0";
    testSimplify(input, input);
  }

  public void testSimplify_unchanged_ga()
    throws EvalException, ParseException
  {
    final String input = "(a = 0) & (b > 2) & (b += a)";
    testSimplify(input, input);
  }


  public void testSimplify_var_boolean()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String input = "a";
    testSimplify(input, input);
  }

  public void testSimplify_var_enum()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    final String input = "crc==nok";
    testSimplify(input, input);
  }

  public void testSimplify_var_int()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 10);
    addVariable("x", range);
    final String input = "x<x";
    final String expected = "0";
    testSimplify(input, expected);
  }

  public void testSimplify_var_undef()
    throws EvalException, ParseException
  {
    final String input = "x >= 0";
    testSimplify(input, input);
  }


  public void testReentrant()
    throws EvalException, ParseException
  {
    testSimplify_var_undef();
    resetContext();
    testSimplify_rewrite_leq();
    resetContext();
    testSimplify_var_enum();
    resetContext();
    testSimplify_var_int();
    resetContext();
    testSimplify_var_undef();
    resetContext();
    testSimplify_redundant_dup_assign();
    resetContext();
    testSimplify_var_enum();
  }


  //#########################################################################
  //# Utilities
  private void testGuardActionBlock(final String[] guardInput,
                                    final String[] actionInput,
                                    final String expected)
    throws EvalException, ParseException
  {
    final ExpressionParser parser = getExpressionParser();
    final List<SimpleExpressionProxy> guards = new LinkedList<>();
    if (guardInput != null) {
      for (final String input : guardInput) {
        final SimpleExpressionProxy expr = parser.parse(input);
        guards.add(expr);
      }
    }
    final List<BinaryExpressionProxy> actions = new LinkedList<>();
    if (actionInput != null) {
      for (final String input : actionInput) {
        final SimpleExpressionProxy expr = parser.parse(input);
        assertTrue("Bad type of action!", expr instanceof BinaryExpressionProxy);
        final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
        actions.add(binary);
      }
    }
    final ModuleProxyFactory factory = getFactory();
    final GuardActionBlockProxy ga =
      factory.createGuardActionBlockProxy(guards, actions, null);
    final VariableContext context = getContext();
    guards.clear();
    actions.clear();
    final GuardActionCompiler.Result result =
      mCompiler.separateGuardActionBlock(ga, context);
    checkResult(result, expected);
  }

  private void testSimplify(final String input, final String expected)
    throws EvalException, ParseException
  {
    final ExpressionParser parser = getExpressionParser();
    final SimpleExpressionProxy inExpr =
      input == null ? null : parser.parse(input);
    final VariableContext context = getContext();
    final GuardActionCompiler.Result result =
      mCompiler.separateCondition(inExpr, context, true);
    checkResult(result, expected);
  }

  private void checkResult(final GuardActionCompiler.Result result,
                           final String expected)
    throws ParseException
  {
    final List<SimpleExpressionProxy> guards = result.getGuards();
    final List<BinaryExpressionProxy> actions = result.getActions();
    final SimpleExpressionProxy combined =
      mCompiler.createCondition(guards, actions);
    final ExpressionParser parser = getExpressionParser();
    final SimpleExpressionProxy expectedExpr =
      expected == null ? null : parser.parse(expected);
    if (!mEquality.equals(combined, expectedExpr)) {
      final StringBuilder builder = new StringBuilder
        ("Wrong output from guard/action compiler!");
      builder.append("Expected: ");
      builder.append(expected == null ? "null" : expected.toString());
      builder.append(", but was: ");
      builder.append(combined == null ? "null" : combined.toString());
      fail(builder.toString());
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
  {
    super.setUp();
    final ModuleProxyFactory factory = getFactory();
    final CompilerOperatorTable optable = getOperatorTable();
    final CompilationInfo info = new CompilationInfo(false, false);
    mCompiler = new GuardActionCompiler(factory, optable, info);
    mEquality = new ModuleEqualityVisitor(false);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCompiler = null;
    mEquality = null;
  }


  //#########################################################################
  //# Data Members
  private GuardActionCompiler mCompiler;
  private ModuleEqualityVisitor mEquality;

}

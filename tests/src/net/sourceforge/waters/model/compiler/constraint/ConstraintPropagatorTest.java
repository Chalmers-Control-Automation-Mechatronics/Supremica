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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.AbstractExpressionCompilerTest;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class ConstraintPropagatorTest extends AbstractExpressionCompilerTest
{

  //#########################################################################
  //# Successful Converter Tests
  public void testPropagate_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_not_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!a"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_not_not_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!!a"};
    final String[] expected = {"a"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_and_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a & b"};
    final String[] expected = {"a", "b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_or_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a | b"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_b_or_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"b | a"};
    final String[] expected = {"a | b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_b_or_c_or_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"b | c | a"};
    final String[] expected = {"a | b | c"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!(a | b)"};
    final String[] expected = {"!a", "!b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_not_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!(a | !b)"};
    final String[] expected = {"!a", "b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_and_b()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!(a & b)"};
    final String[] expected = {"!a | !b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_or_not_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a | !a"};
    final String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_not_a_or_b_or_a()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!a | b | a"};
    final String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_a_and_aprime()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String[] constraints = {"a", "a'"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_a_and_notaprime()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    final String[] constraints = {"a", "!a'"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_crc_1()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    final String[] constraints = {"crc==nok", "crc==ok | trouble"};
    final String[] expected = {"crc==nok", "trouble"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_crc_2()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"ok", "nok"});
    addVariable("crc", range);
    final String[] constraints = {"crc!=ok", "crc==ok | trouble"};
    final String[] expected = {"crc==nok", "trouble"};
    testPropagate(constraints, expected);
  }


  public void testPropagate_absorption_1()
    throws EvalException, ParseException
  {
    final String[] constraints = {"a!=b", "a!=b | c!=d"};
    final String[] expected = {"a!=b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_absorption_2()
    throws EvalException, ParseException
  {
    final String[] constraints = {"b", "a | b | c"};
    final String[] expected = {"b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_absorption_3()
    throws EvalException, ParseException
  {
    final String[] constraints = {"!b", "a | b | c"};
    final String[] expected = {"!b", "a | c"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("x'");
    final String[] constraints = {"x'==0"};
    final String[] expected = {"!x'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("x'");
    final String[] constraints = {"x'==1"};
    final String[] expected = {"x'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x'", range);
    final String[] constraints = {"x'==1"};
    final String[] expected = {"x'==1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_boolrange_4()
    throws EvalException, ParseException
  {
    final String[] constraints = {"xx'==0"};
    final String[] expected = {"xx'==0"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_compare_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 10);
    addVariable("x", range);
    final String[] constraints = {"x<x"};
    final String[] expected = null;
    testPropagate(constraints, expected);
  }

  public void testPropagate_divide_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("SR");
    final CompiledIntRange range12 = createIntRange(1, 2);
    addVariable("NP", range12);
    final CompiledIntRange range68 = createIntRange(6, 8);
    addVariable("PCO", range68);
    final String[] constraints = {"NP' == (NP + SR - 1) / PCO + 1"};
    final String[] expected = {"NP' == 1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_enumvar()
    throws EvalException, ParseException
  {
    final CompiledEnumRange range =
      createEnumRange(new String[] {"a", "b", "c"});
    addVariable("enumvar", range);
    final String[] constraints = {"a==enumvar"};
    final String[] expected = {"enumvar==a"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_increment()
    throws EvalException, ParseException
  {
    addBooleanVariable("v");
    addBooleanVariable("v'");
    final String[] constraints = {"v' == v+1"};
    final String[] expected = {"!v", "v'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x<=5"};
    final String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>5"};
    testPropagate(constraints, null);
  }

  public void testPropagate_intrange_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>=2 & x<3"};
    final String[] expected = {"x==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>1 & x<3"};
    final String[] expected = {"x==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_5()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x>1 & x<1"};
    testPropagate(constraints, null);
  }

  public void testPropagate_intrange_6()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    final String[] constraints = {"x<4 & x<2"};
    final String[] expected = {"x<=1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_7()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 6);
    addVariable("x", range);
    final String[] constraints = {"x>4 & x>2"};
    final String[] expected = {"5<=x"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_intrange_8()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 5);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x>1 & x==y & y<=2"};
    final String[] expected = {"x==2", "y==2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_ite_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 31);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x > 5", "y == \\ite(x<3, x+1, 0)"};
    final String[] expected = {"6 <= x", "y == 0"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_ite_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 31);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x > 5", "y == \\ite(x<3, x+1, x-1)"};
    final String[] expected = {"6 <= x", "x == 1+y"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_ite_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 31);
    addVariable("x", range);
    addVariable("y", range);
    addVariable("z", range);
    final String[] constraints = {"x == 1", "z == \\ite(x<y, x+1, y+x)"};
    final String[] expected = {"x == 1", "z == \\ite(1<y, 2, 1+y)"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_ite_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 31);
    addVariable("x", range);
    addVariable("y", range);
    addVariable("z", range);
    final String[] constraints = {"x == y", "z == \\ite(x<y, 1, 0)"};
    final String[] expected = {"x == y", "z == 0"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_mod_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(2, 10);
    addVariable("p", range);
    final String[] constraints = {"0 == 2 % p"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_mod_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange rangeN = createIntRange(1, 2);
    addVariable("N", rangeN);
    final CompiledIntRange rangeX = createIntRange(0, 3);
    addVariable("X", rangeX);
    final String[] constraints = {"X == 1 % N"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_mod_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange rangeN = createIntRange(2, 3);
    addVariable("N", rangeN);
    final CompiledIntRange rangeX = createIntRange(0, 3);
    addVariable("X", rangeX);
    final String[] constraints = {"X == 1 % N"};
    final String[] expected = {"X == 1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_mod_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange rangeN = createIntRange(2, 3);
    addVariable("N", rangeN);
    final CompiledIntRange rangeX = createIntRange(0, 2);
    addVariable("X", rangeX);
    final CompiledIntRange rangeY = createIntRange(0, 4);
    addVariable("Y", rangeY);
    final String[] constraints = {"Y == X % N", "Y >= 2"};
    final String[] expected = {"2 == X % N", "Y == 2"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_mod_5()
    throws EvalException, ParseException
  {
    addBooleanVariable("SR");
    final CompiledIntRange range = createIntRange(1, 2);
    addVariable("NP", range);
    addVariable("PCO", range);
    final String[] constraints = {"NP' == (NP + SR - 1) % PCO + 1",
                                  "1 >= NP",
                                  "1 < NP + SR"};
    final String[] expected = {"NP == 1",
                               "SR",
                               "NP' == 1 + 1 % PCO"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_negliteral_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b");
    final String[] constraints = {"!a", "a==b"};
    final String[] expected = {"!a", "!b"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_negliteral_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("a");
    addBooleanVariable("b'");
    final String[] constraints = {"!b'", "a==b'"};
    final String[] expected = {"!a", "!b'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    addVariable("x'", range);
    final String[] constraints = {"x' == x + 1"};
    final String[] expected = {"x' == 1 + x"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    final String[] constraints = {"x == x + 1"};
    testPropagate(constraints, null);
  }

  public void testPropagate_sum_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    final String[] constraints = {"y + x + 2 == 1 + y + 1 + x"};
    final String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    final String[] constraints = {"x != x + 1"};
    final String[] expected = {};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_5()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    final String[] constraints = {"x + 1 == x + 2"};
    testPropagate(constraints, null);
  }

  public void testPropagate_sum_6()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("xa", range);
    addVariable("xaf", range);
    addVariable("ya", range);
    addVariable("yaf", range);
    addVariable("u", range);
    final String[] constraints = {"ya==yaf", "xa==xaf", "u==ya+xa-yaf-xaf+1"};
    final String[] expected = {"ya==yaf", "xa==xaf", "u==1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_7()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x == y + 1 - y"};
    final String[] expected = {"x == 1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_8()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x == y+1", "y == 2"};
    testPropagate(constraints, null);
  }

  public void testPropagate_sum_9()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x + y - 1 == y"};
    final String[] expected = {"x == 1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_10()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(-2, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"-(x + y) == 0"};
    final String[] expected = {"x == -y"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_11()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(-2, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"-(x + y) > 0"};
    final String[] expected = {"x < -y", "x <= 1"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_12()  // nesting !!!
    throws EvalException, ParseException
  {
    addBooleanVariable("SR");
    final CompiledIntRange range = createIntRange(1, 2);
    addVariable("NP", range);
    addVariable("PCO", range);
    final String[] constraints = {"NP' == (NP + SR - 1) % PCO + 1",
                                  "1 >= NP",
                                  "1 >= NP + SR"};
    final String[] expected = {"NP == 1",
                               "NP' == 1",
                               "!SR"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_13()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(-2, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"x == y + y"};
    testPropagate(constraints, constraints);
  }

  public void testPropagate_sum_14()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(-2, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"1 == x + y"};
    final String[] expected = {"x == 1 - y"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_sum_15()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(-2, 2);
    addVariable("x", range);
    addVariable("y", range);
    final String[] constraints = {"1 == x % 2 + x % 3"};
    testPropagate(constraints, constraints);
  }


  public void testPropagate_balllift_1()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    final String[] constraints = {"qUp'",
                                  "qUp' == ((qUp | c_iBallDn) & c_iBallUp)"};
    final String[] expected = {"qUp'", "c_iBallDn | qUp", "c_iBallUp"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_2()
    throws EvalException, ParseException
  {
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallUp");
    final String[] constraints = {"!qUp'",
                                  "qUp' == ((qUp | c_iBallDn) & c_iBallUp)"};
    final String[] expected = {"!qUp'", "!c_iBallUp | !(qUp | c_iBallDn)"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_3()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    final String[] constraints = {"!c_iBallDn",
    "qUp' == ((qUp|c_iBallDn) & qOut')"};
    final String[] expected = {"!c_iBallDn", "qUp' == (qUp & qOut')"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_outprime()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("level", range);
    addVariable("level'", range);
    addBooleanVariable("out");
    addBooleanVariable("out'");
    final String[] constraints = {"out'==1"};
    final String[] expected = {"out'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_keptprime()
    throws EvalException, ParseException
  {
    final CompiledIntRange range = createIntRange(0, 2);
    addVariable("x'", range);
    final String[] constraints = {"x'==x'"};
    final String[] expected = constraints;
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_4()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallDn'");
    addBooleanVariable("iBallDn");
    final String[] constraints = {"!c_iBallDn",
                                  "iBallDn == c_iBallDn'",
                                  "qUp' == ((qUp|c_iBallDn) & qOut')"};
    final String[] expected = {"!c_iBallDn",
                               "c_iBallDn' == iBallDn",
                               "qUp' == (qUp & qOut')"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_5()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallDn'");
    addBooleanVariable("c_iBallUp");
    addBooleanVariable("c_iBallUp'");
    addBooleanVariable("iBallDn");
    addBooleanVariable("iBallUp");
    final String[] constraints = {"c_iBallUp==qOut'",
                                  "iBallDn==c_iBallDn'",
                                  "iBallUp==c_iBallUp'",
                                  "qUp'==((qUp|c_iBallDn)&qOut')",
                                  "!c_iBallDn'"};
    final String[] expected = {"qOut'==c_iBallUp",
                               "!iBallDn",
                               "c_iBallUp'==iBallUp",
                               "qUp'==((qUp|c_iBallDn)&c_iBallUp)",
                               "!c_iBallDn'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_balllift_5a()
    throws EvalException, ParseException
  {
    addBooleanVariable("qOut");
    addBooleanVariable("qOut'");
    addBooleanVariable("qUp");
    addBooleanVariable("qUp'");
    addBooleanVariable("c_iBallDn");
    addBooleanVariable("c_iBallDn'");
    addBooleanVariable("c_iBallUp");
    addBooleanVariable("c_iBallUp'");
    addBooleanVariable("iBallDn");
    addBooleanVariable("iBallUp");
    final String[] constraints1 = {"c_iBallUp==qOut'",
                                   "iBallDn==c_iBallDn'",
                                   "iBallUp==c_iBallUp'",
                                   "qUp'==((qUp|c_iBallDn)&qOut')"};
    final String[] constraints2 = {"!c_iBallDn'"};
    final String[] expected = {"qOut'==c_iBallUp",
                               "!iBallDn",
                               "c_iBallUp'==iBallUp",
                               "qUp'==((qUp|c_iBallDn)&c_iBallUp)",
                               "!c_iBallDn'"};
    testPropagate(constraints1, constraints2, expected);
  }

  public void testPropagate_profisafe_1()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 0",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"!fs_master_bit2_CRCNO'",
       "in_cons_num == out_cons_num",
       "in_CRC == ok"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_2()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 1",
       "in_CRC == ok",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"fs_master_bit2_CRCNO'",
       "in_CRC == ok",
       "in_cons_num!=out_cons_num"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_3()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 1",
       "in_CRC == nok",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num | in_CRC==nok)"};
    final String[] expected =
      {"fs_master_bit2_CRCNO'",
       "in_CRC == nok"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_4()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == 0",
       "in_CRC == ok",
       "fs_master_bit2_CRCNO' == (in_cons_num!=out_cons_num) | in_CRC==nok"};
    final String[] expected =
      {"!fs_master_bit2_CRCNO'",
       "in_cons_num == out_cons_num",
       "in_CRC == ok"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_5()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1",
       "fs_master_bit2_CRCNO'"};
    final String[] expected =
      {"out_cons_num!=1",
       "fs_master_bit2_CRCNO'"};
    testPropagate(constraints, expected);
  }

  public void testPropagate_profisafe_6()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1",
       "!fs_master_bit2_CRCNO'"};
    testPropagate(constraints, null);
  }

  public void testPropagate_profisafe_7()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints1 =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1"};
    final String[] constraints2 =
      {"fs_master_bit2_CRCNO'"};
    final String[] expected =
      {"out_cons_num!=1",
       "fs_master_bit2_CRCNO'"};
    testPropagate(constraints1, constraints2,
                  expected, "fs_master_bit2_CRCNO'");
  }

  public void testPropagate_profisafe_8()
    throws EvalException, ParseException
  {
    final CompiledIntRange seqno = createIntRange(0, 5);
    addVariable("in_cons_num", seqno);
    addVariable("out_cons_num", seqno);
    final CompiledEnumRange crc = createEnumRange(new String[] {"ok", "nok"});
    addVariable("in_CRC", crc);
    addBooleanVariable("fs_master_bit2_CRCNO'");
    final String[] constraints1 =
      {"fs_master_bit2_CRCNO' == (1!=out_cons_num)",
       "out_cons_num!=1"};
    final String[] constraints2 =
      {"!fs_master_bit2_CRCNO'"};
    testPropagate(constraints1, constraints2, null, "fs_master_bit2_CRCNO'");
  }

  public void testReentrant()
    throws EvalException, ParseException
  {
    testPropagate_a();
    resetContext();
    testPropagate_not_not_a();
    resetContext();
    testPropagate_a_and_b();
    resetContext();
    testPropagate_b_or_c_or_a();
    resetContext();
    testPropagate_crc_1();
    resetContext();
    testPropagate_intrange_3();
  }


  //#########################################################################
  //# Utilities
  private void testPropagate(final String[] inputs, final String[] outputs)
    throws EvalException, ParseException
  {
    final ConstraintList constraints = parse(inputs);
    // System.err.println(constraints);
    final ConstraintList expected = parse(outputs);
    mPropagator.init(constraints);
    mPropagator.propagate();
    final ConstraintList result = mPropagator.getAllConstraints();
    assertEquals("Wrong output from constraint propagator!", expected, result);
  }

  private void testPropagate(final String[] inputs1,
                             final String[] inputs2,
                             final String[] outputs)
    throws EvalException, ParseException
  {
    testPropagate(inputs1, inputs2, outputs, null);
  }

  private void testPropagate(final String[] inputs1,
                             final String[] inputs2,
                             final String[] outputs,
                             final String recall)
    throws EvalException, ParseException
  {
    final ConstraintList constraints1 = parse(inputs1);
    final ConstraintList constraints2 = parse(inputs2);
    final ConstraintList expected = parse(outputs);
    mPropagator.init(constraints1);
    mPropagator.propagate();
    final ConstraintPropagator propagator2 =
      new ConstraintPropagator(mPropagator);
    if (recall != null) {
      final ExpressionParser parser = getExpressionParser();
      final SimpleExpressionProxy varname = parser.parse(recall);
      propagator2.recallBinding(varname);
    }
    propagator2.addConstraints(constraints2);
    propagator2.propagate();
    final ConstraintList result = propagator2.getAllConstraints();
    assertEquals("Wrong output from constraint propagator!", expected, result);
  }

  private ConstraintList parse(final String[] inputs)
    throws ParseException
  {
    if (inputs == null) {
      return null;
    } else {
      final ExpressionParser parser = getExpressionParser();
      final Comparator<SimpleExpressionProxy> comparator =
        mPropagator.getListComparator();
      final List<SimpleExpressionProxy> list =
        new ArrayList<SimpleExpressionProxy>(inputs.length);
      for (final String input : inputs) {
        final SimpleExpressionProxy expr = parser.parse(input);
        list.add(expr);
      }
      Collections.sort(list, comparator);
      return new ConstraintList(list);
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
    final VariableContext context = getContext();
    mPropagator = new ConstraintPropagator(factory, optable, context);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPropagator = null;
  }


  //#########################################################################
  //# Data Members
  private ConstraintPropagator mPropagator;

}

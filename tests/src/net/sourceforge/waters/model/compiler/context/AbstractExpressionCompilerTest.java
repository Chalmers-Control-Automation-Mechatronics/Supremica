//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public abstract class AbstractExpressionCompilerTest extends TestCase
{

  //#########################################################################
  //# Simple Access
  protected ModuleProxyFactory getFactory()
  {
    return mFactory;
  }

  protected CompilerOperatorTable getOperatorTable()
  {
    return mOperatorTable;
  }

  protected VariableContext getContext()
  {
    return mContext;
  }

  protected ExpressionParser getExpressionParser()
  {
    return mParser;
  }


  //#########################################################################
  //# Utilities
  protected void addAtom(final String name)
    throws ParseException
  {
    final IdentifierProxy ident = mParser.parseIdentifier(name);
    mContext.addAtom(ident);
  }

  protected void addBooleanVariable(final String name)
    throws ParseException
  {
    addVariable(name, BOOLEAN_RANGE);
  }

  protected CompiledIntRange createIntRange(final int lower, final int upper)
  {
    return new CompiledIntRange(lower, upper);
  }

  protected CompiledEnumRange createEnumRange(final String[] names)
    throws ParseException
  {
    final List<SimpleIdentifierProxy> list =
      new ArrayList<SimpleIdentifierProxy>(names.length);
    for (final String name : names) {
      final SimpleIdentifierProxy ident = mParser.parseSimpleIdentifier(name);
      mContext.addAtom(ident);
      list.add(ident);
    }
    return new CompiledEnumRange(list);
  }

  protected void addVariable(final String name, final CompiledRange range)
    throws ParseException
  {
    final SimpleExpressionProxy varname = mParser.parse(name);
    mContext.addVariable(varname, range);
  }

  protected void resetContext()
  {
    mContext.reset();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
  {
    mFactory = ModuleElementFactory.getInstance();
    mOperatorTable = CompilerOperatorTable.getInstance();
    mContext = new DummyContext();
    mParser = new ExpressionParser(mFactory, mOperatorTable);
  }

  @Override
  protected void tearDown()
  {
    mContext = null;
    mParser = null;
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;
  private CompilerOperatorTable mOperatorTable;
  private DummyContext mContext;
  private ExpressionParser mParser;


  //#########################################################################
  //# Class Constants
  private static final CompiledIntRange BOOLEAN_RANGE =
    new CompiledIntRange(0, 1);

}

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

package net.sourceforge.waters.external.promela;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class PromelaIntRange extends PromelaType
{
  public final static PromelaIntRange BIT = new PromelaIntRange("bit", 0, 1);
  public final static PromelaIntRange BYTE = new PromelaIntRange("byte", 0, 255);
  public final static PromelaIntRange SHORT = new PromelaIntRange("short", Short.MIN_VALUE , Short.MAX_VALUE);
  public final static PromelaIntRange INT = new PromelaIntRange("short", Integer.MIN_VALUE , Integer.MAX_VALUE);

  private final int mLower;
  private final int mUpper;

  public PromelaIntRange(final String name, final int lower, final int upper)
  {
    super(name);
    mLower = lower;
    mUpper = upper;
  }

  public SimpleExpressionProxy getRangeExpression(final ModuleProxyFactory factory)
  {
    final CompilerOperatorTable opTable = CompilerOperatorTable.getInstance();
    final BinaryOperator rangeOperator = opTable.getRangeOperator();
    final SimpleExpressionProxy lower = factory.createIntConstantProxy(mLower);
    final SimpleExpressionProxy upper = factory.createIntConstantProxy(mUpper);
    return factory.createBinaryExpressionProxy(rangeOperator, lower, upper);
  }

  public SimpleExpressionProxy getInitialValue(final ModuleProxyFactory factory)
  {
    return factory.createIntConstantProxy(0);
  }

  public int getLower()
  {
    return mLower;
  }

  public int getUpper()
  {
    return mUpper;
  }
}









//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class DummyContext implements VariableContext
{

  //#########################################################################
  //# Constructor
  public DummyContext()
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mAtoms = new ProxyAccessorHashSet<>(eq);
    mRangeMap = new ProxyAccessorHashMap<>(eq);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
  @Override
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    if (mRangeMap.containsProxyKey(ident)) {
      return ident;
    } else {
      return null;
    }
  }

  @Override
  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mAtoms.containsProxy(ident);
  }

  @Override
  public ModuleBindingContext getModuleBindingContext()
  {
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
   return mRangeMap.getByProxy(varname);
  }

  @Override
  public int getNumberOfVariables()
  {
    return mRangeMap.size();
  }


  //#########################################################################
  //# Assignments
  public void addAtom(final IdentifierProxy ident)
  {
    mAtoms.addProxy(ident);
  }

  public void addVariable(final SimpleExpressionProxy varname,
                          final CompiledRange range)
  {
    mRangeMap.putByProxy(varname, range);
  }

  public void reset()
  {
    mAtoms.clear();
    mRangeMap.clear();
  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorSet<IdentifierProxy> mAtoms;
  private final ProxyAccessorMap<SimpleExpressionProxy,CompiledRange>
    mRangeMap;

}

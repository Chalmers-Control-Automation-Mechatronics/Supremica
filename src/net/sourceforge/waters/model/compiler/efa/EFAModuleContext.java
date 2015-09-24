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

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * The binding context used by the EFA compiler.
 *
 * In addition to bindings of enumeration atoms, the EFA module context
 * includes a symbol table with all variables maintained by the EFA compiler
 * and their range. These are the variables that occur in guards and
 * represent the current state of EFA components such as variables ({@link
 * net.sourceforge.waters.model.module.VariableComponentProxy
 * VariableComponentProxy}) or automata {@link
 * net.sourceforge.waters.model.module.SimpleComponentProxy
 * SimpleComponentProxy}). The symbol table maps expressions ({@link
 * SimpleExpressionProxy}) representing variable names to variable objects
 * ({@link EFAVariable}) containing the the computed range of its state
 * space. The table contains entries for the current and the next state of
 * each variable.
 *
 * @see EFACompiler
 * @author Robi Malik
 */

public class EFAModuleContext
  extends ModuleBindingContext
  implements VariableContext
{

  //#########################################################################
  //# Constructors
  public EFAModuleContext(final ModuleProxy module)
  {
    this(module, null, null);
  }

  public EFAModuleContext(final ModuleProxy module,
                          final IdentifierProxy prefix,
                          final SourceInfo info)
  {
    super(module, prefix, info);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    final int size = 2 * module.getComponentList().size();
    mMap = new ProxyAccessorHashMap<>(eq, size);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    final EFAVariable var = getVariable(varname);
    if (var == null) {
      return null;
    } else {
      return var.getRange();
    }
  }

  @Override
  public int getNumberOfVariables()
  {
    return mMap.size() >> 1;
  }


  //#########################################################################
  //# Simple Access
  EFAVariable getVariable(final SimpleExpressionProxy varname)
  {
    return mMap.getByProxy(varname);
  }

  EFAVariable findVariable(final SimpleExpressionProxy varname)
    throws UndefinedIdentifierException
  {
    final EFAVariable var = getVariable(varname);
    if (var == null) {
      throw new UndefinedIdentifierException(varname, "variable");
    } else {
      return var;
    }
  }

  /**
   * Creates current and next-state variables for the given component.
   * @param  comp   Component (variable or automaton) to create variables for.
   * @param  range  Computed range of the variables.
   */
  public void createVariables(final ComponentProxy comp,
                              final CompiledRange range,
                              final ModuleProxyFactory factory,
                              final CompilerOperatorTable optable)
    throws DuplicateIdentifierException
  {
    final IdentifierProxy ident = comp.getIdentifier();
    if (getBoundExpression(ident) != null) {
      throw new DuplicateIdentifierException(ident);
    }
    final EFAVariable var =
      new EFAVariable(false, comp, range, factory, optable);
    final ProxyAccessor<SimpleExpressionProxy> key =
      mMap.createAccessor(ident);
    if (mMap.containsKey(key)) {
      throw new DuplicateIdentifierException(ident);
    }
    mMap.put(key, var);
    final EFAVariable nextvar =
      new EFAVariable(true, comp, range, factory, optable);
    final SimpleExpressionProxy nextident = nextvar.getVariableName();
    mMap.putByProxy(nextident, nextvar);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.compiler.context.ModuleBindingContext
  @Override
  public void insertBinding(final SimpleExpressionProxy ident,
                            final SimpleExpressionProxy value)
    throws DuplicateIdentifierException
  {
    if (mMap.containsProxyKey(ident)) {
      throw new DuplicateIdentifierException(ident);
    } else {
      super.insertBinding(ident, value);
    }
  }

  @Override
  public void insertEnumAtom(final SimpleIdentifierProxy ident)
    throws DuplicateIdentifierException
  {
    if (mMap.containsProxyKey(ident)) {
      throw new DuplicateIdentifierException(ident);
    } else {
      super.insertEnumAtom(ident);
    }
  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorMap<SimpleExpressionProxy,EFAVariable> mMap;

}
